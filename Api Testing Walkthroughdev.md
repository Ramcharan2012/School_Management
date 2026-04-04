# Phase 5: API Testing Walkthrough

> **Base URL:** `http://localhost:8080/api`
> **Swagger UI:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

---

## 🚀 Step 1: Start the Application

Run from IntelliJ or terminal:
```
.\mvnw.cmd spring-boot:run
```

On first startup you'll see in logs:
```
🎓 ADMIN USER SEEDED SUCCESSFULLY!
   Email    : admin@school.com
   Password : Admin@123
```

---

## 🔐 Step 2: Login as Admin

**Swagger UI → Tag: Auth → `POST /api/auth/login`**

```json
{
  "email": "admin@school.com",
  "password": "Admin@123"
}
```

**Expected response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciO...",
    "refreshToken": "eyJhbGciO...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "email": "admin@school.com",
    "role": "ADMIN",
    "fullName": "School Admin",
    "isFirstLogin": false
  }
}
```

**👉 Copy `accessToken` → Click "Authorize" in Swagger → Paste it as `Bearer <token>`**

---

## 📚 Step 3: Academic Setup (Admin only)

> These must be done in order — each step depends on the previous.

### 3.1 Create Academic Year
`POST /api/admin/academic-years`
```json
{
  "yearName": "2024-2025",
  "startDate": "2024-06-01",
  "endDate": "2025-03-31",
  "isActive": true
}
```
📎 **Save the `id` returned** (e.g., `1`)

### 3.2 Create a Department
`POST /api/admin/departments`
```json
{
  "name": "Science",
  "code": "SCI",
  "description": "Science Department"
}
```

### 3.3 Create a Class
`POST /api/admin/classes`
```json
{
  "gradeName": "Grade 10",
  "section": "A",
  "capacity": 40,
  "roomNumber": "Room 101",
  "academicYearId": 1
}
```
📎 **Save the `id`** (e.g., `1`)

### 3.4 Create a Subject
`POST /api/admin/subjects`
```json
{
  "name": "Mathematics",
  "code": "MATH101",
  "departmentId": 1,
  "creditHours": 4,
  "isElective": false
}
```
📎 **Save the `id`** (e.g., `1`)

---

## 👨‍🏫 Step 4: Create a Teacher

`POST /api/admin/teachers`
```json
{
  "firstName": "Priya",
  "lastName": "Sharma",
  "email": "priya.sharma@school.com",
  "phoneNumber": "9876543210",
  "gender": "FEMALE",
  "qualification": "M.Sc Mathematics",
  "specialization": "Algebra and Calculus",
  "departmentId": 1,
  "joiningDate": "2024-06-01"
}
```

Teacher's credentials are auto-generated. Check logs for:
```
Teacher created: employeeId=TCH-2024-001, username=priya.sharma
```

---

## 🎓 Step 5: Student Admission Flow

### 5.1 Submit Admission Form (no token needed — public endpoint)
`POST /api/admissions` *(no auth required)*
```json
{
  "firstName": "Arjun",
  "lastName": "Kumar",
  "applicantEmail": "arjun.kumar@gmail.com",
  "phoneNumber": "9000000001",
  "dateOfBirth": "2009-05-15",
  "gender": "MALE",
  "address": "123 Main Street, Chennai",
  "bloodGroup": "O+",
  "parentName": "Suresh Kumar",
  "parentPhone": "9111111111",
  "parentEmail": "suresh.kumar@gmail.com",
  "parentOccupation": "Engineer",
  "applyingForGrade": "Grade 10",
  "academicYear": "2024-2025",
  "previousSchool": "ABC High School",
  "previousClassCompleted": "Grade 9",
  "previousPercentage": 85.5,
  "admissionFee": 5000.00,
  "tuitionFeePerMonth": 3000.00,
  "otherCharges": 500.00
}
```
📎 Save the `id` (e.g., `1`)

### 5.2 Approve the Admission (Admin)
`PUT /api/admin/admissions/1/review`
```json
{
  "status": "APPROVED",
  "classGradeId": 1,
  "adminRemarks": "Eligible. Documents verified."
}
```

**What happens automatically:**
- ✅ User account created (`arjun.kumar@school.com` / `Arjun@15052009`)
- ✅ Student profile created with roll number
- ✅ Email sent to applicant (if SMTP configured)
- ✅ Logged to console

---

## 🔑 Step 6: Login as Teacher & Student

### Teacher Login
```json
{
  "email": "priya.sharma@school.com",
  "password": "Priya@<joiningdate_ddmmyyyy>"
}
```
> Check logs for temp password pattern, typically `FirstName@DDMMYYYY`

### Student Login
```json
{
  "email": "arjun.kumar@school.com",
  "password": "Arjun@15052009"
}
```

---

## 📋 Step 7: Assign Subject to Teacher

*(Login as Admin)*  
`POST /api/admin/subject-assignments`
```json
{
  "teacherId": 1,
  "subjectId": 1,
  "classGradeId": 1,
  "academicYearId": 1
}
```

---

## ✅ Step 8: Mark Attendance (Teacher)

*(Login as Teacher)*  
`POST /api/teacher/attendance`
```json
{
  "subjectAssignmentId": 1,
  "attendanceDate": "2026-03-03",
  "attendances": [
    { "studentId": 1, "status": "PRESENT", "remarks": "" }
  ]
}
```

View attendance summary:  
`GET /api/teacher/attendance/summary?subjectAssignmentId=1&studentId=1`

---

## 📊 Step 9: Exam & Marks (Teacher)

### 9.1 Create Exam
`POST /api/teacher/exams`
```json
{
  "examName": "Unit Test 1",
  "subjectAssignmentId": 1,
  "examDate": "2026-03-10",
  "totalMarks": 100,
  "passingMarks": 35
}
```

### 9.2 Enter Marks
`POST /api/teacher/marks`
```json
{
  "examId": 1,
  "marks": [
    { "studentId": 1, "marksObtained": 78, "remarks": "Good performance" }
  ]
}
```

### 9.3 Publish Exam
`PUT /api/teacher/exams/1/publish`

### 9.4 Student sees Report Card
*(Login as Student)*  
`GET /api/student/marks/report-card`

---

## 💰 Step 10: Fee Payment (Admin)

`POST /api/admin/fee-payments`
```json
{
  "studentId": 1,
  "feeStructureId": 1,
  "amountPaid": 3000.00,
  "paymentMethod": "ONLINE",
  "paymentReference": "TXN123456",
  "remarks": "Monthly tuition fee"
}
```

Student fee statement:  
`GET /api/student/fees/statement`

---

## 📢 Step 11: Notice Board (Admin)

`POST /api/admin/notices`
```json
{
  "title": "School Annual Day",
  "content": "Annual Day celebration on March 20, 2026. All students must attend.",
  "targetAudience": "ALL",
  "isPinned": true,
  "expiresAt": "2026-03-21"
}
```

Students see:  
`GET /api/student/notices`

---

## 🚌 Step 12: Leave Request (Student)

*(Login as Student)*  
`POST /api/student/leave/apply`
```json
{
  "leaveType": "SICK_LEAVE",
  "startDate": "2026-03-05",
  "endDate": "2026-03-06",
  "reason": "Fever and doctor appointment"
}
```

*(Admin approves)*  
`PUT /api/admin/leave/{id}/review`
```json
{
  "status": "APPROVED",
  "remarks": "Approved with medical certificate."
}
```

---

## 📊 Step 13: Admin Dashboard (cached)

`GET /api/admin/dashboard/stats`

Returns aggregated stats. Call it twice — **second call is from Redis cache** (check logs: no SQL queries).

---

## 🔒 Step 14: JWT & Redis Testing

### Test Token Blacklist (Logout)
```
POST /api/auth/logout    → token added to Redis blacklist
Try any /api/admin/...  → 401 Unauthorized (blacklisted token)
```

### Refresh Token
```
POST /api/auth/refresh
{ "refreshToken": "eyJ..." }
→ New accessToken + new refreshToken (rotation)
```

### Check Auth Status
```
GET /api/auth/me   → returns current user profile
```

---

## 🗃️ Step 15: Audit Trail Check (PostgreSQL)

After making changes, check these tables in pgAdmin or psql:
```sql
-- See all revisions (who changed what, when)
SELECT * FROM REVINFO ORDER BY revtstmp DESC;

-- See user change history
SELECT * FROM users_AUD ORDER BY REV DESC;

-- See fee payment history
SELECT * FROM fee_payments_AUD ORDER BY REV DESC;

-- See marks history (catch tampering!)
SELECT * FROM marks_AUD ORDER BY REV DESC;
```

---

## ⚠️ Common Issues & Fixes

| Issue | Cause | Fix |
|-------|-------|-----|
| `401 Unauthorized` | Token not set in Swagger | Click "Authorize" and paste `Bearer <token>` |
| `403 Forbidden` | Wrong role for endpoint | Login with correct role (ADMIN/TEACHER/STUDENT) |
| Redis errors | Redis not running | Start Redis: go to Redis folder, run `redis-server.exe` |
| Email not sending | SMTP not configured | Set `MAIL_USERNAME` and `MAIL_PASSWORD` env vars |
| Table not found | DB not connected | Check `application.yaml` DB credentials |
