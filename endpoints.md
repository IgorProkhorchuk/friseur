# Application Endpoints

This file lists all the HTTP endpoints available in the application.

## Home Controller (`/`)

*   `GET /`: Displays the homepage.
*   `GET /home`: Displays the homepage.
*   `GET /login`: Displays the login page.
*   `GET /register`: Displays the registration page.
*   `POST /register`: Handles user registration.

## Schedule Controller (`/admin/schedule`)

*   `GET /create`: Displays the page for creating a new schedule.
*   `POST /create`: Handles the creation of a new schedule.
*   `GET /`: Displays the list of all schedules.

## User Appointment Controller (`/appointments`)

*   `GET /book`: Displays the appointment booking page with available slots.
*   `POST /book`: Handles the booking of an appointment.
*   `GET /my-appointments`: Displays the appointments for the currently logged-in user.
*   `POST /cancel/{id}`: Cancels an appointment by its ID.

## User Controller (`/user`, `/admin`)

*   `GET /user/dashboard`: Displays the user dashboard.
*   `GET /admin/dashboard`: Displays the admin dashboard.
*   `GET /admin/users`: Displays a list of all users (admin only).
*   `GET /admin/users/{id}`: Displays the details of a specific user (admin only).
*   `POST /admin/users/delete/{id}`: Deletes a user by their ID (admin only).
