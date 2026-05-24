import { RefreshCcw } from 'lucide-react'

import { ErrorState } from '@/components/feedback/ErrorState'
import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import type { ProviderStatusResponse } from '@/features/stocks/api/stocks-types'
import { cn } from '@/lib/utils'

type ProvidersStatusCardProps = {
  data?: ProviderStatusResponse
  isLoading: boolean
  isError: boolean
  error?: unknown
  onRetry: () => void
}

function getStatusClassName(status: string) {
  if (status === 'UP') {
    return 'border-emerald-500/30 bg-emerald-500/10 text-emerald-200'
  }

  if (status === 'DEGRADED') {
    return 'border-amber-500/30 bg-amber-500/10 text-amber-200'
  }

  return 'border-rose-500/30 bg-rose-500/10 text-rose-200'
}

export function ProvidersStatusCard({
  data,
  isLoading,
  isError,
  error,
  onRetry,
}: ProvidersStatusCardProps) {
  if (isLoading) {
    return <SkeletonCard rows={4} />
  }

  if (isError) {
    return <ErrorState error={error} onRetry={onRetry} title="Providers indisponiveis" />
  }

  const providers = data?.providers ?? []

  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <div>
          <CardTitle>Providers</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            Providers retornados pelo stock-service.
          </p>
        </div>
        <Button aria-label="Atualizar providers" onClick={onRetry} size="icon" variant="ghost">
          <RefreshCcw className="h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent className="space-y-3">
        {providers.length > 0 ? (
          providers.map((provider) => (
            <div
              className="flex items-center justify-between gap-4 rounded-lg border border-border/70 bg-secondary/30 p-4"
              key={provider.name}
            >
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{provider.name}</p>
                <p className="text-xs text-muted-foreground">
                  Circuit breaker: {provider.circuitBreakerState ?? 'N/A'}
                </p>
              </div>
              <Badge className={cn('shrink-0', getStatusClassName(provider.status))}>
                {provider.status}
              </Badge>
            </div>
          ))
        ) : (
          <div className="rounded-lg border border-dashed border-border/70 p-6 text-center">
            <p className="text-sm font-medium">Nenhum provider encontrado</p>
            <p className="mt-1 text-sm text-muted-foreground">
              O endpoint respondeu sem providers.
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
