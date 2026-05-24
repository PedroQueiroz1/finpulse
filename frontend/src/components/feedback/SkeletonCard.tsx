import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

type SkeletonCardProps = {
  rows?: number
}

export function SkeletonCard({ rows = 3 }: SkeletonCardProps) {
  return (
    <Card aria-busy="true" className="border-border/70 bg-card/80">
      <CardHeader className="space-y-2">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-6 w-40" />
      </CardHeader>
      <CardContent className="space-y-3">
        {Array.from({ length: rows }).map((_, index) => (
          <Skeleton className="h-4 w-full" key={index} />
        ))}
      </CardContent>
    </Card>
  )
}
