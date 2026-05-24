import { Loader2, Trash2 } from 'lucide-react'
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
import type { Note } from '@/features/notes/api/notes-types'
import { useDeleteNote } from '@/features/notes/hooks/useDeleteNote'
import { getErrorMessage } from '@/lib/errors/get-error-message'

type DeleteNoteDialogProps = {
  note: Note | null
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function DeleteNoteDialog({
  note,
  open,
  onOpenChange,
}: DeleteNoteDialogProps) {
  const deleteNote = useDeleteNote()

  async function handleDelete() {
    if (!note) {
      return
    }

    try {
      await deleteNote.mutateAsync(note.id)
      toast.success('Nota excluida.')
      onOpenChange(false)
    } catch (error) {
      toast.error(getErrorMessage(error))
    }
  }

  return (
    <Dialog onOpenChange={onOpenChange} open={open}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Excluir nota</DialogTitle>
          <DialogDescription>
            Esta acao remove a nota selecionada permanentemente.
          </DialogDescription>
        </DialogHeader>
        <div className="rounded-lg border border-border/70 bg-secondary/30 p-4">
          <p className="line-clamp-2 text-sm font-medium">
            {note?.title ?? 'Nota selecionada'}
          </p>
        </div>
        <DialogFooter>
          <Button
            disabled={deleteNote.isPending}
            onClick={() => onOpenChange(false)}
            type="button"
            variant="outline"
          >
            Cancelar
          </Button>
          <Button
            disabled={deleteNote.isPending}
            onClick={handleDelete}
            type="button"
            variant="destructive"
          >
            {deleteNote.isPending ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="mr-2 h-4 w-4" />
            )}
            Excluir
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
