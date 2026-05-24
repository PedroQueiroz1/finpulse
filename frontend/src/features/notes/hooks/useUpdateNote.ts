import { useMutation } from '@tanstack/react-query'

import { queryClient } from '@/app/providers/query-client'
import { notesApi } from '@/features/notes/api/notes-api'
import type { UpdateNoteRequest } from '@/features/notes/api/notes-types'
import { queryKeys } from '@/lib/api/query-keys'

type UpdateNoteVariables = {
  id: string
  payload: UpdateNoteRequest
}

export function useUpdateNote() {
  return useMutation({
    mutationFn: ({ id, payload }: UpdateNoteVariables) =>
      notesApi.updateNote(id, payload),
    onSuccess: (note) => {
      queryClient.setQueryData(queryKeys.notes.detail(note.id), note)
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.all })
      void queryClient.invalidateQueries({ queryKey: queryKeys.notes.groups })
    },
  })
}
