import { httpClient } from '@/lib/api/http-client'

import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '@/features/auth/api/auth-types'

export const authApi = {
  async register(payload: RegisterRequest) {
    const response = await httpClient.post<AuthResponse>(
      '/api/auth/register',
      payload,
    )

    return response.data
  },

  async login(payload: LoginRequest) {
    const response = await httpClient.post<AuthResponse>('/api/auth/login', payload)

    return response.data
  },

  async refresh(refreshToken: string) {
    const response = await httpClient.post<AuthResponse>(
      '/api/auth/refresh',
      undefined,
      {
        headers: {
          Authorization: `Bearer ${refreshToken}`,
        },
      },
    )

    return response.data
  },

  async getCurrentUser() {
    const response = await httpClient.get<User>('/api/auth/me')

    return response.data
  },
}
