import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/** Gornja navigacija - stavke se menjaju u zavisnosti od uloge korisnika. */
export default function Navbar() {
  const { korisnik, jePrijavljen, jeAdmin, odjava } = useAuth()
  const navigacija = useNavigate()

  function odjaviSe() {
    odjava()
    navigacija('/')
  }

  return (
    <header className="navbar">
      <div className="navbar-sadrzaj">
        <Link to="/" className="logo">
          Salon <span>Lepote</span>
        </Link>

        <nav className="linkovi">
          <NavLink to="/">Usluge</NavLink>

          {jePrijavljen && !jeAdmin && (
            <>
              <NavLink to="/zakazivanje">Zakaži termin</NavLink>
              <NavLink to="/moji-termini">Moji termini</NavLink>
            </>
          )}

          {jeAdmin && <NavLink to="/admin">Admin panel</NavLink>}
        </nav>

        <div className="nalog">
          {jePrijavljen ? (
            <>
              <span className="ime-korisnika">
                {korisnik.punoIme}
                {jeAdmin && <span className="znacka">ADMIN</span>}
                {korisnik.student && <span className="znacka znacka-student">STUDENT</span>}
              </span>
              <button className="dugme dugme-sporedno" onClick={odjaviSe}>
                Odjavi se
              </button>
            </>
          ) : (
            <>
              <Link className="dugme dugme-sporedno" to="/prijava">Prijava</Link>
              <Link className="dugme" to="/registracija">Registracija</Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
