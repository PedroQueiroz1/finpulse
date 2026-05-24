import { useEffect } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, Save } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import type { ApiError } from '@/lib/api/api-types'
import type { Note, NoteGroup } from '@/features/notes/api/notes-types'
import { useCreateNote } from '@/features/notes/hooks/useCreateNote'
import { useUpdateNote } from '@/features/notes/hooks/useUpdateNote'
import {
  noteFormSchema,
  type NoteFormValues,
} from '@/features/notes/schemas/note-schemas'
import {
  getErrorMessage,
  getValidationMessage,
} from '@/lib/errors/get-error-message'

type NoteFormDialogProps = {
  note: Note | null
  groups: NoteGroup[]
  open: boolean
  onOpenChange: (open: boolean) => void
}

function tagsToInput(tags?: string[]) {
  return tags?.join(', ') ?? ''
}

function parseTags(value?: string) {
  if (!value) {
    return []
  }

  return Array.from(
    new Set(
      value
        .split(',')
        .map((tag) => tag.trim())
        .filter(Boolean),
    ),
  )
}

function getDefaultValues(note: Note | null): NoteFormValues {
  return {
    title: note?.title ?? '',
    content: note?.content ?? '',
    tagsInput: tagsToInput(note?.tags),
    groupId: note?.groupId ?? '',
    color: note?.color ?? '#10b981',
  }
}

export function NoteFormDialog({
  note,
  groups,
  open,
  onOpenChange,
}: NoteFormDialogProps) {
  const createNote = useCreateNote()
  const updateNote = useUpdateNote()
  const isEditing = Boolean(note)
  const isPending = createNote.isPending || updateNote.isPending
  const mutationError = (createNote.error ?? updateNote.error) as ApiError | null
  const form = useForm<NoteFormValues>({
    resolver: zodResolver(noteFormSchema),
    defaultValues: getDefaultValues(note),
  })

  useEffect(() => {
    if (open) {
      form.reset(getDefaultValues(note))
    }
  }, [form, note, open])

  async function onSubmit(values: NoteFormValues) {
    const payload = {
      title: values.title,
      content: values.content || undefined,
      groupId: values.groupId || undefined,
      tags: parseTags(values.tagsInput),
      color: values.color || undefined,
    }

    try {
      if (note) {
        await updateNote.mutateAsync({
          id: note.id,
          payload,
        })
        toast.success('Nota atualizada.')
      } else {
        await createNote.mutateAsync(payload)
        toast.success('Nota criada.')
      }

      onOpenChange(false)
      form.reset(getDefaultValues(null))
    } catch (error) {
      form.setError('root', {
        message: getErrorMessage(error),
      })
    }
  }

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar nota' : 'Nova nota'}</DialogTitle>
          <DialogDescription>
            Use titulo, conteudo e tags para organizar o workspace.
          </DialogDescription>
        </DialogHeader>

        <form className="space-y-5" onSubmit={form.handleSubmit(onSubmit)}>
          <div className="space-y-2">
            <Label htmlFor="note-title">Titulo</Label>
            <Input id="note-title" {...form.register('title')} />
            {form.formState.errors.title ? (
              <p className="text-sm text-destructive">
                {form.formState.errors.title.message}
              </p>
            ) : null}
            {getValidationMessage(mutationError, 'title') ? (
              <p className="text-sm text-destructive">
                {getValidationMessage(mutationError, 'title')}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="note-content">Conteudo</Label>
            <Textarea
              className="min-h-40"
              id="note-content"
              {...form.register('content')}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-[1fr_140px]">
            <div className="space-y-2">
              <Label htmlFor="note-tags">Tags</Label>
              <Input
                id="note-tags"
                placeholder="java, estudos, mercado"
                {...form.register('tagsInput')}
              />
              <p className="text-xs text-muted-foreground">
                Separe tags por virgula.
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="note-color">Cor</Label>
              <Input
                className="h-10 p-1"
                id="note-color"
                type="color"
                {...form.register('color')}
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="note-group">Grupo</Label>
            <select
              className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              id="note-group"
              {...form.register('groupId')}
            >
              <option value="">Sem grupo</option>
              {groups.map((group) => (
                <option key={group.id} value={group.id}>
                  {group.name}
                </option>
              ))}
            </select>
          </div>

          {form.formState.errors.root ? (
            <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
              {form.formState.errors.root.message}
            </p>
          ) : null}

          <DialogFooter>
            <Button
              disabled={isPending}
              onClick={() => onOpenChange(false)}
              type="button"
              variant="outline"
            >
              Cancelar
            </Button>
            <Button disabled={isPending} type="submit">
              {isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <Save className="mr-2 h-4 w-4" />
              )}
              Salvar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
