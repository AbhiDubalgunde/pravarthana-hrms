# 🚀 Quick Start - Run Pravarthana HRMS

Follow these steps to get the application running locally.

---

## ⚡ Step 1: Setup Database (Supabase)

1. Go to https://supabase.com and create a free account
2. Create a new project
3. Wait for it to initialize (~2 minutes)
4. Go to **SQL Editor** in the left sidebar
5. Run the SQL files in order:
   - First: Copy content from `database/schema/01_init.sql` and run
   - Then: Copy content from `database/schema/02_seed_data.sql` and run
6. Go to **Settings → Database** and copy your connection string

---

## 🎨 Step 2: Run Frontend (Next.js)

```bash
# Navigate to frontend directory
cd pravarthana-hrms/frontend

# Install dependencies
npm install

# Create environment file
cp .env.example .env.local

# Edit .env.local if needed (default values work for local development)

# Start development server
npm run dev
```

**Frontend will run at:** http://localhost:3000

---

## ⚙️ Step 3: Run Backend (Spring Boot)

### Option A: Using Maven (Recommended)

```bash
# Navigate to backend directory
cd pravarthana-hrms/backend

# Edit application.properties
# Update these lines with your Supabase credentials:
# spring.datasource.url=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
# spring.datasource.username=postgres
# spring.datasource.password=YOUR_SUPABASE_PASSWORD

# Run the application
mvn spring-boot:run
```

### Option B: Using IDE (IntelliJ/Eclipse)

1. Open `backend` folder in your IDE
2. Wait for Maven to download dependencies
3. Edit `src/main/resources/application.properties`
4. Right-click `HrmsApplication.java` → Run

**Backend will run at:** http://localhost:8080

---

## ✅ Step 4: Verify Setup

### 1. Check Backend Health
Open browser: http://localhost:8080/api/health

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2024-02-15T10:30:00",
  "message": "Pravarthana HRMS Backend is running!"
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
3. Click "Sign In"
4. You should be redirected to the dashboard

---

## 🔑 Demo Accounts

All passwords: `password123`

| Role | Email |
|------|-------|
| Super Admin | admin@pravarthana.com |
| HR Admin | hr@pravarthana.com |
| Manager | manager@pravarthana.com |
| Employee | employee@pravarthana.com |
| Employee 2 | john.doe@pravarthana.com |

---

## 🐛 Troubleshooting

### Backend won't start - Database connection error
- ✅ Verify Supabase credentials in `application.properties`
- ✅ Check if Supabase project is active
- ✅ Make sure you ran both SQL files in correct order

### Frontend shows "Network Error"
- ✅ Make sure backend is running on port 8080
- ✅ Check `NEXT_PUBLIC_API_URL` in `.env.local`
- ✅ Check browser console for CORS errors

### Login fails with "Invalid credentials"
- ✅ Make sure you ran `02_seed_data.sql`
- ✅ Check backend logs for errors
- ✅ Try password: `password123` (all lowercase)

### Port already in use
- Frontend: Change port with `npm run dev -- -p 3001`
- Backend: Change `server.port` in `application.properties`

---

## 📂 Project Structure

```
pravarthana-hrms/
├── frontend/              # Next.js frontend
│   ├── src/
│   │   ├── app/          # Pages
│   │   └── components/   # React components
│   └── package.json
│
├── backend/               # Spring Boot backend
│   ├── src/main/java/    # Java source code
│   └── pom.xml           # Maven dependencies
│
└── database/
    └── schema/           # SQL files
```

---

## 🎯 What's Working

✅ **Frontend:**
- Home page (marketing website)
- Login page with demo credentials
- Dashboard placeholder
- Responsive design
- Tailwind CSS styling

✅ **Backend:**
- Health check endpoint
- Login API with JWT authentication
- BCrypt password hashing
- CORS configuration
- Database connection

✅ **Database:**
- Complete schema (11 tables)
- 5 demo user accounts
- Sample data

---

## 🚀 Next Steps

This is the **MVP foundation**. In Step 2 and beyond, we'll add:
- Complete marketing website pages
- Full HRMS dashboard with sidebar
- Employee management (CRUD)
- Attendance tracking
- Leave management
- Offer letter generator
- Real-time chat
- And more...

---

## 📞 Need Help?

If you encounter any issues:
1. Check the error message in terminal
2. Look at browser console (F12)
3. Verify database connection
4. Make sure all dependencies are installed

**Everything ready? You're all set! 🎉**
