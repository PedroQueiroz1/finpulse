import type { ComponentType } from 'react'

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

type MetricCardProps = {
  title: string
  value: string | number
  description?: string
  icon: ComponentType<{ className?: string }>
  isLoading?: boolean
  isError?: boolean
  tone?: 'emerald' | 'cyan' | 'amber' | 'rose'
}

const toneClassName = {
  emerald: 'text-emerald-300 bg-emerald-500/10',
  cyan: 'text-cyan-300 bg-cyan-500/10',
  amber: 'text-amber-300 bg-amber-500/10',
  rose: 'text-rose-300 bg-rose-500/10',
}

export function MetricCard({
  title,
  value,
  description,
  icon: Icon,
  isLoading,
  isError,
  tone = 'emerald',
}: MetricCardProps) {
  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-center justify-between gap-4 space-y-0">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <div
          className={cn(
            'flex h-9 w-9 items-center justify-center rounded-md',
            toneClassName[tone],
          )}
        >
          <Icon className="h-4 w-4" />
        </div>
      </CardHeader>
      <CardContent className="space-y-2">
        {isLoading ? (
          <Skeleton className="h-8 w-20" />
        ) : (
          <div className="text-2xl font-semibold">
            {isError ? 'Indisponivel' : value}
          </div>
        )}
        {description ? (
          <p className="text-sm leading-6 text-muted-foreground">{description}</p>
        ) : null}
      </CardContent>
    </Card>
  )
}
