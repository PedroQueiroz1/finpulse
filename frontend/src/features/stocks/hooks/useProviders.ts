import { useQuery } from '@tanstack/react-query'

import { stocksApi } from '@/features/stocks/api/stocks-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useProviders() {
  return useQuery({
    queryKey: queryKeys.stocks.providers,
    queryFn: stocksApi.getProviders,
    staleTime: 60_000,
  })
}
