import { useMutation } from '@tanstack/react-query'

import { queryClient } from '@/app/providers/query-client'
import { authApi } from '@/features/auth/api/auth-api'
import type { LoginRequest } from '@/features/auth/api/auth-types'
import { useAuthStore } from '@/features/auth/store/useAuthStore'
import { queryKeys } from '@/lib/api/query-keys'

export function useLogin() {
  const setSession = useAuthStore((state) => state.setSession)

  return useMutation({
    mutationFn: (payload: LoginRequest) => authApi.login(payload),
    onSuccess: (response) => {
      setSession(response)
      queryClient.setQueryData(queryKeys.auth.me, response.user)
    },
  })
}
