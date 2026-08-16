import { useState } from 'react'
import AdminTermini from './admin/AdminTermini'
import AdminUsluge from './admin/AdminUsluge'
import AdminStatistika from './admin/AdminStatistika'

/**
 * Administratorski panel sa tri kartice (tab-a).
 *
 * Sve rute koje ove komponente pozivaju su pod /api/admin/** i na backendu
 * su zasticene sa hasRole('ADMIN') - klijentski token bi dobio 403.
 */
export default function AdminPanel() {
  const [kartica, setKartica] = useState('termini')

  return (
    <div>
      <div className="zaglavlje-sekcije">
        <h2>Administratorski panel</h2>
      </div>

      <div className="kartice-tabovi">
        <button
          className={kartica === 'termini' ? 'tab tab-aktivan' : 'tab'}
          onClick={() => setKartica('termini')}
        >
          Termini
        </button>
        <button
          className={kartica === 'usluge' ? 'tab tab-aktivan' : 'tab'}
          onClick={() => setKartica('usluge')}
        >
          Usluge
        </button>
        <button
          className={kartica === 'statistika' ? 'tab tab-aktivan' : 'tab'}
          onClick={() => setKartica('statistika')}
        >
          Statistika
        </button>
      </div>

      {kartica === 'termini' && <AdminTermini />}
      {kartica === 'usluge' && <AdminUsluge />}
      {kartica === 'statistika' && <AdminStatistika />}
    </div>
  )
}
