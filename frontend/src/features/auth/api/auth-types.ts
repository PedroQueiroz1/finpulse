export type User = {
  id: string
  name: string
  email: string
  role: 'USER' | 'PREMIUM' | 'ADMIN' | 'SUPER_ADMIN' | 'GUEST' | string
  active: boolean
}

export type TokenPair = {
  accessToken: string
  refreshToken: string
}

export type AuthResponse = TokenPair & {
  tokenType: 'Bearer' | string
  expiresIn: number
  user: User
}

export type RegisterRequest = {
  name: string
  email: string
  password: string
}

export type LoginRequest = {
  email: string
  password: string
}
