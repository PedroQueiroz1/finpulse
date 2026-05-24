import { create } from 'zustand'

import type { AuthResponse, User } from '@/features/auth/api/auth-types'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/lib/auth/token-storage'

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  user: User | null
  isAuthenticated: boolean
  setSession: (response: AuthResponse) => void
  setUser: (user: User | null) => void
  clearSession: () => void
}

const initialAccessToken = getAccessToken()
const initialRefreshToken = getRefreshToken()

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: initialAccessToken,
  refreshToken: initialRefreshToken,
  user: null,
  isAuthenticated: Boolean(initialAccessToken),
  setSession: (response) => {
    setTokens({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    })

    set({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      user: response.user,
      isAuthenticated: true,
    })
  },
  setUser: (user) => set({ user }),
  clearSession: () => {
    clearTokens()

    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
    })
  },
}))
