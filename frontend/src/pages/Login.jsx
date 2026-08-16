import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Poruka from '../components/Poruka'

/** Forma za prijavu. Nakon uspeha token se cuva u localStorage (AuthContext). */
export default function Login() {
  const [email, setEmail] = useState('')
  const [lozinka, setLozinka] = useState('')
  const [greska, setGreska] = useState('')
  const [salje, setSalje] = useState(false)

  const { prijava } = useAuth()
  const navigacija = useNavigate()
  const lokacija = useLocation()

  async function posalji(dogadjaj) {
    dogadjaj.preventDefault()   // sprecava podrazumevano osvezavanje stranice
    setGreska('')
    setSalje(true)

    try {
      const korisnik = await prijava(email, lozinka)

      // Admin ide u admin panel, klijent na stranicu odakle je dosao
      const odakle = lokacija.state?.od
      if (korisnik.uloga === 'ROLE_ADMIN') {
        navigacija('/admin')
      } else {
        navigacija(odakle || '/moji-termini')
      }
    } catch (e) {
      setGreska(e.poruka || 'Prijava nije uspela.')
    } finally {
      setSalje(false)
    }
  }

  return (
    <div className="uski-sadrzaj">
      <div className="kartica">
        <h2>Prijava</h2>
        <p className="podnaslov">Prijavite se da biste zakazali termin.</p>

        <Poruka tekst={greska} />

        <form onSubmit={posalji}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="ana@primer.rs"
              required
            />
          </label>

          <label>
            Lozinka
            <input
              type="password"
              value={lozinka}
              onChange={(e) => setLozinka(e.target.value)}
              placeholder="••••••••"
              required
            />
          </label>

          <button className="dugme dugme-puno" type="submit" disabled={salje}>
            {salje ? 'Prijavljivanje…' : 'Prijavi se'}
          </button>
        </form>

        <p className="pomocni-tekst">
          Nemate nalog? <Link to="/registracija">Registrujte se</Link>
        </p>

        <div className="demo-nalozi">
          <strong>Demo nalozi:</strong>
          <div>admin@salon.rs / admin123 (administrator)</div>
          <div>ana@primer.rs / klijent123 (klijent-student)</div>
        </div>
      </div>
    </div>
  )
}
