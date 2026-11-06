# HB+ Workspace

React Native + React Native for Web (Expo), with a Java 21 / Spring Boot backend. Includes domain-restricted Google login, dashboard, logout, loading and error states, database profiles, Admin/User roles and server-protected endpoints.

## Package

- frontend/: shared Android, iOS and web application.
- backend/: Java API, Maven tests and Dockerfile.
- netlify.toml: Git-based Netlify deployment settings.
- This ZIP contains source code. Build frontend/dist before uploading to Netlify Drop.

**Netlify hosts the frontend only.** Deploy Spring Boot separately to Render, Railway or another Java host. This package does not create a GitHub repository or live deployment. Sign-in is disabled until built with your Google client IDs and API URL. There is no fake login or domain bypass.

## Google Cloud configuration

Create a Google Auth Platform project and consent screen. Choose Internal for your Workspace organization where available, or add test users while in Testing. Create a Web application OAuth client. Add http://localhost:8081 and your exact HTTPS Netlify origin to authorized JavaScript origins and authorized redirect URIs. Match the redirect URI exactly, including trailing slash if Google requests it.

For Android, create an Android OAuth client with package fit.hbplus.access and your development/release signing certificate SHA-1. For iOS, create an iOS OAuth client for bundle fit.hbplus.access. Register each signing identity used. Enable custom URI scheme support where Google client settings require it. Native OAuth policy varies by client configuration; organizations that prohibit custom-scheme browser OAuth should integrate the native Google Sign-In SDK in an Expo development build. No client secret is embedded in the app.

## Backend setup

Install Java 21+ and Maven 3.9+. In backend/, set GOOGLE_CLIENT_IDS to a comma-separated list of your web, Android and iOS Google client IDs. This value is required; the API fails closed without it. Run `mvn spring-boot:run`.

| Variable | Value |
| --- | --- |
| GOOGLE_CLIENT_IDS | Comma-separated accepted Google client IDs |
| ALLOWED_DOMAIN | hbplus.fit (default) |
| FRONTEND_ORIGINS | http://localhost:8081,https://your-site.netlify.app |
| ADMIN_EMAILS | Optional comma-separated official admin emails |
| DB_URL | JDBC URL; local default is file-backed H2 |
| DB_USER / DB_PASSWORD | Database credentials |
| PORT | 8080 (default) |

PowerShell: `$env:GOOGLE_CLIENT_IDS="your-client.apps.googleusercontent.com"`, then `mvn spring-boot:run`.

Render: create a Docker web service with repository root directory backend. Set the environment variables above. Use managed PostgreSQL: DB_URL=jdbc:postgresql://HOST:5432/DATABASE?sslmode=require, plus DB_USER and DB_PASSWORD. H2 is for local development; container storage may be ephemeral. Health check: /api/health. Use migrations instead of automatic schema updates before evolving a production database.

## Frontend setup

In frontend/, run `npm ci`, copy .env.example to .env, and fill in the API URL and Google client IDs. Run `npm run web`. On PowerShell use `Copy-Item .env.example .env`. Local web API URL: http://localhost:8080. Deployed API and web app must use HTTPS. EXPO_PUBLIC_* values are public and embedded at build time.

## Netlify deployment

Recommended: push this folder to GitHub and import it into Netlify. The root netlify.toml specifies frontend as the base directory, npm ci followed by npm run build as the build command, and dist as the publish folder. Add all EXPO_PUBLIC_* variables from frontend/.env.example in Netlify. Register the resulting Netlify origin in Google and backend FRONTEND_ORIGINS. Rebuild after changing environment variables.

Manual upload: configure frontend/.env, run `npm ci` then `npm run build`. Upload the contents of frontend/dist to Netlify Drop. Redirect and security-header files are included in frontend/public and copied into the build. No compiled web ZIP is included; changing Netlify environment variables does not alter an already uploaded static build. You must rebuild with your credentials to enable real sign-in.

## Android and iOS

Use an Expo development build, not Expo Go, for custom-scheme OAuth. Install Android Studio and configure an emulator, then run `npm run android`. Use the Android client ID and correct SHA-1. Android emulator localhost refers to the emulator: use an HTTPS hosted API, or http://10.0.2.2:8080 with suitable development cleartext-network settings.

iOS requires macOS and Xcode: run `npm run ios` with the iOS client ID. A physical phone must use an API reachable from the device. Native sign-in must be validated on each platform with your registered credentials and signing identity.

## Authentication flow and domain restriction

1. Expo AuthSession opens Google OAuth with state checking. The hd hint improves account selection but is never used as authorization.
2. The app sends the Google ID token to POST /api/auth/google over HTTPS.
3. Google Java token verification checks signature, issuer, accepted audience and expiry. The server requires email_verified and compares the complete email domain case-insensitively against ALLOWED_DOMAIN. Gmail, subdomains and suffix lookalikes fail with: **Access restricted. Please login using your official domain email.**
4. Profiles are stored by stable Google subject. The API issues a random 256-bit session token and stores only its SHA-256 hash, with a one-hour expiry. The frontend retains the bearer token only in memory, never localStorage, URLs or logs.
5. GET /api/me validates the session before the dashboard renders. Direct /dashboard navigation serves the app shell but redirects to login without a session. Every private API route independently checks authentication.
6. Admin roles come from server ADMIN_EMAILS, recomputed on each request. /api/admin/summary requires ADMIN; users cannot assign themselves a role.
7. Logout revokes the server session and clears app memory. Sessions expire after one hour. Refreshing the browser or restarting the app intentionally requires a new sign-in. This does not sign out the Google account.

CSRF is disabled because authentication uses explicit Authorization bearer headers and no authentication cookies. CORS permits only configured origins without credentials. CORS is not authentication: native and non-browser clients still need a valid token. Responses use Cache-Control: no-store. Configure HTTPS, body-size limits and login rate limiting at your hosting gateway. Never log ID tokens or Authorization headers. Keep database secrets on the server.

## API

| Method | Path | Access |
| --- | --- | --- |
| GET | /api/health | Public |
| POST | /api/auth/google | JSON {idToken} |
| GET | /api/me | Bearer session |
| POST | /api/auth/logout | Bearer session |
| GET | /api/admin/summary | Admin bearer session |

## Testing

Frontend: `npm run typecheck` and `npm run build`. Backend: `mvn test`. Tests cover exact domain matching, unverified emails, anonymous route denial, outsider denial, session creation, role enforcement and logout revocation. Google verification is mocked in integration tests; real Google login needs your credentials and a Workspace account. Before publishing, verify official-domain success, Gmail rejection, direct-route protection, logout, expiry and OAuth on both native platforms. See VALIDATION.md for checks actually executed in this environment.

## GitHub submission

Create an empty repository in your account. From this folder run `git init`, `git add .`, `git commit -m "Build HB+ workspace"`, `git branch -M main`, `git remote add origin YOUR_REPOSITORY_URL`, then `git push -u origin main`. Submit that URL and the actual Netlify site URL. Never commit .env, database files or credentials.
