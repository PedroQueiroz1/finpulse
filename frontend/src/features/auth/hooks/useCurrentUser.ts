import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'

import { authApi } from '@/features/auth/api/auth-api'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import type { ApiError } from '@/lib/api/api-types'
import { queryKeys } from '@/lib/api/query-keys'

export function useCurrentUser() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const setUser = useAuthStore((state) => state.setUser)
  const clearSession = useAuthStore((state) => state.clearSession)

  const query = useQuery({
    queryKey: queryKeys.auth.me,
    queryFn: authApi.getCurrentUser,
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
    retry: (failureCount, error) => {
      const status = (error as Partial<ApiError>).status

      if (status === 401 || status === 403) {
        return false
      }

      return failureCount < 1
    },
  })

  useEffect(() => {
    if (query.data) {
      setUser(query.data)
    }
  }, [query.data, setUser])

  useEffect(() => {
    const status = (query.error as Partial<ApiError> | null)?.status

    if (status === 401 || status === 403) {
      clearSession()
    }
  }, [clearSession, query.error])

  return query
}
