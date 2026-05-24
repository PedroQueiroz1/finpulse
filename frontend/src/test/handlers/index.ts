import { authHandlers } from '@/test/handlers/auth-handlers'
import { notesHandlers } from '@/test/handlers/notes-handlers'
import { stocksHandlers } from '@/test/handlers/stocks-handlers'

export const handlers = [...authHandlers, ...notesHandlers, ...stocksHandlers]
