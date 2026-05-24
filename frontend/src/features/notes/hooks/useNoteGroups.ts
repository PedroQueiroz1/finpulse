import { useQuery } from '@tanstack/react-query'

import { notesApi } from '@/features/notes/api/notes-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useNoteGroups() {
  return useQuery({
    queryKey: queryKeys.notes.groups,
    queryFn: notesApi.listGroups,
    staleTime: 30_000,
  })
}
