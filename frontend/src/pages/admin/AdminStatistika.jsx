import { useEffect, useState } from 'react'
import { adminApi } from '../../api/api'
import { formatirajCenu, nazivStatusa } from '../../utils/format'
import Poruka from '../../components/Poruka'

/** Pregled agregiranih podataka o poslovanju salona. */
export default function AdminStatistika() {
  const [statistika, setStatistika] = useState(null)
  const [korisnici, setKorisnici] = useState([])
  const [greska, setGreska] = useState('')

  useEffect(() => {
    Promise.all([adminApi.statistika(), adminApi.korisnici()])
      .then(([s, k]) => {
        setStatistika(s.data)
        setKorisnici(k.data)
      })
      .catch((e) => setGreska(e.poruka || 'Greška pri učitavanju statistike.'))
  }, [])

  if (greska) return <Poruka tekst={greska} />
  if (!statistika) return <div className="ucitavanje">Učitavanje…</div>

  return (
    <div>
      <div className="mreza-statistika">
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Ukupno termina</span>
          <strong>{statistika.ukupnoTermina}</strong>
        </div>
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Prihod (završeni)</span>
          <strong>{formatirajCenu(statistika.ukupanPrihod)}</strong>
        </div>
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Broj klijenata</span>
          <strong>{statistika.brojKlijenata}</strong>
        </div>
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Prosečna ocena</span>
          <strong>
            {statistika.prosecnaOcenaSalona
              ? `★ ${statistika.prosecnaOcenaSalona.toFixed(2)}`
              : '—'}
          </strong>
        </div>
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Usluga u ponudi</span>
          <strong>{statistika.brojUsluga}</strong>
        </div>
        <div className="kartica kartica-broj">
          <span className="oznaka-broja">Kozmetičara</span>
          <strong>{statistika.brojKozmeticara}</strong>
        </div>
      </div>

      <div className="kartica">
        <h3>Termini po statusu</h3>
        <ul className="lista-statusa">
          {Object.entries(statistika.terminiPoStatusu).map(([status, broj]) => (
            <li key={status}>
              <span>{nazivStatusa(status)}</span>
              <strong>{broj}</strong>
            </li>
          ))}
        </ul>
        <p className="sitno">
          Najtraženija usluga: <strong>{statistika.najtrazenijaUsluga}</strong>
        </p>
      </div>

      <div className="kartica">
        <h3>Registrovani korisnici ({korisnici.length})</h3>
        <div className="okvir-tabele">
          <table className="tabela">
            <thead>
              <tr>
                <th>#</th>
                <th>Ime i prezime</th>
                <th>Email</th>
                <th>Telefon</th>
                <th>Uloga</th>
                <th>Student</th>
              </tr>
            </thead>
            <tbody>
              {korisnici.map((k) => (
                <tr key={k.id}>
                  <td>{k.id}</td>
                  <td>{k.punoIme}</td>
                  <td>{k.email}</td>
                  <td>{k.telefon || '—'}</td>
                  <td>
                    <span
                      className={
                        k.uloga === 'ROLE_ADMIN'
                          ? 'status status-potvrdjen'
                          : 'status status-zakazan'
                      }
                    >
                      {k.uloga === 'ROLE_ADMIN' ? 'Administrator' : 'Klijent'}
                    </span>
                  </td>
                  <td>{k.student ? 'Da' : 'Ne'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
