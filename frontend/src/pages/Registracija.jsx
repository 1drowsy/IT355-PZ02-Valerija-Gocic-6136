import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Poruka from '../components/Poruka'

/** Forma za registraciju novog klijenta (backend uvek dodeljuje ROLE_KLIJENT). */
export default function Registracija() {
  const [podaci, setPodaci] = useState({
    ime: '',
    prezime: '',
    email: '',
    lozinka: '',
    telefon: '',
    student: false
  })
  const [greska, setGreska] = useState('')
  const [salje, setSalje] = useState(false)

  const { registracija } = useAuth()
  const navigacija = useNavigate()

  /** Jedna funkcija za sva polja - koristi name atribut inputa. */
  function promena(dogadjaj) {
    const { name, value, type, checked } = dogadjaj.target
    setPodaci((prethodno) => ({
      ...prethodno,
      [name]: type === 'checkbox' ? checked : value
    }))
  }

  async function posalji(dogadjaj) {
    dogadjaj.preventDefault()
    setGreska('')
    setSalje(true)

    try {
      await registracija(podaci)
      navigacija('/zakazivanje')
    } catch (e) {
      setGreska(e.poruka || 'Registracija nije uspela.')
    } finally {
      setSalje(false)
    }
  }

  return (
    <div className="uski-sadrzaj">
      <div className="kartica">
        <h2>Registracija</h2>
        <p className="podnaslov">Napravite nalog i zakažite prvi termin.</p>

        <Poruka tekst={greska} />

        <form onSubmit={posalji}>
          <div className="red">
            <label>
              Ime
              <input name="ime" value={podaci.ime} onChange={promena} required />
            </label>
            <label>
              Prezime
              <input name="prezime" value={podaci.prezime} onChange={promena} required />
            </label>
          </div>

          <label>
            Email
            <input
              type="email"
              name="email"
              value={podaci.email}
              onChange={promena}
              required
            />
          </label>

          <label>
            Lozinka <small>(najmanje 6 karaktera)</small>
            <input
              type="password"
              name="lozinka"
              value={podaci.lozinka}
              onChange={promena}
              minLength={6}
              required
            />
          </label>

          <label>
            Telefon
            <input
              name="telefon"
              value={podaci.telefon}
              onChange={promena}
              placeholder="0641234567"
            />
          </label>

          <label className="polje-checkbox">
            <input
              type="checkbox"
              name="student"
              checked={podaci.student}
              onChange={promena}
            />
            Student sam (ostvarujem 10% popusta na svaki termin)
          </label>

          <button className="dugme dugme-puno" type="submit" disabled={salje}>
            {salje ? 'Slanje…' : 'Registruj se'}
          </button>
        </form>

        <p className="pomocni-tekst">
          Već imate nalog? <Link to="/prijava">Prijavite se</Link>
        </p>
      </div>
    </div>
  )
}
