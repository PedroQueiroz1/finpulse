import { useMutation } from '@tanstack/react-query'

import { queryClient } from '@/app/providers/query-client'
import { notesApi } from '@/features/notes/api/notes-api'
import { queryKeys } from '@/lib/api/query-keys'

export function useDeleteNote() {
  return useMutation({
    mutationFn: (id: string) => notesApi.deleteNote(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.all })
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.groups })
    },
  })
}
