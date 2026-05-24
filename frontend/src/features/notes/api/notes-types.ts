export type Note = {
  id: string
  title: string
  content?: string
  groupId?: string
  userId?: string
  tags?: string[]
  color?: string
  pinned?: boolean
  archived?: boolean
  createdAt?: string
  updatedAt?: string
}

export type NoteGroup = {
  id: string
  name: string
  description?: string
  color?: string
  icon?: string
  noteCount?: number
  count: number
  createdAt?: string
  updatedAt?: string
}

export type NoteFilters = {
  tag?: string
  search?: string
}

export type CreateNoteRequest = {
  title: string
  content?: string
  groupId?: string
  tags?: string[]
  color?: string
}

export type UpdateNoteRequest = {
  title?: string
  content?: string
  groupId?: string
  tags?: string[]
  color?: string
  pinned?: boolean
  archived?: boolean
}
