import { X } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'

export type TagOption = {
  name: string
  count: number
}

type TagFilterProps = {
  tags: TagOption[]
  selectedTag?: string
  onSelectTag: (tag: string | null) => void
}

export function TagFilter({ tags, selectedTag, onSelectTag }: TagFilterProps) {
  if (tags.length === 0) {
    return null
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      {selectedTag ? (
        <Button onClick={() => onSelectTag(null)} size="sm" variant="outline">
          <X className="mr-2 h-3.5 w-3.5" />
          Limpar tag
        </Button>
      ) : null}
      {tags.map((tag) => (
        <Button
          key={tag.name}
          onClick={() => onSelectTag(tag.name)}
          size="sm"
          variant={selectedTag === tag.name ? 'secondary' : 'outline'}
        >
          {tag.name}
          <Badge className="ml-2" variant="outline">
            {tag.count}
          </Badge>
        </Button>
      ))}
    </div>
  )
}
