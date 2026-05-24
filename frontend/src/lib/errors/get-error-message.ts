import type { ApiError } from '@/lib/api/api-types'

export function getErrorMessage(error: unknown) {
  if (error && typeof error === 'object' && 'message' in error) {
    return String(error.message)
  }

  return 'Nao foi possivel concluir a solicitacao.'
}

export function getValidationMessage(
  error: ApiError | null | undefined,
  fieldName: string,
) {
  return error?.validationErrors?.[fieldName] ?? null
}
