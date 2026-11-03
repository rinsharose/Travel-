const KEY = 'traveleasy.session';

export const session = {
  get() { try { return JSON.parse(localStorage.getItem(KEY)); } catch { return null; } },
  set(u) { localStorage.setItem(KEY, JSON.stringify(u)); },
  clear() { localStorage.removeItem(KEY); }
};

async function call(path, { method = 'GET', body } = {}) {
  const s = session.get();
  const res = await fetch('/api' + path, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(s ? { Authorization: 'Bearer ' + s.token } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 401 && s && !path.startsWith('/auth/')) {
    session.clear();
    window.dispatchEvent(new Event('auth-expired'));
    throw new Error('Session expired, please log in again');
  }
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error((data && data.message) || 'Request failed (' + res.status + ')');
  return data;
}

export const api = {
  register: d => call('/auth/register', { method: 'POST', body: d }),
  login: d => call('/auth/login', { method: 'POST', body: d }),
  search: q => call('/trips/search?' + new URLSearchParams({ from: q.from, to: q.to, date: q.date, ...(q.type ? { type: q.type } : {}) })),
  count: () => call('/trips/count'),
  book: (tripId, passengerName, seats) => call('/bookings', { method: 'POST', body: { tripId, passengerName, seats } }),
  myBookings: () => call('/bookings/me'),
  cancel: id => call('/bookings/' + id + '/cancel', { method: 'POST' }),
  admin: {
    stats: () => call('/admin/stats'),
    trips: () => call('/admin/trips'),
    bookings: () => call('/admin/bookings'),
    addTrip: f => call('/admin/trips', {
      method: 'POST',
      body: { type: f.type, operator: f.op, source: f.src, destination: f.dst, departure: f.dep, arrival: f.arr, price: Number(f.price), totalSeats: Number(f.seats) }
    }),
    delTrip: id => call('/admin/trips/' + id, { method: 'DELETE' })
  }
};
