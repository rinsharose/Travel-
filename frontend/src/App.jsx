import { useEffect, useState } from 'react';
import { api, session } from './api';
import { Nav, Hero, SearchView, AuthForm, BookingsView, AdminView, money } from './components';

const ds = d => d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');

export default function App() {
  const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1);
  const [me, setMe] = useState(session.get());
  const [view, setView] = useState('search');
  const [msg, setMsg] = useState(null);
  const [count, setCount] = useState(0);
  const [trips, setTrips] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [q, setQ] = useState({ from: 'Bangalore', to: 'Chennai', date: ds(tomorrow), type: '' });

  const notify = (ok, t) => setMsg({ ok, t });
  const go = (v, m) => { setView(v); setMsg(m || null); window.scrollTo(0, 0); };

  // expired/invalid token -> back to login
  useEffect(() => {
    const h = () => { setMe(null); go('login', { ok: false, t: 'Session expired, please log in again' }); };
    window.addEventListener('auth-expired', h);
    return () => window.removeEventListener('auth-expired', h);
  }, []);

  const runSearch = () => api.search(q).then(setTrips).catch(e => notify(false, e.message));
  useEffect(() => { if (view === 'search') runSearch(); }, [q, view]);
  useEffect(() => { api.count().then(setCount).catch(() => {}); }, [view]);
  useEffect(() => {
    if (view === 'bookings' && me) api.myBookings().then(setBookings).catch(e => notify(false, e.message));
  }, [view, me]);

  const login = async d => {
    try {
      const u = await api.login(d);
      session.set(u); setMe(u);
      go(u.role === 'ADMIN' ? 'admin' : 'search', { ok: true, t: 'Welcome, ' + u.name });
    } catch (e) { go('login', { ok: false, t: e.message }); }
  };
  const register = async d => {
    try {
      await api.register(d);
      go('login', { ok: true, t: 'Registered! Please log in.' });
    } catch (e) { go('register', { ok: false, t: e.message }); }
  };
  const logout = () => { session.clear(); setMe(null); go('search'); };

  const book = async (t, pax, n) => {
    if (!me) return go('login', { ok: false, t: 'Please log in to book tickets' });
    if (!pax.trim()) return go('search', { ok: false, t: 'Passenger name is required' });
    if (!(n >= 1 && n <= 6)) return go('search', { ok: false, t: 'You can book 1 to 6 seats at once' });
    try {
      const b = await api.book(t.id, pax.trim(), n);
      go('search', { ok: true, t: 'Booked ' + n + ' seat(s) on ' + t.operator + ' for ' + money(b.totalAmount) + '. See My bookings.' });
    } catch (e) { go('search', { ok: false, t: e.message }); runSearch(); }
  };
  const cancel = async id => {
    try {
      await api.cancel(id);
      setBookings(await api.myBookings());
      notify(true, 'Booking #' + id + ' cancelled. Seats released.');
    } catch (e) { notify(false, e.message); }
  };

  return (
    <>
      <Nav me={me} go={go} logout={logout} />
      {view === 'search' && <Hero count={count} />}
      <main className={view === 'search' ? 'up' : ''}>
        {msg && <p className={'msg ' + (msg.ok ? 'ok' : 'err')}>{msg.t}</p>}
        {view === 'search' && <SearchView q={q} setQ={setQ} trips={trips} me={me} book={book} />}
        {view === 'login' && <AuthForm title="Welcome back" button="Login" onSubmit={login} note="Demo admin: admin@travel.com / Admin@123"
          fields={[{ name: 'email', type: 'email', placeholder: 'Email' }, { name: 'password', type: 'password', placeholder: 'Password' }]} />}
        {view === 'register' && <AuthForm title="Create your account" button="Register" onSubmit={register}
          fields={[{ name: 'name', placeholder: 'Full name' }, { name: 'email', type: 'email', placeholder: 'Email' }, { name: 'password', type: 'password', minLength: 8, placeholder: 'Password (min 8 characters)' }]} />}
        {view === 'bookings' && me && <BookingsView bookings={bookings} cancel={cancel} />}
        {view === 'admin' && me && me.role === 'ADMIN' && <AdminView notify={notify} />}
      </main>
      <footer>
        <div className="stack"><span>React</span><span>Java</span><span>Spring Boot</span><span>MySQL</span><span>JWT</span></div>
        React frontend · Spring Boot REST API · MySQL
      </footer>
    </>
  );
}
