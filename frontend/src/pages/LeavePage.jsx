import React, { useEffect, useState } from 'react';
import { Calendar } from 'lucide-react';
import { leaveAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';
import Modal, { Field, inputStyle, selectStyle } from '../components/Modal';

function formatDate(d) { return d ? new Date(d).toLocaleDateString('en-IN') : '—'; }

// Backend LeaveStatus enum: PENDING | APPROVED | REJECTED
// Leave types (free text field in backend)
const LEAVE_TYPES = ['SICK', 'CASUAL', 'EARNED', 'MATERNITY', 'PATERNITY', 'EMERGENCY', 'OTHER'];

export default function LeavePage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);

  // Backend ApplyLeaveRequest fields: applicantId, subject, reason, fromDate, toDate
  const [form, setForm] = useState({
    subject: '',
    reason: '',
    fromDate: '',
    toDate: '',
    leaveType: 'SICK',   // we'll use leaveType as the subject
  });

  const load = (p = 0) => {
    setLoading(true);
    // Admin: GET /admin/leave?page=0  |  Others: GET /leave/my/{userId}?page=0
    const call = isAdmin
      ? leaveAPI.getAllLeaves(p)
      : leaveAPI.getMyLeaves(user?.id, p);

    call
      .then(r => {
        const pg = r.data.data;
        setData(pg.content ?? pg ?? []);
        setTotalPages(pg.totalPages ?? 1);
      })
      .catch(() => setData([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  // Admin: PATCH /admin/leave/{id}/review  body: { status, adminRemarks, reviewerUserId }
  const handleReview = async (id, status) => {
    try {
      await leaveAPI.review(id, status, `${status} by admin`, user?.id || null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update leave status');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.fromDate || !form.toDate) { alert('Please select both From and To dates'); return; }
    if (!user?.id) { alert('User ID not found — please logout and login again'); return; }
    setSaving(true);
    try {
      // POST /leave/apply  body: { applicantId, subject, reason, fromDate (YYYY-MM-DD), toDate }
      await leaveAPI.apply({
        applicantId: user.id,
        subject: `${form.leaveType} Leave`,
        reason: form.reason,
        fromDate: form.fromDate,   // YYYY-MM-DD string — spring @DateTimeFormat parses this
        toDate: form.toDate,
      });
      setShowModal(false);
      setForm({ subject: '', reason: '', fromDate: '', toDate: '', leaveType: 'SICK' });
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to apply for leave');
    } finally {
      setSaving(false);
    }
  };

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }));

  const columns = [
    { key: 'id',           label: 'ID' },
    { key: 'applicant',    label: 'Applicant',  render: r => r.applicant ? `${r.applicant.firstName ?? ''} ${r.applicant.lastName ?? ''}`.trim() : r.applicantName ?? '—' },
    { key: 'subject',      label: 'Subject',    render: r => r.subject ?? '—' },
    { key: 'fromDate',     label: 'From',       render: r => formatDate(r.fromDate) },
    { key: 'toDate',       label: 'To',         render: r => formatDate(r.toDate) },
    { key: 'reason',       label: 'Reason',     render: r => (r.reason?.length > 35 ? r.reason.slice(0, 35) + '...' : r.reason) ?? '—' },
    { key: 'leaveStatus',  label: 'Status',     render: r => <Badge status={r.leaveStatus ?? r.status ?? 'PENDING'} /> },
    ...(isAdmin ? [{
      key: 'actions', label: 'Actions',
      render: (r) => {
        const st = r.leaveStatus ?? r.status;
        if (st !== 'PENDING') return <span style={{ color: '#64748b', fontSize: '11px' }}>{st}</span>;
        return (
          <div style={{ display: 'flex', gap: '6px' }}>
            <button style={btnStyle.approve} onClick={() => handleReview(r.id, 'APPROVED')}>✓ Approve</button>
            <button style={btnStyle.reject} onClick={() => handleReview(r.id, 'REJECTED')}>✕ Reject</button>
          </div>
        );
      }
    }] : []),
  ];

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto' }}>
      <PageHeader
        title="Leave Requests" icon={Calendar}
        subtitle={isAdmin ? 'Review and manage all leave applications' : 'Apply and view your leave history'}
        action="Apply for Leave"
        onAction={() => setShowModal(true)}
      />
      <DataTable
        columns={columns} data={data} loading={loading}
        page={page} totalPages={totalPages}
        onPageChange={p => { setPage(p); load(p); }}
        emptyMessage="No leave requests found."
      />

      {showModal && (
        <Modal title="Apply for Leave" onClose={() => setShowModal(false)} width="480px">
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <Field label="Leave Type">
              <select style={selectStyle} value={form.leaveType} onChange={f('leaveType')}>
                {LEAVE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </Field>
            <Field label="From Date *">
              <input style={inputStyle} type="date" value={form.fromDate} onChange={f('fromDate')} required />
            </Field>
            <Field label="To Date *">
              <input style={inputStyle} type="date" value={form.toDate} onChange={f('toDate')} required />
            </Field>
            <Field label="Reason *">
              <textarea
                style={{ ...inputStyle, minHeight: '80px', resize: 'vertical' }}
                placeholder="Reason for leave..."
                value={form.reason} onChange={f('reason')} required
              />
            </Field>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '4px' }}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowModal(false)}>Cancel</button>
              <button type="submit" style={styles.submitBtn} disabled={saving}>
                {saving ? 'Submitting...' : 'Submit Application'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

const btnStyle = {
  approve: { padding: '4px 10px', borderRadius: '6px', background: 'rgba(34,197,94,0.15)', border: '1px solid rgba(34,197,94,0.4)', color: '#22c55e', cursor: 'pointer', fontSize: '11px', fontWeight: '600' },
  reject:  { padding: '4px 10px', borderRadius: '6px', background: 'rgba(239,68,68,0.15)',  border: '1px solid rgba(239,68,68,0.4)',  color: '#ef4444', cursor: 'pointer', fontSize: '11px', fontWeight: '600' },
};

const styles = {
  cancelBtn: { padding: '9px 18px', borderRadius: '8px', border: '1px solid #334155', background: 'none', color: '#94a3b8', cursor: 'pointer' },
  submitBtn: { padding: '9px 20px', borderRadius: '8px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600' },
};
