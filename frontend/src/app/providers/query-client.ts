import { QueryClient } from '@tanstack/react-query'

import type { ApiError } from '@/lib/api/api-types'

function shouldRetry(failureCount: number, error: unknown) {
  const status = (error as Partial<ApiError> | undefined)?.status

  if (status === 401 || status === 403) {
    return false
  }

  return failureCount < 2
}

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: shouldRetry,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
})
