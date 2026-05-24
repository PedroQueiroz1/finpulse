import { apiHeaders } from '@/lib/api/api-types'
import { httpClient } from '@/lib/api/http-client'

import type {
  CompanyInfo,
  ProviderStatus,
  ProviderStatusResponse,
  StockQuote,
} from '@/features/stocks/api/stocks-types'

function readHeader(headers: unknown, headerName: string) {
  if (!headers || typeof headers !== 'object') {
    return undefined
  }

  const headerRecord = headers as Record<string, unknown>
  const value = headerRecord[headerName] ?? headerRecord[headerName.toLowerCase()]

  return typeof value === 'string' ? value : undefined
}

function normalizeProvider(provider: string | ProviderStatus): ProviderStatus {
  if (typeof provider !== 'string') {
    return provider
  }

  return {
    name: provider,
    status: 'UP',
    circuitBreakerState: 'CLOSED',
  }
}

export const stocksApi = {
  async getQuote(symbol: string) {
    const normalizedSymbol = symbol.trim().toUpperCase()
    const response = await httpClient.get<StockQuote>(
      `/api/stocks/${normalizedSymbol}/quote`,
    )
    const cacheStatus = readHeader(response.headers, apiHeaders.cacheStatus)
    const provider = readHeader(response.headers, apiHeaders.provider)

    return {
      ...response.data,
      symbol: response.data.symbol ?? normalizedSymbol,
      currency: response.data.currency ?? 'USD',
      timestamp: response.data.timestamp ?? response.data.fetchedAt,
      cacheStatus: response.data.cacheStatus ?? cacheStatus,
      provider: response.data.provider ?? provider,
    }
  },

  async getCompany(symbol: string) {
    const normalizedSymbol = symbol.trim().toUpperCase()
    const response = await httpClient.get<CompanyInfo>(
      `/api/stocks/${normalizedSymbol}/company`,
    )

    return {
      ...response.data,
      symbol: response.data.symbol ?? normalizedSymbol,
    }
  },

  async getProviders() {
    const response = await httpClient.get<
      ProviderStatusResponse | ProviderStatus[] | string[]
    >(
      '/api/stocks/providers',
    )

    if (Array.isArray(response.data)) {
      return { providers: response.data.map(normalizeProvider) }
    }

    return {
      providers: response.data.providers.map(normalizeProvider),
    }
  },
}
