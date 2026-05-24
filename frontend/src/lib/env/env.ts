type RawEnv = ImportMetaEnv & Record<string, string | undefined>

export type AppEnv = {
  appName: string
  apiBaseUrl: string
  useDirectServices: boolean
  authBaseUrl: string
  notesBaseUrl: string
  stocksBaseUrl: string
  enableMocks: boolean
  mode: string
}

function readBoolean(value: string | undefined, fallback: boolean) {
  if (value == null || value === '') {
    return fallback
  }

  return value.toLowerCase() === 'true'
}

function requireEnv(value: string | undefined, name: string) {
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`)
  }

  return value
}

function assertUrl(value: string, name: string) {
  try {
    new URL(value)
    return value
  } catch {
    throw new Error(`Invalid URL in environment variable: ${name}`)
  }
}

function createEnv(rawEnv: RawEnv): AppEnv {
  const apiBaseUrl = requireEnv(
    rawEnv.VITE_API_BASE_URL ?? rawEnv.VITE_API_GATEWAY_URL,
    'VITE_API_BASE_URL',
  )

  return {
    appName: rawEnv.VITE_APP_NAME ?? 'FinPulse',
    apiBaseUrl: assertUrl(apiBaseUrl, 'VITE_API_BASE_URL'),
    useDirectServices: readBoolean(rawEnv.VITE_USE_DIRECT_SERVICES, false),
    authBaseUrl: assertUrl(
      rawEnv.VITE_AUTH_BASE_URL ??
        rawEnv.VITE_AUTH_SERVICE_URL ??
        'http://localhost:8081',
      'VITE_AUTH_BASE_URL',
    ),
    notesBaseUrl: assertUrl(
      rawEnv.VITE_NOTES_BASE_URL ??
        rawEnv.VITE_NOTES_SERVICE_URL ??
        'http://localhost:8082',
      'VITE_NOTES_BASE_URL',
    ),
    stocksBaseUrl: assertUrl(
      rawEnv.VITE_STOCKS_BASE_URL ??
        rawEnv.VITE_STOCK_SERVICE_URL ??
        'http://localhost:8083',
      'VITE_STOCKS_BASE_URL',
    ),
    enableMocks: readBoolean(rawEnv.VITE_ENABLE_MOCKS, false),
    mode: rawEnv.MODE,
  }
}

export const env = createEnv(import.meta.env)
