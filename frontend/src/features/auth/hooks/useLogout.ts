import { useCallback } from 'react'

import { queryClient } from '@/app/providers/query-client'
import { useAuthStore } from '@/features/auth/store/useAuthStore'

export function useLogout() {
  const clearSession = useAuthStore((state) => state.clearSession)

  return useCallback(() => {
    clearSession()
    queryClient.clear()
  }, [clearSession])
}
