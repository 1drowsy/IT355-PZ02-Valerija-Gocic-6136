import { useEffect, useState } from 'react'
import { adminApi } from '../../api/api'
import {
  formatirajCenu,
  formatirajDatum,
  formatirajVreme,
  klasaStatusa,
  nazivStatusa
} from '../../utils/format'
import Poruka from '../../components/Poruka'

/** Dozvoljeni sledeci statusi - preslikava pravila iz TerminServiceImpl. */
const SLEDECI_STATUSI = {
  ZAKAZAN: ['POTVRDJEN', 'OTKAZAN'],
  POTVRDJEN: ['ZAVRSEN', 'OTKAZAN'],
  ZAVRSEN: [],
  OTKAZAN: []
}

/** Tabela SVIH termina u salonu sa mogucnoscu promene statusa. */
export default function AdminTermini() {
  const [termini, setTermini] = useState([])
  const [filter, setFilter] = useState('')
  const [greska, setGreska] = useState('')
  const [uspeh, setUspeh] = useState('')
  const [ucitava, setUcitava] = useState(true)

  useEffect(() => {
    ucitaj()
  }, [filter])

  async function ucitaj() {
    setUcitava(true)
    try {
      const { data } = await adminApi.termini(filter || undefined)
      setTermini(data)
      setGreska('')
    } catch (e) {
      setGreska(e.poruka || 'Greška pri učitavanju termina.')
    } finally {
      setUcitava(false)
    }
  }

  async function promeniStatus(id, noviStatus) {
    setGreska('')
    setUspeh('')
    try {
      await adminApi.promeniStatus(id, noviStatus)
      setUspeh(`Status termina #${id} je promenjen u ${nazivStatusa(noviStatus)}.`)
      ucitaj()
    } catch (e) {
      setGreska(e.poruka || 'Promena statusa nije uspela.')
    }
  }

  return (
    <div>
      <div className="traka-alata">
        <label className="inline">
          Filter po statusu:
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="">Svi termini</option>
            <option value="ZAKAZAN">Na čekanju</option>
            <option value="POTVRDJEN">Odobreni</option>
            <option value="ZAVRSEN">Završeni</option>
            <option value="OTKAZAN">Otkazani</option>
          </select>
        </label>
        <button className="dugme dugme-sporedno dugme-malo" onClick={ucitaj}>
          Osveži
        </button>
      </div>

      <Poruka tekst={greska} />
      <Poruka tekst={uspeh} vrsta="uspeh" />

      {ucitava ? (
        <div className="ucitavanje">Učitavanje…</div>
      ) : termini.length === 0 ? (
        <p className="prazno">Nema termina za izabrani filter.</p>
      ) : (
        <div className="okvir-tabele">
          <table className="tabela">
            <thead>
              <tr>
                <th>#</th>
                <th>Termin</th>
                <th>Klijent</th>
                <th>Usluga</th>
                <th>Kozmetičar</th>
                <th>Cena</th>
                <th>Status</th>
                <th>Promena statusa</th>
              </tr>
            </thead>
            <tbody>
              {termini.map((t) => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>
                    {formatirajDatum(t.datumVremePocetka)}
                    <div className="sitno">do {formatirajVreme(t.datumVremeKraja)}</div>
                  </td>
                  <td>
                    {t.korisnikIme}
                    <div className="sitno">{t.korisnikTelefon || t.korisnikEmail}</div>
                  </td>
                  <td>
                    {t.uslugaNaziv}
                    {t.napomena && <div className="sitno">„{t.napomena}”</div>}
                  </td>
                  <td>{t.kozmeticarIme}</td>
                  <td>
                    {formatirajCenu(t.ukupnaCena)}
                    {t.primenjenPopust > 0 && (
                      <div className="sitno popust">−{t.primenjenPopust}%</div>
                    )}
                  </td>
                  <td>
                    <span className={klasaStatusa(t.status)}>{nazivStatusa(t.status)}</span>
                  </td>
                  <td>
                    <div className="red-dugmadi">
                      {SLEDECI_STATUSI[t.status].map((s) => (
                        <button
                          key={s}
                          className={
                            s === 'OTKAZAN'
                              ? 'dugme dugme-opasno dugme-malo'
                              : 'dugme dugme-malo'
                          }
                          onClick={() => promeniStatus(t.id, s)}
                        >
                          {nazivStatusa(s)}
                        </button>
                      ))}
                      {SLEDECI_STATUSI[t.status].length === 0 && (
                        <span className="sitno">—</span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
