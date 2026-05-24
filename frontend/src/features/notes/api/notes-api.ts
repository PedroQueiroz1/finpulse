import { httpClient } from '@/lib/api/http-client'

import type {
  CreateNoteRequest,
  Note,
  NoteFilters,
  NoteGroup,
  UpdateNoteRequest,
} from '@/features/notes/api/notes-types'

function filterByTag(notes: Note[], tag?: string) {
  if (!tag) {
    return notes
  }

  return notes.filter((note) => note.tags?.includes(tag))
}

function normalizeGroup(group: NoteGroup): NoteGroup {
  return {
    ...group,
    count: group.count ?? group.noteCount ?? 0,
  }
}

export const notesApi = {
  async listNotes(filters: NoteFilters = {}) {
    if (filters.search) {
      const response = await httpClient.get<Note[]>('/api/notes/search', {
        params: { q: filters.search },
      })

      return filterByTag(response.data, filters.tag)
    }

    const response = await httpClient.get<Note[]>('/api/notes')

    return filterByTag(response.data, filters.tag)
  },

  async getNote(id: string) {
    const response = await httpClient.get<Note>(`/api/notes/${id}`)

    return response.data
  },

  async createNote(payload: CreateNoteRequest) {
    const response = await httpClient.post<Note>('/api/notes', payload)

    return response.data
  },

  async updateNote(id: string, payload: UpdateNoteRequest) {
    const response = await httpClient.put<Note>(`/api/notes/${id}`, payload)

    return response.data
  },

  async deleteNote(id: string) {
    await httpClient.delete(`/api/notes/${id}`)
  },

  async listGroups() {
    const response = await httpClient.get<NoteGroup[]>('/api/notes/groups')

    return response.data.map(normalizeGroup)
  },
}
