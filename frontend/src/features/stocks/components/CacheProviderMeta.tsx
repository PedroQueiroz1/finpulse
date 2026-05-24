import { Badge } from '@/components/ui/badge'

type CacheProviderMetaProps = {
  provider?: string
  cacheStatus?: string
  timestamp?: string
}

function formatTimestamp(value?: string) {
  if (!value) {
    return null
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return null
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}

export function CacheProviderMeta({
  provider,
  cacheStatus,
  timestamp,
}: CacheProviderMetaProps) {
  const formattedTimestamp = formatTimestamp(timestamp)

  return (
    <div className="flex flex-wrap gap-2">
      <Badge variant="outline">Provider: {provider ?? 'N/A'}</Badge>
      <Badge variant="outline">Cache: {cacheStatus ?? 'N/A'}</Badge>
      {formattedTimestamp ? (
        <Badge variant="outline">Atualizado: {formattedTimestamp}</Badge>
      ) : null}
    </div>
  )
}
