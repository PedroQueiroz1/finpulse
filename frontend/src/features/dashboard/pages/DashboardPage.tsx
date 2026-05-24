import { Activity, FileText, LineChart, ShieldCheck, Tags } from 'lucide-react'

import { PageHeader } from '@/components/layout/PageHeader'
import { CorrelationIdPanel } from '@/features/dashboard/components/CorrelationIdPanel'
import { MetricCard } from '@/features/dashboard/components/MetricCard'
import { ProvidersStatusCard } from '@/features/dashboard/components/ProvidersStatusCard'
import { QuickStockSearchCard } from '@/features/dashboard/components/QuickStockSearchCard'
import { RecentNotesCard } from '@/features/dashboard/components/RecentNotesCard'
import { useCurrentUser } from '@/features/auth/hooks/useCurrentUser'
import { useNoteGroups } from '@/features/notes/hooks/useNoteGroups'
import { useNotes } from '@/features/notes/hooks/useNotes'
import { useProviders } from '@/features/stocks/hooks/useProviders'
import { getLastCorrelationId } from '@/lib/api/correlation-id'

export function DashboardPage() {
  const currentUserQuery = useCurrentUser()
  const notesQuery = useNotes()
  const noteGroupsQuery = useNoteGroups()
  const providersQuery = useProviders()
  const notes = notesQuery.data ?? []
  const groups = noteGroupsQuery.data ?? []
  const providers = providersQuery.data?.providers ?? []
  const healthyProviders = providers.filter((provider) => provider.status === 'UP')
  const mostUsedTag = [...groups].sort((left, right) => right.count - left.count)[0]
  const lastCorrelationId = getLastCorrelationId()

  return (
    <div className="space-y-6">
      <PageHeader
        description={
          currentUserQuery.data?.name
            ? `Ola, ${currentUserQuery.data.name}. Aqui esta o resumo do seu workspace.`
            : 'Visao inicial para produtividade, mercado e diagnostico.'
        }
        eyebrow="Overview"
        title="Dashboard"
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          description={
            currentUserQuery.data?.email ?? 'Validando usuario autenticado.'
          }
          icon={ShieldCheck}
          isError={currentUserQuery.isError}
          isLoading={currentUserQuery.isLoading}
          title="Sessao"
          value={currentUserQuery.data?.active ? 'Ativa' : 'Pendente'}
        />
        <MetricCard
          description="Notas encontradas para o usuario autenticado."
          icon={FileText}
          isError={notesQuery.isError}
          isLoading={notesQuery.isLoading}
          title="Notas"
          value={notes.length}
          tone="cyan"
        />
        <MetricCard
          description={
            mostUsedTag
              ? `${mostUsedTag.name} aparece ${mostUsedTag.count} vez(es).`
              : 'Nenhuma tag retornada ainda.'
          }
          icon={Tags}
          isError={noteGroupsQuery.isError}
          isLoading={noteGroupsQuery.isLoading}
          title="Top tag"
          value={mostUsedTag?.name ?? '--'}
          tone="amber"
        />
        <MetricCard
          description="Providers UP no stock-service."
          icon={LineChart}
          isError={providersQuery.isError}
          isLoading={providersQuery.isLoading}
          title="Providers"
          value={`${healthyProviders.length}/${providers.length || 0}`}
          tone="emerald"
        />
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <RecentNotesCard
          error={notesQuery.error}
          isError={notesQuery.isError}
          isLoading={notesQuery.isLoading}
          notes={notes}
          onRetry={() => void notesQuery.refetch()}
        />
        <div className="space-y-6">
          <ProvidersStatusCard
            data={providersQuery.data}
            error={providersQuery.error}
            isError={providersQuery.isError}
            isLoading={providersQuery.isLoading}
            onRetry={() => void providersQuery.refetch()}
          />
          <MetricCard
            description={
              lastCorrelationId
                ? 'Ultima chamada HTTP registrada.'
                : 'Ainda nao ha chamada HTTP capturada.'
            }
            icon={Activity}
            title="Observability"
            value={lastCorrelationId ? 'Ativa' : '--'}
            tone="rose"
          />
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <QuickStockSearchCard />
        <CorrelationIdPanel />
      </div>
    </div>
  )
}
