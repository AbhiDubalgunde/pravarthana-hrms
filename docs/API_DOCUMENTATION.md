# Pravarthana HRMS - API Documentation

Base URL: `http://localhost:8080/api`

Authentication: JWT Bearer Token (except login endpoints)

---

## 🔐 Authentication

### POST `/auth/login`
Login with email and password.

**Request:**
```json
{
  "email": "admin@pravarthana.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "admin@pravarthana.com",
    "role": "SUPER_ADMIN",
    "employee": {
      "id": 1,
      "employeeId": "EMP001",
      "firstName": "Admin",
      "lastName": "User"
    }
  }
}
```

### POST `/auth/forgot-password`
Request password reset email.

**Request:**
```json
{
  "email": "user@pravarthana.com"
}
```

**Response:**
```json
{
  "message": "Password reset email sent"
}
```

### POST `/auth/reset-password`
Reset password with token.

**Request:**
```json
{
  "token": "reset-token-here",
  "newPassword": "newPassword123"
}
```

**Response:**
```json
{
  "message": "Password reset successful"
}
```

---

## 👥 Employee Management

### GET `/employees`
Get all employees (paginated).

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search by name/email/employee ID
- `department`: Filter by department ID
- `status`: Filter by status (ACTIVE/INACTIVE)

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "employeeId": "EMP001",
      "firstName": "Admin",
      "lastName": "User",
      "email": "admin@pravarthana.com",
      "phone": "+91-9876543210",
      "department": {
        "id": 1,
        "name": "Human Resources"
      },
      "designation": "System Administrator",
      "status": "ACTIVE",
      "dateOfJoining": "2020-01-01"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "currentPage": 0
}
```

### GET `/employees/{id}`
Get employee details by ID.

**Response:**
```json
{
  "id": 1,
  "employeeId": "EMP001",
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@pravarthana.com",
  "phone": "+91-9876543210",
  "dateOfBirth": "1985-01-15",
  "gender": "Male",
  "address": "123 Main St",
  "city": "Bangalore",
  "state": "Karnataka",
  "country": "India",
  "department": {
    "id": 1,
    "name": "Human Resources"
  },
  "designation": "System Administrator",
  "reportingManager": null,
  "dateOfJoining": "2020-01-01",
  "employmentType": "Full-time",
  "salary": 150000.00,
  "status": "ACTIVE"
}
```

### POST `/employees`
Create new employee (HR/Admin only).

**Request:**
```json
{
  "email": "newuser@pravarthana.com",
  "firstName": "New",
  "lastName": "User",
  "phone": "+91-9876543210",
  "dateOfBirth": "1995-05-15",
  "gender": "Male",
  "departmentId": 1,
  "designation": "Software Engineer",
  "reportingManagerId": 3,
  "dateOfJoining": "2024-02-15",
  "employmentType": "Full-time",
  "salary": 80000.00,
  "roleId": 4
}
```

**Response:**
```json
{
  "id": 6,
  "employeeId": "EMP006",
  "message": "Employee created successfully. Welcome email sent."
}
```

### PUT `/employees/{id}`
Update employee details.

**Request:** (Same as POST)

**Response:**
```json
{
  "message": "Employee updated successfully"
}
```

### DELETE `/employees/{id}`
Deactivate employee (soft delete).

**Response:**
```json
{
  "message": "Employee deactivated"
}
```

---

## 📅 Attendance Management

### POST `/attendance/check-in`
Manual check-in.

**Request:**
```json
{
  "employeeId": 1
}
```

**Response:**
```json
{
  "id": 123,
  "date": "2024-02-15",
  "checkInTime": "09:15:00",
  "status": "PRESENT",
  "isLate": false,
  "message": "Checked in successfully"
}
```

### POST `/attendance/check-out`
Manual check-out.

**Request:**
```json
{
  "employeeId": 1
}
```

**Response:**
```json
{
  "id": 123,
  "checkOutTime": "18:30:00",
  "totalHours": 9.25,
  "message": "Checked out successfully"
}
```

### GET `/attendance/employee/{employeeId}`
Get attendance records for an employee.

**Query Parameters:**
- `month`: Month (1-12)
- `year`: Year (e.g., 2024)

**Response:**
```json
{
  "employee": {
    "id": 1,
    "name": "Admin User"
  },
  "month": 2,
  "year": 2024,
  "summary": {
    "totalDays": 20,
    "presentDays": 18,
    "absentDays": 1,
    "lateDays": 2,
    "leaveDays": 1
  },
  "records": [
    {
      "date": "2024-02-15",
      "checkInTime": "09:15:00",
      "checkOutTime": "18:30:00",
      "status": "PRESENT",
      "isLate": false,
      "totalHours": 9.25
    }
  ]
}
```

### GET `/attendance/report/monthly`
Monthly attendance report (HR/Admin).

**Query Parameters:**
- `month`: Month (1-12)
- `year`: Year
- `departmentId`: (optional) Filter by department

**Response:**
```json
{
  "month": 2,
  "year": 2024,
  "employees": [
    {
      "employeeId": "EMP001",
      "name": "Admin User",
      "department": "Human Resources",
      "presentDays": 18,
      "absentDays": 1,
      "lateDays": 2,
      "leaveDays": 1,
      "totalWorkingDays": 20
    }
  ]
}
```

---

## 🏖️ Leave Management

### GET `/leaves/balance/{employeeId}`
Get leave balance for employee.

**Response:**
```json
{
  "year": 2024,
  "leaveTypes": [
    {
      "type": "Casual Leave",
      "code": "CL",
      "totalBalance": 12,
      "usedBalance": 3,
      "remainingBalance": 9
    },
    {
      "type": "Sick Leave",
      "code": "SL",
      "totalBalance": 12,
      "usedBalance": 0,
      "remainingBalance": 12
    },
    {
      "type": "Earned Leave",
      "code": "EL",
      "totalBalance": 18,
      "usedBalance": 2,
      "remainingBalance": 16
    }
  ]
}
```

### POST `/leaves/apply`
Apply for leave.

**Request:**
```json
{
  "employeeId": 1,
  "leaveTypeId": 1,
  "startDate": "2024-02-20",
  "endDate": "2024-02-22",
  "reason": "Family function"
}
```

**Response:**
```json
{
  "id": 5,
  "totalDays": 3,
  "status": "PENDING",
  "message": "Leave application submitted successfully"
}
```

### GET `/leaves/employee/{employeeId}`
Get leave history for employee.

**Response:**
```json
{
  "leaves": [
    {
      "id": 5,
      "leaveType": "Casual Leave",
      "startDate": "2024-02-20",
      "endDate": "2024-02-22",
      "totalDays": 3,
      "reason": "Family function",
      "status": "PENDING",
      "appliedAt": "2024-02-15T10:30:00"
    }
  ]
}
```

### GET `/leaves/pending`
Get pending leave requests (Manager/HR).

**Response:**
```json
{
  "pendingLeaves": [
    {
      "id": 5,
      "employee": {
        "id": 4,
        "name": "Amit Patel",
        "employeeId": "EMP004"
      },
      "leaveType": "Casual Leave",
      "startDate": "2024-02-20",
      "endDate": "2024-02-22",
      "totalDays": 3,
      "reason": "Family function",
      "appliedAt": "2024-02-15T10:30:00"
    }
  ]
}
```

### PUT `/leaves/{id}/approve`
Approve leave request.

**Request:**
```json
{
  "approverId": 3
}
```

**Response:**
```json
{
  "message": "Leave approved successfully"
}
```

### PUT `/leaves/{id}/reject`
Reject leave request.

**Request:**
```json
{
  "approverId": 3,
  "reason": "Insufficient staffing during this period"
}
```

**Response:**
```json
{
  "message": "Leave rejected"
}
```

---

## 📄 Offer Letter Management

### POST `/offer-letters/generate`
Generate offer letter (HR only).

**Request:**
```json
{
  "candidateName": "New Candidate",
  "candidateEmail": "candidate@example.com",
  "designation": "Software Engineer",
  "department": "Engineering",
  "salary": 80000.00,
  "joiningDate": "2024-03-01",
  "location": "Bangalore"
}
```

**Response:**
```json
{
  "id": 10,
  "letterNumber": "OL-2024-010",
  "pdfUrl": "https://storage.supabase.co/...",
  "message": "Offer letter generated successfully"
}
```

### GET `/offer-letters/{id}/download`
Download offer letter PDF.

**Response:** PDF file download

---

## 💬 Chat (WebSocket)

### Connect to WebSocket
```
ws://localhost:8080/ws
```

### Subscribe to user messages
```
/user/queue/messages
```

### Send message
```javascript
stompClient.send("/app/chat/send", {}, JSON.stringify({
  roomId: 1,
  message: "Hello team!"
}));
```

---

## 📊 Dashboard

### GET `/dashboard/stats`
Get dashboard statistics.

**Response:**
```json
{
  "totalEmployees": 5,
  "activeEmployees": 5,
  "presentToday": 4,
  "onLeaveToday": 1,
  "pendingLeaveRequests": 2,
  "upcomingBirthdays": [
    {
      "name": "John Doe",
      "date": "2024-02-20"
    }
  ]
}
```

---

## 🔒 Role-Based Access Control

| Endpoint | Super Admin | HR Admin | Manager | Employee |
|----------|-------------|----------|---------|----------|
| GET /employees | ✅ | ✅ | ✅ (team only) | ❌ |
| POST /employees | ✅ | ✅ | ❌ | ❌ |
| PUT /employees | ✅ | ✅ | ✅ (team only) | ❌ |
| DELETE /employees | ✅ | ✅ | ❌ | ❌ |
| POST /attendance/check-in | ✅ | ✅ | ✅ | ✅ |
| GET /attendance/report | ✅ | ✅ | ✅ | ❌ |
| POST /leaves/apply | ✅ | ✅ | ✅ | ✅ |
| PUT /leaves/approve | ✅ | ✅ | ✅ | ❌ |
| POST /offer-letters | ✅ | ✅ | ❌ | ❌ |

---

## ⚠️ Error Responses

### 400 Bad Request
```json
{
  "error": "Validation failed",
  "message": "Email is required"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Employee not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

**Note:** All timestamps are in ISO 8601 format (UTC).
