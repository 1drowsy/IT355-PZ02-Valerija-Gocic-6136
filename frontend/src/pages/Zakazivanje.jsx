import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { javnoApi, terminApi } from '../api/api'
import { formatirajCenu, formatirajVreme } from '../utils/format'
import Poruka from '../components/Poruka'

/**
 * Forma za zakazivanje termina.
 *
 * Tok:
 *  1. korisnik bira uslugu     -> ucitavaju se samo kozmeticari koji je pruzaju
 *  2. bira kozmeticara i datum -> backend vraca listu SLOBODNIH slotova
 *  3. bira slot i salje zahtev -> backend racuna kraj i cenu i proverava preklapanje
 */
export default function Zakazivanje() {
  const [usluge, setUsluge] = useState([])
  const [kozmeticari, setKozmeticari] = useState([])
  const [slobodni, setSlobodni] = useState([])

  const [uslugaId, setUslugaId] = useState('')
  const [kozmeticarId, setKozmeticarId] = useState('')
  const [datum, setDatum] = useState('')
  const [izabranSlot, setIzabranSlot] = useState('')
  const [napomena, setNapomena] = useState('')

  const [greska, setGreska] = useState('')
  const [uspeh, setUspeh] = useState('')
  const [salje, setSalje] = useState(false)

  const navigacija = useNavigate()
  const danas = new Date().toISOString().split('T')[0]
  const izabranaUsluga = usluge.find((u) => String(u.id) === uslugaId)

  // 1) katalog usluga
  useEffect(() => {
    javnoApi
      .usluge()
      .then((o) => setUsluge(o.data))
      .catch((e) => setGreska(e.poruka))
  }, [])

  // 2) kada se promeni usluga -> ucitaj kozmeticare koji je pruzaju
  useEffect(() => {
    if (!uslugaId) {
      setKozmeticari([])
      return
    }
    setKozmeticarId('')
    setSlobodni([])
    javnoApi
      .kozmeticariZaUslugu(uslugaId)
      .then((o) => setKozmeticari(o.data))
      .catch((e) => setGreska(e.poruka))
  }, [uslugaId])

  // 3) kada su izabrani kozmeticar i datum -> ucitaj slobodne slotove
  useEffect(() => {
    if (!uslugaId || !kozmeticarId || !datum) {
      setSlobodni([])
      return
    }
    setIzabranSlot('')
    terminApi
      .dostupnost(kozmeticarId, uslugaId, datum)
      .then((o) => setSlobodni(o.data))
      .catch((e) => setGreska(e.poruka))
  }, [uslugaId, kozmeticarId, datum])

  async function posalji(dogadjaj) {
    dogadjaj.preventDefault()
    setGreska('')
    setUspeh('')

    if (!izabranSlot) {
      setGreska('Izaberite slobodan termin iz liste.')
      return
    }

    setSalje(true)
    try {
      const { data } = await terminApi.zakazi({
        uslugaId: Number(uslugaId),
        kozmeticarId: Number(kozmeticarId),
        datumVremePocetka: izabranSlot,
        napomena: napomena || null
      })

      setUspeh(
        `Termin je uspešno zakazan! Cena: ${formatirajCenu(data.ukupnaCena)}` +
          (data.primenjenPopust > 0 ? ` (popust ${data.primenjenPopust}%)` : '')
      )
      setTimeout(() => navigacija('/moji-termini'), 1500)
    } catch (e) {
      setGreska(e.poruka || 'Zakazivanje nije uspelo.')
      // osvezi slobodne slotove - neko je mozda upravo zauzeo termin
      terminApi
        .dostupnost(kozmeticarId, uslugaId, datum)
        .then((o) => setSlobodni(o.data))
        .catch(() => {})
    } finally {
      setSalje(false)
    }
  }

  return (
    <div className="srednji-sadrzaj">
      <h2>Zakazivanje termina</h2>
      <p className="podnaslov">
        Vreme završetka i konačnu cenu automatski izračunava sistem.
      </p>

      <Poruka tekst={greska} />
      <Poruka tekst={uspeh} vrsta="uspeh" />

      <form className="kartica" onSubmit={posalji}>
        {/* KORAK 1 */}
        <label>
          1. Usluga
          <select value={uslugaId} onChange={(e) => setUslugaId(e.target.value)} required>
            <option value="">— izaberite uslugu —</option>
            {usluge.map((u) => (
              <option key={u.id} value={u.id}>
                {u.naziv} ({u.trajanjeMinuta} min, {formatirajCenu(u.cena)})
              </option>
            ))}
          </select>
        </label>

        {/* KORAK 2 */}
        <label>
          2. Kozmetičar
          <select
            value={kozmeticarId}
            onChange={(e) => setKozmeticarId(e.target.value)}
            disabled={!uslugaId}
            required
          >
            <option value="">
              {uslugaId ? '— izaberite kozmetičara —' : 'prvo izaberite uslugu'}
            </option>
            {kozmeticari.map((k) => (
              <option key={k.id} value={k.id}>
                {k.punoIme} {k.ocena > 0 ? `(★ ${k.ocena.toFixed(1)})` : ''}
              </option>
            ))}
          </select>
        </label>

        {/* KORAK 3 */}
        <label>
          3. Datum
          <input
            type="date"
            value={datum}
            min={danas}
            onChange={(e) => setDatum(e.target.value)}
            disabled={!kozmeticarId}
            required
          />
        </label>

        {/* KORAK 4 - slobodni slotovi */}
        {datum && kozmeticarId && (
          <div className="blok-slotova">
            <span className="oznaka-polja">4. Slobodni termini</span>

            {slobodni.length === 0 ? (
              <p className="prazno">
                Za izabrani dan nema slobodnih termina. Probajte drugi datum ili
                drugog kozmetičara.
              </p>
            ) : (
              <div className="slotovi">
                {slobodni.map((s) => (
                  <button
                    type="button"
                    key={s.pocetak}
                    className={`slot ${izabranSlot === s.pocetak ? 'slot-izabran' : ''}`}
                    onClick={() => setIzabranSlot(s.pocetak)}
                  >
                    {formatirajVreme(s.pocetak)}–{formatirajVreme(s.kraj)}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        <label>
          Napomena (opciono)
          <textarea
            value={napomena}
            onChange={(e) => setNapomena(e.target.value)}
            rows={3}
            maxLength={300}
            placeholder="npr. osetljiva koža, alergija na određene preparate…"
          />
        </label>

        {izabranaUsluga && izabranSlot && (
          <div className="rekapitulacija">
            <div>
              <span>Usluga</span>
              <strong>{izabranaUsluga.naziv}</strong>
            </div>
            <div>
              <span>Trajanje</span>
              <strong>{izabranaUsluga.trajanjeMinuta} min</strong>
            </div>
            <div>
              <span>Cena pre popusta</span>
              <strong>{formatirajCenu(izabranaUsluga.cena)}</strong>
            </div>
          </div>
        )}

        <button className="dugme dugme-puno" type="submit" disabled={salje}>
          {salje ? 'Zakazivanje…' : 'Zakaži termin'}
        </button>
      </form>
    </div>
  )
}
