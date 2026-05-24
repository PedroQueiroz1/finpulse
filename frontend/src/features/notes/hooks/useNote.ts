import { useQuery } from '@tanstack/react-query'

import { notesApi } from '@/features/notes/api/notes-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useNote(id: string | null) {
  return useQuery({
    queryKey: queryKeys.notes.detail(id ?? ''),
    queryFn: () => notesApi.getNote(id ?? ''),
    enabled: Boolean(id),
    staleTime: 30_000,
  })
}
