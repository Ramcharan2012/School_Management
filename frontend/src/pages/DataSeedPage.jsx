import React, { useState } from 'react';
import api, { academicAPI, teacherAPI, admissionAPI, feeAPI, noticeAPI } from '../services/api';

// ─── Seed data ────────────────────────────────────────────────────────────────

const YEAR = { yearLabel: '2024-2025', startDate: '2024-06-01', endDate: '2025-03-31' };

const DEPARTMENTS = [
  { name: 'Science & Mathematics', code: 'SCI-MAT', description: 'Sciences and core mathematics' },
  { name: 'Languages & Humanities', code: 'LANG-HUM', description: 'English, Telugu, Hindi and Social Studies' },
  { name: 'Physical Education', code: 'PHY-ED', description: 'Sports and physical activities' },
  { name: 'Administration', code: 'ADMIN', description: 'School administration and office staff' },
];

const SUBJECTS = [
  { name: 'Mathematics', code: 'MTH', description: 'Core arithmetic and algebra', creditHours: 5, deptIdx: 0 },
  { name: 'Science', code: 'SCI', description: 'Physics, Chemistry and Biology basics', creditHours: 5, deptIdx: 0 },
  { name: 'English', code: 'ENG', description: 'English language and literature', creditHours: 4, deptIdx: 1 },
  { name: 'Telugu', code: 'TEL', description: 'Telugu language', creditHours: 3, deptIdx: 1 },
  { name: 'Social Studies', code: 'SS', description: 'History, civics and geography', creditHours: 4, deptIdx: 1 },
  { name: 'Hindi', code: 'HIN', description: 'Hindi language', creditHours: 3, deptIdx: 1 },
  { name: 'Physical Education', code: 'PE', description: 'Sports and health', creditHours: 2, deptIdx: 2 },
  { name: 'Computer Science', code: 'CS', description: 'Fundamentals of computing', creditHours: 3, deptIdx: 0 },
];

const CLASS_GRADES = [
  { gradeName: '1st Class', section: 'A', capacity: 40, roomNumber: 'R-101' },
  { gradeName: '2nd Class', section: 'A', capacity: 40, roomNumber: 'R-102' },
  { gradeName: '3rd Class', section: 'A', capacity: 40, roomNumber: 'R-103' },
  { gradeName: '4th Class', section: 'A', capacity: 40, roomNumber: 'R-104' },
  { gradeName: '5th Class', section: 'A', capacity: 40, roomNumber: 'R-105' },
  { gradeName: '6th Class', section: 'A', capacity: 40, roomNumber: 'R-201' },
  { gradeName: '7th Class', section: 'A', capacity: 40, roomNumber: 'R-202' },
  { gradeName: '8th Class', section: 'A', capacity: 40, roomNumber: 'R-203' },
  { gradeName: '9th Class', section: 'A', capacity: 40, roomNumber: 'R-204' },
  { gradeName: '10th Class', section: 'A', capacity: 40, roomNumber: 'R-301' },
];

const TEACHERS = [
  { firstName: 'Ramcharan', lastName: 'Pandavula', email: 'ramcharanpandavula01@gmail.com', phoneNumber: '9876543210', qualification: 'M.Sc Mathematics', designation: 'Senior Math Teacher', specialization: 'Algebra & Calculus', experienceYears: 8, deptIdx: 0 },
  { firstName: 'Ramcharan', lastName: 'Teja', email: 'ramcharantejapandavula@gmail.com', phoneNumber: '9876543211', qualification: 'M.Sc Physics', designation: 'Science Teacher', specialization: 'Physics', experienceYears: 5, deptIdx: 0 },
  { firstName: 'Varma', lastName: 'Karma', email: 'varmakarma01@gmial.com', phoneNumber: '9876543212', qualification: 'M.A English', designation: 'English Teacher', specialization: 'English Literature', experienceYears: 6, deptIdx: 1 },
  { firstName: 'Teja', lastName: 'Photos', email: 'ramcharantejaphotos@gmail.com', phoneNumber: '9876543213', qualification: 'B.Ed Telugu', designation: 'Telugu Teacher', specialization: 'Telugu Grammar', experienceYears: 4, deptIdx: 1 },
  { firstName: 'Priya', lastName: 'Sharma', email: 'priya.sharma@school.edu', phoneNumber: '9876543214', qualification: 'M.A Social Studies', designation: 'Social Studies Teacher', specialization: 'History', experienceYears: 7, deptIdx: 1 },
  { firstName: 'Kiran', lastName: 'Kumar', email: 'kiran.kumar@school.edu', phoneNumber: '9876543215', qualification: 'M.Sc Computer Science', designation: 'Computer Teacher', specialization: 'Programming', experienceYears: 3, deptIdx: 0 },
];

const STAFF_MEMBERS = [
  { firstName: 'Panda', lastName: 'Badhrachalam', email: 'pandasbadhrachalamphotos@gmail.com', phoneNumber: '9876543220', staffCategory: 'LIBRARIAN', designation: 'Senior Librarian', qualification: 'B.Lib', deptIdx: null },
  { firstName: 'Suresh', lastName: 'Babu', email: 'suresh.babu@school.edu', phoneNumber: '9876543221', staffCategory: 'ACCOUNTANT', designation: 'Accountant', qualification: 'B.Com', deptIdx: 3 },
  { firstName: 'Gopi', lastName: 'Krishna', email: 'gopi.krishna@school.edu', phoneNumber: '9876543222', staffCategory: 'SECURITY', designation: 'Security Guard', qualification: 'Secondary School', deptIdx: null },
  { firstName: 'Anitha', lastName: 'Rao', email: 'anitha.rao@school.edu', phoneNumber: '9876543223', staffCategory: 'ADMINISTRATIVE', designation: 'Office Assistant', qualification: 'B.A', deptIdx: 3 },
];

// 15 students across all classes
const STUDENTS = [
  { firstName: 'Arjun',   lastName: 'Reddy',      applicantEmail: 'arjun.reddy.parent@gmail.com',     phoneNumber: '9900001001', dateOfBirth: '2012-05-15', gender: 'MALE',   address: 'H.No 12, MG Road, Hyderabad',   bloodGroup: 'B+', parentName: 'Ravi Reddy',       parentPhone: '9900001000', parentEmail: 'ravi.reddy@gmail.com',       parentOccupation: 'Software Engineer', applyingForGrade: '6th Class', feeConcessionRequested: false },
  { firstName: 'Sneha',   lastName: 'Patel',       applicantEmail: 'sneha.patel.parent@gmail.com',     phoneNumber: '9900001003', dateOfBirth: '2011-08-22', gender: 'FEMALE', address: 'Flat 5B, Jubilee Hills',        bloodGroup: 'O+', parentName: 'Raj Patel',         parentPhone: '9900001002', parentEmail: 'raj.patel@gmail.com',        parentOccupation: 'Doctor',            applyingForGrade: '7th Class', feeConcessionRequested: false },
  { firstName: 'Vikram',  lastName: 'Singh',       applicantEmail: 'vikram.singh.parent@gmail.com',    phoneNumber: '9900001005', dateOfBirth: '2010-11-10', gender: 'MALE',   address: '45, Banjara Hills, Hyderabad',  bloodGroup: 'A+', parentName: 'Harinder Singh',    parentPhone: '9900001004', parentEmail: 'harinder.singh@gmail.com',   parentOccupation: 'Businessman',       applyingForGrade: '8th Class', feeConcessionRequested: false },
  { firstName: 'Divya',   lastName: 'Sharma',      applicantEmail: 'divya.sharma.parent@gmail.com',    phoneNumber: '9900001007', dateOfBirth: '2009-03-18', gender: 'FEMALE', address: '7, Nampally, Hyderabad',        bloodGroup: 'AB+',parentName: 'Mahesh Sharma',     parentPhone: '9900001006', parentEmail: 'mahesh.sharma@gmail.com',    parentOccupation: 'Teacher',           applyingForGrade: '9th Class', feeConcessionRequested: false },
  { firstName: 'Aditya',  lastName: 'Kumar',       applicantEmail: 'aditya.kumar.parent@gmail.com',    phoneNumber: '9900001009', dateOfBirth: '2008-06-25', gender: 'MALE',   address: '22, Secunderabad, Hyderabad',   bloodGroup: 'O-', parentName: 'Vijay Kumar',       parentPhone: '9900001008', parentEmail: 'vijay.kumar@gmail.com',      parentOccupation: 'Government Employee',applyingForGrade: '10th Class',feeConcessionRequested: false },
  { firstName: 'Kavya',   lastName: 'Nair',        applicantEmail: 'kavya.nair.parent@gmail.com',      phoneNumber: '9900001011', dateOfBirth: '2015-01-12', gender: 'FEMALE', address: '3, Kondapur, Hyderabad',        bloodGroup: 'B-', parentName: 'Sunil Nair',        parentPhone: '9900001010', parentEmail: 'sunil.nair@gmail.com',       parentOccupation: 'Chartered Accountant',applyingForGrade: '2nd Class',feeConcessionRequested: false },
  { firstName: 'Rohit',   lastName: 'Verma',       applicantEmail: 'rohit.verma.parent@gmail.com',     phoneNumber: '9900001013', dateOfBirth: '2014-09-30', gender: 'MALE',   address: '10, Madhapur, Hyderabad',       bloodGroup: 'A-', parentName: 'Suresh Verma',      parentPhone: '9900001012', parentEmail: 'suresh.verma@gmail.com',     parentOccupation: 'Engineer',          applyingForGrade: '3rd Class', feeConcessionRequested: true  },
  { firstName: 'Pooja',   lastName: 'Gupta',       applicantEmail: 'pooja.gupta.parent@gmail.com',     phoneNumber: '9900001015', dateOfBirth: '2013-07-05', gender: 'FEMALE', address: '18, Gachibowli, Hyderabad',     bloodGroup: 'O+', parentName: 'Anil Gupta',        parentPhone: '9900001014', parentEmail: 'anil.gupta@gmail.com',       parentOccupation: 'Retailer',          applyingForGrade: '4th Class', feeConcessionRequested: false },
  { firstName: 'Sai',     lastName: 'Raju',        applicantEmail: 'sai.raju.parent@gmail.com',        phoneNumber: '9900001017', dateOfBirth: '2016-02-28', gender: 'MALE',   address: '5, Kukatpally, Hyderabad',      bloodGroup: 'B+', parentName: 'Raju Sai',          parentPhone: '9900001016', parentEmail: 'raju.sai@gmail.com',         parentOccupation: 'Farmer',            applyingForGrade: '1st Class', feeConcessionRequested: true  },
  { firstName: 'Meghana', lastName: 'Chowdary',    applicantEmail: 'meghana.chowdary.parent@gmail.com',phoneNumber: '9900001019', dateOfBirth: '2012-12-15', gender: 'FEMALE', address: '33, Ameerpet, Hyderabad',       bloodGroup: 'A+', parentName: 'Chandra Chowdary',  parentPhone: '9900001018', parentEmail: 'chandra.chowdary@gmail.com', parentOccupation: 'Lawyer',            applyingForGrade: '6th Class', feeConcessionRequested: false },
  { firstName: 'Nikhil',  lastName: 'Yadav',       applicantEmail: 'nikhil.yadav.parent@gmail.com',    phoneNumber: '9900001021', dateOfBirth: '2011-04-20', gender: 'MALE',   address: '77, Dilsukhnagar, Hyderabad',   bloodGroup: 'O+', parentName: 'Ram Yadav',         parentPhone: '9900001020', parentEmail: 'ram.yadav@gmail.com',        parentOccupation: 'Police Officer',    applyingForGrade: '7th Class', feeConcessionRequested: false },
  { firstName: 'Anusha',  lastName: 'Reddappa',    applicantEmail: 'anusha.reddappa.parent@gmail.com', phoneNumber: '9900001023', dateOfBirth: '2014-11-01', gender: 'FEMALE', address: '8, LB Nagar, Hyderabad',        bloodGroup: 'AB-',parentName: 'Bheema Reddappa',   parentPhone: '9900001022', parentEmail: 'bheema.reddappa@gmail.com',  parentOccupation: 'Farmer',            applyingForGrade: '3rd Class', feeConcessionRequested: true  },
  { firstName: 'Teja',    lastName: 'Babu',        applicantEmail: 'teja.babu.parent@gmail.com',       phoneNumber: '9900001025', dateOfBirth: '2013-03-17', gender: 'MALE',   address: '14, Uppal, Hyderabad',          bloodGroup: 'B+', parentName: 'Subba Babu',        parentPhone: '9900001024', parentEmail: 'subba.babu@gmail.com',       parentOccupation: 'Driver',            applyingForGrade: '4th Class', feeConcessionRequested: false },
  { firstName: 'Lavanya', lastName: 'Devi',        applicantEmail: 'lavanya.devi.parent@gmail.com',    phoneNumber: '9900001027', dateOfBirth: '2010-07-09', gender: 'FEMALE', address: '92, Malakpet, Hyderabad',       bloodGroup: 'O+', parentName: 'Venkat Devi',       parentPhone: '9900001026', parentEmail: 'venkat.devi@gmail.com',      parentOccupation: 'Nurse',             applyingForGrade: '8th Class', feeConcessionRequested: false },
  { firstName: 'Aravind', lastName: 'Prasad',      applicantEmail: 'aravind.prasad.parent@gmail.com',  phoneNumber: '9900001029', dateOfBirth: '2016-08-14', gender: 'MALE',   address: '1, Hayathnagar, Hyderabad',     bloodGroup: 'A+', parentName: 'Krishna Prasad',    parentPhone: '9900001028', parentEmail: 'krishna.prasad@gmail.com',   parentOccupation: 'Bank Employee',     applyingForGrade: '1st Class', feeConcessionRequested: false },
];

const NOTICES = [
  { title: 'Welcome to 2024-2025 Academic Year!', content: 'We are delighted to welcome all students, parents, and staff for the 2024-2025 academic year. Classes commence from 10th June 2024. Please collect timetables from the office.', targetAudience: 'ALL', isPinned: true },
  { title: 'Annual Sports Day – 25 August 2024', content: 'The Annual Sports Day will be held on 25th August 2024. All students are encouraged to participate.', targetAudience: 'STUDENTS', isPinned: false },
  { title: 'Fee Collection Notice – Term 1', content: 'Parents are kindly requested to pay Term 1 fees before 30th June 2024 to avoid late charges.', targetAudience: 'ALL', isPinned: true },
  { title: 'Staff Meeting – 15 June 2024', content: 'All staff are required to attend the orientation on 15th June 2024 at 10:00 AM in the Main Hall.', targetAudience: 'TEACHERS', isPinned: false },
];

// ─── Helper ───────────────────────────────────────────────────────────────────
const sleep = ms => new Promise(r => setTimeout(r, ms));

// ─── Seeder Component ─────────────────────────────────────────────────────────
export default function DataSeedPage() {
  const [logs, setLogs] = useState([]);
  const [running, setRunning] = useState(false);
  const [done, setDone] = useState(false);
  const [ids, setIds] = useState({});

  const addLog = (msg, type = 'info') => {
    setLogs(prev => [...prev, { msg, type, ts: new Date().toLocaleTimeString() }]);
    console.log(`[${type.toUpperCase()}] ${msg}`);
  };

  const runSeed = async () => {
    setRunning(true); setDone(false); setLogs([]);
    const collected = {};

    try {
      // ── Step 1: Academic Year ──────────────────────────────────────────────
      addLog('Creating / finding Academic Year 2024-2025...');
      try {
        const r = await academicAPI.createYear(YEAR);
        collected.yearId = r.data.data?.id;
        addLog(`✅ Academic Year created: ID=${collected.yearId}`, 'success');
      } catch (e) {
        // Try to fetch existing year
        const all = await academicAPI.getYears();
        const found = (all.data.data ?? []).find(y => y.yearLabel === '2024-2025');
        if (found) { collected.yearId = found.id; addLog(`ℹ️  Year already exists: ID=${found.id}`, 'warn'); }
        else addLog(`❌ Year: ${e.response?.data?.message || e.message}`, 'error');
      }

      // Activate
      if (collected.yearId) {
        try { await academicAPI.activateYear(collected.yearId); addLog('✅ Year activated', 'success'); }
        catch (e) { addLog(`ℹ️  Year activate: ${e.response?.data?.message || 'already active'}`, 'warn'); }
      }
      await sleep(300);

      // ── Step 2: Departments ────────────────────────────────────────────────
      addLog('Creating Departments...');
      const deptIds = [];
      // Try fetching existing first
      const existingDepts = (await academicAPI.getDepartments()).data.data ?? [];
      for (const dept of DEPARTMENTS) {
        const existing = existingDepts.find(d => d.code === dept.code);
        if (existing) { deptIds.push(existing.id); addLog(`ℹ️  Dept "${dept.name}" exists: ID=${existing.id}`, 'warn'); continue; }
        try {
          const r = await academicAPI.createDepartment(dept);
          deptIds.push(r.data.data?.id);
          addLog(`✅ Dept: ${dept.name} (ID=${r.data.data?.id})`, 'success');
        } catch (e) { addLog(`⚠️  Dept ${dept.name}: ${e.response?.data?.message || e.message}`, 'warn'); deptIds.push(null); }
        await sleep(80);
      }
      collected.deptIds = deptIds;

      // ── Step 3: Subjects ───────────────────────────────────────────────────
      addLog('Creating Subjects...');
      const existingSubjects = (await academicAPI.getSubjects(0, 100)).data.data?.content ?? [];
      const subjectIds = [];
      for (const sub of SUBJECTS) {
        const existing = existingSubjects.find(s => s.code === sub.code);
        if (existing) { subjectIds.push(existing.id); addLog(`ℹ️  Subject "${sub.name}" exists: ID=${existing.id}`, 'warn'); continue; }
        try {
          const r = await academicAPI.createSubject({ name: sub.name, code: sub.code, description: sub.description, creditHours: sub.creditHours, departmentId: deptIds[sub.deptIdx] });
          subjectIds.push(r.data.data?.id);
          addLog(`✅ Subject: ${sub.name} (ID=${r.data.data?.id})`, 'success');
        } catch (e) { addLog(`⚠️  Subject ${sub.name}: ${e.response?.data?.message || e.message}`, 'warn'); subjectIds.push(null); }
        await sleep(80);
      }
      collected.subjectIds = subjectIds;

      // ── Step 4: Classes 1–10 ───────────────────────────────────────────────
      addLog('Creating Classes 1st–10th...');
      const classIds = [];
      if (collected.yearId) {
        const existingClasses = (await academicAPI.getClasses(collected.yearId, 0, 50)).data.data?.content ?? [];
        for (const cls of CLASS_GRADES) {
          const existing = existingClasses.find(c => c.gradeName === cls.gradeName && c.section === cls.section);
          if (existing) { classIds.push(existing.id); addLog(`ℹ️  Class "${cls.gradeName}" exists: ID=${existing.id}`, 'warn'); continue; }
          try {
            const r = await academicAPI.createClass({ ...cls, academicYearId: collected.yearId });
            classIds.push(r.data.data?.id);
            addLog(`✅ Class: ${cls.gradeName} (ID=${r.data.data?.id})`, 'success');
          } catch (e) { addLog(`⚠️  Class ${cls.gradeName}: ${e.response?.data?.message || e.message}`, 'warn'); classIds.push(null); }
          await sleep(80);
        }
      } else { addLog('❌ Skipping classes — no academic year ID', 'error'); CLASS_GRADES.forEach(() => classIds.push(null)); }
      collected.classIds = classIds;

      // ── Step 5: Teachers ───────────────────────────────────────────────────
      addLog('Creating / finding Teachers...');
      const teacherIds = [];
      // Pre-fetch all existing teachers to match by email
      let existingTeachers = [];
      try { existingTeachers = (await teacherAPI.getAll(0, 100)).data.data?.content ?? []; } catch {}

      for (const t of TEACHERS) {
        // First try to find by fetching and matching email
        const found = existingTeachers.find(et => et.email === t.email);
        if (found) {
          teacherIds.push(found.id);
          addLog(`ℹ️  Teacher "${t.firstName}" exists: ID=${found.id}`, 'warn');
          continue;
        }
        try {
          const r = await teacherAPI.create({ firstName: t.firstName, lastName: t.lastName, email: t.email, phoneNumber: t.phoneNumber, qualification: t.qualification, designation: t.designation, specialization: t.specialization, experienceYears: t.experienceYears, departmentId: deptIds[t.deptIdx] });
          const tid = r.data.data?.teacher?.id ?? r.data.data?.id;
          teacherIds.push(tid);
          addLog(`✅ Teacher: ${t.firstName} ${t.lastName} (ID=${tid})`, 'success');
        } catch (e) {
          addLog(`⚠️  Teacher ${t.firstName}: ${e.response?.data?.message || e.message}`, 'warn');
          teacherIds.push(null);
        }
        await sleep(150);
      }
      collected.teacherIds = teacherIds;
      addLog(`ℹ️  Teacher IDs: [${teacherIds.join(', ')}]`, 'info');

      // ── Step 6: Staff ──────────────────────────────────────────────────────
      addLog('Creating / finding Staff...');
      let existingStaff = [];
      try { existingStaff = (await api.get('/admin/staff', { params: { page: 0, size: 50 } })).data.data?.content ?? []; } catch {}

      for (const s of STAFF_MEMBERS) {
        const foundStaff = existingStaff.find(es => es.email === s.email);
        if (foundStaff) {
          addLog(`ℹ️  Staff "${s.firstName}" exists: ID=${foundStaff.id}`, 'warn');
          continue;
        }
        try {
          await api.post('/admin/staff', { firstName: s.firstName, lastName: s.lastName, email: s.email, phoneNumber: s.phoneNumber, staffCategory: s.staffCategory, designation: s.designation, qualification: s.qualification, departmentId: s.deptIdx != null ? deptIds[s.deptIdx] : null });
          addLog(`✅ Staff: ${s.firstName} ${s.lastName} (${s.staffCategory})`, 'success');
        } catch (e) { addLog(`⚠️  Staff ${s.firstName}: ${e.response?.data?.message || e.message}`, 'warn'); }
        await sleep(150);
      }

      // ── Step 7: Teacher → Subject → Class Assignments ──────────────────────
      addLog('Assigning Teachers to Classes...');
      const assignments = [
        { tIdx: 0, sIdx: 0, cIdx: 8 }, { tIdx: 0, sIdx: 0, cIdx: 9 },  // Math 9th,10th
        { tIdx: 1, sIdx: 1, cIdx: 7 }, { tIdx: 1, sIdx: 1, cIdx: 8 },  // Science 8th,9th
        { tIdx: 2, sIdx: 2, cIdx: 5 }, { tIdx: 2, sIdx: 2, cIdx: 6 },  // English 6th,7th
        { tIdx: 3, sIdx: 3, cIdx: 4 }, { tIdx: 3, sIdx: 3, cIdx: 5 },  // Telugu 5th,6th
        { tIdx: 4, sIdx: 4, cIdx: 6 }, { tIdx: 4, sIdx: 4, cIdx: 7 },  // SST 7th,8th
        { tIdx: 5, sIdx: 7, cIdx: 8 }, { tIdx: 5, sIdx: 7, cIdx: 9 },  // CS 9th,10th
      ];
      for (const a of assignments) {
        const tId = teacherIds[a.tIdx], sId = subjectIds[a.sIdx], cId = classIds[a.cIdx];
        if (!tId || !sId || !cId) { addLog(`⚠️  Skip assignment (missing IDs t=${tId} s=${sId} c=${cId})`, 'warn'); continue; }
        try {
          await academicAPI.createAssignment({ teacherId: tId, subjectId: sId, classGradeId: cId });
          addLog(`✅ Assigned t${a.tIdx + 1}→s${a.sIdx + 1}→c${a.cIdx + 1}`, 'success');
        } catch (e) { addLog(`⚠️  Assignment: ${e.response?.data?.message || e.message}`, 'warn'); }
        await sleep(80);
      }

      // ── Step 8: Student Admissions ─────────────────────────────────────────
      addLog('Submitting Student Admission Forms...');
      const admissionIds = [];
      for (const s of STUDENTS) {
        try {
          const r = await admissionAPI.apply({ ...s, academicYear: '2024-2025' });
          const appId = r.data.data?.id;
          admissionIds.push(appId);
          addLog(`✅ Admission: ${s.firstName} ${s.lastName} (ID=${appId})`, 'success');
        } catch (e) {
          addLog(`⚠️  Admission ${s.firstName}: ${e.response?.data?.message || e.message}`, 'warn');
          admissionIds.push(null);
        }
        await sleep(120);
      }

      // ── Step 9: Approve Admissions with correct classGradeId ───────────────
      addLog('Approving All Admission Applications...');
      // Refresh admission list to get ALL pending (including older ones)
      const allAdm = (await admissionAPI.getAll(0, 50, 'PENDING')).data.data?.content ?? [];
      addLog(`ℹ️  Found ${allAdm.length} pending admission(s)`, 'info');

      // Build grade → classId lookup
      const gradeToClassId = {};
      CLASS_GRADES.forEach((c, i) => { if (classIds[i]) gradeToClassId[c.gradeName] = classIds[i]; });

      for (const adm of allAdm) {
        const cId = gradeToClassId[adm.applyingForGrade];
        if (!cId) { addLog(`⚠️  No class ID for grade "${adm.applyingForGrade}" — need to create classes first`, 'warn'); continue; }
        try {
          await admissionAPI.review(adm.id, { status: 'APPROVED', remarks: 'Admission approved. Welcome!', classGradeId: cId });
          addLog(`✅ Approved: ${adm.firstName} ${adm.lastName} → ${adm.applyingForGrade}`, 'success');
        } catch (e) { addLog(`⚠️  Approve ${adm.firstName}: ${e.response?.data?.message || e.message}`, 'warn'); }
        await sleep(200);
      }

      // ── Step 10: Fee Structures ────────────────────────────────────────────
      addLog('Creating Fee Structures...');
      const feeStructures = [
        { feeType: 'TUITION_FEE',     amount: 15000, dueDate: '2024-07-31', description: 'Term 1 Tuition Fee',    isMandatory: true,  academicYearId: collected.yearId },
        { feeType: 'TRANSPORT_FEE',   amount: 5000,  dueDate: '2024-07-31', description: 'Annual Transport Fee', isMandatory: false, academicYearId: collected.yearId },
        { feeType: 'EXAMINATION_FEE', amount: 2000,  dueDate: '2024-09-30', description: 'First Term Exam Fee',  isMandatory: true,  academicYearId: collected.yearId },
        { feeType: 'LIBRARY_FEE',     amount: 500,   dueDate: '2024-07-31', description: 'Library Access Fee',   isMandatory: false, academicYearId: collected.yearId },
      ];
      const feeStructureIds = [];
      for (const fs of feeStructures) {
        try {
          const r = await feeAPI.createStructure(fs);
          feeStructureIds.push(r.data.data?.id);
          addLog(`✅ Fee: ${fs.feeType} ₹${fs.amount} (ID=${r.data.data?.id})`, 'success');
        } catch (e) { addLog(`⚠️  Fee ${fs.feeType}: ${e.response?.data?.message || e.message}`, 'warn'); feeStructureIds.push(null); }
        await sleep(80);
      }

      // ── Step 11: Notices ───────────────────────────────────────────────────
      addLog('Creating Notices...');
      // Get admin userId from JWT stored in localStorage
      let adminUserId = null;
      try {
        const token = localStorage.getItem('token');
        if (token) {
          const payload = JSON.parse(atob(token.split('.')[1]));
          adminUserId = payload.userId || payload.sub || null;
        }
      } catch {}
      // Fallback: fetch from /auth/me
      if (!adminUserId) {
        try {
          const me = await api.get('/auth/me');
          adminUserId = me.data.data?.userId ?? me.data.data?.id ?? me.data?.userId ?? null;
        } catch {}
      }
      addLog(`ℹ️  Admin user ID for notices: ${adminUserId}`, 'info');

      for (const n of NOTICES) {
        try {
          const body = { title: n.title, content: n.content, targetAudience: n.targetAudience, isPinned: n.isPinned };
          if (adminUserId) body.postedByUserId = Number(adminUserId);
          await noticeAPI.create(body);
          addLog(`✅ Notice: "${n.title}"`, 'success');
        } catch (e) { addLog(`⚠️  Notice "${n.title}": ${e.response?.data?.message || e.message}`, 'warn'); }
        await sleep(80);
      }

      // ── Step 12: Record fee payment for some students ─────────────────────
      addLog('Recording fee payments for approved students...');
      await sleep(1500);
      try {
        const stuRes = await api.get('/admin/students', { params: { page: 0, size: 50 } });
        const students = stuRes.data.data?.content ?? [];
        addLog(`ℹ️  ${students.length} enrolled students found`, 'info');
        const tuitionFeeId = feeStructureIds[0];
        if (tuitionFeeId) {
          for (let i = 0; i < Math.min(5, students.length); i++) {
            try {
              await feeAPI.recordPayment({ studentId: students[i].id, feeStructureId: tuitionFeeId, amountPaid: 15000, paymentMethod: 'UPI', transactionReference: `UPI-${Date.now()}-${i}`, remarks: 'Term 1 Tuition' });
              addLog(`✅ Fee paid: Student ID ${students[i].id}`, 'success');
            } catch (e) { addLog(`⚠️  Fee payment: ${e.response?.data?.message || e.message}`, 'warn'); }
            await sleep(100);
          }
        }
      } catch (e) { addLog(`⚠️  Fee payments: ${e.message}`, 'warn'); }

      setIds(collected);
      addLog('🎉 All done! Seeding complete.', 'success');
    } catch (err) {
      addLog(`❌ Fatal: ${err.message}`, 'error');
    } finally { setDone(true); setRunning(false); }
  };

  const color = { info: '#94a3b8', success: '#22c55e', warn: '#f59e0b', error: '#ef4444' };

  return (
    <div style={{ padding: '32px', minHeight: '100vh', background: '#0f172a', color: '#f1f5f9', fontFamily: 'Courier New,monospace' }}>
      <h1 style={{ color: '#6366f1', marginBottom: '6px', fontFamily: 'Inter,sans-serif' }}>🌱 Data Seeder</h1>
      <p style={{ color: '#64748b', marginBottom: '24px', fontSize: '13px', fontFamily: 'Inter,sans-serif' }}>
        Inserts: Academic Year, 10 Classes, 8 Subjects, 4 Departments, 6 Teachers, 4 Staff, 15 Students (approved), Fee Structures, Notices.
        Safe to re-run — will skip existing records.
      </p>

      <button onClick={runSeed} disabled={running}
        style={{ padding: '12px 28px', background: running ? '#334155' : 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '15px', fontWeight: 700, cursor: running ? 'not-allowed' : 'pointer', marginBottom: '24px', fontFamily: 'Inter,sans-serif' }}>
        {running ? '⏳ Running...' : done ? '✅ Done (Click to Re-Run)' : '🚀 Start Seeding'}
      </button>

      <div style={{ background: '#1e293b', borderRadius: '12px', border: '1px solid #334155', padding: '16px', maxHeight: '560px', overflowY: 'auto' }}>
        {logs.length === 0 && <p style={{ color: '#475569', margin: 0 }}>Logs appear here when seeding starts...</p>}
        {logs.map((l, i) => (
          <div key={i} style={{ color: color[l.type], fontSize: '12px', lineHeight: '1.9' }}>
            <span style={{ color: '#334155' }}>[{l.ts}]</span> {l.msg}
          </div>
        ))}
      </div>

      {done && (
        <div style={{ marginTop: '20px', padding: '16px', background: 'rgba(34,197,94,0.08)', border: '1px solid rgba(34,197,94,0.25)', borderRadius: '10px' }}>
          <p style={{ color: '#22c55e', fontWeight: 700, margin: '0 0 8px', fontFamily: 'Inter,sans-serif' }}>✅ Seeding Complete!</p>
          <p style={{ color: '#94a3b8', fontSize: '12px', margin: '0 0 8px', fontFamily: 'Inter,sans-serif' }}>Navigate to Students / Teachers / Classes to verify. Refresh the page if numbers seem off.</p>
          <details><summary style={{ color: '#475569', cursor: 'pointer', fontSize: '11px' }}>IDs collected</summary>
            <pre style={{ color: '#64748b', fontSize: '10px', marginTop: '4px' }}>{JSON.stringify(ids, null, 2)}</pre>
          </details>
        </div>
      )}
    </div>
  );
}
