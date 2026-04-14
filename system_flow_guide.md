# 🏫 School Management System — Operational Flow Guide

**Academic Year: 2024-2025 | School: School MS**

This document defines the complete operational workflow with all seeded login credentials, step-by-step setup sequence, and role-based access guide.

---

## 🔑 Login Credentials (All Active Accounts)

### Admin
| Role  | Email | Password | Username |
|-------|-------|----------|----------|
| ADMIN | admin@school.com | `Admin@123` | admin |

### Teachers (Standard Password Pattern: `FirstName@Teacher123`)
| Name | Email | Password | Subject | Class Assignment |
|------|-------|----------|---------|-----------------|
| Ramcharan Pandavula | ramcharanpandavula01@gmail.com | `Ramcharan@Teacher123` | Mathematics | 9th & 10th Class |
| Ramcharan Teja Pandavula | ramcharantejapandavula@gmail.com | `Ramcharan@Teacher123` | Science | 8th & 9th Class |
| Varma Karma | varmakarma01@gmial.com | `Varma@Teacher123` | English | 6th & 7th Class |
| Ramcharan Teja Photos | ramcharantejaphotos@gmail.com | `Ramcharan@Teacher123` | Telugu | 5th & 6th Class |
| Priya Sharma | priya.sharma@school.edu | `Priya@Teacher123` | Social Studies | 7th & 8th Class |
| Kiran Kumar | kiran.kumar@school.edu | `Kiran@Teacher123` | Computer Science | 9th & 10th Class |

> [!NOTE]
> Teacher passwords are auto-generated on creation. Use the **Forgot Password** flow on the login page to set a new one via OTP sent to the email.

### Non-Teaching Staff (Standard Password Pattern: `FirstName@Staff123`)
| Name | Email | Password | Role |
|------|-------|----------|------|
| Panda Badhrachalam | pandasbadhrachalamphotos@gmail.com | `Panda@Staff123` | LIBRARIAN |
| Suresh Babu | suresh.babu@school.edu | `Suresh@Staff123` | ACCOUNTANT |
| Gopi Krishna | gopi.krishna@school.edu | `Gopi@Staff123` | SECURITY |
| Anitha Rao | anitha.rao@school.edu | `Anitha@Staff123` | ADMINISTRATIVE |

### Students (Standard Password Pattern: `FirstName@DDMMYYYY`)
*Note: Approved students can login via Roll Number or Email.*

| Name | Class | Roll Number | Password | parent |
|------|-------|-------------|----------|--------|
| Arjun Reddy | 6th A | STU-202x-xxx | `Arjun@15052012` | Ravi Reddy |
| Sneha Patel | 7th A | STU-202x-xxx | `Sneha@22082011` | Raj Patel |
| Vikram Singh | 8th A | STU-202x-xxx | `Vikram@10112010` | Harinder Singh |
| Divya Sharma | 9th A | STU-202x-xxx | `Divya@18032009` | Mahesh Sharma |
| Aditya Kumar | 10th A | STU-202x-xxx | `Aditya@25062008` | Vijay Kumar |
| Kavya Nair | 2nd A | STU-202x-xxx | `Kavya@12012015` | Sunil Nair |
| Rohit Verma | 3rd A *(concession)* | STU-202x-xxx | `Rohit@30092014` | Suresh Verma |
| Pooja Gupta | 4th A | STU-202x-xxx | `Pooja@05072013` | Anil Gupta |
| Sai Raju | 1st A *(concession)* | STU-202x-xxx | `Sai@28022016` | Raju Sai |
| Meghana Chowdary | 6th A | STU-202x-xxx | `Meghana@15122012` | Chandra Chowdary |
| Nikhil Yadav | 7th A | STU-202x-xxx | `Nikhil@20042011` | Ram Yadav |
| Anusha Reddappa | 3rd A *(concession)* | STU-202x-xxx | `Anusha@01112014` | Bheema Reddappa |
| Teja Babu | 4th A | STU-202x-xxx | `Teja@17032013` | Subba Babu |
| Lavanya Devi | 8th A | STU-202x-xxx | `Lavanya@09072010` | Venkat Devi |
| Aravind Prasad | 1st A | STU-202x-xxx | `Aravind@14082016` | Krishna Prasad |

> [!TIP]
> Find exact roll numbers in the **Students** page under the admin dashboard. Students log in using their Roll Number (e.g., `STU-2024-001`) as the identifier and the auto-generated password in their parent's email.

---

## 📚 Academic Structure (2024-2025)

### Classes (1st–10th)
| Grade | Section | Room | Capacity |
|-------|---------|------|----------|
| 1st Class | A | R-101 | 40 |
| 2nd Class | A | R-102 | 40 |
| 3rd Class | A | R-103 | 40 |
| 4th Class | A | R-104 | 40 |
| 5th Class | A | R-105 | 40 |
| 6th Class | A | R-201 | 40 |
| 7th Class | A | R-202 | 40 |
| 8th Class | A | R-203 | 40 |
| 9th Class | A | R-204 | 40 |
| 10th Class | A | R-301 | 40 |

### Departments
- Science & Mathematics (SCI-MAT)
- Languages & Humanities (LANG-HUM)
- Physical Education (PHY-ED)
- Administration (ADMIN)

### Subjects
| Subject | Code | Dept |
|---------|------|------|
| Mathematics | MTH | Science & Math |
| Science | SCI | Science & Math |
| English | ENG | Languages |
| Telugu | TEL | Languages |
| Social Studies | SS | Languages |
| Hindi | HIN | Languages |
| Physical Education | PE | Physical Ed |
| Computer Science | CS | Science & Math |

---

## 💰 Fee Structures

| Fee Type | Amount | Due Date |
|----------|--------|----------|
| Term 1 Tuition Fee | ₹15,000 | 31 July 2024 |
| Annual Transport Fee | ₹5,000 | 31 July 2024 |
| First Term Exam Fee | ₹2,000 | 30 Sept 2024 |
| Library Access Fee | ₹500 | 31 July 2024 |

> First 5 enrolled students have a recorded payment of **₹15,000 (Tuition, via UPI)**.

---

## 📋 Operational Workflow

### Phase 1: Academic Infrastructure (Admin)
Before anyone can be added to the system, the foundation must exist.

1. **Set Academic Year & Terms:**
   - Active: **2024-2025** (June 1, 2024 → March 31, 2025)
2. **Define Classes (Grades):**
   - Classes 1st through 10th are set up with Section A
3. **Set Up Departments & Subjects:**
   - 4 departments and 8 subjects have been configured

### Phase 2: Staff & Faculty Onboarding (Admin)
4. **Register Teachers:**
   - 6 teachers registered (see credentials table above)
   - Each teacher gets auto-generated login credentials emailed
5. **Register Non-Teaching Staff:**
   - 4 staff registered (Librarian, Accountant, Security, Admin)
6. **Assign Teachers to Classes:**
   - Each teacher assigned to specific subjects and classes (see table)

### Phase 3: Student Admissions (Public + Admin)
7. **Public Application:**
   - Parents visit **/apply** (linked from the login page) — no login needed
   - 15 student applications submitted via public form
8. **Admin Review:**
   - Admin reviews pending applications at **/admissions**
   - All 15 applications were **APPROVED** with class assignments
9. **Student Login Created:**
   - On approval, roll numbers are auto-assigned (STU-2026-xxxxx)
   - Credentials emailed to parent email

### Phase 4: Daily Operations

10. **Mark Attendance:**
    - Teachers select Subject + Date → click-to-mark grid appears
    - Statuses: PRESENT / ABSENT / LATE / EXCUSED

11. **Fee Collection:**
    - Admin creates fee structures (already done above)
    - Record payments at `/fee` → "Record Payment" form

12. **Leave Applications:**
    - Any user submits a leave at `/leave`
    - Admin reviews and approves/rejects at the same page

13. **Exams & Report Cards:**
    - Admin creates an Exam via the Exams section
    - Teacher enters marks via `/admin/exams`
    - Admin publishes results → downloadable PDF Report Cards at `/students`

---

## 🔐 Role-Based Access Summary

| Feature | ADMIN | TEACHER | STUDENT | STAFF |
|---------|-------|---------|---------|-------|
| Academic Setup | ✅ | ❌ | ❌ | ❌ |
| Students List | ✅ | ❌ | ❌ | ❌ |
| Admissions Review | ✅ | ❌ | ❌ | ❌ |
| Teachers Management | ✅ | ❌ | ❌ | ❌ |
| Attendance Mark | ✅ | ✅ | ❌ | ❌ |
| Notices (View) | ✅ | ✅ | ✅ | ✅ |
| Notices (Post) | ✅ | ❌ | ❌ | ❌ |
| Fee Management | ✅ | ❌ | ❌ | ❌ |
| Leave (Apply) | ✅ | ✅ | ✅ | ✅ |
| Leave (Review) | ✅ | ❌ | ❌ | ❌ |
| Bus Tracking | ✅ | ✅ | ✅ | ✅ |
| ID Card Download | ✅ | ❌ | ❌ | ❌ |
| Report Card PDF | ✅ | ❌ | ✅ (own) | ❌ |

---

## ⚠️ Known Issue: Names Not Showing (Backend Restart Required)

The Java entity fix (`FetchType.LAZY` → `FetchType.EAGER`) has been applied to `Student.java`, `Teacher.java`, and `Staff.java`. **The backend Spring Boot application must be restarted** for these changes to take effect. After restart, teacher and student names will display correctly in all lists.

**To restart the backend:**  
Stop the running IntelliJ/Maven process and run it again (or click the Run button in IntelliJ IDEA).
