# Admin Flow

Admin capabilities focus on defining working periods, generating slots, monitoring bookings, and moderating slot visibility.

## Steps
- Log in via `/login` with an account that has `ROLE_ADMIN`; `CustomAuthenticationSuccessHandler` redirects to `/admin/dashboard` after issuing JWT cookies.
- Create schedule window on `/admin` (start/end dates). `ScheduleService` rejects overlaps.
- Generate timeslots on `/admin/schedule` by selecting hours per day; submit to `/admin/save-schedule` to persist `Slot` rows as `AVAILABLE`.
- Review booked slots on `/admin/booked` (filtered to `RESERVED`).
- View all slots on `/admin/schedule/all` and toggle visibility on `/admin/slots/manage` → POST `/admin/slots/toggle/{id}` (switches between `AVAILABLE` and `HIDDEN`).
- Use `/success` as the post-creation confirmation screen.

## Diagram
```mermaid
flowchart TD
  Login[Admin login\nPOST /login]
  Dash[Admin dashboard]
  Dates[Set schedule window\nPOST /admin]
  SlotsPage[Generate slots\nGET /admin/schedule]
  SaveSlots[Persist slots\nPOST /admin/save-schedule]
  Booked[Review booked slots\nGET /admin/booked]
  AllSlots[All slots\nGET /admin/schedule/all]
  Toggle[Toggle slot visibility\nPOST /admin/slots/toggle]
  Success[Success]

  Login --> Dash --> Dates --> SlotsPage --> SaveSlots --> Success --> Dash
  Dash --> Booked
  Dash --> AllSlots --> Toggle --> AllSlots
```
