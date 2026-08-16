import { createContext, useContext, useEffect, useState } from 'react'
import { authApi, KLJUC_KORISNIK, KLJUC_TOKEN } from '../api/api'

/**
 * Globalno stanje prijave.
 *
 * React Context se koristi da bi podaci o prijavljenom korisniku bili
 * dostupni svim komponentama (Navbar, ProtectedRoute, stranice) bez
 * rucnog prosledjivanja kroz props.
 */
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [korisnik, setKorisnik] = useState(null)
  const [ucitavanje, setUcitavanje] = useState(true)

  // Pri osvezavanju stranice vracamo korisnika iz localStorage
  useEffect(() => {
    const sacuvan = localStorage.getItem(KLJUC_KORISNIK)
    if (sacuvan) {
      try {
        setKorisnik(JSON.parse(sacuvan))
      } catch {
        localStorage.removeItem(KLJUC_KORISNIK)
      }
    }
    setUcitavanje(false)
  }, [])

  /** Sacuva token i podatke o korisniku nakon prijave/registracije. */
  function sacuvajSesiju(podaci) {
    const noviKorisnik = {
      id: podaci.korisnikId,
      email: podaci.email,
      punoIme: podaci.punoIme,
      uloga: podaci.uloga,
      student: podaci.student
    }
    localStorage.setItem(KLJUC_TOKEN, podaci.token)
    localStorage.setItem(KLJUC_KORISNIK, JSON.stringify(noviKorisnik))
    setKorisnik(noviKorisnik)
    return noviKorisnik
  }

  async function prijava(email, lozinka) {
    const { data } = await authApi.prijava({ email, lozinka })
    return sacuvajSesiju(data)
  }

  async function registracija(podaci) {
    const { data } = await authApi.registracija(podaci)
    return sacuvajSesiju(data)
  }

  function odjava() {
    localStorage.removeItem(KLJUC_TOKEN)
    localStorage.removeItem(KLJUC_KORISNIK)
    setKorisnik(null)
  }

  const vrednost = {
    korisnik,
    ucitavanje,
    prijava,
    registracija,
    odjava,
    jePrijavljen: korisnik !== null,
    jeAdmin: korisnik?.uloga === 'ROLE_ADMIN'
  }

  return <AuthContext.Provider value={vrednost}>{children}</AuthContext.Provider>
}

/** Prakticna kuka: const { korisnik, odjava } = useAuth() */
export function useAuth() {
  const kontekst = useContext(AuthContext)
  if (!kontekst) {
    throw new Error('useAuth se mora koristiti unutar <AuthProvider>')
  }
  return kontekst
}
