import { useQuery } from '@tanstack/react-query'

import { stocksApi } from '@/features/stocks/api/stocks-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useCompanyInfo(symbol: string | null, enabled = Boolean(symbol)) {
  const normalizedSymbol = symbol?.trim().toUpperCase() ?? ''

  return useQuery({
    queryKey: queryKeys.stocks.company(normalizedSymbol),
    queryFn: () => stocksApi.getCompany(normalizedSymbol),
    enabled: enabled && normalizedSymbol.length > 0,
    staleTime: 24 * 60 * 60 * 1000,
  })
}
