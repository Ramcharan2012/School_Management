import React, { useState, useEffect } from 'react';
import { BookOpen, Plus, ChevronDown, CheckCircle, Clock, Award, X, Users } from 'lucide-react';
import api from '../services/api';
import { academicAPI } from '../services/api';

const EXAM_TYPES = ['UNIT_TEST', 'MID_TERM', 'FINAL_EXAM', 'QUIZ', 'ASSIGNMENT', 'PRACTICAL'];
const GRADES = [
  { min: 90, label: 'A+', color: '#22c55e' },
  { min: 80, label: 'A',  color: '#4ade80' },
  { min: 70, label: 'B+', color: '#84cc16' },
  { min: 60, label: 'B',  color: '#eab308' },
  { min: 50, label: 'C',  color: '#f59e0b' },
  { min: 35, label: 'D',  color: '#f97316' },
  { min: 0,  label: 'F',  color: '#ef4444' },
];
const getGrade = (obtained, total) => {
  if (!obtained || !total) return { label: '—', color: '#64748b' };
  const pct = (obtained / total) * 100;
  return GRADES.find(g => pct >= g.min) || GRADES[GRADES.length - 1];
};

export default function MarksPage() {
  const [classes, setClasses] = useState([]);
  const [selectedClass, setSelectedClass] = useState(null);
  const [exams, setExams] = useState([]);
  const [selectedExam, setSelectedExam] = useState(null);
  const [examResults, setExamResults] = useState([]);
  const [students, setStudents] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [showCreateExam, setShowCreateExam] = useState(false);
  const [showEnterMarks, setShowEnterMarks] = useState(false);
  const [loading, setLoading] = useState(false);

  // Form states
  const [examForm, setExamForm] = useState({ title: '', examType: 'UNIT_TEST', examDate: '', totalMarks: 100, passingMarks: 35, durationMinutes: 60, description: '', subjectId: '', classGradeId: '' });
  const [marksMap, setMarksMap] = useState({}); // { studentId: { marksObtained, isAbsent, remarks } }

  useEffect(() => {
    loadClasses();
    loadSubjects();
  }, []);

  const loadClasses = async () => {
    try {
      const yr = await api.get('/admin/academic/years/active');
      const yId = yr.data.data?.id;
      if (!yId) return;
      const r = await api.get('/admin/academic/classes', { params: { academicYearId: yId, page: 0, size: 50 } });
      setClasses(r.data.data?.content || []);
    } catch {}
  };

  const loadSubjects = async () => {
    try {
      const r = await api.get('/admin/academic/subjects', { params: { page: 0, size: 100 } });
      setSubjects(r.data.data?.content || []);
    } catch {}
  };

  const loadExams = async (classId) => {
    setLoading(true);
    try {
      const r = await api.get(`/admin/exams/class/${classId}`);
      setExams(r.data.data?.content || []);
    } catch { setExams([]); } finally { setLoading(false); }
  };

  const loadStudents = async (classId) => {
    try {
      const r = await api.get('/admin/students', { params: { page: 0, size: 100 } });
      const all = r.data.data?.content || [];
      // Filter by classGradeId
      setStudents(all.filter(s => s.classGradeId === classId || s.classGrade?.id === classId));
    } catch { setStudents([]); }
  };

  const loadExamResults = async (examId) => {
    try {
      const r = await api.get(`/teacher/marks/exam/${examId}/results`);
      setExamResults(r.data.data || []);
    } catch { setExamResults([]); }
  };

  const selectClass = (cls) => {
    setSelectedClass(cls);
    setSelectedExam(null);
    setExamResults([]);
    setExamForm(f => ({ ...f, classGradeId: cls.id }));
    loadExams(cls.id);
    loadStudents(cls.id);
  };

  const selectExam = (exam) => {
    setSelectedExam(exam);
    loadExamResults(exam.id);
    // Pre-fill marks map
    const map = {};
    students.forEach(s => { map[s.id] = { marksObtained: '', isAbsent: false, remarks: '' }; });
    setMarksMap(map);
  };

  const createExam = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/exams', { ...examForm, totalMarks: +examForm.totalMarks, passingMarks: +examForm.passingMarks, durationMinutes: +examForm.durationMinutes, subjectId: +examForm.subjectId, classGradeId: +examForm.classGradeId });
      setShowCreateExam(false);
      loadExams(selectedClass.id);
    } catch (err) { alert(err.response?.data?.message || 'Failed to create exam'); }
  };

  const submitMarks = async (e) => {
    e.preventDefault();
    let success = 0;
    for (const [studentId, data] of Object.entries(marksMap)) {
      if (!data.isAbsent && data.marksObtained === '') continue;
      try {
        await api.post('/teacher/marks', { studentId: +studentId, examId: selectedExam.id, teacherId: 1, marksObtained: data.isAbsent ? 0 : +data.marksObtained, isAbsent: data.isAbsent, remarks: data.remarks });
        success++;
      } catch {}
    }
    alert(`${success} marks saved!`);
    setShowEnterMarks(false);
    loadExamResults(selectedExam.id);
  };

  const publishExam = async (examId) => {
    try {
      await api.patch(`/admin/exams/${examId}/publish`);
      loadExams(selectedClass.id);
    } catch (err) { alert(err.response?.data?.message || 'Failed to publish'); }
  };

  return (
    <div style={s.page}>
      {/* Header */}
      <div style={s.header}>
        <div style={s.headerLeft}>
          <div style={s.headerIcon}><BookOpen size={20} color="#6366f1" /></div>
          <div>
            <h1 style={s.title}>Marks & Examinations</h1>
            <p style={s.subtitle}>Create exams, enter marks and publish results</p>
          </div>
        </div>
        {selectedClass && (
          <button style={s.primaryBtn} onClick={() => setShowCreateExam(true)}>
            <Plus size={15} /> Create Exam
          </button>
        )}
      </div>

      <div style={s.layout}>
        {/* Left: Class + Exam selector */}
        <div style={s.leftPanel}>
          {/* Class List */}
          <div style={s.panelCard}>
            <div style={s.panelTitle}>Select Class</div>
            {classes.length === 0 && <p style={s.empty}>No classes found</p>}
            {classes.map(cls => (
              <button key={cls.id} style={{ ...s.listItem, ...(selectedClass?.id === cls.id ? s.listItemActive : {}) }} onClick={() => selectClass(cls)}>
                <span style={s.listItemText}>{cls.gradeName} {cls.section}</span>
                <span style={s.pill}>{cls.currentStrength || 0}</span>
              </button>
            ))}
          </div>

          {/* Exam List */}
          {selectedClass && (
            <div style={s.panelCard}>
              <div style={s.panelTitle}>Exams — {selectedClass.gradeName}</div>
              {loading && <p style={s.empty}>Loading...</p>}
              {!loading && exams.length === 0 && <p style={s.empty}>No exams yet</p>}
              {exams.map(exam => (
                <button key={exam.id} style={{ ...s.listItem, ...(selectedExam?.id === exam.id ? s.listItemActive : {}) }} onClick={() => selectExam(exam)}>
                  <div>
                    <div style={s.listItemText}>{exam.title}</div>
                    <div style={s.listItemSub}>{exam.examType} · {exam.totalMarks} marks</div>
                  </div>
                  {exam.isPublished
                    ? <CheckCircle size={14} color="#22c55e" />
                    : <Clock size={14} color="#f59e0b" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Results Panel */}
        <div style={s.rightPanel}>
          {!selectedExam && (
            <div style={s.emptyState}>
              <BookOpen size={48} color="#334155" />
              <p style={{ color: '#475569', fontSize: '15px', marginTop: '12px' }}>Select a class and exam to view results</p>
            </div>
          )}

          {selectedExam && (
            <div style={s.panelCard}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
                <div>
                  <h2 style={{ color: '#f1f5f9', fontSize: '18px', fontWeight: '700', margin: 0 }}>{selectedExam.title}</h2>
                  <p style={{ color: '#64748b', fontSize: '12px', margin: '4px 0 0' }}>{selectedExam.examType} · {selectedExam.totalMarks} marks · Pass: {selectedExam.passingMarks}</p>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button style={s.secondaryBtn} onClick={() => { setShowEnterMarks(true); }}>
                    <Plus size={13} /> Enter Marks
                  </button>
                  {!selectedExam.isPublished && (
                    <button style={s.publishBtn} onClick={() => publishExam(selectedExam.id)}>
                      <Award size={13} /> Publish
                    </button>
                  )}
                  {selectedExam.isPublished && (
                    <span style={s.publishedTag}><CheckCircle size={12} /> Published</span>
                  )}
                </div>
              </div>

              {/* Results Table */}
              {examResults.length === 0 && (
                <div style={{ textAlign: 'center', padding: '40px', color: '#475569' }}>
                  No marks entered yet. Click "Enter Marks" to begin.
                </div>
              )}
              {examResults.length > 0 && (
                <div style={{ overflowX: 'auto' }}>
                  <table style={s.table}>
                    <thead>
                      <tr style={s.thead}>
                        <th style={s.th}>Student</th>
                        <th style={s.th}>Roll No</th>
                        <th style={s.th}>Marks</th>
                        <th style={s.th}>%</th>
                        <th style={s.th}>Grade</th>
                        <th style={s.th}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {examResults.map(m => {
                        const pct = m.isAbsent ? 0 : ((m.marksObtained / selectedExam.totalMarks) * 100).toFixed(1);
                        const grade = m.isAbsent ? { label: 'AB', color: '#64748b' } : getGrade(m.marksObtained, selectedExam.totalMarks);
                        const passed = !m.isAbsent && m.marksObtained >= selectedExam.passingMarks;
                        return (
                          <tr key={m.id} style={s.tr}>
                            <td style={s.td}>{m.student?.user?.firstName} {m.student?.user?.lastName}</td>
                            <td style={s.td}>{m.student?.rollNumber || '—'}</td>
                            <td style={s.td}>{m.isAbsent ? 'Absent' : `${m.marksObtained}/${selectedExam.totalMarks}`}</td>
                            <td style={s.td}>{m.isAbsent ? '—' : `${pct}%`}</td>
                            <td style={s.td}><span style={{ ...s.gradeBadge, background: `${grade.color}20`, color: grade.color }}>{grade.label}</span></td>
                            <td style={s.td}><span style={{ ...s.statusBadge, background: passed ? '#22c55e20' : '#ef444420', color: passed ? '#22c55e' : '#ef4444' }}>{m.isAbsent ? 'Absent' : passed ? 'Pass' : 'Fail'}</span></td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>

                  {/* Summary */}
                  <div style={s.summary}>
                    {(() => {
                      const passed = examResults.filter(m => !m.isAbsent && m.marksObtained >= selectedExam.passingMarks).length;
                      const avg = examResults.filter(m => !m.isAbsent).reduce((a, m) => a + m.marksObtained, 0) / (examResults.filter(m => !m.isAbsent).length || 1);
                      return (
                        <>
                          <StatBox label="Total Students" value={examResults.length} color="#6366f1" />
                          <StatBox label="Passed" value={passed} color="#22c55e" />
                          <StatBox label="Failed" value={examResults.filter(m => !m.isAbsent && m.marksObtained < selectedExam.passingMarks).length} color="#ef4444" />
                          <StatBox label="Absent" value={examResults.filter(m => m.isAbsent).length} color="#f59e0b" />
                          <StatBox label="Class Avg" value={`${avg.toFixed(1)}`} color="#06b6d4" />
                        </>
                      );
                    })()}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Create Exam Modal */}
      {showCreateExam && (
        <Modal title="Create New Exam" onClose={() => setShowCreateExam(false)}>
          <form onSubmit={createExam} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <FormField label="Exam Title"><input style={s.input} value={examForm.title} onChange={e => setExamForm(f => ({ ...f, title: e.target.value }))} required /></FormField>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <FormField label="Exam Type">
                <select style={s.input} value={examForm.examType} onChange={e => setExamForm(f => ({ ...f, examType: e.target.value }))}>
                  {EXAM_TYPES.map(t => <option key={t}>{t}</option>)}
                </select>
              </FormField>
              <FormField label="Subject">
                <select style={s.input} value={examForm.subjectId} onChange={e => setExamForm(f => ({ ...f, subjectId: e.target.value }))} required>
                  <option value="">Select Subject</option>
                  {subjects.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                </select>
              </FormField>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '14px' }}>
              <FormField label="Total Marks"><input style={s.input} type="number" value={examForm.totalMarks} onChange={e => setExamForm(f => ({ ...f, totalMarks: e.target.value }))} /></FormField>
              <FormField label="Passing Marks"><input style={s.input} type="number" value={examForm.passingMarks} onChange={e => setExamForm(f => ({ ...f, passingMarks: e.target.value }))} /></FormField>
              <FormField label="Duration (min)"><input style={s.input} type="number" value={examForm.durationMinutes} onChange={e => setExamForm(f => ({ ...f, durationMinutes: e.target.value }))} /></FormField>
            </div>
            <FormField label="Exam Date"><input style={s.input} type="date" value={examForm.examDate} onChange={e => setExamForm(f => ({ ...f, examDate: e.target.value }))} required /></FormField>
            <FormField label="Description (optional)"><textarea style={{ ...s.input, height: '70px', resize: 'none' }} value={examForm.description} onChange={e => setExamForm(f => ({ ...f, description: e.target.value }))} /></FormField>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '4px' }}>
              <button type="button" style={s.cancelBtn} onClick={() => setShowCreateExam(false)}>Cancel</button>
              <button type="submit" style={s.primaryBtn}>Create Exam</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Enter Marks Modal */}
      {showEnterMarks && selectedExam && (
        <Modal title={`Enter Marks — ${selectedExam.title}`} onClose={() => setShowEnterMarks(false)} wide>
          <form onSubmit={submitMarks}>
            {students.length === 0 && <p style={{ color: '#64748b', textAlign: 'center', padding: '20px' }}>No students in this class.</p>}
            <div style={{ overflowX: 'auto', marginBottom: '16px' }}>
              <table style={s.table}>
                <thead>
                  <tr style={s.thead}>
                    <th style={s.th}>Student</th>
                    <th style={s.th}>Absent</th>
                    <th style={s.th}>Marks (/{selectedExam.totalMarks})</th>
                    <th style={s.th}>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {students.map(st => (
                    <tr key={st.id} style={s.tr}>
                      <td style={s.td}>{st.user?.firstName || st.firstName} {st.user?.lastName || st.lastName}</td>
                      <td style={s.td}>
                        <input type="checkbox" checked={marksMap[st.id]?.isAbsent || false} onChange={e => setMarksMap(m => ({ ...m, [st.id]: { ...m[st.id], isAbsent: e.target.checked, marksObtained: '' } }))} />
                      </td>
                      <td style={s.td}>
                        <input disabled={marksMap[st.id]?.isAbsent} style={{ ...s.input, width: '80px', padding: '6px 8px' }} type="number" min={0} max={selectedExam.totalMarks} value={marksMap[st.id]?.marksObtained || ''} onChange={e => setMarksMap(m => ({ ...m, [st.id]: { ...m[st.id], marksObtained: e.target.value } }))} />
                      </td>
                      <td style={s.td}>
                        <input style={{ ...s.input, width: '150px', padding: '6px 8px' }} value={marksMap[st.id]?.remarks || ''} onChange={e => setMarksMap(m => ({ ...m, [st.id]: { ...m[st.id], remarks: e.target.value } }))} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button type="button" style={s.cancelBtn} onClick={() => setShowEnterMarks(false)}>Cancel</button>
              <button type="submit" style={s.primaryBtn}>Save All Marks</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

function Modal({ title, onClose, children, wide }) {
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' }}>
      <div style={{ background: '#1e293b', borderRadius: '16px', border: '1px solid #334155', padding: '28px', width: '100%', maxWidth: wide ? '850px' : '550px', maxHeight: '90vh', overflowY: 'auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ color: '#f1f5f9', fontSize: '17px', fontWeight: '700', margin: 0 }}>{title}</h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}><X size={18} /></button>
        </div>
        {children}
      </div>
    </div>
  );
}

function FormField({ label, children }) {
  return (
    <div>
      <label style={{ display: 'block', fontSize: '12px', fontWeight: '600', color: '#94a3b8', marginBottom: '6px' }}>{label}</label>
      {children}
    </div>
  );
}

function StatBox({ label, value, color }) {
  return (
    <div style={{ textAlign: 'center', padding: '12px', background: `${color}10`, borderRadius: '10px', border: `1px solid ${color}30` }}>
      <div style={{ fontSize: '22px', fontWeight: '800', color }}>{value}</div>
      <div style={{ fontSize: '11px', color: '#64748b', marginTop: '2px' }}>{label}</div>
    </div>
  );
}

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '28px' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '14px' },
  headerIcon: { width: '46px', height: '46px', borderRadius: '12px', background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: 0 },
  subtitle: { fontSize: '13px', color: '#64748b', margin: '4px 0 0' },
  layout: { display: 'grid', gridTemplateColumns: '240px 1fr', gap: '20px', alignItems: 'start' },
  leftPanel: { display: 'flex', flexDirection: 'column', gap: '16px' },
  rightPanel: { minHeight: '400px' },
  panelCard: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '16px' },
  panelTitle: { fontSize: '12px', fontWeight: '700', color: '#64748b', letterSpacing: '0.5px', textTransform: 'uppercase', marginBottom: '12px' },
  listItem: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', padding: '10px 10px', borderRadius: '8px', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left', marginBottom: '2px' },
  listItemActive: { background: 'rgba(99,102,241,0.15)', border: '1px solid rgba(99,102,241,0.3)' },
  listItemText: { fontSize: '13px', fontWeight: '500', color: '#cbd5e1' },
  listItemSub: { fontSize: '11px', color: '#64748b', marginTop: '2px' },
  pill: { fontSize: '10px', fontWeight: '700', background: '#334155', color: '#94a3b8', padding: '2px 8px', borderRadius: '10px' },
  emptyState: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '300px', background: '#1e293b', borderRadius: '14px', border: '1px solid #334155' },
  empty: { color: '#475569', fontSize: '13px', textAlign: 'center', padding: '8px 0' },
  table: { width: '100%', borderCollapse: 'collapse' },
  thead: { background: '#0f172a' },
  th: { padding: '10px 14px', textAlign: 'left', fontSize: '11px', fontWeight: '700', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px' },
  tr: { borderBottom: '1px solid #1e293b' },
  td: { padding: '10px 14px', fontSize: '13px', color: '#cbd5e1' },
  gradeBadge: { padding: '2px 10px', borderRadius: '20px', fontWeight: '700', fontSize: '12px' },
  statusBadge: { padding: '3px 10px', borderRadius: '20px', fontWeight: '600', fontSize: '11px' },
  summary: { display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '10px', marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #334155' },
  primaryBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 18px', borderRadius: '10px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', fontWeight: '700', fontSize: '13px', border: 'none', cursor: 'pointer' },
  secondaryBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 14px', borderRadius: '8px', background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.3)', color: '#6366f1', fontWeight: '600', fontSize: '12px', cursor: 'pointer' },
  publishBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 14px', borderRadius: '8px', background: 'rgba(34,197,94,0.1)', border: '1px solid rgba(34,197,94,0.3)', color: '#22c55e', fontWeight: '600', fontSize: '12px', cursor: 'pointer' },
  publishedTag: { display: 'flex', alignItems: 'center', gap: '5px', padding: '8px 14px', borderRadius: '8px', background: 'rgba(34,197,94,0.08)', color: '#22c55e', fontSize: '12px', fontWeight: '600' },
  cancelBtn: { padding: '10px 16px', borderRadius: '8px', background: '#334155', color: '#94a3b8', fontWeight: '600', fontSize: '13px', border: 'none', cursor: 'pointer' },
  input: { width: '100%', padding: '10px 12px', borderRadius: '8px', background: '#0f172a', border: '1px solid #334155', color: '#f1f5f9', fontSize: '13px', boxSizing: 'border-box', outline: 'none' },
};
