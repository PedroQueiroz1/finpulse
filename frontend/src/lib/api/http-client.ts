import axios, {
  AxiosError,
  AxiosHeaders,
  type InternalAxiosRequestConfig,
} from 'axios'

import type { AuthResponse } from '@/lib/api/api-types'
import { apiHeaders } from '@/lib/api/api-types'
import { createCorrelationId, setLastCorrelationId } from '@/lib/api/correlation-id'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/lib/auth/token-storage'
import { env } from '@/lib/env/env'
import { normalizeApiError } from '@/lib/errors/api-error'

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
}

function getBaseUrlForPath(path?: string) {
  if (!env.useDirectServices || !path) {
    return env.apiBaseUrl
  }

  if (path.startsWith('/api/auth')) {
    return env.authBaseUrl
  }

  if (path.startsWith('/api/notes')) {
    return env.notesBaseUrl
  }

  if (path.startsWith('/api/stocks')) {
    return env.stocksBaseUrl
  }

  return env.apiBaseUrl
}

function readHeader(headers: unknown, headerName: string) {
  if (headers instanceof AxiosHeaders) {
    return headers.get(headerName)
  }

  if (headers && typeof headers === 'object') {
    const headerRecord = headers as Record<string, string | undefined>
    return headerRecord[headerName] ?? headerRecord[headerName.toLowerCase()]
  }

  return null
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken()

  if (!refreshToken) {
    return null
  }

  const correlationId = createCorrelationId()

  const response = await axios.post<AuthResponse>(
    '/api/auth/refresh',
    undefined,
    {
      baseURL: getBaseUrlForPath('/api/auth/refresh'),
      headers: {
        [apiHeaders.authorization]: `Bearer ${refreshToken}`,
        [apiHeaders.correlationId]: correlationId,
      },
    },
  )

  const responseCorrelationId = readHeader(
    response.headers,
    apiHeaders.correlationId,
  )

  if (responseCorrelationId) {
    setLastCorrelationId(String(responseCorrelationId))
  }

  setTokens({
    accessToken: response.data.accessToken,
    refreshToken: response.data.refreshToken,
  })

  return response.data.accessToken
}

export const httpClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
})

httpClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const headers = AxiosHeaders.from(config.headers)
  const accessToken = getAccessToken()
  const existingCorrelationId = readHeader(headers, apiHeaders.correlationId)
  const correlationId = existingCorrelationId ?? createCorrelationId()

  config.baseURL = getBaseUrlForPath(config.url)
  headers.set(apiHeaders.correlationId, String(correlationId))
  setLastCorrelationId(String(correlationId))

  if (accessToken && !readHeader(headers, apiHeaders.authorization)) {
    headers.set(apiHeaders.authorization, `Bearer ${accessToken}`)
  }

  config.headers = headers

  return config
})

httpClient.interceptors.response.use(
  (response) => {
    const correlationId = readHeader(response.headers, apiHeaders.correlationId)

    if (correlationId) {
      setLastCorrelationId(String(correlationId))
    }

    return response
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined
    const correlationId = readHeader(error.response?.headers, apiHeaders.correlationId)

    if (correlationId) {
      setLastCorrelationId(String(correlationId))
    }

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      getRefreshToken()
    ) {
      originalRequest._retry = true

      try {
        const accessToken = await refreshAccessToken()

        if (accessToken) {
          const headers = AxiosHeaders.from(originalRequest.headers)
          headers.set(apiHeaders.authorization, `Bearer ${accessToken}`)
          originalRequest.headers = headers

          return httpClient(originalRequest)
        }
      } catch {
        clearTokens()
      }
    }

    return Promise.reject(normalizeApiError(error))
  },
)
