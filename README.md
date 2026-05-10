# 🚚 Delivery Tracker App

A real-time delivery management and tracking system. This project handles the lifecycle of a delivery—from order creation to live driver tracking—using **Server-Sent Events (SSE)** to stream coordinates to the customer.

---

## 🛠️ Tech Stack

* **Language:** Java 17
* **Framework:** Spring Boot 3.x
* **Security:** Spring Security + JWT (JSON Web Tokens)
* **Database:** PostgreSQL 11.4
* **Real-time:** Server-Sent Events (SSE)
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 🚀 Features

-   **User Management:** Secure registration and login with Role-Based Access Control (RBAC).
-   **Order Lifecycle:** Flow from order placement to assignment and completion.
-   **SSE Real-time Tracking:** One-way data streaming from server to client for live location updates.
-   **Infrastructure as Code:** Fully containerized setup for consistent development environments.

---

## 📡 API Documentation

### 🔐 1. Authentication & User Management (`/api/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/register` | Register a new user |
| `POST` | `/login` | Authenticate and receive JWT |
| `GET` | `/verify` | Verify account via email token |
| `POST` | `/forgot-password` | Request password reset link |
| `POST` | `/change-password` | Update password (Authenticated) |
| `PUT` | `/update-profile` | Update profile details (Multipart) |
| `POST` | `/profile/image` | Upload/Update profile picture |

### 📦 2. Order Management (`/api/orders`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/create` | Create a new delivery request |
| `GET` | `/getAll` | Fetch all orders in the system |
| `GET` | `/my-orders/{id}` | Fetch orders for a specific customer |
| `PUT` | `/update/{id}` | Modify existing order details |
| `DELETE` | `/{id}` | Cancel/Delete an order |

### 🧭 3. Driver Actions (`/api/driver`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `PUT` | `/start/{oId}/{dId}` | Mark order as "In Delivery" |
| `PUT` | `/complete/{oId}/{dId}` | Mark order as "Completed" |
| `POST` | `/locations/{oId}/{dId}` | Push GPS coordinates (Lat/Lng) |

### 📡 4. Real-time Tracking (SSE)
The system uses SSE for low-latency, one-way location streaming.
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/sse/track/{orderId}` | **Customer Connection:** Subscribe to live updates |
| `GET` | `/api/location/{id}/latest` | Fetch the single most recent coordinate |

### 🛡️ 5. Admin Controls (`/api/admin`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/assignments/{oId}/{dId}` | Manually assign a driver to an order |
| `DELETE` | `/users/{id}` | Soft-delete/Deactivate a user account |
| `GET` | `/orders/getAll` | Admin view of all global orders |

---

## 🔗 Real-time Flow (SSE)

1.  **Driver:** Sends a `POST` request to `/api/driver/locations/...` with their current `lat` and `lng`.
2.  **Server:** The `LocationUpdateService` processes the data and triggers `sendLocationUpdate` in the `LocationSseController`.
3.  **Customer:** Receives a push event named `location-update` containing the new coordinates automatically.

---

## 🐳 Running with Docker

### 1. Build and Run
```bash
docker-compose up --build
```

### 2. Services Access
* **Backend API:** `http://localhost:8080`
* **pgAdmin:** `http://localhost:5050` 
  * **Login:** `admin@pgadmin.org` (or your configured email)
  * **Password:** `admin`
* **Database:** PostgreSQL running on port `5432` internally.

### 3. Environment Configuration
The application is configured to wait for the database to be fully "Healthy" before starting, preventing JDBC connection failures during the initial container orchestration.

---

## 🛰️ Real-time Tracking Flow (SSE)

The system utilizes **Server-Sent Events (SSE)** for efficient, low-overhead live updates. This is a one-way data stream from the server to the client.

1.  **Subscription:** The Customer connects to `GET /sse/track/{orderId}`.
2.  **Update:** The Driver sends location data via `POST /api/driver/locations/{orderId}/{driverId}`.
3.  **Broadcast:** The `LocationSseController` identifies the active `SseEmitter` for that order and pushes a `location-update` event to the Customer.

---

## 🏗️ Architecture & Implementation Notes

### 🔄 Handling Circular Dependencies
To prevent `StackOverflowError` during JSON serialization and logging, bidirectional JPA relationships (such as `User` ↔ `UserProfile` and `Order` ↔ `LocationUpdate`) are managed using:
* **@JsonIgnore:** To prevent Jackson from entering infinite loops during API responses.
* **@ToString(exclude = "..."):** To prevent Lombok from crashing the stack during string concatenation.

### 🛡️ Security & Role-Based Access
* **JWT Authentication:** Stateless security context using Bearer tokens.
* **RBAC:** Specific endpoints are protected using `@PreAuthorize("hasRole('ADMIN')")`, ensuring only authorized users can assign drivers or deactivate accounts.
* **Soft Delete:** User accounts are deactivated rather than permanently removed to preserve data integrity for historical order tracking.

### 📂 File Management
The system supports multipart file uploads for profile pictures, stored locally and mapped to the user's profile through the `UserService`.

---

## 🧑‍💻 Author

**Zainab Alzaimoor**

*Java & Spring Boot Developer*

---

### 📄 License
This project is for educational and portfolio purposes. Feel free to reach out for collaborations!
