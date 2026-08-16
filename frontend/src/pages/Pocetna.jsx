import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { javnoApi } from '../api/api'
import { formatirajCenu } from '../utils/format'
import Poruka from '../components/Poruka'

/**
 * Pocetna strana - javni katalog usluga i tim salona.
 * Poziva /api/javno/** rute koje NE zahtevaju prijavu.
 */
export default function Pocetna() {
  const [usluge, setUsluge] = useState([])
  const [kategorije, setKategorije] = useState([])
  const [kozmeticari, setKozmeticari] = useState([])
  const [izabranaKategorija, setIzabranaKategorija] = useState('')
  const [greska, setGreska] = useState('')
  const [ucitava, setUcitava] = useState(true)

  // useEffect sa praznim nizom zavisnosti = izvrsi se jednom, po montiranju
  useEffect(() => {
    async function ucitaj() {
      try {
        const [u, k, koz] = await Promise.all([
          javnoApi.usluge(),
          javnoApi.kategorije(),
          javnoApi.kozmeticari()
        ])
        setUsluge(u.data)
        setKategorije(k.data)
        setKozmeticari(koz.data)
      } catch (e) {
        setGreska(e.poruka || 'Greška pri učitavanju kataloga.')
      } finally {
        setUcitava(false)
      }
    }
    ucitaj()
  }, [])

  const prikazaneUsluge = izabranaKategorija
    ? usluge.filter((u) => String(u.kategorijaId) === izabranaKategorija)
    : usluge

  if (ucitava) return <div className="ucitavanje">Učitavanje…</div>

  return (
    <div>
      <section className="hero">
        <h1>Vaša lepota, naš posao</h1>
        <p>
          Izaberite uslugu, kozmetičara i termin koji vam odgovara — sve online,
          za manje od minuta.
        </p>
        <Link className="dugme dugme-veliko" to="/zakazivanje">
          Zakaži termin
        </Link>
      </section>

      <Poruka tekst={greska} />

      {/* ----------------------------- KATALOG ----------------------------- */}
      <section>
        <div className="zaglavlje-sekcije">
          <h2>Naše usluge</h2>
          <select
            value={izabranaKategorija}
            onChange={(e) => setIzabranaKategorija(e.target.value)}
          >
            <option value="">Sve kategorije</option>
            {kategorije.map((k) => (
              <option key={k.id} value={k.id}>
                {k.naziv}
              </option>
            ))}
          </select>
        </div>

        <div className="mreza">
          {prikazaneUsluge.map((u) => (
            <article key={u.id} className="kartica kartica-usluga">
              <span className="oznaka">{u.kategorijaNaziv}</span>
              <h3>{u.naziv}</h3>
              <p className="opis">{u.opis}</p>
              <div className="podnozje-kartice">
                <span className="trajanje">{u.trajanjeMinuta} min</span>
                <span className="cena">{formatirajCenu(u.cena)}</span>
              </div>
            </article>
          ))}
        </div>

        {prikazaneUsluge.length === 0 && (
          <p className="prazno">Nema usluga u izabranoj kategoriji.</p>
        )}
      </section>

      {/* ------------------------------- TIM ------------------------------- */}
      <section>
        <div className="zaglavlje-sekcije">
          <h2>Naš tim</h2>
        </div>

        <div className="mreza">
          {kozmeticari.map((k) => (
            <article key={k.id} className="kartica kartica-kozmeticar">
              <div className="avatar">
                {k.ime[0]}
                {k.prezime[0]}
              </div>
              <h3>{k.punoIme}</h3>
              <div className="ocena">
                {k.ocena > 0 ? `★ ${k.ocena.toFixed(1)}` : 'Još nema ocena'}
              </div>
              <p className="opis">{k.biografija}</p>
              <div className="spisak-usluga">
                {k.usluge.map((u) => (
                  <span key={u.id} className="cip">
                    {u.naziv}
                  </span>
                ))}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}
