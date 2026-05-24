import { describe, expect, it } from 'vitest'

import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  getTokens,
  setTokens,
} from '@/lib/auth/token-storage'

describe('token-storage', () => {
  it('salva, le e limpa tokens no localStorage', () => {
    setTokens({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
    })

    expect(getAccessToken()).toBe('access-token')
    expect(getRefreshToken()).toBe('refresh-token')
    expect(getTokens()).toEqual({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
    })

    clearTokens()

    expect(getTokens()).toBeNull()
  })
})
