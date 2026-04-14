import React, { useEffect, useState } from 'react';
import { FileText } from 'lucide-react';
import { admissionAPI, academicAPI } from '../services/api';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';
import Modal, { Field, inputStyle, selectStyle } from '../components/Modal';

function formatDate(d) { return d ? new Date(d).toLocaleDateString('en-IN') : '—'; }

export default function AdmissionsPage() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filterStatus, setFilterStatus] = useState('');
  const [saving, setSaving] = useState(false);

  // ── Review modal state ─────────────────────────────────────────────────────
  const [reviewId, setReviewId] = useState(null);
  const [reviewStatus, setReviewStatus] = useState('APPROVED');
  const [reviewRemarks, setReviewRemarks] = useState('');
  const [reviewClassGradeId, setReviewClassGradeId] = useState('');
  const [classes, setClasses] = useState([]);

  // ── Load admissions list ───────────────────────────────────────────────────
  const load = (p = 0, status = filterStatus) => {
    setLoading(true);
    admissionAPI.getAll(p, 10, status)
      .then(r => {
        const pg = r.data?.data;
        setData(pg?.content ?? []);
        setTotalPages(pg?.totalPages ?? 1);
      })
      .catch(() => setData([]))
      .finally(() => setLoading(false));
  };

  // ── Load active-year classes for the Approve dropdown ─────────────────────
  const loadClasses = () => {
    academicAPI.getActiveYear()
      .then(r => {
        const yearId = r.data?.data?.id;
        if (yearId) return academicAPI.getClasses(yearId, 0, 50);
        return null;
      })
      .then(r => { if (r) setClasses(r.data?.data?.content ?? []); })
      .catch(() => setClasses([]));
  };

  useEffect(() => { load(); loadClasses(); }, []);

  // ── Open review modal ─────────────────────────────────────────────────────
  const openReview = (row) => {
    setReviewId(row.id);
    setReviewStatus('APPROVED');
    setReviewRemarks('');
    setReviewClassGradeId('');
  };

  // ── Submit review decision ────────────────────────────────────────────────
  const handleReview = async () => {
    if (!reviewId) return;
    if (reviewStatus === 'APPROVED' && !reviewClassGradeId) {
      alert('Please select the class to assign this student to before approving.');
      return;
    }
    setSaving(true);
    try {
      await admissionAPI.review(reviewId, {
        status: reviewStatus,
        remarks: reviewRemarks,
        classGradeId: reviewStatus === 'APPROVED' ? Number(reviewClassGradeId) : undefined,
      });
      setReviewId(null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to review admission');
    } finally { setSaving(false); }
  };

  // ── Table columns ─────────────────────────────────────────────────────────
  const columns = [
    { key: 'id',               label: 'ID' },
    { key: 'applicationNumber',label: 'App No',        render: r => r?.applicationNumber ?? '—' },
    { key: 'name',             label: 'Name',           render: r => (`${r?.firstName ?? ''} ${r?.lastName ?? ''}`).trim() || '—' },
    { key: 'applicantEmail',   label: 'Email',          render: r => r?.applicantEmail ?? '—' },
    { key: 'applyingForGrade', label: 'Grade Applied',  render: r => r?.applyingForGrade ?? '—' },
    { key: 'parentName',       label: 'Parent',         render: r => r?.parentName ?? '—' },
    { key: 'status',           label: 'Status',         render: r => <Badge status={r?.status ?? 'PENDING'} /> },
    { key: 'submittedAt',      label: 'Applied On',     render: r => formatDate(r?.submittedAt ?? r?.createdAt) },
    {
      key: 'actions', label: 'Actions',
      render: r => {
        if (r?.status === 'PENDING' || r?.status === 'UNDER_REVIEW') {
          return <button style={btnStyle.review} onClick={() => openReview(r)}>Review</button>;
        }
        if (r?.status === 'APPROVED') return <span style={{ color: '#22c55e', fontSize: '11px', fontWeight: 600 }}>✓ Approved</span>;
        if (r?.status === 'REJECTED') return <span style={{ color: '#ef4444', fontSize: '11px', fontWeight: 600 }}>✗ Rejected</span>;
        return null;
      }
    },
  ];

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <PageHeader
        title="Admissions" icon={FileText}
        subtitle="Application management for new student enrollments"
        action="Submit Application (Public Portal)"
        onAction={() => window.open('/apply', '_blank')}
      />

      {/* Filter bar */}
      <div style={styles.filterBar}>
        <span style={styles.filterLabel}>Filter by status:</span>
        {['', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'].map(s => (
          <button key={s}
            style={{ ...styles.filterBtn, ...(filterStatus === s ? styles.filterActive : {}) }}
            onClick={() => { setFilterStatus(s); load(0, s); }}>
            {s || 'ALL'}
          </button>
        ))}
      </div>

      <DataTable
        columns={columns} data={data} loading={loading}
        page={page} totalPages={totalPages}
        onPageChange={p => { setPage(p); load(p); }}
        emptyMessage="No admission applications found."
      />

      {/* ── Review Modal ─────────────────────────────────────────────────── */}
      {reviewId && (
        <Modal title={`Review Admission #${reviewId}`} onClose={() => setReviewId(null)} width="440px">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

            <Field label="Decision">
              <select style={selectStyle} value={reviewStatus} onChange={e => setReviewStatus(e.target.value)}>
                <option value="APPROVED">✅ APPROVE</option>
                <option value="REJECTED">❌ REJECT</option>
                <option value="UNDER_REVIEW">🔍 MARK UNDER REVIEW</option>
              </select>
            </Field>

            {reviewStatus === 'APPROVED' && (
              <Field label="Assign to Class *">
                <select
                  style={selectStyle}
                  value={reviewClassGradeId}
                  onChange={e => setReviewClassGradeId(e.target.value)}
                  required
                >
                  <option value="">— Select Class —</option>
                  {classes.length === 0 && <option disabled>No classes found — create classes in Academic Setup first</option>}
                  {classes.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.gradeName}{c.section ? ` - Section ${c.section}` : ''}
                    </option>
                  ))}
                </select>
              </Field>
            )}

            <Field label="Remarks">
              <textarea
                style={{ ...inputStyle, minHeight: '70px', resize: 'vertical' }}
                placeholder="Optional remarks..."
                value={reviewRemarks}
                onChange={e => setReviewRemarks(e.target.value)}
              />
            </Field>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button style={styles.cancelBtn} onClick={() => setReviewId(null)}>Cancel</button>
              <button
                style={reviewStatus === 'APPROVED' ? styles.submitBtn : styles.rejectBtn}
                onClick={handleReview}
                disabled={saving || (reviewStatus === 'APPROVED' && !reviewClassGradeId)}
              >
                {saving ? 'Saving...' : `Confirm ${reviewStatus}`}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}

const btnStyle = {
  review: {
    padding: '4px 12px', borderRadius: '6px',
    background: 'rgba(99,102,241,0.15)', border: '1px solid rgba(99,102,241,0.4)',
    color: '#818cf8', cursor: 'pointer', fontSize: '11px', fontWeight: '600',
  },
};

const styles = {
  filterBar:   { display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' },
  filterLabel: { fontSize: '13px', color: '#64748b', marginRight: '4px' },
  filterBtn:   { padding: '5px 12px', borderRadius: '6px', background: '#1e293b', border: '1px solid #334155', color: '#94a3b8', cursor: 'pointer', fontSize: '12px', fontWeight: '500' },
  filterActive:{ background: 'rgba(99,102,241,0.15)', borderColor: 'rgba(99,102,241,0.5)', color: '#818cf8' },
  cancelBtn:   { padding: '9px 18px', borderRadius: '8px', border: '1px solid #334155', background: 'none', color: '#94a3b8', cursor: 'pointer' },
  submitBtn:   { padding: '9px 20px', borderRadius: '8px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600' },
  rejectBtn:   { padding: '9px 20px', borderRadius: '8px', background: 'linear-gradient(135deg,#ef4444,#dc2626)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600' },
};
