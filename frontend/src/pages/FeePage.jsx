import React, { useEffect, useState } from 'react';
import { CreditCard } from 'lucide-react';
import { feeAPI, academicAPI, studentAPI } from '../services/api';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';
import Modal, { Field, inputStyle, selectStyle } from '../components/Modal';

function formatMoney(n) { return n != null ? `₹${Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '—'; }
function formatDate(d) { return d ? new Date(d).toLocaleDateString('en-IN') : '—'; }

// Backend PaymentMethod enum (check your enum, typical values):
const PAYMENT_METHODS = ['CASH', 'ONLINE', 'CHEQUE', 'DEMAND_DRAFT', 'UPI'];

// Backend FeeType enum (typical values):
const FEE_TYPES = ['TUITION', 'EXAMINATION', 'LIBRARY', 'SPORTS', 'TRANSPORT', 'HOSTEL', 'OTHER'];

export default function FeePage() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeYear, setActiveYear] = useState(null);
  const [structures, setStructures] = useState([]);

  // Modal state
  const [showPayModal, setShowPayModal] = useState(false);
  const [showStructModal, setShowStructModal] = useState(false);
  const [saving, setSaving] = useState(false);

  // Record payment form — matches backend FeePaymentRequest:
  // { studentId, feeStructureId, amountPaid, paymentMethod, transactionReference, remarks }
  const [payForm, setPayForm] = useState({
    studentId: '', feeStructureId: '', amountPaid: '',
    paymentMethod: 'CASH', transactionReference: '', remarks: '',
  });

  // Create structure form — matches backend FeeStructureRequest:
  // { feeType, amount, dueDate, description, isMandatory, academicYearId, classGradeId }
  const [structForm, setStructForm] = useState({
    feeType: 'TUITION', amount: '', dueDate: '',
    description: '', isMandatory: true, classGradeId: '',
  });

  const loadStructures = (yearId) => {
    if (!yearId) return;
    feeAPI.getStructures(yearId)
      .then(r => setStructures(r.data.data ?? []))
      .catch(() => {});
  };

  // Load a specific student's payments when studentId is entered
  const [viewStudentId, setViewStudentId] = useState('');
  const loadPayments = (sid) => {
    if (!sid) { setPayments([]); setLoading(false); return; }
    setLoading(true);
    feeAPI.getStudentPayments(sid)
      .then(r => {
        const pg = r.data.data;
        setPayments(pg.content ?? pg ?? []);
      })
      .catch(() => setPayments([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    academicAPI.getActiveYear()
      .then(r => {
        const yr = r.data.data;
        setActiveYear(yr);
        loadStructures(yr?.id);
      })
      .catch(() => {});
  }, []);

  const handleRecordPayment = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await feeAPI.recordPayment({
        studentId: Number(payForm.studentId),
        feeStructureId: Number(payForm.feeStructureId),
        amountPaid: Number(payForm.amountPaid),
        paymentMethod: payForm.paymentMethod,
        transactionReference: payForm.transactionReference || null,
        remarks: payForm.remarks || null,
      });
      setShowPayModal(false);
      setPayForm({ studentId: '', feeStructureId: '', amountPaid: '', paymentMethod: 'CASH', transactionReference: '', remarks: '' });
      if (payForm.studentId) loadPayments(payForm.studentId);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to record payment');
    } finally { setSaving(false); }
  };

  const handleCreateStructure = async (e) => {
    e.preventDefault();
    if (!activeYear) { alert('No active academic year. Set one at /admin/academic/years'); return; }
    setSaving(true);
    try {
      await feeAPI.createStructure({
        feeType: structForm.feeType,
        amount: Number(structForm.amount),
        dueDate: structForm.dueDate,
        description: structForm.description || null,
        isMandatory: structForm.isMandatory,
        academicYearId: activeYear.id,
        classGradeId: structForm.classGradeId ? Number(structForm.classGradeId) : null,
      });
      setShowStructModal(false);
      setStructForm({ feeType: 'TUITION', amount: '', dueDate: '', description: '', isMandatory: true, classGradeId: '' });
      loadStructures(activeYear.id);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create fee structure');
    } finally { setSaving(false); }
  };

  const pf = (k) => (e) => setPayForm(p => ({ ...p, [k]: e.target.value }));
  const sf = (k) => (e) => setStructForm(p => ({ ...p, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }));

  const columns = [
    { key: 'id',          label: 'ID' },
    { key: 'receiptNumber', label: 'Receipt No', render: r => r.receiptNumber ?? '—' },
    { key: 'feeStructure', label: 'Fee Type',  render: r => r.feeStructure?.feeType ?? '—' },
    { key: 'amountPaid',  label: 'Paid',       render: r => formatMoney(r.amountPaid) },
    { key: 'amountDue',   label: 'Due',        render: r => formatMoney(r.amountDue) },
    { key: 'paymentMethod', label: 'Mode',     render: r => r.paymentMethod ?? '—' },
    { key: 'paymentDate', label: 'Date',       render: r => formatDate(r.paymentDate) },
    { key: 'paymentStatus', label: 'Status',   render: r => <Badge status={r.paymentStatus ?? 'UNPAID'} /> },
  ];

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <PageHeader
        title="Fee Management" icon={CreditCard}
        subtitle={`Active Year: ${activeYear?.yearLabel ?? 'None set'}`}
      />

      {/* Top action bar */}
      <div style={styles.actionBar}>
        <button style={styles.btnGreen} onClick={() => setShowPayModal(true)}>+ Record Payment</button>
        <button style={styles.btnIndigo} onClick={() => setShowStructModal(true)}>+ Fee Structure</button>
      </div>

      {/* Fee Structures Section */}
      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>Fee Structures — {activeYear?.yearLabel}</h2>
        <div style={styles.structGrid}>
          {structures.length === 0 ? (
            <div style={styles.emptyMsg}>No fee structures defined for this year.</div>
          ) : structures.map(s => (
            <div key={s.id} style={styles.structCard}>
              <div style={styles.structType}>{s.feeType}</div>
              <div style={styles.structAmount}>{formatMoney(s.amount)}</div>
              <div style={styles.structMeta}>Due: {formatDate(s.dueDate)}</div>
              {s.description && <div style={styles.structDesc}>{s.description}</div>}
              {s.isMandatory && <span style={styles.mandatoryBadge}>Mandatory</span>}
            </div>
          ))}
        </div>
      </div>

      {/* Payment History — look up by student */}
      <div style={styles.section}>
        <div style={styles.lookupRow}>
          <h2 style={styles.sectionTitle}>Payment History</h2>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <input
              style={{ ...inputStyle, width: '160px', padding: '8px 12px' }}
              type="number"
              placeholder="Student ID"
              value={viewStudentId}
              onChange={e => setViewStudentId(e.target.value)}
            />
            <button style={styles.searchBtn} onClick={() => loadPayments(viewStudentId)}>
              Search
            </button>
          </div>
        </div>
        <DataTable
          columns={columns} data={payments} loading={loading}
          emptyMessage={viewStudentId ? 'No payments found for this student.' : 'Enter a Student ID and click Search to view payments.'}
        />
      </div>

      {/* Record Payment Modal */}
      {showPayModal && (
        <Modal title="Record Fee Payment" onClose={() => setShowPayModal(false)} width="480px">
          <form onSubmit={handleRecordPayment} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <Field label="Student ID *">
              <input style={inputStyle} type="number" placeholder="Enter student numeric ID from DB" value={payForm.studentId} onChange={pf('studentId')} required />
            </Field>
            <Field label="Fee Structure *">
              <select style={selectStyle} value={payForm.feeStructureId} onChange={pf('feeStructureId')} required>
                <option value="">Select fee structure</option>
                {structures.map(s => (
                  <option key={s.id} value={s.id}>{s.feeType} — {formatMoney(s.amount)} (due {formatDate(s.dueDate)})</option>
                ))}
              </select>
            </Field>
            <Field label="Amount Paid (₹) *">
              <input style={inputStyle} type="number" step="0.01" placeholder="0.00" value={payForm.amountPaid} onChange={pf('amountPaid')} required />
            </Field>
            <Field label="Payment Method">
              <select style={selectStyle} value={payForm.paymentMethod} onChange={pf('paymentMethod')}>
                {PAYMENT_METHODS.map(m => <option key={m}>{m}</option>)}
              </select>
            </Field>
            <Field label="Transaction Reference">
              <input style={inputStyle} placeholder="UTR / Cheque no (optional)" value={payForm.transactionReference} onChange={pf('transactionReference')} />
            </Field>
            <Field label="Remarks">
              <input style={inputStyle} placeholder="Optional note" value={payForm.remarks} onChange={pf('remarks')} />
            </Field>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowPayModal(false)}>Cancel</button>
              <button type="submit" style={styles.btnGreen} disabled={saving}>{saving ? 'Recording...' : 'Record Payment'}</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Create Fee Structure Modal */}
      {showStructModal && (
        <Modal title="Create Fee Structure" onClose={() => setShowStructModal(false)} width="460px">
          <form onSubmit={handleCreateStructure} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <Field label="Fee Type *">
              <select style={selectStyle} value={structForm.feeType} onChange={sf('feeType')}>
                {FEE_TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </Field>
            <Field label="Amount (₹) *">
              <input style={inputStyle} type="number" step="0.01" placeholder="0.00" value={structForm.amount} onChange={sf('amount')} required />
            </Field>
            <Field label="Due Date *">
              <input style={inputStyle} type="date" value={structForm.dueDate} onChange={sf('dueDate')} required />
            </Field>
            <Field label="Description">
              <input style={inputStyle} placeholder="Optional description" value={structForm.description} onChange={sf('description')} />
            </Field>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', fontSize: '13px' }}>
              <input type="checkbox" checked={structForm.isMandatory} onChange={sf('isMandatory')} />
              Mandatory fee
            </label>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowStructModal(false)}>Cancel</button>
              <button type="submit" style={styles.btnIndigo} disabled={saving}>{saving ? 'Creating...' : 'Create Structure'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

const styles = {
  actionBar: { display: 'flex', gap: '10px' },
  btnGreen: { padding: '10px 18px', borderRadius: '8px', background: 'linear-gradient(135deg,#22c55e,#16a34a)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600', fontSize: '13px' },
  btnIndigo: { padding: '10px 18px', borderRadius: '8px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600', fontSize: '13px' },
  section: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '20px' },
  sectionTitle: { fontSize: '14px', fontWeight: '700', color: '#f1f5f9', marginBottom: '16px' },
  structGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '12px' },
  structCard: { background: '#0f172a', borderRadius: '10px', border: '1px solid #334155', padding: '14px', display: 'flex', flexDirection: 'column', gap: '4px' },
  structType: { fontSize: '11px', fontWeight: '700', color: '#6366f1', textTransform: 'uppercase', letterSpacing: '1px' },
  structAmount: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9' },
  structMeta: { fontSize: '11px', color: '#64748b' },
  structDesc: { fontSize: '12px', color: '#94a3b8', marginTop: '4px' },
  mandatoryBadge: { fontSize: '10px', background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.3)', color: '#ef4444', padding: '2px 8px', borderRadius: '20px', alignSelf: 'flex-start', marginTop: '6px' },
  emptyMsg: { color: '#64748b', fontSize: '13px', padding: '20px 0' },
  lookupRow: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' },
  searchBtn: { padding: '8px 14px', borderRadius: '8px', background: '#334155', border: 'none', color: '#f1f5f9', cursor: 'pointer', fontSize: '13px' },
  cancelBtn: { padding: '9px 18px', borderRadius: '8px', border: '1px solid #334155', background: 'none', color: '#94a3b8', cursor: 'pointer' },
};
