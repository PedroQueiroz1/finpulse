import { useMutation } from '@tanstack/react-query'

import { queryClient } from '@/app/providers/query-client'
import { notesApi } from '@/features/notes/api/notes-api'
import type { CreateNoteRequest } from '@/features/notes/api/notes-types'
import { queryKeys } from '@/lib/api/query-keys'

export function useCreateNote() {
  return useMutation({
    mutationFn: (payload: CreateNoteRequest) => notesApi.createNote(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.all })
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.groups })
    },
  })
}
