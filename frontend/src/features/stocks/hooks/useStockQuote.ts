import { useQuery } from '@tanstack/react-query'

import { stocksApi } from '@/features/stocks/api/stocks-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useStockQuote(symbol: string | null, enabled = Boolean(symbol)) {
  const normalizedSymbol = symbol?.trim().toUpperCase() ?? ''

  return useQuery({
    queryKey: queryKeys.stocks.quote(normalizedSymbol),
    queryFn: () => stocksApi.getQuote(normalizedSymbol),
    enabled: enabled && normalizedSymbol.length > 0,
    staleTime: 60_000,
  })
}
