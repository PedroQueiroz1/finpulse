import { useMemo } from 'react'
import { AlertTriangle, Search } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'

import { EmptyState } from '@/components/feedback/EmptyState'
import { ErrorState } from '@/components/feedback/ErrorState'
import { SkeletonCard } from '@/components/feedback/SkeletonCard'
import { PageHeader } from '@/components/layout/PageHeader'
import { CompanyCard } from '@/features/stocks/components/CompanyCard'
import { ProvidersStatusCard } from '@/features/stocks/components/ProvidersStatusCard'
import { QuoteCard } from '@/features/stocks/components/QuoteCard'
import { TickerSearchForm } from '@/features/stocks/components/TickerSearchForm'
import { useCompanyInfo } from '@/features/stocks/hooks/useCompanyInfo'
import { useProviders } from '@/features/stocks/hooks/useProviders'
import { useStockQuote } from '@/features/stocks/hooks/useStockQuote'
import { isValidTicker } from '@/features/stocks/utils/ticker'

export function StocksPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const symbol = useMemo(
    () => searchParams.get('symbol')?.trim().toUpperCase() ?? '',
    [searchParams],
  )
  const hasSymbol = symbol.length > 0
  const symbolIsValid = hasSymbol && isValidTicker(symbol)
  const quoteQuery = useStockQuote(symbol, symbolIsValid)
  const companyQuery = useCompanyInfo(symbol, symbolIsValid)
  const providersQuery = useProviders()
  const isSearching = quoteQuery.isFetching || companyQuery.isFetching

  function handleSubmit(nextSymbol: string) {
    setSearchParams({ symbol: nextSymbol })
  }

  function handleClear() {
    setSearchParams({})
  }

  return (
    <div className="space-y-6">
      <PageHeader
        description="Consulta de ticker, empresa, cache e providers."
        eyebrow="Mercado"
        title="Stocks"
      />

      <TickerSearchForm
        isLoading={isSearching}
        onClear={handleClear}
        onSubmit={handleSubmit}
        symbol={symbol}
      />

      {hasSymbol && !symbolIsValid ? (
        <ErrorState
          description="Use um ticker valido, como AAPL, MSFT ou PETR4."
          title="Ticker invalido"
        />
      ) : null}

      {!hasSymbol ? (
        <EmptyState
          icon={<Search className="h-6 w-6" />}
          description="Busque um ativo para ver cotacao, dados da empresa, provider e cache."
          title="Busque um ticker"
        />
      ) : null}

      {symbolIsValid ? (
        <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
          <div className="space-y-6">
            {quoteQuery.isLoading ? <SkeletonCard rows={5} /> : null}
            {quoteQuery.isError ? (
              <ErrorState
                error={quoteQuery.error}
                onRetry={() => void quoteQuery.refetch()}
                title="Nao foi possivel carregar a cotacao"
              />
            ) : null}
            {quoteQuery.data ? <QuoteCard quote={quoteQuery.data} /> : null}

            {companyQuery.isLoading ? <SkeletonCard rows={6} /> : null}
            {companyQuery.isError ? (
              <ErrorState
                error={companyQuery.error}
                onRetry={() => void companyQuery.refetch()}
                title="Dados da empresa indisponiveis"
              />
            ) : null}
            {companyQuery.data ? <CompanyCard company={companyQuery.data} /> : null}
          </div>

          <div className="space-y-6">
            <ProvidersStatusCard
              data={providersQuery.data}
              error={providersQuery.error}
              isError={providersQuery.isError}
              isLoading={providersQuery.isLoading}
              onRetry={() => void providersQuery.refetch()}
            />
            <EmptyState
              icon={<AlertTriangle className="h-6 w-6" />}
              description="Erros 503 aparecem isolados no card afetado, mantendo o restante da tela utilizavel."
              title="Resiliencia por card"
            />
          </div>
        </div>
      ) : null}

      {!hasSymbol ? (
        <ProvidersStatusCard
          data={providersQuery.data}
          error={providersQuery.error}
          isError={providersQuery.isError}
          isLoading={providersQuery.isLoading}
          onRetry={() => void providersQuery.refetch()}
        />
      ) : null}
    </div>
  )
}
