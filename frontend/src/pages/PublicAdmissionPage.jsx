import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { School, CheckCircle, ArrowLeft } from 'lucide-react';
import { admissionAPI, academicAPI } from '../services/api';

const GENDERS = ['MALE', 'FEMALE', 'OTHER'];
const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-'];

export default function PublicAdmissionPage() {
  const navigate = useNavigate();
  const [successAppNo, setSuccessAppNo] = useState(null);
  const [loading, setLoading] = useState(false);
  
  // Data for dropdowns
  const [activeYear, setActiveYear] = useState(null);
  const [classes, setClasses] = useState([]);

  const [form, setForm] = useState({
    firstName: '', lastName: '', applicantEmail: '', phoneNumber: '',
    dateOfBirth: '', gender: 'MALE', address: '', bloodGroup: 'O+',
    parentName: '', parentPhone: '', parentEmail: '', parentOccupation: '',
    applyingForGrade: '', academicYear: '', previousSchool: '',
    previousClassCompleted: '', previousPercentage: '', 
    admissionFee: 0, tuitionFeePerMonth: 0, otherCharges: 0, feeConcessionRequested: false
  });

  useEffect(() => {
    academicAPI.getActiveYear().then(r => {
      const yr = r.data.data;
      if (yr) {
        setActiveYear(yr);
        setForm(p => ({ ...p, academicYear: yr.yearLabel }));
        academicAPI.getClasses(yr.id).then(cr => {
          setClasses(cr.data.data?.content || cr.data.data || []);
        });
      }
    }).catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const req = {
        ...form,
        applyingForGrade: form.applyingForGrade,
        previousPercentage: form.previousPercentage ? Number(form.previousPercentage) : null,
      };
      const res = await admissionAPI.apply(req);
      setSuccessAppNo(res.data.data.applicationNumber);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit application. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));

  if (successAppNo) {
    return (
      <div style={styles.container}>
        <div style={styles.successCard}>
          <CheckCircle size={64} color="#10b981" style={{ marginBottom: '20px' }} />
          <h2 style={{ color: '#f1f5f9', fontSize: '24px', marginBottom: '10px' }}>Application Submitted!</h2>
          <p style={{ color: '#94a3b8', fontSize: '15px', marginBottom: '20px' }}>
            We have received your admission request.
          </p>
          <div style={{ background: '#0f172a', padding: '20px', borderRadius: '12px', border: '1px solid #334155', marginBottom: '30px' }}>
            <span style={{ display: 'block', color: '#64748b', fontSize: '13px', marginBottom: '8px' }}>Your Reference Number</span>
            <span style={{ fontSize: '28px', fontWeight: '800', color: '#6366f1', letterSpacing: '2px' }}>{successAppNo}</span>
          </div>
          <p style={{ color: '#94a3b8', fontSize: '14px', marginBottom: '30px' }}>
            Please keep this number safe. You may be contacted by the school administration for document verification.
          </p>
          <button onClick={() => navigate('/login')} style={styles.btnPrimary}>Return to Login</button>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <button onClick={() => navigate('/login')} style={styles.backBtn}><ArrowLeft size={16} /> Back</button>
        <div style={styles.logoRow}>
          <School size={36} color="#6366f1" />
          <h1 style={{ color: '#f1f5f9', fontSize: '22px' }}>School Admission Portal</h1>
        </div>
      </div>

      <div style={styles.formContainer}>
        <h2 style={styles.sectionTitle}>New Admission Application</h2>
        {activeYear ? (
          <p style={styles.subtitle}>Applying for Academic Year: <strong style={{ color: '#6366f1' }}>{activeYear.yearLabel}</strong></p>
        ) : (
          <p style={{ color: '#ef4444', fontSize: '13px', marginBottom: '20px' }}>Error: No Active Academic Year set by admin. Cannot proceed.</p>
        )}

        <form onSubmit={handleSubmit} style={styles.grid}>
          {/* Student Info */}
          <h3 style={styles.groupTitle}>Student Personal Details</h3>
          <Field label="First Name *"><input style={styles.input} value={form.firstName} onChange={f('firstName')} required /></Field>
          <Field label="Last Name *"><input style={styles.input} value={form.lastName} onChange={f('lastName')} required /></Field>
          <Field label="Student Email *"><input style={styles.input} type="email" value={form.applicantEmail} onChange={f('applicantEmail')} required /></Field>
          <Field label="Phone"><input style={styles.input} value={form.phoneNumber} onChange={f('phoneNumber')} /></Field>
          <Field label="Date of Birth"><input style={styles.input} type="date" value={form.dateOfBirth} onChange={f('dateOfBirth')} /></Field>
          <Field label="Gender">
            <select style={styles.input} value={form.gender} onChange={f('gender')}>
              {GENDERS.map(g => <option key={g}>{g}</option>)}
            </select>
          </Field>
          <Field label="Blood Group">
            <select style={styles.input} value={form.bloodGroup} onChange={f('bloodGroup')}>
              {BLOOD_GROUPS.map(g => <option key={g}>{g}</option>)}
            </select>
          </Field>
          <div style={{ gridColumn: '1 / -1' }}>
            <Field label="Address"><input style={styles.input} value={form.address} onChange={f('address')} /></Field>
          </div>

          {/* Academic Info */}
          <h3 style={styles.groupTitle}>Academic Details</h3>
          <Field label="Class Applying For *">
            <select style={styles.input} value={form.applyingForGrade} onChange={f('applyingForGrade')} required>
              <option value="">Select Class</option>
              {classes.map(c => <option key={c.id} value={c.gradeName}>{c.gradeName}</option>)}
            </select>
          </Field>
          <Field label="Previous School"><input style={styles.input} value={form.previousSchool} onChange={f('previousSchool')} /></Field>
          <Field label="Previous Class Completed"><input style={styles.input} value={form.previousClassCompleted} onChange={f('previousClassCompleted')} /></Field>
          <Field label="Previous Score (%)"><input style={styles.input} type="number" step="0.01" value={form.previousPercentage} onChange={f('previousPercentage')} /></Field>

          {/* Parent Info */}
          <h3 style={styles.groupTitle}>Parent / Guardian Details</h3>
          <Field label="Parent Name *"><input style={styles.input} value={form.parentName} onChange={f('parentName')} required /></Field>
          <Field label="Parent Phone *"><input style={styles.input} value={form.parentPhone} onChange={f('parentPhone')} required /></Field>
          <Field label="Parent Email"><input style={styles.input} type="email" value={form.parentEmail} onChange={f('parentEmail')} /></Field>
          <Field label="Parent Occupation"><input style={styles.input} value={form.parentOccupation} onChange={f('parentOccupation')} /></Field>

          {/* Fees */}
          <h3 style={styles.groupTitle}>Financial</h3>
          <div style={{ gridColumn: '1 / -1', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <input type="checkbox" checked={form.feeConcessionRequested} onChange={f('feeConcessionRequested')} id="concess" />
            <label htmlFor="concess" style={{ color: '#94a3b8', fontSize: '14px', cursor: 'pointer' }}>Request Fee Concession (Provide documents to admin later)</label>
          </div>

          <div style={styles.footer}>
            <button type="submit" style={styles.submitBtn} disabled={loading || !activeYear}>
              {loading ? 'Submitting...' : 'Submit Application'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({ label, children }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <label style={{ fontSize: '13px', color: '#94a3b8', fontWeight: '500' }}>{label}</label>
      {children}
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', background: '#0f172a', padding: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center' },
  header: { width: '100%', maxWidth: '800px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
  backBtn: { display: 'flex', alignItems: 'center', gap: '6px', background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '14px', padding: '8px' },
  logoRow: { display: 'flex', alignItems: 'center', gap: '12px' },
  formContainer: { width: '100%', maxWidth: '800px', background: '#1e293b', borderRadius: '16px', border: '1px solid #334155', padding: '40px' },
  sectionTitle: { color: '#f1f5f9', fontSize: '20px', fontWeight: '700', marginBottom: '8px' },
  subtitle: { color: '#94a3b8', fontSize: '14px', marginBottom: '30px' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '20px' },
  groupTitle: { gridColumn: '1 / -1', color: '#818cf8', fontSize: '15px', fontWeight: '600', marginTop: '20px', borderBottom: '1px solid #334155', paddingBottom: '8px' },
  input: { padding: '12px 14px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', fontSize: '14px', outline: 'none', width: '100%', boxSizing: 'border-box' },
  footer: { gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #334155' },
  submitBtn: { padding: '12px 24px', background: 'linear-gradient(135deg, #6366f1, #4f46e5)', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '15px', fontWeight: '600', cursor: 'pointer' },
  successCard: { background: '#1e293b', borderRadius: '16px', border: '1px solid #334155', padding: '50px', textAlign: 'center', maxWidth: '500px', width: '100%' },
  btnPrimary: { padding: '12px 24px', background: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '15px', fontWeight: '600', cursor: 'pointer', width: '100%' }
};
