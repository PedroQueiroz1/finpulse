import { http, HttpResponse } from 'msw'

export const stocksHandlers = [
  http.get('*/api/stocks/:symbol/quote', ({ params }) =>
    HttpResponse.json(
      {
        symbol: String(params.symbol).toUpperCase(),
        price: 177.3,
        change: 1.42,
        changePercent: 0.81,
        dayHigh: 178,
        dayLow: 174.5,
        dayOpen: 175.2,
        previousClose: 175.88,
        volume: 1000000,
        provider: 'alpha-vantage',
        fetchedAt: '2026-05-22T10:00:00',
      },
      {
        headers: {
          'X-Cache-Status': 'MISS',
          'X-Provider': 'alpha-vantage',
        },
      },
    ),
  ),
  http.get('*/api/stocks/:symbol/company', ({ params }) =>
    HttpResponse.json({
      symbol: String(params.symbol).toUpperCase(),
      name: 'Apple Inc',
      description: 'Consumer technology company.',
      exchange: 'NASDAQ',
      country: 'US',
      sector: 'Technology',
      industry: 'Consumer Electronics',
      currency: 'USD',
      website: 'https://www.apple.com',
      provider: 'alpha-vantage',
    }),
  ),
  http.get('*/api/stocks/providers', () =>
    HttpResponse.json(['alpha-vantage', 'finnhub']),
  ),
]
