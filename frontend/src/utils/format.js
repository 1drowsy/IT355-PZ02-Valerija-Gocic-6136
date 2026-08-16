/** Pomocne funkcije za prikaz podataka (formatiranje datuma, cena, statusa). */

export function formatirajDatum(isoTekst) {
  if (!isoTekst) return '-'
  const d = new Date(isoTekst)
  return d.toLocaleString('sr-RS', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatirajVreme(isoTekst) {
  if (!isoTekst) return '-'
  return new Date(isoTekst).toLocaleTimeString('sr-RS', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function formatirajCenu(iznos) {
  if (iznos === null || iznos === undefined) return '-'
  return `${Number(iznos).toLocaleString('sr-RS', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })} RSD`
}

/** CSS klasa za "znacku" statusa termina. */
export function klasaStatusa(status) {
  const mapa = {
    ZAKAZAN: 'status-zakazan',
    POTVRDJEN: 'status-potvrdjen',
    ZAVRSEN: 'status-zavrsen',
    OTKAZAN: 'status-otkazan'
  }
  return `status ${mapa[status] || ''}`
}

export function nazivStatusa(status) {
  const mapa = {
    ZAKAZAN: 'Na čekanju',
    POTVRDJEN: 'Odobren',
    ZAVRSEN: 'Završen',
    OTKAZAN: 'Otkazan'
  }
  return mapa[status] || status
}
