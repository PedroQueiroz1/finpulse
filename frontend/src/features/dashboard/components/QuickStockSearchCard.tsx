import { FormEvent, useMemo, useState } from 'react'
import { ArrowDownRight, ArrowUpRight, Loader2, Search } from 'lucide-react'
import { Link } from 'react-router-dom'

import { routes } from '@/app/router/routes'
import { ErrorState } from '@/components/feedback/ErrorState'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useStockQuote } from '@/features/stocks/hooks/useStockQuote'
import { cn } from '@/lib/utils'

function isValidTicker(value: string) {
  return /^[A-Z][A-Z0-9.-]{0,9}$/.test(value)
}

function formatCurrency(value: number, currency: string) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD',
  }).format(value)
}

export function QuickStockSearchCard() {
  const [inputValue, setInputValue] = useState('')
  const [submittedSymbol, setSubmittedSymbol] = useState<string | null>(null)
  const [validationError, setValidationError] = useState<string | null>(null)
  const quoteQuery = useStockQuote(submittedSymbol)
  const quote = quoteQuery.data
  const changePercent = quote?.changePercent ?? 0
  const isPositive = changePercent >= 0
  const formattedTimestamp = useMemo(() => {
    if (!quote?.timestamp) {
      return null
    }

    const date = new Date(quote.timestamp)

    if (Number.isNaN(date.getTime())) {
      return null
    }

    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(date)
  }, [quote?.timestamp])

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const normalizedValue = inputValue.trim().toUpperCase()

    if (!isValidTicker(normalizedValue)) {
      setValidationError('Use um ticker valido, como AAPL ou MSFT.')
      return
    }

    setValidationError(null)
    setInputValue(normalizedValue)
    setSubmittedSymbol(normalizedValue)
  }

  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div>
          <CardTitle>Cotacao rapida</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            Consulte um ticker sem sair do dashboard.
          </p>
        </div>
        <Button asChild variant="outline">
          <Link to={routes.stocks}>Abrir stocks</Link>
        </Button>
      </CardHeader>
      <CardContent className="space-y-5">
        <form className="flex flex-col gap-3 sm:flex-row" onSubmit={handleSubmit}>
          <div className="flex-1 space-y-2">
            <Label htmlFor="quick-stock-symbol">Ticker</Label>
            <Input
              id="quick-stock-symbol"
              onChange={(event) => setInputValue(event.target.value.toUpperCase())}
              placeholder="AAPL"
              value={inputValue}
            />
          </div>
          <Button className="sm:mt-8" disabled={quoteQuery.isFetching} type="submit">
            {quoteQuery.isFetching ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Search className="mr-2 h-4 w-4" />
            )}
            Buscar
          </Button>
        </form>

        {validationError ? (
          <p className="rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {validationError}
          </p>
        ) : null}

        {quoteQuery.isError ? (
          <ErrorState
            error={quoteQuery.error}
            onRetry={() => void quoteQuery.refetch()}
            title="Nao foi possivel buscar o ticker"
          />
        ) : null}

        {!quote && !quoteQuery.isError ? (
          <div className="rounded-lg border border-dashed border-border/70 p-5 text-sm text-muted-foreground">
            Busque um ativo para ver preco, variacao, provider e cache.
          </div>
        ) : null}

        {quote ? (
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <p className="text-sm text-muted-foreground">{quote.symbol}</p>
                <p className="mt-1 text-3xl font-semibold">
                  {formatCurrency(quote.price, quote.currency)}
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
                {changePercent.toFixed(2)}%
              </Badge>
            </div>
            <div className="mt-5 flex flex-wrap gap-2">
              <Badge variant="outline">Provider: {quote.provider ?? 'N/A'}</Badge>
              <Badge variant="outline">Cache: {quote.cacheStatus ?? 'N/A'}</Badge>
              {formattedTimestamp ? (
                <Badge variant="outline">{formattedTimestamp}</Badge>
              ) : null}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
