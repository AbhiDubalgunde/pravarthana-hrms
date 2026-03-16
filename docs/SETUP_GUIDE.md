# Pravarthana HRMS - Setup Guide

Complete step-by-step guide to set up the Pravarthana HRMS platform locally.

---

## 📋 Prerequisites

### Required Software

1. **Node.js** (v18 or higher)
   - Download: https://nodejs.org/
   - Verify: `node --version`

2. **Java Development Kit** (JDK 17 or higher)
   - Download: https://adoptium.net/
   - Verify: `java --version`

3. **Maven** (v3.8 or higher)
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn --version`

4. **Git**
   - Download: https://git-scm.com/
   - Verify: `git --version`

5. **Code Editor** (VS Code recommended)
   - Download: https://code.visualstudio.com/

### Required Accounts

1. **Supabase Account** (Free tier)
   - Sign up: https://supabase.com/
   - Create a new project

2. **Gmail Account** (for email sending)
   - Enable 2FA and create App Password
   - Guide: https://support.google.com/accounts/answer/185833

---

## 🗄️ Database Setup (Supabase)

### Step 1: Create Supabase Project

1. Go to https://supabase.com/dashboard
2. Click **"New Project"**
3. Fill in details:
   - **Name:** pravarthana-hrms
   - **Database Password:** (Save this securely!)
   - **Region:** Choose closest to you
4. Wait for project to be created (~2 minutes)

### Step 2: Get Database Credentials

1. In your Supabase project, go to **Settings → Database**
2. Copy the connection string under **Connection string → URI**
3. It will look like:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxxx.supabase.co:5432/postgres
   ```

### Step 3: Run Database Schema

1. In Supabase dashboard, go to **SQL Editor**
2. Click **"New Query"**
3. Copy content from `database/schema/01_init.sql`
4. Paste and click **"Run"**
5. Repeat for `database/schema/02_seed_data.sql`

✅ **Database setup complete!**

---

## 🎨 Frontend Setup (Next.js)

### Step 1: Navigate to Frontend Directory

```bash
cd pravarthana-hrms/frontend
```

### Step 2: Install Dependencies

```bash
npm install
```

This will install:
- Next.js 14
- React 18
- Tailwind CSS
- shadcn/ui components
- Axios (HTTP client)
- Socket.IO client
- And more...

### Step 3: Configure Environment Variables

1. Create `.env.local` file:

```bash
cp .env.example .env.local
```

2. Edit `.env.local`:

```env
# Backend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080/api

# WebSocket URL
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

# App Configuration
NEXT_PUBLIC_APP_NAME=Pravarthana HRMS
NEXT_PUBLIC_COMPANY_NAME=Pravarthana Technologies Pvt Ltd
```

### Step 4: Run Development Server

```bash
npm run dev
```

Frontend will start at: **http://localhost:3000**

---

## ⚙️ Backend Setup (Spring Boot)

### Step 1: Navigate to Backend Directory

```bash
cd pravarthana-hrms/backend
```

### Step 2: Configure Application Properties

1. Navigate to: `src/main/resources/`
2. Create `application.properties` file:

```properties
# =============================================
# Server Configuration
# =============================================
server.port=8080
spring.application.name=pravarthana-hrms-backend

# =============================================
# Database Configuration (Supabase PostgreSQL)
# =============================================
spring.datasource.url=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_SUPABASE_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# =============================================
# JWT Configuration
# =============================================
jwt.secret=mySecretKeyForJWT2024PravarthanaHRMS!@#$%^&*()1234567890
jwt.expiration=86400000
# 86400000 ms = 24 hours

# =============================================
# CORS Configuration
# =============================================
cors.allowed-origins=http://localhost:3000,https://pravarthana-hrms.vercel.app

# =============================================
# Email Configuration (Gmail)
# =============================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Email sender details
app.email.from=noreply@pravarthana.com
app.email.from-name=Pravarthana HRMS

# =============================================
# File Upload Configuration
# =============================================
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# =============================================
# Logging
# =============================================
logging.level.com.pravarthana.hrms=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# =============================================
# Flyway (Database Migrations)
# =============================================
spring.flyway.enabled=false
# Set to true if using Flyway migrations
```

### Step 3: Install Dependencies

```bash
mvn clean install
```

This will download all Spring Boot dependencies:
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- JWT libraries
- iText PDF
- And more...

### Step 4: Run Backend Server

```bash
mvn spring-boot:run
```

Backend will start at: **http://localhost:8080**

---

## 🧪 Verify Setup

### 1. Check Backend Health

Open browser: http://localhost:8080/api/health

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2024-02-15T10:30:00"
}
```

### 2. Check Frontend

Open browser: http://localhost:3000

You should see the Pravarthana HRMS home page.

### 3. Test Login

1. Go to: http://localhost:3000/login
2. Use demo credentials:
   - **Email:** `admin@pravarthana.com`
   - **Password:** `password123`
3. You should be redirected to dashboard

---

## 🔑 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Super Admin | admin@pravarthana.com | password123 |
| HR Admin | hr@pravarthana.com | password123 |
| Manager | manager@pravarthana.com | password123 |
| Employee | employee@pravarthana.com | password123 |
| Employee 2 | john.doe@pravarthana.com | password123 |

---

## 🐛 Troubleshooting

### Frontend Issues

**Problem:** `Module not found` errors
```bash
# Solution: Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Problem:** Port 3000 already in use
```bash
# Solution: Use different port
npm run dev -- -p 3001
```

### Backend Issues

**Problem:** Database connection refused
- ✅ Check Supabase credentials in `application.properties`
- ✅ Verify Supabase project is active
- ✅ Check firewall/network settings

**Problem:** Port 8080 already in use
- Change port in `application.properties`:
  ```properties
  server.port=8081
  ```
- Update frontend `.env.local`:
  ```env
  NEXT_PUBLIC_API_URL=http://localhost:8081/api
  ```

**Problem:** JWT token errors
- ✅ Ensure `jwt.secret` is at least 256 bits (32 characters)
- ✅ Check token expiration time

### Database Issues

**Problem:** Table does not exist
- ✅ Re-run SQL schema files in Supabase SQL Editor
- ✅ Check if tables are created: Supabase → Table Editor

**Problem:** Authentication failed for user "postgres"
- ✅ Double-check database password
- ✅ Reset password in Supabase Settings → Database

---

## 📦 Project Structure After Setup

```
pravarthana-hrms/
├── frontend/
│   ├── node_modules/        ✅ Installed
│   ├── .next/               ✅ Build output
│   ├── .env.local           ✅ Configured
│   └── package.json
│
├── backend/
│   ├── target/              ✅ Maven build
│   ├── src/main/resources/
│   │   └── application.properties  ✅ Configured
│   └── pom.xml
│
└── database/
    └── schema/              ✅ Executed in Supabase
```

---

## 🚀 Next Steps

1. ✅ **Step 1 Complete:** Project setup
2. **Step 2:** Build public marketing website UI
3. **Step 3:** Build HRMS dashboard UI
4. **Step 4:** Implement backend APIs
5. **Step 5:** Connect frontend to backend
6. **Step 6:** Add PDF generation
7. **Step 7:** Implement real-time chat

---

## 📞 Need Help?

- Check logs:
  - Frontend: Terminal where `npm run dev` is running
  - Backend: Terminal where `mvn spring-boot:run` is running
  - Database: Supabase Dashboard → Logs

- Common issues: See **Troubleshooting** section above

---

**Setup complete! Ready to proceed with development.** 🎉
