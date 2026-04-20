import axios from 'axios';

// Relative URL — Vite proxy forwards /api → http://localhost:8080/api
// Spring Boot context-path is already /api, so this is: localhost:8080/api/...
const BASE_URL = '/api';

const api = axios.create({ baseURL: BASE_URL });

// ── Request interceptor: attach JWT ─────────────────────────────────────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Response interceptor: auto-logout on 401 ────────────────────────────────
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default api;

// ══════════════════════════════════════════════════════════════════════════════
// AUTH —— /auth/...
// Controller: @RequestMapping("/auth")
// ══════════════════════════════════════════════════════════════════════════════
export const authAPI = {
  // POST /auth/login  →  body: { identifier, password }
  login: (identifier, password) =>
    api.post('/auth/login', { identifier, password }),
  // GET /auth/me
  me: () => api.get('/auth/me'),
  // POST /auth/logout  (requires Bearer token header)
  logout: () => api.post('/auth/logout'),
};

// ══════════════════════════════════════════════════════════════════════════════
// DASHBOARD —— /admin/dashboard/stats
// Controller: @RequestMapping("/admin/dashboard")
// ══════════════════════════════════════════════════════════════════════════════
export const dashboardAPI = {
  // GET /admin/dashboard/stats
  getStats: () => api.get('/admin/dashboard/stats'),
};

// ══════════════════════════════════════════════════════════════════════════════
// STUDENTS —— /admin/students
// Controller: no @RequestMapping prefix, individual @GetMapping/@PatchMapping
// Response shape: ApiResponse<PageResponse<Student>>
// PageResponse: { content:[], totalPages, totalElements, pageNumber, pageSize }
// ══════════════════════════════════════════════════════════════════════════════
export const studentAPI = {
  // GET /admin/students?page=0&size=10&search=...
  getAll: (page = 0, size = 10, search = '') => {
    const params = { page, size };
    if (search) params.search = search;
    return api.get('/admin/students', { params });
  },
  // GET /admin/students/{id}
  getById: (id) => api.get(`/admin/students/${id}`),
  // PATCH /admin/students/{id}/transfer?newClassGradeId=X
  transfer: (id, newClassGradeId) =>
    api.patch(`/admin/students/${id}/transfer`, null, { params: { newClassGradeId } }),
  // GET /admin/students/{id}/idcard (PDF)
  downloadIdCard: (id) =>
    api.get(`/admin/students/${id}/idcard`, { responseType: 'blob' }),
  // GET /student/marks/{id}/report-card/pdf (PDF)
  downloadReportCard: (id, classGradeId) =>
    api.get(`/student/marks/${id}/report-card/pdf`, { params: { classGradeId }, responseType: 'blob' }),
  // GET /admin/classes/{classGradeId}/idcards (ZIP)
  downloadBulkIdCards: (classGradeId) =>
    api.get(`/admin/classes/${classGradeId}/idcards`, { responseType: 'blob' }),
};

// ══════════════════════════════════════════════════════════════════════════════
// ADMISSIONS —— /public/admissions or /admin/admissions
// Controller: no @RequestMapping prefix
// ══════════════════════════════════════════════════════════════════════════════
export const admissionAPI = {
  // POST /public/admissions/apply  (public endpoint, no auth required)
  // body: AdmissionFormRequest — see dto
  apply: (data) => api.post('/public/admissions/apply', data),
  // GET /public/admissions/status/{applicationNumber}
  trackStatus: (applicationNumber) =>
    api.get(`/public/admissions/status/${applicationNumber}`),
  // GET /admin/admissions?page=0&size=10&status=PENDING
  getAll: (page = 0, size = 10, status = '') => {
    const params = { page, size };
    if (status) params.status = status;
    return api.get('/admin/admissions', { params });
  },
  // PATCH /admin/admissions/{id}/review  body: { status, remarks, classGradeId }
  review: (id, data) => api.patch(`/admin/admissions/${id}/review`, data),
  // GET /admin/admissions/stats
  getStats: () => api.get('/admin/admissions/stats'),
};

// ══════════════════════════════════════════════════════════════════════════════
// TEACHERS —— /admin/teachers
// Controller: no @RequestMapping prefix
// Response shape: ApiResponse<PageResponse<Teacher>>
// POST body fields: firstName, lastName, email, phoneNumber,
//                   qualification, designation, specialization,
//                   experienceYears(int), departmentId(Long)
// ══════════════════════════════════════════════════════════════════════════════
export const teacherAPI = {
  // GET /admin/teachers?page=0&size=10&search=...
  getAll: (page = 0, size = 10, search = '') => {
    const params = { page, size };
    if (search) params.search = search;
    return api.get('/admin/teachers', { params });
  },
  // GET /admin/teachers/{id}
  getById: (id) => api.get(`/admin/teachers/${id}`),
  // POST /admin/teachers
  create: (data) => api.post('/admin/teachers', data),
  // PUT /admin/teachers/{id}  body: qualification, designation, specialization, experienceYears, departmentId
  update: (id, data) => api.put(`/admin/teachers/${id}`, data),
  // DELETE /admin/teachers/{id}/deactivate
  deactivate: (id) => api.delete(`/admin/teachers/${id}/deactivate`),
};

// ══════════════════════════════════════════════════════════════════════════════
// ACADEMIC —— /admin/academic/...
// Controller: @RequestMapping("/admin/academic")
// ══════════════════════════════════════════════════════════════════════════════
export const academicAPI = {
  // GET /admin/academic/departments
  getDepartments: () => api.get('/admin/academic/departments'),
  // GET /admin/academic/years
  getYears: () => api.get('/admin/academic/years'),
  // POST /admin/academic/years
  createYear: (data) => api.post('/admin/academic/years', data),
  // GET /admin/academic/years/active
  getActiveYear: () => api.get('/admin/academic/years/active'),
  // PATCH /admin/academic/years/{id}/activate
  activateYear: (id) => api.patch(`/admin/academic/years/${id}/activate`),
  // GET /admin/academic/classes?academicYearId=X&page=0&size=50
  getClasses: (academicYearId, page = 0, size = 50) =>
    api.get('/admin/academic/classes', { params: { academicYearId, page, size } }),
  // GET /admin/academic/subjects?page=0&size=50
  getSubjects: (page = 0, size = 50) =>
    api.get('/admin/academic/subjects', { params: { page, size } }),
  // GET /admin/academic/assignments/class/{classGradeId}
  getAssignmentsByClass: (classGradeId) =>
    api.get(`/admin/academic/assignments/class/${classGradeId}`),
  // POST /admin/academic/departments  body: { name, code, description }
  createDepartment: (data) => api.post('/admin/academic/departments', data),
  // POST /admin/academic/classes  body: { gradeName, section, capacity, roomNumber, academicYearId }
  createClass: (data) => api.post('/admin/academic/classes', data),
  // POST /admin/academic/subjects  body: { name, code, description, creditHours, departmentId }
  createSubject: (data) => api.post('/admin/academic/subjects', data),
  // POST /admin/academic/assignments  body: { teacherId, subjectId, classGradeId }
  createAssignment: (data) => api.post('/admin/academic/assignments', data),
};

// ══════════════════════════════════════════════════════════════════════════════
// ATTENDANCE —— /teacher/attendance or /student/attendance
// Controller: no @RequestMapping prefix
// Mark body: { studentId, subjectId, teacherId, date (YYYY-MM-DD), status, remarks }
// status enum: PRESENT | ABSENT | LATE | EXCUSED
// ══════════════════════════════════════════════════════════════════════════════
export const attendanceAPI = {
  // POST /teacher/attendance/mark
  mark: (data) => api.post('/teacher/attendance/mark', data),
  // PATCH /teacher/attendance/{id}  body: { status, remarks }
  update: (id, data) => api.patch(`/teacher/attendance/${id}`, data),
  // GET /teacher/attendance/subject/{subjectId}/date?date=YYYY-MM-DD
  getBySubjectAndDate: (subjectId, date) =>
    api.get(`/teacher/attendance/subject/${subjectId}/date`, { params: { date } }),
  // GET /student/attendance/{studentId}/subject/{subjectId}
  getStudentBySubject: (studentId, subjectId) =>
    api.get(`/student/attendance/${studentId}/subject/${subjectId}`),
};

// ══════════════════════════════════════════════════════════════════════════════
// NOTICES —— /notices (GET) or /admin/notices (POST/DELETE)
// Controller: no @RequestMapping prefix
// target enum: ALL | STUDENTS | TEACHERS | PARENTS | STAFF
// ══════════════════════════════════════════════════════════════════════════════
export const noticeAPI = {
  // GET /notices?target=ALL&page=0
  getAll: (target = 'ALL', page = 0) =>
    api.get('/notices', { params: { target, page } }),
  // GET /notices/{id}
  getById: (id) => api.get(`/notices/${id}`),
  // POST /admin/notices  body: { title, content, targetAudience, isPinned, expiresAt, attachmentUrl, postedByUserId }
  create: (data) => api.post('/admin/notices', data),
  // PATCH /admin/notices/{id}/pin
  togglePin: (id) => api.patch(`/admin/notices/${id}/pin`),
  // DELETE /admin/notices/{id}
  delete: (id) => api.delete(`/admin/notices/${id}`),
};

// ══════════════════════════════════════════════════════════════════════════════
// FEE —— /admin/fees or /student/fees
// Controller: no @RequestMapping prefix
// ══════════════════════════════════════════════════════════════════════════════
export const feeAPI = {
  // GET /admin/fees/structures?academicYearId=1
  getStructures: (academicYearId) =>
    api.get('/admin/fees/structures', { params: { academicYearId } }),
  // POST /admin/fees/structures  body: { feeType, amount, dueDate, description, isMandatory, academicYearId, classGradeId }
  createStructure: (data) => api.post('/admin/fees/structures', data),
  // POST /admin/fees/payments  body: { studentId, feeStructureId, amountPaid, paymentMethod, transactionReference, remarks }
  recordPayment: (data) => api.post('/admin/fees/payments', data),
  // GET /student/fees/{studentId}/payments?page=0
  getStudentPayments: (studentId, page = 0) =>
    api.get(`/student/fees/${studentId}/payments`, { params: { page } }),
  // GET /student/fees/{studentId}/statement
  getStudentStatement: (studentId) =>
    api.get(`/student/fees/${studentId}/statement`),
  // GET /student/fees/{studentId}/applicable
  getApplicableFees: (studentId) =>
    api.get(`/student/fees/${studentId}/applicable`),
};

// ══════════════════════════════════════════════════════════════════════════════
// LEAVE —— /leave or /admin/leave
// Controller: no @RequestMapping prefix
// applyLeave body: { applicantId, subject, reason, fromDate (YYYY-MM-DD), toDate }
// review body:    { status (APPROVED|REJECTED), adminRemarks, reviewerUserId }
// ══════════════════════════════════════════════════════════════════════════════
export const leaveAPI = {
  // POST /leave/apply  body: { applicantId, subject, reason, fromDate, toDate }
  apply: (data) => api.post('/leave/apply', data),
  // GET /leave/my/{userId}?page=0
  getMyLeaves: (userId, page = 0) =>
    api.get(`/leave/my/${userId}`, { params: { page } }),
  // GET /admin/leave?page=0   (all leaves)
  getAllLeaves: (page = 0) =>
    api.get('/admin/leave', { params: { page } }),
  // GET /admin/leave/pending?page=0
  getPendingLeaves: (page = 0) =>
    api.get('/admin/leave/pending', { params: { page } }),
  // PATCH /admin/leave/{id}/review  body: { status, adminRemarks, reviewerUserId }
  review: (id, status, adminRemarks, reviewerUserId) =>
    api.patch(`/admin/leave/${id}/review`, { status, adminRemarks, reviewerUserId }),
};

// ══════════════════════════════════════════════════════════════════════════════
// TRANSPORT —— /admin/transport or /transport
// Controller: no @RequestMapping prefix inferred from prior session
// ══════════════════════════════════════════════════════════════════════════════
export const transportAPI = {
  getVehicles: () => api.get('/admin/transport/vehicles'),
  createVehicle: (data) => api.post('/admin/transport/vehicles', data),
  getRoutes: () => api.get('/admin/transport/routes'),
  createRoute: (data) => api.post('/admin/transport/routes', data),
  getStops: (routeId) => api.get(`/admin/transport/routes/${routeId}/stops`),
  addStop: (routeId, data) => api.post(`/admin/transport/routes/${routeId}/stops`, data),
  getBusLocation: (vehicleId) => api.get(`/transport/bus/${vehicleId}/location`),
  sendLocation: (data) => api.post('/transport/location', data),
};

// ══════════════════════════════════════════════════════════════════════════════
// MARKS —— /admin/exams, /teacher/marks, /student/marks
// ══════════════════════════════════════════════════════════════════════════════
export const marksAPI = {
  // GET /admin/exams/class/{classGradeId}
  getExamsByClass: (classGradeId, page = 0, size = 20) =>
    api.get(`/admin/exams/class/${classGradeId}`, { params: { page, size } }),
  // POST /admin/exams
  createExam: (data) => api.post('/admin/exams', data),
  // PATCH /admin/exams/{examId}/publish
  publishExam: (examId) => api.patch(`/admin/exams/${examId}/publish`),
  // GET /teacher/marks/exam/{examId}/results
  getExamResults: (examId) => api.get(`/teacher/marks/exam/${examId}/results`),
  // POST /teacher/marks
  enterMark: (data) => api.post('/teacher/marks', data),
  // GET /student/marks/{studentId}  — student views own marks
  getStudentMarks: (studentId, page = 0) =>
    api.get(`/student/marks/${studentId}`, { params: { page } }),
};

// ══════════════════════════════════════════════════════════════════════════════
// TIMETABLE —— /timetable or /admin/timetable
// ══════════════════════════════════════════════════════════════════════════════
export const timetableAPI = {
  // GET /timetable/class/{classGradeId}
  getByClass: (classGradeId) => api.get(`/timetable/class/${classGradeId}`),
  // GET /timetable/teacher/{teacherId}
  getByTeacher: (teacherId) => api.get(`/timetable/teacher/${teacherId}`),
  // POST /admin/timetable
  create: (data) => api.post('/admin/timetable', data),
  // DELETE /admin/timetable/{id}
  delete: (id) => api.delete(`/admin/timetable/${id}`),
};
