import { RefreshCcw, Server } from 'lucide-react'

import { EmptyState } from '@/components/feedback/EmptyState'
import { ErrorState } from '@/components/feedback/ErrorState'
import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { ServiceStatusCard } from '@/features/dashboard/components/ServiceStatusCard'
import type { ProviderStatusResponse } from '@/features/stocks/api/stocks-types'

type ProvidersStatusCardProps = {
  data?: ProviderStatusResponse
  isLoading: boolean
  isError: boolean
  error?: unknown
  onRetry: () => void
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
          <CardTitle>Status dos providers</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            Saude resumida do stock-service.
          </p>
        </div>
        <Button aria-label="Atualizar providers" onClick={onRetry} size="icon" variant="ghost">
          <RefreshCcw className="h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent className="space-y-3">
        {providers.length > 0 ? (
          providers.map((provider) => (
            <ServiceStatusCard key={provider.name} provider={provider} />
          ))
        ) : (
          <EmptyState
            description="O endpoint respondeu sem providers para exibir."
            icon={<Server className="h-6 w-6" />}
            title="Sem status de providers"
          />
        )}
      </CardContent>
    </Card>
  )
}
