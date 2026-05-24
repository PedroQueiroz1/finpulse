import { useQuery } from '@tanstack/react-query'

import { notesApi } from '@/features/notes/api/notes-api'
import type { NoteFilters } from '@/features/notes/api/notes-types'
import { queryKeys } from '@/lib/api/query-keys'

export function useNotes(filters: NoteFilters = {}) {
  return useQuery({
    queryKey: queryKeys.notes.list(filters),
    queryFn: () => notesApi.listNotes(filters),
    staleTime: 30_000,
  })
}
