import { Fragment, useEffect, useState } from 'react';
import { api } from './api';

/* ---------- helpers ---------- */
export const ROUTES = [['Bangalore', 'Chennai'], ['Delhi', 'Mumbai'], ['Delhi', 'Jaipur']];
const fmt = d => new Date(d).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
const clock = d => new Date(d).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
const dur = (a, b) => { const m = Math.round((new Date(b) - new Date(a)) / 6e4); return Math.floor(m / 60) + 'h ' + String(m % 60).padStart(2, '0') + 'm'; };
export const money = n => '₹' + Number(n).toLocaleString('en-IN');

/* ---------- shared ---------- */
export function Nav({ me, go, logout }) {
  return (
    <header><b>🚌 TravelEasy</b>
      <nav>
        <a onClick={() => go('search')}>Search</a>
        {me && <a onClick={() => go('bookings')}>My bookings</a>}
        {me && me.role === 'ADMIN' && <a onClick={() => go('admin')}>Admin</a>}
        {me
          ? <a onClick={logout}>Logout ({me.name})</a>
          : <><a onClick={() => go('login')}>Login</a><a className="cta" onClick={() => go('register')}>Register</a></>}
      </nav>
    </header>
  );
}

export const Hero = ({ count }) => (
  <section className="hero">
    <h1>Book bus &amp; train tickets in seconds</h1>
    <p>Search routes, pick your seats and manage every booking in one place.</p>
    <div className="stats"><div><b>{count}</b>Trips</div><div><b>3</b>Popular routes</div><div><b>24/7</b>Booking</div></div>
  </section>
);

const Timeline = ({ t }) => (
  <div className="tl">
    <div className="t"><b>{clock(t.departure)}</b><span>{t.source}</span></div>
    <div className="mid">{dur(t.departure, t.arrival)}<i></i></div>
    <div className="t"><b>{clock(t.arrival)}</b><span>{t.destination}</span></div>
  </div>
);

/* ---------- search ---------- */
function TripCard({ t, me, book }) {
  const [seats, setSeats] = useState(1);
  const [pax, setPax] = useState(me ? me.name : '');
  return (
    <div className={'card trip ' + t.type}>
      <Timeline t={t} />
      <div className="info">
        <span className={'tag ' + t.type}>{t.type}</span><strong>{t.operator}</strong>
        <p className="muted">{new Date(t.departure).toLocaleDateString([], { weekday: 'short', day: 'numeric', month: 'short' })}</p>
        <div className="bar"><i style={{ width: ((t.totalSeats - t.availableSeats) / t.totalSeats * 100) + '%' }}></i></div>
        <p className="muted">{t.availableSeats} of {t.totalSeats} seats left</p>
      </div>
      <div className="side">
        <div className="price">{money(t.price)}<small> / seat</small></div>
        <input placeholder="Passenger name" value={pax} onChange={e => setPax(e.target.value)} />
        <input type="number" min="1" max={Math.min(6, t.availableSeats)} value={seats} onChange={e => setSeats(e.target.value)} aria-label="Seats" />
        <button onClick={() => book(t, pax, Number(seats))}>Book now</button>
      </div>
    </div>
  );
}

export function SearchView({ q, setQ, trips, me, book }) {
  const submit = ev => { ev.preventDefault(); setQ(Object.fromEntries(new FormData(ev.target))); };
  return (
    <>
      <form className="card search grid" key={q.from + q.to + q.date + q.type} onSubmit={submit}>
        <label>From<input name="from" required defaultValue={q.from} /></label>
        <label>To<input name="to" required defaultValue={q.to} /></label>
        <label>Date<input type="date" name="date" required defaultValue={q.date} /></label>
        <label>Type<select name="type" defaultValue={q.type}><option value="">Bus &amp; Train</option><option value="BUS">Bus</option><option value="TRAIN">Train</option></select></label>
        <button>🔍 Search</button>
      </form>
      <div className="chips"><span>Popular:</span>
        {ROUTES.map(r => <button key={r[0] + r[1]} className="chip" onClick={() => setQ({ ...q, from: r[0], to: r[1] })}>{r[0]} → {r[1]}</button>)}
      </div>
      {trips.length
        ? trips.map(t => <TripCard key={t.id} t={t} me={me} book={book} />)
        : <div className="card center"><p><b>No trips found</b></p><p className="muted">Try a popular route above and pick tomorrow or the next two days.</p></div>}
    </>
  );
}

/* ---------- auth ---------- */
export function AuthForm({ title, fields, button, onSubmit, note }) {
  const submit = e => { e.preventDefault(); onSubmit(Object.fromEntries(new FormData(e.target))); };
  return (
    <form className="card form" onSubmit={submit}>
      <h2>{title}</h2>
      {fields.map(f => <input key={f.name} {...f} required />)}
      <button>{button}</button>
      {note && <p className="muted">{note}</p>}
    </form>
  );
}

/* ---------- my bookings ---------- */
export function BookingsView({ bookings, cancel }) {
  return (
    <>
      <h2>My bookings</h2>
      {bookings.length ? bookings.map(b => (
        <div className={'card trip ' + b.trip.type} key={b.id}>
          <Timeline t={b.trip} />
          <div className="info">
            <span className={'tag ' + b.trip.type}>{b.trip.type}</span><strong>{b.trip.operator}</strong>
            <p className="muted">{fmt(b.trip.departure)}</p>
            <p className="muted">{b.passengerName} · {b.seats} seat(s) · Booking #{b.id}</p>
          </div>
          <div className="side">
            <div className="price">{money(b.totalAmount)}</div>
            <span className={'st ' + b.status}>{b.status}</span>
            {b.status === 'CONFIRMED' && new Date(b.trip.departure) > new Date() &&
              <button className="red" onClick={() => cancel(b.id)}>Cancel booking</button>}
          </div>
        </div>
      )) : <div className="card center"><p className="muted">You have no bookings yet. Search a trip and book your first ticket!</p></div>}
    </>
  );
}

/* ---------- admin (loads its own data) ---------- */
export function AdminView({ notify }) {
  const [tab, setTab] = useState('stats');
  const [data, setData] = useState({ stats: null, trips: [], bookings: [] });

  const load = async () => {
    try {
      const [stats, trips, bookings] = await Promise.all([api.admin.stats(), api.admin.trips(), api.admin.bookings()]);
      setData({ stats, trips, bookings });
    } catch (e) { notify(false, e.message); }
  };
  useEffect(() => { load(); }, []);

  const addTrip = async e => {
    e.preventDefault();
    const form = e.target;
    try {
      await api.admin.addTrip(Object.fromEntries(new FormData(form)));
      form.reset(); notify(true, 'Trip added'); load();
    } catch (err) { notify(false, err.message); }
  };
  const delTrip = async id => {
    try { await api.admin.delTrip(id); notify(true, 'Trip deleted'); load(); }
    catch (err) { notify(false, err.message); }
  };

  const s = data.stats;
  const cards = s ? [[s.users, 'Users'], [s.trips, 'Trips'], [s.bookings, 'Total bookings'], [s.confirmed, 'Confirmed'], [s.cancelled, 'Cancelled'], [money(s.revenue), 'Revenue']] : [];

  return (
    <>
      <h2>Admin dashboard</h2>
      <div className="tabs">{['stats', 'trips', 'bookings'].map(t =>
        <button key={t} className={'tab ' + (t === tab ? 'on' : '')} onClick={() => setTab(t)}>{t[0].toUpperCase() + t.slice(1)}</button>)}</div>

      {tab === 'stats' && <div className="stats2">{cards.map(c => <div className="card stat" key={c[1]}><b>{c[0]}</b><span>{c[1]}</span></div>)}</div>}

      {tab === 'trips' && (
        <>
          <form className="card grid" onSubmit={addTrip}>
            <label>Type<select name="type"><option>BUS</option><option>TRAIN</option></select></label>
            <label>Operator<input name="op" required /></label>
            <label>Source<input name="src" required /></label>
            <label>Destination<input name="dst" required /></label>
            <label>Departure<input type="datetime-local" name="dep" required /></label>
            <label>Arrival<input type="datetime-local" name="arr" required /></label>
            <label>Price<input type="number" name="price" min="0" required /></label>
            <label>Seats<input type="number" name="seats" min="1" required /></label>
            <button>Add trip</button>
          </form>
          <div className="wrap"><table>
            <thead><tr><th>ID</th><th>Type</th><th>Operator</th><th>Route</th><th>Departure</th><th>Price</th><th>Seats</th><th></th></tr></thead>
            <tbody>{data.trips.map(t => (
              <tr key={t.id}><td>{t.id}</td><td>{t.type}</td><td>{t.operator}</td><td>{t.source} → {t.destination}</td><td>{fmt(t.departure)}</td><td>{money(t.price)}</td><td>{t.availableSeats}/{t.totalSeats}</td>
                <td><button className="red sm" onClick={() => delTrip(t.id)}>Delete</button></td></tr>))}</tbody>
          </table></div>
        </>
      )}

      {tab === 'bookings' && (
        <div className="wrap"><table>
          <thead><tr><th>ID</th><th>User</th><th>Passenger</th><th>Trip</th><th>Seats</th><th>Total</th><th>Status</th></tr></thead>
          <tbody>{data.bookings.length ? data.bookings.map(b => (
            <tr key={b.id}><td>{b.id}</td><td>{b.userEmail}</td><td>{b.passengerName}</td><td>{b.trip.operator}: {b.trip.source} → {b.trip.destination}</td><td>{b.seats}</td><td>{money(b.totalAmount)}</td><td><span className={'st ' + b.status}>{b.status}</span></td></tr>))
            : <tr><td colSpan="7" className="muted">No bookings yet</td></tr>}</tbody>
        </table></div>
      )}
    </>
  );
}
