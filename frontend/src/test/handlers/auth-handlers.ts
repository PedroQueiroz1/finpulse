import { http, HttpResponse } from 'msw'

const user = {
  id: 'user-1',
  name: 'Pedro Silva',
  email: 'pedro@example.com',
  role: 'USER',
  active: true,
}

const authResponse = {
  accessToken: 'access-token-test',
  refreshToken: 'refresh-token-test',
  tokenType: 'Bearer',
  expiresIn: 3600,
  user,
}

export const authHandlers = [
  http.post('*/api/auth/login', () => HttpResponse.json(authResponse)),
  http.post('*/api/auth/register', () => HttpResponse.json(authResponse, { status: 201 })),
  http.post('*/api/auth/refresh', () => HttpResponse.json(authResponse)),
  http.get('*/api/auth/me', () => HttpResponse.json(user)),
]
