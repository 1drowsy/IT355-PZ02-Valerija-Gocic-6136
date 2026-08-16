import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { recenzijaApi, terminApi } from '../api/api'
import {
  formatirajCenu,
  formatirajDatum,
  formatirajVreme,
  klasaStatusa,
  nazivStatusa
} from '../utils/format'
import Poruka from '../components/Poruka'

/** Tabela termina prijavljenog klijenta + otkazivanje i ocenjivanje. */
export default function MojiTermini() {
  const [termini, setTermini] = useState([])
  const [greska, setGreska] = useState('')
  const [uspeh, setUspeh] = useState('')
  const [ucitava, setUcitava] = useState(true)

  // termin koji se trenutno ocenjuje
  const [recenzijaZa, setRecenzijaZa] = useState(null)
  const [ocena, setOcena] = useState(5)
  const [komentar, setKomentar] = useState('')

  useEffect(() => {
    ucitajTermine()
  }, [])

  async function ucitajTermine() {
    try {
      const { data } = await terminApi.moji()
      setTermini(data)
    } catch (e) {
      setGreska(e.poruka || 'Greška pri učitavanju termina.')
    } finally {
      setUcitava(false)
    }
  }

  async function otkazi(id) {
    if (!window.confirm('Da li ste sigurni da želite da otkažete ovaj termin?')) return

    setGreska('')
    setUspeh('')
    try {
      await terminApi.otkazi(id)
      setUspeh('Termin je otkazan.')
      ucitajTermine()
    } catch (e) {
      setGreska(e.poruka || 'Otkazivanje nije uspelo.')
    }
  }

  async function posaljiRecenziju(dogadjaj) {
    dogadjaj.preventDefault()
    setGreska('')
    setUspeh('')
    try {
      await recenzijaApi.kreiraj({
        terminId: recenzijaZa,
        ocena: Number(ocena),
        komentar
      })
      setUspeh('Hvala na oceni!')
      setRecenzijaZa(null)
      setKomentar('')
      setOcena(5)
      ucitajTermine()
    } catch (e) {
      setGreska(e.poruka || 'Slanje recenzije nije uspelo.')
    }
  }

  if (ucitava) return <div className="ucitavanje">Učitavanje…</div>

  return (
    <div>
      <div className="zaglavlje-sekcije">
        <h2>Moji termini</h2>
        <Link className="dugme" to="/zakazivanje">
          + Novi termin
        </Link>
      </div>

      <Poruka tekst={greska} />
      <Poruka tekst={uspeh} vrsta="uspeh" />

      {termini.length === 0 ? (
        <p className="prazno">
          Još uvek nemate zakazanih termina. <Link to="/zakazivanje">Zakažite prvi.</Link>
        </p>
      ) : (
        <div className="okvir-tabele">
          <table className="tabela">
            <thead>
              <tr>
                <th>Datum i vreme</th>
                <th>Usluga</th>
                <th>Kozmetičar</th>
                <th>Cena</th>
                <th>Status</th>
                <th>Akcija</th>
              </tr>
            </thead>
            <tbody>
              {termini.map((t) => (
                <tr key={t.id}>
                  <td>
                    {formatirajDatum(t.datumVremePocetka)}
                    <div className="sitno">do {formatirajVreme(t.datumVremeKraja)}</div>
                  </td>
                  <td>
                    {t.uslugaNaziv}
                    <div className="sitno">{t.trajanjeMinuta} min</div>
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
                    {(t.status === 'ZAKAZAN' || t.status === 'POTVRDJEN') && (
                      <button
                        className="dugme dugme-opasno dugme-malo"
                        onClick={() => otkazi(t.id)}
                      >
                        Otkaži
                      </button>
                    )}
                    {t.status === 'ZAVRSEN' && !t.imaRecenziju && (
                      <button
                        className="dugme dugme-malo"
                        onClick={() => setRecenzijaZa(t.id)}
                      >
                        Oceni
                      </button>
                    )}
                    {t.status === 'ZAVRSEN' && t.imaRecenziju && (
                      <span className="sitno">Ocenjeno ✓</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ------------------------- FORMA ZA RECENZIJU ------------------------- */}
      {recenzijaZa && (
        <div className="kartica forma-recenzije">
          <h3>Ocenite uslugu</h3>
          <form onSubmit={posaljiRecenziju}>
            <label>
              Ocena
              <select value={ocena} onChange={(e) => setOcena(e.target.value)}>
                {[5, 4, 3, 2, 1].map((o) => (
                  <option key={o} value={o}>
                    {'★'.repeat(o)} ({o})
                  </option>
                ))}
              </select>
            </label>

            <label>
              Komentar
              <textarea
                value={komentar}
                onChange={(e) => setKomentar(e.target.value)}
                rows={3}
                maxLength={500}
              />
            </label>

            <div className="red-dugmadi">
              <button className="dugme" type="submit">
                Pošalji ocenu
              </button>
              <button
                className="dugme dugme-sporedno"
                type="button"
                onClick={() => setRecenzijaZa(null)}
              >
                Odustani
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  )
}
