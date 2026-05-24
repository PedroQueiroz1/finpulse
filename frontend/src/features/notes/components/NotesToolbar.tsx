import { FormEvent, useEffect, useState } from 'react'
import { Search, X } from 'lucide-react'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

type NotesToolbarProps = {
  search: string
  resultCount: number
  onSearch: (value: string) => void
  onClear: () => void
}

export function NotesToolbar({
  search,
  resultCount,
  onSearch,
  onClear,
}: NotesToolbarProps) {
  const [draftSearch, setDraftSearch] = useState(search)

  useEffect(() => {
    setDraftSearch(search)
  }, [search])

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onSearch(draftSearch.trim())
  }

  return (
    <div className="flex flex-col gap-3 rounded-lg border border-border/70 bg-card/80 p-4 lg:flex-row lg:items-center lg:justify-between">
      <form className="flex flex-1 flex-col gap-3 sm:flex-row" onSubmit={handleSubmit}>
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            className="pl-9"
            onChange={(event) => setDraftSearch(event.target.value)}
            placeholder="Buscar por titulo ou conteudo"
            value={draftSearch}
          />
        </div>
        <Button type="submit">Buscar</Button>
        {search ? (
          <Button onClick={onClear} type="button" variant="outline">
            <X className="mr-2 h-4 w-4" />
            Limpar
          </Button>
        ) : null}
      </form>
      <p className="text-sm text-muted-foreground">
        {resultCount} nota{resultCount === 1 ? '' : 's'}
      </p>
    </div>
  )
}
