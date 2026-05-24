import { formatDistanceToNow } from 'date-fns'
import { FileText } from 'lucide-react'
import { Link } from 'react-router-dom'

import { routes } from '@/app/router/routes'
import { EmptyState } from '@/components/feedback/EmptyState'
import { ErrorState } from '@/components/feedback/ErrorState'
import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import type { Note } from '@/features/notes/api/notes-types'

type RecentNotesCardProps = {
  notes?: Note[]
  isLoading: boolean
  isError: boolean
  error?: unknown
  onRetry: () => void
}

function formatDate(value?: string) {
  if (!value) {
    return 'Sem data'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return 'Data invalida'
  }

  return formatDistanceToNow(date, { addSuffix: true })
}

export function RecentNotesCard({
  notes,
  isLoading,
  isError,
  error,
  onRetry,
}: RecentNotesCardProps) {
  if (isLoading) {
    return <SkeletonCard rows={5} />
  }

  if (isError) {
    return <ErrorState error={error} onRetry={onRetry} title="Notas indisponiveis" />
  }

  const recentNotes = [...(notes ?? [])]
    .sort((left, right) => {
      const leftDate = new Date(left.updatedAt ?? left.createdAt ?? 0).getTime()
      const rightDate = new Date(right.updatedAt ?? right.createdAt ?? 0).getTime()

      return rightDate - leftDate
    })
    .slice(0, 4)

  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <div>
          <CardTitle>Notas recentes</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            Ultimas atualizacoes do workspace.
          </p>
        </div>
        <Button asChild variant="outline">
          <Link to={routes.notes}>Abrir notes</Link>
        </Button>
      </CardHeader>
      <CardContent className="space-y-3">
        {recentNotes.length > 0 ? (
          recentNotes.map((note) => (
            <div
              className="rounded-lg border border-border/70 bg-secondary/30 p-4"
              key={note.id}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 space-y-1">
                  <h3 className="truncate text-sm font-medium">{note.title}</h3>
                  <p className="line-clamp-2 whitespace-pre-line text-sm leading-6 text-muted-foreground">
                    {note.content || 'Sem conteudo.'}
                  </p>
                </div>
                <FileText className="mt-1 h-4 w-4 shrink-0 text-emerald-300" />
              </div>
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <span className="text-xs text-muted-foreground">
                  {formatDate(note.updatedAt ?? note.createdAt)}
                </span>
                {(note.tags ?? []).slice(0, 3).map((tag) => (
                  <Badge key={tag} variant="outline">
                    {tag}
                  </Badge>
                ))}
              </div>
            </div>
          ))
        ) : (
          <EmptyState
            actionLabel="Criar nota"
            description="As notas criadas na fase 13.5 aparecerao aqui."
            title="Sem notas recentes"
          />
        )}
      </CardContent>
    </Card>
  )
}
