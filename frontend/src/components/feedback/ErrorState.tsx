import { AlertTriangle } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { getErrorMessage } from '@/lib/errors/get-error-message'

type ErrorStateProps = {
  title?: string
  error?: unknown
  description?: string
  onRetry?: () => void
}

export function ErrorState({
  title = 'Nao foi possivel carregar os dados',
  error,
  description,
  onRetry,
}: ErrorStateProps) {
  const message = description ?? getErrorMessage(error)

  return (
    <Card
      aria-live="assertive"
      className="border-destructive/40 bg-destructive/10"
      role="alert"
    >
      <CardContent className="flex min-h-56 flex-col items-center justify-center gap-4 p-8 text-center">
        <div className="flex h-12 w-12 items-center justify-center rounded-md bg-destructive/15 text-destructive">
          <AlertTriangle className="h-6 w-6" />
        </div>
        <div className="space-y-1">
          <h2 className="text-lg font-semibold">{title}</h2>
          <p className="max-w-md text-sm leading-6 text-muted-foreground">{message}</p>
        </div>
        {onRetry ? (
          <Button onClick={onRetry} type="button">
            Tentar novamente
          </Button>
        ) : null}
      </CardContent>
    </Card>
  )
}
