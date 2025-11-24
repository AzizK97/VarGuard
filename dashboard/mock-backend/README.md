# Dashboard Mock Backend

This is a minimal mock backend that simulates nmap scan results and simple vulnerability data.

Install and run:

```bash
cd dashboard/mock-backend
npm install
npm start
```

The mock server exposes:
- `POST /network/nmap/scan` with JSON body `{ "target": "1.2.3.4" }` to simulate starting a scan
- `GET /network/nmap/results?target=1.2.3.4` to fetch the simulated scan result
- `GET /network/vulnerabilities?target=1.2.3.4` to fetch simulated vulnerabilities

By default the mock server runs on port `4000`. To use it from the frontend, set `VITE_API_URL=http://localhost:4000` when running the dashboard.
