# Hairdress Salon Website

## Overview

The Hairdress Salon Website is designed to provide an easy and convenient way for clients to book appointments, manage their bookings, shop for products, and make payments online. This project consists of four main pages: Homepage, Edit Booking, Shop, and Checkout.

## Pages

### 1. Homepage

**Main Features:**
- **Booking Form:**
    - Fields:
        - Name (required)
        - Phone (required, with validation for correct phone format: +(country code 49, 48, or 380) digits)

**Timeslot Selection:**
- "Book an appointment" button allows users to choose their preferred appointment time.
- **Display Next Five Working Days:**
    - Dropdown buttons for each working day (5 upcoming days) with an option to “View More Days” to load the next five working days.
    - By default, the nearest available day is opened.
- **Available Timeslots per Day:**
    - Clicking on a day dropdown reveals only available timeslots for that day.
    - Three timeslots are displayed per row.
    - If no slots are available: "All slots are booked. Please select another day" message is shown.
    - When a new day is selected, the previously opened day's dropdown is automatically closed.

**Hold Timeslot Mechanism:**
- Once a timeslot is selected, a "confirm selected time" button appears and it is temporarily held for 3 minutes.
- Redirects user to a confirmation page with options: "Confirm" or "Cancel." A countdown timer is displayed above the buttons. If user clicks the "back" button, ask for cancellation of the selected timeslot and, if confirmed, release the timeslot for further bookings.

**Post-Confirmation:**
- After confirming the booking, a confirmation message is sent to the client, suggesting they proceed with the payment for the service.
- Option to link this confirmation with a calendar integration (iCal, Google Calendar).

### 2. Edit Booking / Payment Page

**User Registration/Auto-Login:**
- Clients can log in to view or modify their bookings using name and phone as password.
- No option to register/login with the same phone but a different name.

**Features:**
- **Cancel Booking**: Clients can cancel their appointment if needed.
- **Checkout**: Option for clients to pay for their upcoming service (and products from the shop in future), integrating with common payment gateways (PayPal and Stripe).
- **Reschedule Appointment**:
    - To reschedule, the user must first cancel their existing booking, which redirects them to the homepage to create a new one.

### 3. Shop Page

**Content:**
- The shop page is under construction.


## License

This project is licensed under the MIT License. See the [LICENSE](https://opensource.org/license/mit) file for more details.
