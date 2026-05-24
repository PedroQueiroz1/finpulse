import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'

import { getLastCorrelationId } from '@/lib/api/correlation-id'
import { httpClient } from '@/lib/api/http-client'
import { setTokens } from '@/lib/auth/token-storage'
import { server } from '@/test/server'

describe('httpClient', () => {
  it('envia Authorization e X-Correlation-ID e captura correlation ID da resposta', async () => {
    let authorizationHeader: string | null = null
    let requestCorrelationId: string | null = null

    server.use(
      http.get('*/api/auth/me', ({ request }) => {
        authorizationHeader = request.headers.get('Authorization')
        requestCorrelationId = request.headers.get('X-Correlation-ID')

        return HttpResponse.json(
          {
            id: 'user-1',
            name: 'Pedro Silva',
            email: 'pedro@example.com',
            role: 'USER',
            active: true,
          },
          {
            headers: {
              'X-Correlation-ID': 'correlation-from-response',
            },
          },
        )
      }),
    )

    setTokens({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
    })

    await httpClient.get('/api/auth/me')

    expect(authorizationHeader).toBe('Bearer access-token')
    expect(requestCorrelationId).toBeTruthy()
    expect(getLastCorrelationId()).toBe('correlation-from-response')
  })
})
