import { FormEvent, useEffect, useState } from 'react'
import { Search, X } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { isValidTicker } from '@/features/stocks/utils/ticker'

type TickerSearchFormProps = {
  symbol?: string
  isLoading?: boolean
  onSubmit: (symbol: string) => void
  onClear: () => void
}

export function TickerSearchForm({
  symbol,
  isLoading,
  onSubmit,
  onClear,
}: TickerSearchFormProps) {
  const [draftSymbol, setDraftSymbol] = useState(symbol ?? '')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setDraftSymbol(symbol ?? '')
  }, [symbol])

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const normalizedSymbol = draftSymbol.trim().toUpperCase()

    if (!isValidTicker(normalizedSymbol)) {
      setError('Use um ticker valido, como AAPL, MSFT ou PETR4.')
      return
    }

    setError(null)
    setDraftSymbol(normalizedSymbol)
    onSubmit(normalizedSymbol)
  }

  function handleClear() {
    setError(null)
    setDraftSymbol('')
    onClear()
  }

  return (
    <form
      className="rounded-lg border border-border/70 bg-card/80 p-4"
      onSubmit={handleSubmit}
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
        <div className="flex-1 space-y-2">
          <Label htmlFor="ticker-symbol">Ticker</Label>
          <Input
            id="ticker-symbol"
            onChange={(event) => setDraftSymbol(event.target.value.toUpperCase())}
            placeholder="AAPL"
            value={draftSymbol}
          />
        </div>
        <Button disabled={isLoading} type="submit">
          <Search className="mr-2 h-4 w-4" />
          Buscar
        </Button>
        {symbol ? (
          <Button onClick={handleClear} type="button" variant="outline">
            <X className="mr-2 h-4 w-4" />
            Limpar
          </Button>
        ) : null}
      </div>
      {error ? <p className="mt-3 text-sm text-destructive">{error}</p> : null}
    </form>
  )
}
