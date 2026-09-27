# DayGo

DayGo is a routine-first commute and day-planning app. Where a maps app tells you how to get somewhere, DayGo tells you **when to leave**. It works backward from an event's location, start time, and travel mode to give you a "leave by" time, then keeps that promise up to date as your day and the world around it change.

What started as a single "destination + arrival time → departure time" calculator for ShellHacks has grown into a full day-planning app: a recurring event calendar, a live map with routing, weather-aware daily summaries, notifications, and data import/export.

## Features

**Departure planning**
- Calculates a "leave by" time from a destination, desired arrival time, and travel mode (car, transit, walking, or biking)
- Uses live routing/geocoding (Mapbox) for real travel times, with a sensible fallback estimate per mode if a location can't be geocoded or the routing call fails
- Transit estimates apply a padding multiplier on top of driving time to approximate transfers/waiting

**Events & calendar**
- Create, edit, and delete events with a title, location, date, start/end time, travel mode, and notes
- Recurring events (daily, weekly, monthly) with a configurable interval, specific weekdays for weekly series, and an optional end date
- Edit or delete a single occurrence of a recurring series without touching the rest of the series (exceptions/overrides), or edit the whole series at once
- Month calendar view alongside the default agenda-style home view
- Bulk import events from JSON, and an archive view for past events

**Live map**
- Mapbox-powered map panel (resizable) showing your current location and today's event locations
- Draws the route to your next destination
- Light/dark map style follows the app theme

**Daily summary & weather**
- Home screen surfaces the next "leave by" time front and center
- Weather-aware daily summary (current conditions via Open-Meteo) with a note when weather could affect your commute
- Free-day vs. busy-day summary copy

**Notifications**
- Browser notifications reminding you before an event, with a configurable lead time
- Notification log/inbox with unread count, clear/read state, and per-event or clear-all controls

**Settings & personalization**
- Default travel mode, reminder lead time, and daily summary visibility, persisted locally
- Light/dark theme toggle
- "Use my location" geolocation button

## How it works

Give DayGo a destination, an arrival time, and how you're getting there, as a one-off plan or as part of a calendar event, and it calculates your ideal departure time, automating the transit math people otherwise do by hand. Attach it to an event and DayGo keeps recalculating that departure time as event details, delays, and routing conditions change.

## Tech stack

- **Backend:** Java 17, Spring Boot, Gradle
- **Frontend:** HTML/CSS/JavaScript (no framework), Mapbox GL JS
- **External APIs:** Mapbox (geocoding, directions, maps), Open-Meteo (weather)

## API overview

| Endpoint | Description |
|---|---|
| `POST /api/plan` | Compute a departure time for a destination/arrival time/mode |
| `GET /api/events` | List all events, with recurring series expanded into concrete occurrences |
| `POST /api/events` | Create an event (optionally recurring) |
| `PUT /api/events/{id}` | Update an event or a single occurrence of a series |
| `DELETE /api/events/{id}` | Delete an event, or cancel a single occurrence of a series |
| `POST /api/events/import` | Bulk-create events from a JSON array |
| `POST /api/events/{id}/plan` | Compute the departure time for a specific event |
| `GET /api/ping` | Health check |

Events are currently stored in memory on the backend, so restarting the server clears them.

## Running it locally

**Backend**
```bash
cd backend
./gradlew bootRun
```
The API runs on `http://localhost:8081` by default. You'll need your own Mapbox (and OpenRouteService) API keys in `backend/src/main/resources/application.properties` — don't commit real keys to a public repo.

**Frontend**
Open `frontend/index.html` in a browser (or serve it with any static file server). Update the `API` and `MAPBOX_TOKEN` constants near the top of the `<script>` block to point at your backend and your own Mapbox token.

## Roadmap

- Persistent storage for events (currently in-memory)
- Deeper calendar sync to pull from an external calendar

## Team

- Andres — Full-stack
- Abigail — Frontend

---
*Built for ShellHacks.*
