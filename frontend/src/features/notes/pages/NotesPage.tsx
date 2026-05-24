import { useMemo, useState } from 'react'
import { Plus } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'

import { EmptyState } from '@/components/feedback/EmptyState'
import { ErrorState } from '@/components/feedback/ErrorState'
import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button } from '@/components/ui/button'
import type { Note } from '@/features/notes/api/notes-types'
import { DeleteNoteDialog } from '@/features/notes/components/DeleteNoteDialog'
import { NoteCard } from '@/features/notes/components/NoteCard'
import { NoteFormDialog } from '@/features/notes/components/NoteFormDialog'
import { NotesToolbar } from '@/features/notes/components/NotesToolbar'
import { TagFilter, type TagOption } from '@/features/notes/components/TagFilter'
import { useNoteGroups } from '@/features/notes/hooks/useNoteGroups'
import { useNotes } from '@/features/notes/hooks/useNotes'

function buildTagOptions(notes: Note[]): TagOption[] {
  const tagCounts = new Map<string, number>()

  notes.forEach((note) => {
    note.tags?.forEach((tag) => {
      tagCounts.set(tag, (tagCounts.get(tag) ?? 0) + 1)
    })
  })

  return Array.from(tagCounts.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((left, right) => right.count - left.count || left.name.localeCompare(right.name))
}

export function NotesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [formOpen, setFormOpen] = useState(false)
  const [editingNote, setEditingNote] = useState<Note | null>(null)
  const [deletingNote, setDeletingNote] = useState<Note | null>(null)
  const search = searchParams.get('search') ?? ''
  const selectedTag = searchParams.get('tag') ?? undefined
  const notesQuery = useNotes({ search: search || undefined })
  const groupsQuery = useNoteGroups()
  const notes = useMemo(() => notesQuery.data ?? [], [notesQuery.data])
  const tagOptions = useMemo(() => buildTagOptions(notes), [notes])
  const visibleNotes = useMemo(() => {
    if (!selectedTag) {
      return notes
    }

    return notes.filter((note) => note.tags?.includes(selectedTag))
  }, [notes, selectedTag])

  function updateSearchParam(key: 'search' | 'tag', value: string | null) {
    const nextParams = new URLSearchParams(searchParams)

    if (value) {
      nextParams.set(key, value)
    } else {
      nextParams.delete(key)
    }

    setSearchParams(nextParams)
  }

  function handleCreate() {
    setEditingNote(null)
    setFormOpen(true)
  }

  function handleEdit(note: Note) {
    setEditingNote(note)
    setFormOpen(true)
  }

  return (
    <div className="space-y-6">
      <PageHeader
        actions={
          <Button onClick={handleCreate}>
            <Plus className="mr-2 h-4 w-4" />
            Nova nota
          </Button>
        }
        description="Notas autenticadas com busca, tags e CRUD."
        eyebrow="Produtividade"
        title="Notes"
      />

      <NotesToolbar
        onClear={() => updateSearchParam('search', null)}
        onSearch={(value) => updateSearchParam('search', value || null)}
        resultCount={visibleNotes.length}
        search={search}
      />

      <TagFilter
        onSelectTag={(tag) => updateSearchParam('tag', tag)}
        selectedTag={selectedTag}
        tags={tagOptions}
      />

      {notesQuery.isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 6 }).map((_, index) => (
            <SkeletonCard key={index} rows={4} />
          ))}
        </div>
      ) : null}

      {notesQuery.isError ? (
        <ErrorState
          error={notesQuery.error}
          onRetry={() => void notesQuery.refetch()}
          title="Nao foi possivel carregar suas notas"
        />
      ) : null}

      {!notesQuery.isLoading && !notesQuery.isError && visibleNotes.length === 0 ? (
        <EmptyState
          actionLabel="Nova nota"
          description={
            search || selectedTag
              ? 'Nenhuma nota encontrada para os filtros atuais.'
              : 'Crie a primeira nota para organizar ideias, estudos e decisoes.'
          }
          onAction={handleCreate}
          title="Nenhuma nota encontrada"
        />
      ) : null}

      {!notesQuery.isLoading && !notesQuery.isError && visibleNotes.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {visibleNotes.map((note) => (
            <NoteCard
              key={note.id}
              note={note}
              onDelete={setDeletingNote}
              onEdit={handleEdit}
            />
          ))}
        </div>
      ) : null}

      <NoteFormDialog
        groups={groupsQuery.data ?? []}
        note={editingNote}
        onOpenChange={setFormOpen}
        open={formOpen}
      />
      <DeleteNoteDialog
        note={deletingNote}
        onOpenChange={(open) => {
          if (!open) {
            setDeletingNote(null)
          }
        }}
        open={Boolean(deletingNote)}
      />
    </div>
  )
}
