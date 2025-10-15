# Work Breakdown Structure (WBS) - Friseur Website

This WBS outlines the hierarchical breakdown of the work required to complete the Hairdress Salon Website project.

**1. Project Management**
    1.1. Project Planning & Analysis (Completed)
    1.2. Task Prioritization & Tracking
    1.3. Documentation (WBS, Endpoints, Tasks)

**2. Frontend Development**
    2.1. **Homepage & Booking Flow**
        2.1.1. Implement Dynamic Timeslot Display
            2.1.1.1. Fetch and render available slots for the next 5 days.
            2.1.1.2. Create "View More Days" button and associated logic.
            2.1.1.3. Implement UI logic to close other day-views when a new one is selected.
        2.1.2. Implement Booking Confirmation Flow
            2.1.2.1. Develop client-side logic to temporarily hold a timeslot for 3 minutes.
            2.1.2.2. Build the confirmation page (`/confirmation`).
            2.1.2.3. Add a countdown timer to the confirmation page.
            2.1.2.4. Handle user confirmation and cancellation actions.
    2.2. **User Dashboard**
        2.2.1. Display User Appointments
            2.2.1.1. Fetch and display a list of upcoming appointments.
            2.2.1.2. Fetch and display a list of past appointments.
        2.2.2. Implement Appointment Actions
            2.2.2.1. Add "Cancel Booking" functionality.
            2.2.2.2. Implement the "Reschedule" flow (redirect to booking page after cancellation).
    2.3. **Admin Dashboard**
        2.3.1. Appointment Management View
            2.3.1.1. Display a comprehensive list of all appointments.
            2.3.1.2. Add UI controls for filtering and searching appointments.
        2.3.2. Appointment Manipulation
            2.3.2.1. Create a form for manual appointment creation.
            2.3.2.2. Create a form/modal for editing existing appointments.
            2.3.2.3. Implement "Cancel" functionality for admins.
        2.3.3. Schedule Management Interface
            2.3.3.1. Integrate the existing schedule creation and viewing pages into the dashboard.
    2.4. **Shop & Checkout**
        2.4.1. Build Shop Page
            2.4.1.1. Design and build the product listing page.
            2.4.1.2. Implement a shopping cart UI component.
        2.4.2. Build Checkout Page
            2.4.2.1. Create a unified checkout page for services and products.
            2.4.2.2. Integrate payment forms (e.g., Stripe Elements or PayPal buttons).
    2.5. **General UI/UX**
        2.5.1. Implement Responsive Design across all pages.
        2.5.2. Add User Feedback mechanisms (e.g., loading spinners, success/error messages).

**3. Backend Development**
    3.1. **Core Modules**
        3.1.1. Refine Authentication
            3.1.1.1. Implement logic for client login via Name and Phone Number.
            3.1.1.2. Enforce phone number validation rules on the backend.
        3.1.2. Complete Appointment Logic
            3.1.2.1. Create endpoints to handle timeslot holds, confirmation, and cancellation.
            3.1.2.2. Update database schemas if necessary to support appointment status changes.
    3.2. **Shop Module**
        3.2.1. Data Model & Persistence
            3.2.1.1. Create `Product` entity.
            3.2.1.2. Create `ProductRepository`.
        3.2.2. Business Logic
            3.2.2.1. Create `ProductService` for managing products.
            3.2.2.2. Implement shopping cart logic (e.g., using HTTP sessions).
        3.2.3. API Layer
            3.2.3.1. Create `ProductController` with CRUD endpoints for admin.
            3.2.3.2. Create endpoints for clients to view products and manage their cart.
    3.3. **Payment Integration**
        3.3.1. Service Integration
            3.3.1.1. Configure SDK for the chosen payment gateway (Stripe/PayPal).
            3.3.1.2. Create a `PaymentService` to handle payment intents and processing.
        3.3.2. API Layer
            3.3.2.1. Create a `/checkout` endpoint to initiate payment.
            3.3.2.2. Create a webhook endpoint to handle payment success/failure notifications from the provider.
    3.4. **Notifications**
        3.4.1. Implement Email/SMS Service for booking confirmations.
        3.4.2. Implement iCal/Google Calendar integration.

**4. Testing and Quality Assurance**
    4.1. **Unit Testing**
        4.1.1. Write tests for all Service methods.
        4.1.2. Write tests for Controller endpoints.
    4.2. **Integration Testing**
        4.2.1. Test the complete end-to-end booking flow.
        4.2.2. Test the user registration and authentication flow.
        4.2.3. Test the payment and checkout process.
    4.3. **User Acceptance Testing (UAT)**
        4.3.1. Perform manual testing on all user stories.
        4.3.2. Conduct cross-browser compatibility testing.
