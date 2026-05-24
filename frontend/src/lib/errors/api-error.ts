import { AxiosError } from 'axios'

import type { ApiError } from '@/lib/api/api-types'

const fallbackMessageByStatus: Record<number, string> = {
  400: 'Revise os dados enviados.',
  401: 'Sua sessao expirou. Entre novamente.',
  403: 'Voce nao tem permissao para acessar este recurso.',
  404: 'Recurso nao encontrado.',
  409: 'Conflito ao processar a solicitacao.',
  429: 'Muitas requisicoes. Tente novamente em instantes.',
  503: 'Servico temporariamente indisponivel.',
}

function isApiError(value: unknown): value is ApiError {
  if (!value || typeof value !== 'object') {
    return false
  }

  return 'status' in value && 'message' in value
}

export function normalizeApiError(error: unknown): ApiError {
  if (isApiError(error)) {
    return error
  }

  if (error instanceof AxiosError) {
    const responseData = error.response?.data

    if (isApiError(responseData)) {
      return responseData
    }

    const status = error.response?.status ?? 0

    return {
      status,
      error: error.response?.statusText ?? 'Request Error',
      message:
        fallbackMessageByStatus[status] ??
        error.message ??
        'Nao foi possivel concluir a solicitacao.',
      correlationId: error.response?.headers?.['x-correlation-id'],
    }
  }

  if (error instanceof Error) {
    return {
      status: 0,
      error: 'Client Error',
      message: error.message,
    }
  }

  return {
    status: 0,
    error: 'Unknown Error',
    message: 'Nao foi possivel concluir a solicitacao.',
  }
}
