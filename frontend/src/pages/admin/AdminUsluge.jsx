import { useEffect, useState } from 'react'
import { adminApi, javnoApi } from '../../api/api'
import { formatirajCenu } from '../../utils/format'
import Poruka from '../../components/Poruka'

const PRAZNA_FORMA = {
  naziv: '',
  opis: '',
  trajanjeMinuta: 60,
  cena: '',
  kategorijaId: '',
  aktivna: true
}

/** CRUD nad uslugama: tabela + forma za dodavanje i izmenu. */
export default function AdminUsluge() {
  const [usluge, setUsluge] = useState([])
  const [kategorije, setKategorije] = useState([])
  const [forma, setForma] = useState(PRAZNA_FORMA)
  const [izmenaId, setIzmenaId] = useState(null)

  const [greska, setGreska] = useState('')
  const [uspeh, setUspeh] = useState('')
  const [ucitava, setUcitava] = useState(true)

  useEffect(() => {
    ucitaj()
  }, [])

  async function ucitaj() {
    try {
      const [u, k] = await Promise.all([adminApi.usluge(), javnoApi.kategorije()])
      setUsluge(u.data)
      setKategorije(k.data)
    } catch (e) {
      setGreska(e.poruka || 'Greška pri učitavanju usluga.')
    } finally {
      setUcitava(false)
    }
  }

  function promena(dogadjaj) {
    const { name, value, type, checked } = dogadjaj.target
    setForma((p) => ({ ...p, [name]: type === 'checkbox' ? checked : value }))
  }

  function pocniIzmenu(usluga) {
    setIzmenaId(usluga.id)
    setForma({
      naziv: usluga.naziv,
      opis: usluga.opis || '',
      trajanjeMinuta: usluga.trajanjeMinuta,
      cena: usluga.cena,
      kategorijaId: usluga.kategorijaId,
      aktivna: usluga.aktivna
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function otkaziIzmenu() {
    setIzmenaId(null)
    setForma(PRAZNA_FORMA)
  }

  async function posalji(dogadjaj) {
    dogadjaj.preventDefault()
    setGreska('')
    setUspeh('')

    const telo = {
      naziv: forma.naziv,
      opis: forma.opis,
      trajanjeMinuta: Number(forma.trajanjeMinuta),
      cena: Number(forma.cena),
      kategorijaId: Number(forma.kategorijaId),
      aktivna: forma.aktivna
    }

    try {
      if (izmenaId) {
        await adminApi.izmeniUslugu(izmenaId, telo)
        setUspeh('Usluga je izmenjena.')
      } else {
        await adminApi.kreirajUslugu(telo)
        setUspeh('Usluga je dodata.')
      }
      otkaziIzmenu()
      ucitaj()
    } catch (e) {
      setGreska(e.poruka || 'Čuvanje usluge nije uspelo.')
    }
  }

  async function obrisi(id, naziv) {
    if (!window.confirm(`Obrisati uslugu „${naziv}”?`)) return
    setGreska('')
    setUspeh('')
    try {
      const { data } = await adminApi.obrisiUslugu(id)
      setUspeh(data.poruka)
      ucitaj()
    } catch (e) {
      setGreska(e.poruka || 'Brisanje nije uspelo.')
    }
  }

  if (ucitava) return <div className="ucitavanje">Učitavanje…</div>

  return (
    <div>
      <Poruka tekst={greska} />
      <Poruka tekst={uspeh} vrsta="uspeh" />

      {/* ------------------------------ FORMA ------------------------------ */}
      <form className="kartica" onSubmit={posalji}>
        <h3>{izmenaId ? `Izmena usluge #${izmenaId}` : 'Nova usluga'}</h3>

        <div className="red">
          <label>
            Naziv
            <input name="naziv" value={forma.naziv} onChange={promena} required />
          </label>
          <label>
            Kategorija
            <select
              name="kategorijaId"
              value={forma.kategorijaId}
              onChange={promena}
              required
            >
              <option value="">— izaberite —</option>
              {kategorije.map((k) => (
                <option key={k.id} value={k.id}>
                  {k.naziv}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label>
          Opis
          <textarea name="opis" value={forma.opis} onChange={promena} rows={2} />
        </label>

        <div className="red">
          <label>
            Trajanje (min)
            <input
              type="number"
              name="trajanjeMinuta"
              value={forma.trajanjeMinuta}
              onChange={promena}
              min={15}
              max={480}
              step={15}
              required
            />
          </label>
          <label>
            Cena (RSD)
            <input
              type="number"
              name="cena"
              value={forma.cena}
              onChange={promena}
              min={1}
              step="0.01"
              required
            />
          </label>
        </div>

        <label className="polje-checkbox">
          <input
            type="checkbox"
            name="aktivna"
            checked={forma.aktivna}
            onChange={promena}
          />
          Usluga je aktivna (vidljiva klijentima)
        </label>

        <div className="red-dugmadi">
          <button className="dugme" type="submit">
            {izmenaId ? 'Sačuvaj izmene' : 'Dodaj uslugu'}
          </button>
          {izmenaId && (
            <button className="dugme dugme-sporedno" type="button" onClick={otkaziIzmenu}>
              Odustani
            </button>
          )}
        </div>
      </form>

      {/* ------------------------------ TABELA ------------------------------ */}
      <div className="okvir-tabele">
        <table className="tabela">
          <thead>
            <tr>
              <th>#</th>
              <th>Naziv</th>
              <th>Kategorija</th>
              <th>Trajanje</th>
              <th>Cena</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            {usluge.map((u) => (
              <tr key={u.id} className={u.aktivna ? '' : 'red-neaktivan'}>
                <td>{u.id}</td>
                <td>
                  {u.naziv}
                  <div className="sitno">{u.opis}</div>
                </td>
                <td>{u.kategorijaNaziv}</td>
                <td>{u.trajanjeMinuta} min</td>
                <td>{formatirajCenu(u.cena)}</td>
                <td>
                  <span className={u.aktivna ? 'status status-potvrdjen' : 'status status-otkazan'}>
                    {u.aktivna ? 'Aktivna' : 'Neaktivna'}
                  </span>
                </td>
                <td>
                  <div className="red-dugmadi">
                    <button className="dugme dugme-malo" onClick={() => pocniIzmenu(u)}>
                      Izmeni
                    </button>
                    <button
                      className="dugme dugme-opasno dugme-malo"
                      onClick={() => obrisi(u.id, u.naziv)}
                    >
                      Obriši
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
