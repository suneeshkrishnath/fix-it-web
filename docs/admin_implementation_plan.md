# Fix-it Administrator Web App - Implementation Plan

## Overall Status

**Planning: API-first.** The administrator app is a web application made of two modules: `backend/` (Spring Boot 3.x, Java 21, Maven, PostgreSQL, Flyway, JWT) and `admin-ui/` (Angular 21). The resident/technician mobile app is a separate surface that shares the backend; its APIs are specified separately later.

Approach: confirm `api-spec/openapi.yaml` first, then generate backend server interfaces (`openapi-generator`, spring, interfaceOnly) and the Angular client (`typescript-angular`) from it, then implement. File storage starts on local disk behind an abstraction and moves to S3 later.

## Admin Stages

| Stage | Aim | Status | Dependency / Exit |
|---|---|---|---|
| A1 | Confirm web scope, user roles, permissions, and workflows | Done | Admin web app only; tenant/technician flows stay in the mobile app |
| A2 | Define the OpenAPI contract (`api-spec/openapi.yaml`) | In Review | Draft written; awaiting approval of endpoints, schemas, and request status list |
| A3 | Scaffold `backend/` and `admin-ui/`, wire code generation from the spec | Not Started | A2 approved |
| A4 | Backend foundation: PostgreSQL, Flyway, Spring Security JWT, roles/permissions, audit, ProblemDetail errors, local file storage | Not Started | A3 |
| A5 | Angular foundation: auth, route guards, interceptors, responsive shell, navigation | Not Started | A3 |
| A6 | Campus, building/tower, and apartment management | Not Started | A4, A5; safe deactivation when referenced |
| A7 | Resident and tenancy management, agreement upload | Not Started | A6 |
| A8 | Technician accounts, specialties, availability, admin users, roles, and permissions | Not Started | A4, A5 |
| A9 | Request review and technician assignment/reassignment | Not Started | A7, A8 |
| A10 | Appointment scheduling and dual approval of proposed changes | Not Started | A9 |
| A11 | Work status review, blockers, completion approval, closure, and reopening | Not Started | A10 |
| A12 | Notifications, operational reports, audit viewer, dashboard | Not Started | A11 |
| A13 | Move file storage to S3, release, deployment, monitoring, backup, and support | Deferred | After core stages |

## Detailed Acceptance Workflows

### Property Records

- CRUD/search/deactivation for campus -> building/tower -> apartment/unit.
- Validate required hierarchy relationships and protect referenced records from destructive deletion.

### Accounts and Permissions

- Admin can manage tenant/tenancy and technician accounts within their permission scope.
- Backend rejects cross-role and out-of-scope requests even when a client calls an endpoint directly.

### Request Queue and Assignment

- Admin can inspect, filter, approve, reject, request information, assign, and reassign.
- Every rejection, reassignment, status, and decision stores actor, timestamp, and reason when required.

### Appointment Changes

- Technician proposal is delivered to both tenant and assigned admin.
- Track tenant and admin decisions independently.
- Apply the proposed time only after both approve; a rejection leaves the existing appointment active.

### Work Completion and Reopen

- Technician completion records comments, completion timestamp, and elapsed time since Work Started.
- Tenant accepts or rejects completion; admin approves after tenant acceptance.
- Work order closes only after both approvals. Rejected work returns to an appropriate active state.
- Tenant may reopen within 24 hours of official closure with a required reason; admin must review and accept it before work resumes.

### Notifications

- Work Accepted: tenant receives technician name; admin receives acceptance.
- Work Rejected: admin receives reason; tenant is not notified immediately and keeps Pending / In Progress during review.
- Travel and Work Started: tenant and admin are notified.
- Work Blocked: tenant and admin receive reason/details.
- Completion: tenant receives accept/reject request; admin receives completion and elapsed time.
- Tenant decision, admin approval, closure, and reopening notify relevant roles.

## Quality Gates

- Unit tests for policies, state transitions, access rules, and timing windows.
- API integration tests for role enforcement, auditing, dual approvals, and notification recipients.
- Angular component tests for CRUD, queue filtering, assignment, approvals, and error/empty states.
- End-to-end tests cover resident submission -> admin assignment -> technician work -> tenant acceptance -> admin closure -> tenant reopening.
- Backend `mvn verify` (with Testcontainers PostgreSQL), Angular lint/build/tests, and an OpenAPI lint pass in CI; generated code must match the spec.
