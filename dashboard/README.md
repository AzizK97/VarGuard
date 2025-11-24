# Network Security Monitor Dashboard

A minimalistic React + Vite + TypeScript dashboard for visualizing network traffic and Suricata IDS/IPS alerts in real-time.

## Features

- 📊 **Real-time Alert Monitoring** - Live updates via Server-Sent Events (SSE)
- 📈 **Statistics Dashboard** - Visual metrics for alerts by severity and time
- 🔍 **Alert Filtering** - Filter alerts by severity level
- 🌓 **Dark Mode Support** - Automatic dark mode based on system preferences
- 📱 **Responsive Design** - Works on desktop, tablet, and mobile
- ⚡ **Fast & Lightweight** - Built with Vite for optimal performance

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Axios** - HTTP client
- **CSS3** - Styling with CSS variables for theming

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend API running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The dashboard will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
npm run preview
```

## Configuration

Create a `.env` file in the root directory:

```env
VITE_API_URL=http://localhost:8080/api
```

## Project Structure

```
dashboard/
├── src/
│   ├── components/
│   │   ├── AlertCard.tsx       # Individual alert display
│   │   ├── AlertCard.css
│   │   ├── AlertList.tsx       # List of alerts with filtering
│   │   ├── AlertList.css
│   │   ├── StatisticsCards.tsx # Statistics grid
│   │   └── StatisticsCards.css
│   ├── services/
│   │   └── api.ts              # API client and SSE setup
│   ├── types/
│   │   └── index.ts            # TypeScript interfaces
│   ├── App.tsx                 # Main application
│   ├── App.css
│   └── main.tsx
├── .env                        # Environment variables
└── package.json
```

## API Endpoints Used

- `GET /api/suricata/alerts/recent?limit=50` - Fetch recent alerts
- `GET /api/suricata/statistics` - Get alert statistics
- `GET /api/suricata/alerts/stream` - SSE stream for real-time alerts

## Features in Detail

### Real-time Updates

The dashboard connects to the backend via Server-Sent Events (SSE) to receive alerts in real-time. New alerts appear instantly at the top of the list.

### Alert Filtering

Filter alerts by severity:
- All
- Critical (🔴)
- High (🟠)
- Medium (🟡)
- Low (🟢)

### Statistics Cards

Displays 8 key metrics:
- Total Alerts
- Critical/High/Medium/Low counts
- Alerts in last hour/24h/7 days

### Dark Mode

Automatically adapts to your system's color scheme preference. No manual toggle needed.

## Development

```bash
# Run dev server
npm run dev

# Type checking
npm run type-check

# Lint
npm run lint

# Build
npm run build
```

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)

## License

MIT
