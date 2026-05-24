const LAST_CORRELATION_ID_KEY = 'finpulse.lastCorrelationId'

function canUseLocalStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function fallbackRandomId() {
  return `corr-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

export function createCorrelationId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }

  return fallbackRandomId()
}

export function getLastCorrelationId() {
  if (!canUseLocalStorage()) {
    return null
  }

  return window.localStorage.getItem(LAST_CORRELATION_ID_KEY)
}

export function setLastCorrelationId(correlationId: string) {
  if (!canUseLocalStorage()) {
    return
  }

  window.localStorage.setItem(LAST_CORRELATION_ID_KEY, correlationId)
}
