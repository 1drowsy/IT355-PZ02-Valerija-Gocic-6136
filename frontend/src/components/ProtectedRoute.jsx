import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * Zastita ruta na frontendu.
 *
 * VAZNO ZA ODBRANU: ovo je samo pogodnost za korisnika (da ne vidi prazne
 * ekrane). Prava zastita je na BACKENDU - i da neko rucno otvori /admin,
 * svaki poziv ka /api/admin/** ce vratiti 403 bez ADMIN uloge.
 *
 * Upotreba:
 *   <ProtectedRoute>            -> samo prijavljeni
 *   <ProtectedRoute samoAdmin>  -> samo ROLE_ADMIN
 */
export default function ProtectedRoute({ children, samoAdmin = false }) {
  const { jePrijavljen, jeAdmin, ucitavanje } = useAuth()
  const lokacija = useLocation()

  // Dok se cita localStorage ne donosimo odluku (izbegava se "treperenje")
  if (ucitavanje) {
    return <div className="ucitavanje">Učitavanje…</div>
  }

  if (!jePrijavljen) {
    // state={{ od }} pamti gde je korisnik hteo da ide pre prijave
    return <Navigate to="/prijava" state={{ od: lokacija.pathname }} replace />
  }

  if (samoAdmin && !jeAdmin) {
    return (
      <div className="kartica poruka-greska">
        <h2>403 — Zabranjen pristup</h2>
        <p>Ova stranica je dostupna samo administratoru salona.</p>
      </div>
    )
  }

  return children
}
