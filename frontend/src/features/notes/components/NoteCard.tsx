import { CalendarDays, Edit, MoreVertical, Tags, Trash2 } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { Note } from '@/features/notes/api/notes-types'

type NoteCardProps = {
  note: Note
  onEdit: (note: Note) => void
  onDelete: (note: Note) => void
}

function formatDate(value?: string) {
  if (!value) {
    return 'Sem data'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return 'Data invalida'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

export function NoteCard({ note, onEdit, onDelete }: NoteCardProps) {
  return (
    <Card className="flex h-full flex-col border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div className="min-w-0 space-y-2">
          <CardTitle className="line-clamp-2 text-base">{note.title}</CardTitle>
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <CalendarDays className="h-3.5 w-3.5" />
            {formatDate(note.updatedAt ?? note.createdAt)}
          </div>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button aria-label="Acoes da nota" size="icon" variant="ghost">
              <MoreVertical className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => onEdit(note)}>
              <Edit className="mr-2 h-4 w-4" />
              Editar
            </DropdownMenuItem>
            <DropdownMenuItem
              className="text-destructive focus:text-destructive"
              onClick={() => onDelete(note)}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Excluir
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col gap-4">
        <p className="line-clamp-6 whitespace-pre-line text-sm leading-6 text-muted-foreground">
          {note.content || 'Sem conteudo.'}
        </p>
        <div className="mt-auto flex flex-wrap gap-2">
          {(note.tags ?? []).length > 0 ? (
            note.tags?.map((tag) => (
              <Badge key={tag} variant="outline">
                <Tags className="mr-1 h-3 w-3" />
                {tag}
              </Badge>
            ))
          ) : (
            <Badge variant="outline">Sem tags</Badge>
          )}
          {note.pinned ? <Badge>Fixada</Badge> : null}
          {note.archived ? <Badge variant="secondary">Arquivada</Badge> : null}
        </div>
      </CardContent>
    </Card>
  )
}
