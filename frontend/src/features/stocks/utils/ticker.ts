export function isValidTicker(symbol: string) {
  return /^[A-Z][A-Z0-9.-]{0,9}$/.test(symbol)
}
