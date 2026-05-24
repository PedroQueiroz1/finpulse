import { Building2, ExternalLink } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import type { CompanyInfo } from '@/features/stocks/api/stocks-types'

type CompanyCardProps = {
  company: CompanyInfo
}

export function CompanyCard({ company }: CompanyCardProps) {
  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div>
          <CardTitle>{company.name ?? company.symbol}</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            {company.exchange ?? 'Exchange N/A'} · {company.country ?? 'Pais N/A'}
          </p>
        </div>
        {company.logoUrl ? (
          <img
            alt=""
            className="h-10 w-10 rounded-md border border-border/70 bg-secondary object-contain"
            src={company.logoUrl}
          />
        ) : (
          <div className="flex h-10 w-10 items-center justify-center rounded-md bg-cyan-500/10 text-cyan-300">
            <Building2 className="h-5 w-5" />
          </div>
        )}
      </CardHeader>
      <CardContent className="space-y-5">
        <p className="text-sm leading-6 text-muted-foreground">
          {company.description ?? 'Descricao nao disponivel para este ticker.'}
        </p>

        <div className="grid gap-3 sm:grid-cols-2">
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Setor</p>
            <p className="mt-1 text-sm font-medium">{company.sector ?? 'N/A'}</p>
          </div>
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Industria</p>
            <p className="mt-1 text-sm font-medium">{company.industry ?? 'N/A'}</p>
          </div>
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Moeda</p>
            <p className="mt-1 text-sm font-medium">{company.currency ?? 'N/A'}</p>
          </div>
          <div className="rounded-lg border border-border/70 bg-secondary/30 p-3">
            <p className="text-xs text-muted-foreground">Provider</p>
            <p className="mt-1 text-sm font-medium">{company.provider ?? 'N/A'}</p>
          </div>
        </div>

        {company.website ? (
          <Button asChild variant="outline">
            <a href={company.website} rel="noreferrer" target="_blank">
              Site da empresa
              <ExternalLink className="ml-2 h-4 w-4" />
            </a>
          </Button>
        ) : null}
      </CardContent>
    </Card>
  )
}
