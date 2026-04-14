import React, { useEffect, useState } from 'react';
import { CheckSquare } from 'lucide-react';
import { attendanceAPI, academicAPI, studentAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import PageHeader from '../components/PageHeader';

const STATUS_OPTS = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'];
const statusColor = { PRESENT: '#22c55e', ABSENT: '#ef4444', LATE: '#f59e0b', EXCUSED: '#818cf8', UNMARKED: '#475569' };

export default function AttendancePage() {
  const { user } = useAuth();
  const [subjects, setSubjects] = useState([]);
  const [subjectId, setSubjectId] = useState('');
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  
  // Data
  const [students, setStudents] = useState([]);
  const [attendanceMap, setAttendanceMap] = useState({}); // studentId -> { status, remarks, id (attendanceId) }
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    academicAPI.getSubjects().then(r => {
      const list = r.data.data?.content || r.data.data || [];
      setSubjects(list);
      if (list.length > 0) setSubjectId(String(list[0].id));
    }).catch(()=>{});

    // Load students
    studentAPI.getAll(0, 200).then(r => {
      setStudents(r.data.data?.content || []);
    }).catch(()=>{});
  }, []);

  useEffect(() => {
    if (!subjectId) return;
    loadExisting();
  }, [subjectId, date]);

  const loadExisting = () => {
    setLoading(true);
    attendanceAPI.getBySubjectAndDate(subjectId, date)
      .then(r => {
        const records = r.data.data || [];
        const map = {};
        records.forEach(rc => {
          map[rc.studentId || rc.student?.id] = { status: rc.status, id: rc.id, remarks: rc.remarks };
        });
        setAttendanceMap(map);
      })
      .catch(() => setAttendanceMap({}))
      .finally(() => setLoading(false));
  };

  const markStudent = async (studentId, status) => {
    const existing = attendanceMap[studentId];
    try {
      if (existing?.id) {
        // UPDATE existing
        await attendanceAPI.update(existing.id, { status, remarks: existing.remarks });
      } else {
        // CREATE new
        await attendanceAPI.mark({
          studentId: Number(studentId),
          subjectId: Number(subjectId),
          teacherId: user?.id || null,
          date: date,
          status: status
        });
      }
      // Re-fetch to guarantee sync, or optimistically update
      setAttendanceMap(prev => ({ ...prev, [studentId]: { ...prev[studentId], status } }));
    } catch (e) {
      alert(e.response?.data?.message || 'Failed to update attendance');
    }
  };

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <PageHeader title="Smart Attendance" icon={CheckSquare} subtitle="Click-to-mark bulk attendance mapping" />

      {/* Controls */}
      <div style={styles.controls}>
        <div style={styles.controlGroup}>
          <label style={styles.label}>Subject</label>
          <select style={styles.sel} value={subjectId} onChange={e => setSubjectId(e.target.value)}>
            <option value="">Select subject</option>
            {subjects.map(s => <option key={s.id} value={s.id}>{s.name} ({s.code})</option>)}
          </select>
        </div>
        <div style={styles.controlGroup}>
          <label style={styles.label}>Date</label>
          <input style={styles.sel} type="date" value={date} onChange={e => setDate(e.target.value)} />
        </div>
        <div style={{ ...styles.controlGroup, marginLeft: 'auto', alignItems: 'flex-end' }}>
          <label style={styles.label}>Marked / Total</label>
          <div style={styles.countBadge}>{Object.keys(attendanceMap).length} / {students.length}</div>
        </div>
      </div>

      {/* Grid */}
      <div style={styles.gridContainer}>
        {loading ? (
          <p style={{ color: '#94a3b8' }}>Loading records...</p>
        ) : students.length === 0 ? (
          <p style={{ color: '#94a3b8' }}>No students found.</p>
        ) : (
          <div style={styles.list}>
            {students.map(st => {
              const currentStatus = attendanceMap[st.id]?.status;
              return (
                <div key={st.id} style={styles.row}>
                  <div style={styles.studentInfo}>
                    <div style={styles.avatar}>S{st.id}</div>
                    <div>
                      <div style={styles.name}>{st.firstName} {st.lastName}</div>
                      <div style={styles.grade}>{st.currentClassGrade?.gradeName || 'Enrollment Pending'}</div>
                    </div>
                  </div>
                  <div style={styles.actions}>
                    {STATUS_OPTS.map(opt => {
                      const isActive = currentStatus === opt;
                      return (
                        <button
                          key={opt}
                          onClick={() => markStudent(st.id, opt)}
                          style={{
                            ...styles.btn,
                            background: isActive ? `${statusColor[opt]}22` : '#0f172a',
                            borderColor: isActive ? statusColor[opt] : '#334155',
                            color: isActive ? statusColor[opt] : '#64748b'
                          }}
                        >
                          {opt}
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  controls: { display: 'flex', gap: '16px', alignItems: 'center', flexWrap: 'wrap', background: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' },
  controlGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '12px', fontWeight: '500', color: '#94a3b8' },
  sel: { padding: '9px 14px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', fontSize: '14px', outline: 'none', cursor: 'pointer' },
  countBadge: { fontSize: '24px', fontWeight: '800', color: '#6366f1' },
  gridContainer: { background: '#1e293b', padding: '20px', borderRadius: '14px', border: '1px solid #334155' },
  list: { display: 'flex', flexDirection: 'column', gap: '8px' },
  row: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px', background: '#0f172a', borderRadius: '10px', border: '1px solid #334155' },
  studentInfo: { display: 'flex', alignItems: 'center', gap: '12px' },
  avatar: { width: '40px', height: '40px', borderRadius: '50%', background: 'linear-gradient(135deg,#6366f1,#06b6d4)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px', fontWeight: '700', color: '#fff' },
  name: { fontSize: '15px', fontWeight: '600', color: '#f1f5f9' },
  grade: { fontSize: '12px', color: '#94a3b8' },
  actions: { display: 'flex', gap: '6px' },
  btn: { padding: '8px 16px', borderRadius: '8px', border: '1px solid', cursor: 'pointer', fontSize: '12px', fontWeight: '600', transition: 'all 0.15s' }
};
