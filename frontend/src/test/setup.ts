import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'

import { queryClient } from '@/app/providers/query-client'
import { server } from '@/test/server'

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))

afterEach(() => {
  cleanup()
  server.resetHandlers()
  queryClient.clear()
  window.localStorage.clear()
})

afterAll(() => server.close())
