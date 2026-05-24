import type { NoteFilters } from '@/features/notes/api/notes-types'

export const queryKeys = {
  auth: {
    me: ['auth', 'me'] as const,
  },
  notes: {
    all: ['notes'] as const,
    list: (filters: NoteFilters = {}) => ['notes', 'list', filters] as const,
    detail: (id: string) => ['notes', 'detail', id] as const,
    groups: ['notes', 'groups'] as const,
  },
  stocks: {
    quote: (symbol: string) => ['stocks', 'quote', symbol] as const,
    company: (symbol: string) => ['stocks', 'company', symbol] as const,
    providers: ['stocks', 'providers'] as const,
  },
} as const
