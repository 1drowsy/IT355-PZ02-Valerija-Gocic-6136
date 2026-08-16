/**
 * Mala komponenta za prikaz poruka o gresci ili uspehu.
 * Ne renderuje nista ako tekst nije prosledjen.
 */
export default function Poruka({ tekst, vrsta = 'greska' }) {
  if (!tekst) return null

  const klasa = vrsta === 'uspeh' ? 'poruka-uspeh' : 'poruka-greska'
  return <div className={`poruka ${klasa}`}>{tekst}</div>
}
