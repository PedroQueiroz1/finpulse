import { ArrowDownRight, ArrowUpRight, BarChart3 } from 'lucide-react'

import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import type { StockQuote } from '@/features/stocks/api/stocks-types'
import { CacheProviderMeta } from '@/features/stocks/components/CacheProviderMeta'
import { cn } from '@/lib/utils'

type QuoteCardProps = {
  quote: StockQuote
}

function formatCurrency(value: number, currency?: string) {
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency || 'USD',
    }).format(value)
  } catch {
    return `${value.toFixed(2)} ${currency ?? ''}`.trim()
  }
}

function formatNumber(value?: number) {
  if (value == null) {
    return 'N/A'
  }

  return new Intl.NumberFormat('en-US').format(value)
}

export function QuoteCard({ quote }: QuoteCardProps) {
  const changePercent = quote.changePercent ?? 0
  const isPositive = changePercent >= 0

  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div>
          <CardTitle>{quote.symbol}</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">Cotacao atual</p>
        </div>
        <div className="flex h-10 w-10 items-center justify-center rounded-md bg-emerald-500/10 text-emerald-300">
          <BarChart3 className="h-5 w-5" />
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <p className="text-4xl font-semibold">
              {formatCurrency(quote.price, quote.currency)}
            </p>
            <p className="mt-2 text-sm text-muted-foreground">
              Fechamento anterior: {formatCurrency(quote.previousClose ?? 0, quote.currency)}
            </p>
          </div>
          <Badge
            className={cn(
              'w-fit',
              isPositive
                ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-200'
                : 'border-rose-500/30 bg-rose-500/10 text-rose-200',
            )}
          >
            {isPositive ? (
              <ArrowUpRight className="mr-1 h-3 w-3" />
            ) : (
              <ArrowDownRight className="mr-1 h-3 w-3" />
            )}
            {quote.change != null ? `${quote.change.toFixed(2)} / ` : null}
            {changePercent.toFixed(2)}%
          </Badge>
        </div>

        <div className="grid gap-3 sm:grid-cols-3">
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Abertura</p>
            <p className="mt-1 font-medium">
              {quote.dayOpen != null
                ? formatCurrency(quote.dayOpen, quote.currency)
                : 'N/A'}
            </p>
          </div>
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Max/Min</p>
            <p className="mt-1 font-medium">
              {quote.dayHigh != null && quote.dayLow != null
                ? `${formatCurrency(quote.dayHigh, quote.currency)} / ${formatCurrency(
                    quote.dayLow,
                    quote.currency,
                  )}`
                : 'N/A'}
            </p>
          </div>
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Volume</p>
            <p className="mt-1 font-medium">{formatNumber(quote.volume)}</p>
          </div>
        </div>

        <CacheProviderMeta
          cacheStatus={quote.cacheStatus}
          provider={quote.provider}
          timestamp={quote.timestamp ?? quote.fetchedAt}
        />
      </CardContent>
    </Card>
  )
}
