import axios from 'axios'

/**
 * Centralna Axios konfiguracija.
 *
 * Sve komponente koriste OVAJ objekat (`api`), nikada `axios` direktno -
 * tako se osnovna adresa i JWT token podesavaju na jednom mestu.
 */

const BASE_URL = 'http://localhost:8080/api'

export const KLJUC_TOKEN = 'salon_token'
export const KLJUC_KORISNIK = 'salon_korisnik'

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' }
})

/* ------------------------------------------------------------------
   REQUEST INTERCEPTOR
   Presrece SVAKI odlazni zahtev i, ako u localStorage postoji token,
   dodaje header:  Authorization: Bearer <token>
   Bez ovoga bi svaka zasticena ruta vracala 401.
------------------------------------------------------------------- */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(KLJUC_TOKEN)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/* ------------------------------------------------------------------
   RESPONSE INTERCEPTOR
   1) ako backend vrati 401 (token istekao/neispravan) - brisemo token
      i vracamo korisnika na stranicu za prijavu,
   2) poruku greske izvlacimo iz naseg GreskaOdgovor JSON-a i kacimo je
      na error.poruka, da je komponente lako prikazu.
------------------------------------------------------------------- */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const odgovor = error.response

    if (odgovor?.status === 401 && !odgovor.config.url.includes('/auth/')) {
      localStorage.removeItem(KLJUC_TOKEN)
      localStorage.removeItem(KLJUC_KORISNIK)
      window.location.href = '/prijava'
    }

    error.poruka = izvuciPoruku(error)
    return Promise.reject(error)
  }
)

/** Pretvara odgovor backenda u jednu citljivu poruku za korisnika. */
function izvuciPoruku(error) {
  const podaci = error.response?.data
  if (!podaci) return 'Server nije dostupan. Da li je backend pokrenut?'

  // greske validacije: { greskeValidacije: { email: "...", lozinka: "..." } }
  if (podaci.greskeValidacije) {
    return Object.values(podaci.greskeValidacije).join(' ')
  }
  return podaci.poruka || 'Došlo je do greške.'
}

export default api

/* ==================================================================
   Funkcije po domenima - komponente ne pisu URL-ove rucno.
   ================================================================== */

export const authApi = {
  prijava: (podaci) => api.post('/auth/login', podaci),
  registracija: (podaci) => api.post('/auth/register', podaci),
  profil: () => api.get('/auth/me')
}

export const javnoApi = {
  usluge: () => api.get('/javno/usluge'),
  uslugaPoId: (id) => api.get(`/javno/usluge/${id}`),
  kategorije: () => api.get('/javno/kategorije'),
  kozmeticari: () => api.get('/javno/kozmeticari'),
  kozmeticariZaUslugu: (uslugaId) =>
    api.get('/javno/kozmeticari', { params: { uslugaId } }),
  recenzijeKozmeticara: (id) => api.get(`/javno/kozmeticari/${id}/recenzije`)
}

export const terminApi = {
  zakazi: (podaci) => api.post('/termini', podaci),
  moji: () => api.get('/termini/moji'),
  otkazi: (id) => api.put(`/termini/${id}/otkazi`),
  dostupnost: (kozmeticarId, uslugaId, datum) =>
    api.get('/termini/dostupnost', { params: { kozmeticarId, uslugaId, datum } })
}

export const recenzijaApi = {
  kreiraj: (podaci) => api.post('/recenzije', podaci),
  moje: () => api.get('/recenzije/moje')
}

export const adminApi = {
  termini: (status) =>
    api.get('/admin/termini', { params: status ? { status } : {} }),
  promeniStatus: (id, status) =>
    api.put(`/admin/termini/${id}/status`, { status }),

  usluge: () => api.get('/admin/usluge'),
  kreirajUslugu: (podaci) => api.post('/admin/usluge', podaci),
  izmeniUslugu: (id, podaci) => api.put(`/admin/usluge/${id}`, podaci),
  obrisiUslugu: (id) => api.delete(`/admin/usluge/${id}`),

  kozmeticari: () => api.get('/admin/kozmeticari'),
  kreirajKozmeticara: (podaci) => api.post('/admin/kozmeticari', podaci),
  izmeniKozmeticara: (id, podaci) => api.put(`/admin/kozmeticari/${id}`, podaci),
  obrisiKozmeticara: (id) => api.delete(`/admin/kozmeticari/${id}`),

  statistika: () => api.get('/admin/statistika'),
  korisnici: () => api.get('/admin/korisnici')
}
