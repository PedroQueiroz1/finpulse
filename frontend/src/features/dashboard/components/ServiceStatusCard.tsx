import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import type { ProviderStatus } from '@/features/stocks/api/stocks-types'
import { cn } from '@/lib/utils'

type ServiceStatusCardProps = {
  provider: ProviderStatus
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

export function ServiceStatusCard({ provider }: ServiceStatusCardProps) {
  return (
    <Card className="border-border/70 bg-secondary/30">
      <CardContent className="flex items-center justify-between gap-4 p-4">
        <div className="min-w-0 space-y-1">
          <p className="truncate text-sm font-medium">{provider.name}</p>
          <p className="text-xs text-muted-foreground">
            Circuit breaker: {provider.circuitBreakerState ?? 'N/A'}
          </p>
        </div>
        <Badge className={cn('shrink-0', getStatusClassName(provider.status))}>
          {provider.status}
        </Badge>
      </CardContent>
    </Card>
  )
}
