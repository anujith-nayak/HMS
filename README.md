# ABDM-HMS — Hospital Management System

ABDM-HMS is a full-stack Hospital Management System built with Spring Boot, MySQL, React, Vite, and Tailwind CSS. It provides CRUD workflows for patients, doctors, appointments, prescriptions, and clinical observations with a responsive healthcare dashboard UI.

## Project Structure

```text
ABDM-HMS/
  backend/      Spring Boot REST API (port 8080)
    src/main/java/com/abdm/hms/
      config/       CorsConfig
      controller/   PatientController, DoctorController, AppointmentController,
                    PrescriptionController, ObservationController, DashboardController
      dto/          PatientDto, DoctorDto, AppointmentDto, PrescriptionDto,
                    ObservationDto, DashboardStatsDto
      entity/       Patient, Doctor, Appointment, Prescription, Observation
      exception/    GlobalExceptionHandler, ResourceNotFoundException,
                    DuplicateResourceException
      repository/   (all repositories with search queries)
      service/      (all service classes)
    src/main/resources/
      application.properties
      schema.sql

  frontend/     React + Vite dashboard (port 5173)
    src/
      api/          axios.js, patients.js, doctors.js, appointments.js,
                    prescriptions.js, observations.js, dashboard.js
      components/   Layout, Sidebar, Modal, ConfirmDialog, SearchBar,
                    Spinner, EmptyState
      pages/        Dashboard, Patients, Doctors, Appointments,
                    Prescriptions, Observations
```

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java | 21 |
| Maven | 3.9 |
| MySQL | 8.0 |
| Node.js | 20 |

## Step-by-Step Run Instructions

### 1 — Database

Start MySQL and run:

```sql
CREATE DATABASE IF NOT EXISTS abdm_hms
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Or let Spring Boot create it automatically via the JDBC URL parameter `createDatabaseIfNotExist=true` (already set).

### 2 — Backend

Edit credentials in `backend/src/main/resources/application.properties` if your MySQL password differs from `root`:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

Then start the API:

```bash
cd backend
mvn spring-boot:run
```

Spring Boot will create all tables automatically (`spring.jpa.hibernate.ddl-auto=update`).  
API is available at **http://localhost:8080**.

### 3 — Frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

> The Vite dev server proxies `/api/*` requests to `http://localhost:8080` so no CORS configuration is needed during development.

### Build for production

```bash
cd frontend
npm run build        # outputs to frontend/dist/
```

Serve `dist/` with any static host and point it at the backend URL.

---

## API Documentation

Base URL: `http://localhost:8080/api`

All endpoints accept and return `application/json`.

### Patients — `/api/patients`

| Method | Path | Description | Body |
|--------|------|-------------|------|
| GET | `/api/patients` | List all; optional `?search=` | — |
| GET | `/api/patients/{id}` | Get by ID | — |
| POST | `/api/patients` | Create patient | PatientDto |
| PUT | `/api/patients/{id}` | Update patient | PatientDto |
| DELETE | `/api/patients/{id}` | Delete patient | — |

**PatientDto fields:** `fullName`, `gender`, `dateOfBirth` (YYYY-MM-DD), `phoneNumber`, `address`, `abhaNumber` (optional)

### Doctors — `/api/doctors`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/doctors` | List all; optional `?search=` |
| GET | `/api/doctors/{id}` | Get by ID |
| POST | `/api/doctors` | Create doctor |
| PUT | `/api/doctors/{id}` | Update doctor |
| DELETE | `/api/doctors/{id}` | Delete doctor |

**DoctorDto fields:** `fullName`, `specialization`, `department`, `phoneNumber`, `email`

### Appointments — `/api/appointments`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/appointments` | List all; optional `?search=` |
| GET | `/api/appointments/{id}` | Get by ID |
| POST | `/api/appointments` | Book appointment |
| PUT | `/api/appointments/{id}` | Update appointment |
| DELETE | `/api/appointments/{id}` | Cancel/delete |

**AppointmentDto fields:** `patientId`, `doctorId`, `appointmentDate`, `appointmentTime`, `status`  
**Status values:** `SCHEDULED`, `PENDING`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

### Prescriptions — `/api/prescriptions`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/prescriptions` | List all; optional `?search=` |
| GET | `/api/prescriptions/{id}` | Get by ID |
| POST | `/api/prescriptions` | Create prescription |
| PUT | `/api/prescriptions/{id}` | Update prescription |
| DELETE | `/api/prescriptions/{id}` | Delete prescription |

**PrescriptionDto fields:** `patientId`, `doctorId`, `medicineName`, `dosage`, `instructions` (optional), `prescribedDate`

### Observations — `/api/observations`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/observations` | List all; optional `?search=` |
| GET | `/api/observations/{id}` | Get by ID |
| POST | `/api/observations` | Record vitals |
| PUT | `/api/observations/{id}` | Update observation |
| DELETE | `/api/observations/{id}` | Delete observation |

**ObservationDto fields:** `patientId`, `bloodPressure`, `temperature` (decimal), `heartRate` (int), `weight` (decimal, kg), `height` (decimal, cm), `recordedDate`

### Dashboard — `/api/dashboard`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard/stats` | Returns aggregate counts |

**Response:**
```json
{
  "totalPatients": 12,
  "totalDoctors": 5,
  "totalAppointments": 34,
  "totalPrescriptions": 28,
  "totalObservations": 19
}
```

### Error Responses

| Status | When |
|--------|------|
| 400 | Validation failure — returns `fieldErrors` map |
| 404 | Resource not found |
| 409 | Duplicate (e.g. ABHA number, doctor email) |
| 500 | Unexpected server error |

---

## Future FHIR Integration Notes

The backend keeps REST DTOs separate from JPA entities so FHIR resources can be introduced later without coupling database models to external interoperability contracts. Patient ABHA number, observations, prescriptions, and encounters are modeled as independent domains to support future mapping to FHIR `Patient`, `Observation`, `MedicationRequest`, and `Appointment` resources.

