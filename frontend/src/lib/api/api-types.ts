export type ApiError = {
  status: number
  error: string
  message: string
  path?: string
  timestamp?: string
  validationErrors?: Record<string, string> | null
  correlationId?: string
}

export type AuthResponse = {
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer' | string
  expiresIn: number
  user?: unknown
}

export type ApiDomain = 'gateway' | 'auth' | 'notes' | 'stocks'

export const apiHeaders = {
  authorization: 'Authorization',
  correlationId: 'X-Correlation-ID',
  cacheStatus: 'X-Cache-Status',
  provider: 'X-Provider',
} as const
