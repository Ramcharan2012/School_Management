import React, { useState, useEffect } from 'react';
import { Calendar, Plus, Trash2, Clock, X } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
const DAY_SHORT = { MONDAY: 'Mon', TUESDAY: 'Tue', WEDNESDAY: 'Wed', THURSDAY: 'Thu', FRIDAY: 'Fri', SATURDAY: 'Sat' };
const SUBJECT_COLORS = ['#6366f1', '#06b6d4', '#22c55e', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6', '#f97316'];

export default function TimetablePage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const isTeacher = user?.role === 'TEACHER';
  const [classes, setClasses] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [selectedClass, setSelectedClass] = useState(null);
  const [timetable, setTimetable] = useState([]);
  const [showAdd, setShowAdd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ subjectId: '', teacherId: '', dayOfWeek: 'MONDAY', startTime: '08:00', endTime: '09:00', roomNumber: '' });

  useEffect(() => {
    if (isAdmin) {
      loadClasses();
      loadSubjects();
      loadTeachers();
    } else if (isTeacher) {
      // Teachers: load their own schedule using their teacher profile
      loadTeacherSchedule();
    } else {
      // Students/Staff: auto-load their class
      loadStudentClass();
    }
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

  const loadStudentClass = async () => {
    try {
      // Get student profile to find classGradeId
      const r = await api.get('/student/profile');
      const classGradeId = r.data.data?.classGradeId || r.data.data?.classGrade?.id;
      if (classGradeId) {
        const className = r.data.data?.classGrade?.gradeName || 'My Class';
        const section = r.data.data?.classGrade?.section || '';
        const cls = { id: classGradeId, gradeName: className, section };
        setSelectedClass(cls);
        loadTimetable(classGradeId);
      }
    } catch {}
  };

  const loadTeacherSchedule = async () => {
    try {
      const r = await api.get('/teacher/profile');
      const teacherId = r.data.data?.id;
      if (teacherId) {
        const res = await api.get(`/timetable/teacher/${teacherId}`);
        setTimetable(res.data.data || []);
        setSelectedClass({ id: null, gradeName: 'My Schedule', section: '' });
      }
    } catch {}
  };

  const loadSubjects = async () => {
    try {
      const r = await api.get('/admin/academic/subjects', { params: { page: 0, size: 100 } });
      setSubjects(r.data.data?.content || []);
    } catch {}
  };

  const loadTeachers = async () => {
    try {
      const r = await api.get('/admin/teachers', { params: { page: 0, size: 100 } });
      setTeachers(r.data.data?.content || []);
    } catch {}
  };

  const loadTimetable = async (classId) => {
    setLoading(true);
    try {
      const r = await api.get(`/timetable/class/${classId}`);
      setTimetable(r.data.data || []);
    } catch { setTimetable([]); } finally { setLoading(false); }
  };

  const selectClass = (cls) => {
    setSelectedClass(cls);
    loadTimetable(cls.id);
  };

  const addSlot = async (e) => {
    e.preventDefault();
    try {
      await api.post('/admin/timetable', {
        classGradeId: selectedClass.id,
        subjectId: +form.subjectId,
        teacherId: +form.teacherId,
        dayOfWeek: form.dayOfWeek,
        startTime: form.startTime,
        endTime: form.endTime,
        roomNumber: form.roomNumber,
      });
      setShowAdd(false);
      loadTimetable(selectedClass.id);
    } catch (err) { alert(err.response?.data?.message || 'Failed to add slot'); }
  };

  const deleteSlot = async (id) => {
    if (!confirm('Remove this timetable slot?')) return;
    try {
      await api.delete(`/admin/timetable/${id}`);
      loadTimetable(selectedClass.id);
    } catch {}
  };

  // Build color map for subjects
  const subjectColorMap = {};
  subjects.forEach((sub, i) => { subjectColorMap[sub.id] = SUBJECT_COLORS[i % SUBJECT_COLORS.length]; });

  // Group timetable by day
  const byDay = {};
  DAYS.forEach(d => { byDay[d] = []; });
  timetable.forEach(slot => {
    if (byDay[slot.dayOfWeek]) byDay[slot.dayOfWeek].push(slot);
  });
  DAYS.forEach(d => byDay[d].sort((a, b) => a.startTime.localeCompare(b.startTime)));

  return (
    <div style={s.page}>
      {/* Header */}
      <div style={s.header}>
        <div style={s.headerLeft}>
          <div style={s.headerIcon}><Calendar size={20} color="#06b6d4" /></div>
          <div>
            <h1 style={s.title}>Timetable</h1>
            <p style={s.subtitle}>Manage weekly class schedules</p>
          </div>
        </div>
        {selectedClass && isAdmin && (
          <button style={s.primaryBtn} onClick={() => setShowAdd(true)}>
            <Plus size={15} /> Add Slot
          </button>
        )}
      </div>

      <div style={s.layout}>
        {/* Class Selector — only shown to admins */}
        <div style={s.leftPanel}>
          {isAdmin && (
          <div style={s.panelCard}>
            <div style={s.panelTitle}>Select Class</div>
            {classes.map(cls => (
              <button key={cls.id} style={{ ...s.listItem, ...(selectedClass?.id === cls.id ? s.listItemActive : {}) }} onClick={() => selectClass(cls)}>
                <span>{cls.gradeName} - {cls.section}</span>
              </button>
            ))}
          </div>
          )}

          {/* Legend */}
          {subjects.length > 0 && selectedClass && (
            <div style={s.panelCard}>
              <div style={s.panelTitle}>Subjects</div>
              {subjects.map((sub, i) => (
                <div key={sub.id} style={s.legendItem}>
                  <div style={{ width: '10px', height: '10px', borderRadius: '3px', background: SUBJECT_COLORS[i % SUBJECT_COLORS.length], flexShrink: 0 }} />
                  <span style={{ fontSize: '12px', color: '#94a3b8' }}>{sub.name}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Grid */}
        <div>
          {!selectedClass && (
            <div style={s.emptyState}>
              <Calendar size={48} color="#334155" />
              <p style={{ color: '#475569', fontSize: '15px', marginTop: '12px' }}>Select a class to view its timetable</p>
            </div>
          )}

          {selectedClass && loading && (
            <div style={s.emptyState}><p style={{ color: '#64748b' }}>Loading timetable...</p></div>
          )}

          {selectedClass && !loading && (
            <div style={s.grid}>
              {DAYS.map(day => (
                <div key={day} style={s.dayCol}>
                  <div style={s.dayHeader}>{DAY_SHORT[day]}</div>
                  <div style={s.slotsCol}>
                    {byDay[day].length === 0 && (
                      <div style={s.emptyDay}>No classes</div>
                    )}
                    {byDay[day].map(slot => {
                      const color = subjectColorMap[slot.subject?.id] || '#6366f1';
                      return (
                        <div key={slot.id} style={{ ...s.slot, background: `${color}18`, borderColor: `${color}44`, borderLeft: `3px solid ${color}` }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div style={{ ...s.slotSubject, color }}>{slot.subject?.name || '—'}</div>
                            {isAdmin && (
                            <button onClick={() => deleteSlot(slot.id)} style={s.deleteBtn}>
                              <Trash2 size={11} />
                            </button>
                            )}
                          </div>
                          <div style={s.slotTime}>
                            <Clock size={10} />
                            {slot.startTime?.substring(0, 5)} – {slot.endTime?.substring(0, 5)}
                          </div>
                          <div style={s.slotTeacher}>
                            {slot.teacher?.user?.firstName} {slot.teacher?.user?.lastName}
                          </div>
                          {slot.roomNumber && <div style={s.slotRoom}>Room {slot.roomNumber}</div>}
                        </div>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Add Slot Modal */}
      {showAdd && (
        <div style={s.overlay}>
          <div style={s.modal}>
            <div style={s.modalHeader}>
              <h2 style={s.modalTitle}>Add Timetable Slot</h2>
              <button onClick={() => setShowAdd(false)} style={s.closeBtn}><X size={18} /></button>
            </div>
            <form onSubmit={addSlot} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
                <FormField label="Day of Week">
                  <select style={s.input} value={form.dayOfWeek} onChange={e => setForm(f => ({ ...f, dayOfWeek: e.target.value }))}>
                    {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                  </select>
                </FormField>
                <FormField label="Room Number">
                  <input style={s.input} value={form.roomNumber} onChange={e => setForm(f => ({ ...f, roomNumber: e.target.value }))} placeholder="e.g. R-101" />
                </FormField>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
                <FormField label="Start Time">
                  <input style={s.input} type="time" value={form.startTime} onChange={e => setForm(f => ({ ...f, startTime: e.target.value }))} required />
                </FormField>
                <FormField label="End Time">
                  <input style={s.input} type="time" value={form.endTime} onChange={e => setForm(f => ({ ...f, endTime: e.target.value }))} required />
                </FormField>
              </div>
              <FormField label="Subject">
                <select style={s.input} value={form.subjectId} onChange={e => setForm(f => ({ ...f, subjectId: e.target.value }))} required>
                  <option value="">Select Subject</option>
                  {subjects.map(sub => <option key={sub.id} value={sub.id}>{sub.name}</option>)}
                </select>
              </FormField>
              <FormField label="Teacher">
                <select style={s.input} value={form.teacherId} onChange={e => setForm(f => ({ ...f, teacherId: e.target.value }))} required>
                  <option value="">Select Teacher</option>
                  {teachers.map(t => <option key={t.id} value={t.id}>{t.user?.firstName || t.firstName} {t.user?.lastName || t.lastName}</option>)}
                </select>
              </FormField>
              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '4px' }}>
                <button type="button" style={s.cancelBtn} onClick={() => setShowAdd(false)}>Cancel</button>
                <button type="submit" style={s.primaryBtn}>Add Slot</button>
              </div>
            </form>
          </div>
        </div>
      )}
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

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '28px' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '14px' },
  headerIcon: { width: '46px', height: '46px', borderRadius: '12px', background: 'rgba(6,182,212,0.1)', border: '1px solid rgba(6,182,212,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: 0 },
  subtitle: { fontSize: '13px', color: '#64748b', margin: '4px 0 0' },
  layout: { display: 'grid', gridTemplateColumns: '200px 1fr', gap: '20px', alignItems: 'start' },
  leftPanel: { display: 'flex', flexDirection: 'column', gap: '16px' },
  panelCard: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '16px' },
  panelTitle: { fontSize: '11px', fontWeight: '700', color: '#64748b', letterSpacing: '0.5px', textTransform: 'uppercase', marginBottom: '10px' },
  listItem: { display: 'flex', alignItems: 'center', width: '100%', padding: '9px 10px', borderRadius: '8px', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left', fontSize: '13px', color: '#94a3b8', marginBottom: '2px' },
  listItemActive: { background: 'rgba(99,102,241,0.12)', color: '#f1f5f9', fontWeight: '600' },
  legendItem: { display: 'flex', alignItems: 'center', gap: '8px', padding: '5px 0' },
  emptyState: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '300px', background: '#1e293b', borderRadius: '14px', border: '1px solid #334155' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: '12px' },
  dayCol: { background: '#1e293b', borderRadius: '12px', border: '1px solid #334155', overflow: 'hidden' },
  dayHeader: { padding: '10px', textAlign: 'center', fontWeight: '800', fontSize: '12px', color: '#6366f1', background: 'rgba(99,102,241,0.08)', borderBottom: '1px solid #334155' },
  slotsCol: { padding: '8px', display: 'flex', flexDirection: 'column', gap: '6px', minHeight: '120px' },
  emptyDay: { fontSize: '11px', color: '#475569', textAlign: 'center', padding: '16px 4px' },
  slot: { padding: '8px 9px', borderRadius: '8px', border: '1px solid', display: 'flex', flexDirection: 'column', gap: '3px' },
  slotSubject: { fontSize: '12px', fontWeight: '700', lineHeight: 1.2 },
  slotTime: { display: 'flex', alignItems: 'center', gap: '4px', fontSize: '10px', color: '#94a3b8' },
  slotTeacher: { fontSize: '10px', color: '#64748b' },
  slotRoom: { fontSize: '10px', color: '#475569' },
  deleteBtn: { background: 'none', border: 'none', cursor: 'pointer', color: '#475569', padding: '0 0 0 4px', display: 'flex' },
  primaryBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 18px', borderRadius: '10px', background: 'linear-gradient(135deg,#06b6d4,#0891b2)', color: '#fff', fontWeight: '700', fontSize: '13px', border: 'none', cursor: 'pointer' },
  cancelBtn: { padding: '10px 16px', borderRadius: '8px', background: '#334155', color: '#94a3b8', fontWeight: '600', fontSize: '13px', border: 'none', cursor: 'pointer' },
  input: { width: '100%', padding: '10px 12px', borderRadius: '8px', background: '#0f172a', border: '1px solid #334155', color: '#f1f5f9', fontSize: '13px', boxSizing: 'border-box', outline: 'none' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '20px' },
  modal: { background: '#1e293b', borderRadius: '16px', border: '1px solid #334155', padding: '28px', width: '100%', maxWidth: '520px' },
  modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  modalTitle: { color: '#f1f5f9', fontSize: '17px', fontWeight: '700', margin: 0 },
  closeBtn: { background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' },
};
