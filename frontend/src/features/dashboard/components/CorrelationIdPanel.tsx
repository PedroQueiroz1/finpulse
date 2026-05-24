import { useState } from 'react'
import { Check, Copy, RefreshCcw } from 'lucide-react'
import { toast } from 'sonner'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { getLastCorrelationId } from '@/lib/api/correlation-id'

export function CorrelationIdPanel() {
  const [correlationId, setCorrelationId] = useState(() => getLastCorrelationId())
  const [copied, setCopied] = useState(false)

  async function handleCopy() {
    if (!correlationId) {
      return
    }

    await navigator.clipboard.writeText(correlationId)
    setCopied(true)
    toast.success('Correlation ID copiado.')
    window.setTimeout(() => setCopied(false), 1500)
  }

  return (
    <Card className="border-border/70 bg-card/80">
      <CardHeader className="flex flex-row items-center justify-between gap-4">
        <div>
          <CardTitle>Correlation ID</CardTitle>
          <p className="mt-1 text-sm text-muted-foreground">
            Ultimo identificador capturado nas chamadas HTTP.
          </p>
        </div>
        <Button
          aria-label="Atualizar correlation ID"
          onClick={() => setCorrelationId(getLastCorrelationId())}
          size="icon"
          variant="ghost"
        >
          <RefreshCcw className="h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="rounded-lg border border-border/70 bg-secondary/30 p-4">
          <p className="break-all font-mono text-sm text-muted-foreground">
            {correlationId ?? 'Nenhuma chamada capturada ainda.'}
          </p>
        </div>
        <Button
          className="w-full"
          disabled={!correlationId}
          onClick={handleCopy}
          variant="outline"
        >
          {copied ? (
            <Check className="mr-2 h-4 w-4" />
          ) : (
            <Copy className="mr-2 h-4 w-4" />
          )}
          Copiar ID
        </Button>
      </CardContent>
    </Card>
  )
}
