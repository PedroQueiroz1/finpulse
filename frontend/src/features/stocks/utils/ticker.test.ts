import { describe, expect, it } from 'vitest'

import { isValidTicker } from '@/features/stocks/utils/ticker'

describe('isValidTicker', () => {
  it('aceita tickers validos normalizados', () => {
    expect(isValidTicker('AAPL')).toBe(true)
    expect(isValidTicker('MSFT')).toBe(true)
    expect(isValidTicker('PETR4')).toBe(true)
  })

  it('rejeita entradas invalidas', () => {
    expect(isValidTicker('')).toBe(false)
    expect(isValidTicker('???')).toBe(false)
    expect(isValidTicker('aapl')).toBe(false)
  })
})
