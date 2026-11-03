# TravelEasy – bus & train ticket booking

Spring Boot 3 (Java 17) + Spring Data JPA + MySQL + JWT backend, React (Vite) frontend.

## Run
1. MySQL running locally. The `traveleasy` database is created automatically.
2. Backend:
   ```
   cd backend
   export DB_USER=root DB_PASSWORD=yourpassword   # defaults: root / root
   mvn spring-boot:run                            # http://localhost:8080
   ```
   First start seeds an admin (`admin@travel.com` / `Admin@123`, override with ADMIN_PASSWORD) and 15 sample trips for the next 3 days.
3. Frontend:
   ```
   cd frontend
   npm install
   npm run dev                                    # http://localhost:5173 (proxies /api to :8080)
   ```

## REST API
| Method | Path | Access |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/trips/search?from&to&date=YYYY-MM-DD&type=BUS\|TRAIN | public |
| GET | /api/trips/count | public |
| POST | /api/bookings | user |
| GET | /api/bookings/me | user |
| POST | /api/bookings/{id}/cancel | user (own bookings) |
| GET | /api/admin/stats, /trips, /bookings | admin |
| POST | /api/admin/trips | admin |
| DELETE | /api/admin/trips/{id} | admin (409 if trip has bookings) |

## Notes
- Seat reservation is a single atomic `UPDATE ... WHERE availableSeats >= n`, so concurrent bookings cannot oversell a trip.
- Passwords are BCrypt hashed; JWT is stateless (2h expiry). Set `JWT_SECRET` (32+ chars) and `ADMIN_PASSWORD` before any real deployment.
- Tokens are kept in localStorage for simplicity; for production consider httpOnly cookies.
