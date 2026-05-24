export type StockQuote = {
  symbol: string
  price: number
  change?: number
  currency?: string
  changePercent?: number
  dayHigh?: number
  dayLow?: number
  dayOpen?: number
  previousClose?: number
  volume?: number
  timestamp?: string
  fetchedAt?: string
  provider?: 'alpha-vantage' | 'finnhub' | string
  cached?: boolean
  cacheStatus?: 'HIT' | 'MISS' | string
}

export type CompanyInfo = {
  symbol: string
  name?: string
  description?: string
  exchange?: string
  country?: string
  sector?: string
  industry?: string
  currency?: string
  website?: string
  logoUrl?: string
  provider?: 'alpha-vantage' | 'finnhub' | string
  marketCap?: number
  lastFetchedAt?: string
}

export type ProviderStatus = {
  name: 'alpha-vantage' | 'finnhub' | string
  status: 'UP' | 'DOWN' | 'DEGRADED' | string
  circuitBreakerState?: 'CLOSED' | 'OPEN' | 'HALF_OPEN' | string
  lastSuccessfulCall?: string
}

export type ProviderStatusResponse = {
  providers: ProviderStatus[]
}
