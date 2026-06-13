# Real-Time Task Manager

React + Vite frontend with a Spring Boot backend.

## Deploy

This repo now has root-level files that hosting providers can detect:

- `package.json` for Node-based build/start detection
- `Dockerfile` for a single-container deployment
- `render.yaml` for Render Blueprint deployments
- `railway.json` for Railway Docker deployments

The Docker deployment builds the React app, copies it into Spring Boot static resources, and runs the backend on the host-provided `PORT`.

Required production environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `FRONTEND_URL`

Email variables:

- `RESEND_API_KEY`
- `RESEND_FROM`

`RESEND_FROM` can be omitted for testing; the default is `onboarding@resend.dev`. For production, use a sender from a domain verified in Resend, such as `noreply@yourdomain.com`.

## Local Development

Frontend:

```bash
npm --prefix frontend install
npm --prefix frontend run dev
```

Backend:

```bash
./backend/gradlew -p backend bootRun
```

Root production build:

```bash
npm run build
npm start
```
