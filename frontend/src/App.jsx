import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'

import Pocetna from './pages/Pocetna'
import Login from './pages/Login'
import Registracija from './pages/Registracija'
import Zakazivanje from './pages/Zakazivanje'
import MojiTermini from './pages/MojiTermini'
import AdminPanel from './pages/AdminPanel'

/**
 * Korenska komponenta - definise sve rute aplikacije.
 *
 *  javne rute        : /  /prijava  /registracija
 *  za prijavljene    : /zakazivanje  /moji-termini
 *  samo za admina    : /admin
 */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />

        <main className="sadrzaj">
          <Routes>
            {/* --- javno --- */}
            <Route path="/" element={<Pocetna />} />
            <Route path="/prijava" element={<Login />} />
            <Route path="/registracija" element={<Registracija />} />

            {/* --- samo za prijavljene korisnike --- */}
            <Route
              path="/zakazivanje"
              element={
                <ProtectedRoute>
                  <Zakazivanje />
                </ProtectedRoute>
              }
            />
            <Route
              path="/moji-termini"
              element={
                <ProtectedRoute>
                  <MojiTermini />
                </ProtectedRoute>
              }
            />

            {/* --- samo za administratora --- */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute samoAdmin>
                  <AdminPanel />
                </ProtectedRoute>
              }
            />

            {/* --- nepostojeca ruta --- */}
            <Route
              path="*"
              element={
                <div className="kartica">
                  <h2>404 — Stranica nije pronađena</h2>
                </div>
              }
            />
          </Routes>
        </main>

        <footer className="podnozje">
          IT355 — Veb sistemi 2 · Drugi projektni zadatak · Valerija Gocić 6136
        </footer>
      </BrowserRouter>
    </AuthProvider>
  )
}
