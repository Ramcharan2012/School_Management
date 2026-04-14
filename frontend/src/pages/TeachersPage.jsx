import React, { useEffect, useState } from 'react';
import { GraduationCap } from 'lucide-react';
import { teacherAPI, academicAPI } from '../services/api';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';
import Modal, { Field, inputStyle, selectStyle } from '../components/Modal';

export default function TeachersPage() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [activeYearId, setActiveYearId] = useState(null);

  // Match EXACTLY the backend CreateTeacherRequest fields:
  // firstName, lastName, email, phoneNumber,
  // qualification, designation, specialization, experienceYears, departmentId
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phoneNumber: '',
    qualification: '', designation: '', specialization: '',
    experienceYears: '', departmentId: '',
  });

  const load = (p = 0, search = '') => {
    setLoading(true);
    teacherAPI.getAll(p, 10, search)
      .then(res => {
        const pg = res.data.data;
        setData(pg.content ?? []);
        setTotalPages(pg.totalPages ?? 1);
      })
      .catch(() => setData([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    academicAPI.getDepartments()
      .then(r => setDepartments(r.data.data ?? []))
      .catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await teacherAPI.create({
        ...form,
        experienceYears: form.experienceYears ? Number(form.experienceYears) : null,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
      });
      setShowModal(false);
      setForm({ firstName: '', lastName: '', email: '', phoneNumber: '', qualification: '', designation: '', specialization: '', experienceYears: '', departmentId: '' });
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create teacher. Make sure email is unique.');
    } finally {
      setSaving(false);
    }
  };

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }));

  const columns = [
    { key: 'id',            label: 'ID' },
    { key: 'name',          label: 'Name',          render: r => `${r.firstName ?? ''} ${r.lastName ?? ''}`.trim() || '—' },
    { key: 'email',         label: 'Email',         render: r => r.user?.email ?? r.email ?? '—' },
    { key: 'designation',   label: 'Designation',   render: r => r.designation ?? '—' },
    { key: 'specialization',label: 'Specialization',render: r => r.specialization ?? '—' },
    { key: 'qualification', label: 'Qualification', render: r => r.qualification ?? '—' },
    { key: 'department',    label: 'Department',    render: r => r.department?.name ?? '—' },
    { key: 'active',        label: 'Status',        render: r => <Badge status={r.active ? 'ACTIVE' : 'INACTIVE'} /> },
  ];

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto' }}>
      <PageHeader
        title="Teachers" icon={GraduationCap}
        subtitle="Manage teacher records"
        action="Add Teacher"
        onAction={() => setShowModal(true)}
      />
      <DataTable
        columns={columns} data={data} loading={loading}
        onSearch={q => { setSearchTerm(q); load(0, q); }}
        searchPlaceholder="Search by name or specialization..."
        page={page} totalPages={totalPages}
        onPageChange={p => { setPage(p); load(p, searchTerm); }}
        emptyMessage="No teachers found."
      />

      {showModal && (
        <Modal title="Add New Teacher" onClose={() => setShowModal(false)} width="580px">
          <form onSubmit={handleSubmit} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <Field label="First Name *">
              <input style={inputStyle} value={form.firstName} onChange={f('firstName')} required />
            </Field>
            <Field label="Last Name *">
              <input style={inputStyle} value={form.lastName} onChange={f('lastName')} required />
            </Field>
            <Field label="Email *">
              <input style={inputStyle} type="email" value={form.email} onChange={f('email')} required />
            </Field>
            <Field label="Phone Number">
              <input style={inputStyle} value={form.phoneNumber} onChange={f('phoneNumber')} />
            </Field>
            <Field label="Qualification">
              <input style={inputStyle} placeholder="e.g. M.Sc, B.Ed" value={form.qualification} onChange={f('qualification')} />
            </Field>
            <Field label="Designation">
              <input style={inputStyle} placeholder="e.g. Senior Teacher" value={form.designation} onChange={f('designation')} />
            </Field>
            <Field label="Specialization">
              <input style={inputStyle} placeholder="e.g. Mathematics" value={form.specialization} onChange={f('specialization')} />
            </Field>
            <Field label="Experience (Years)">
              <input style={inputStyle} type="number" min="0" value={form.experienceYears} onChange={f('experienceYears')} />
            </Field>
            <Field label="Department">
              <select style={selectStyle} value={form.departmentId} onChange={f('departmentId')}>
                <option value="">No department</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>{d.name}</option>
                ))}
              </select>
            </Field>
            <div />
            <div style={{ gridColumn: '1/-1', display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '8px', borderTop: '1px solid #334155' }}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowModal(false)}>Cancel</button>
              <button type="submit" style={styles.submitBtn} disabled={saving}>
                {saving ? 'Creating...' : 'Create Teacher'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

const styles = {
  cancelBtn: { padding: '9px 18px', borderRadius: '8px', border: '1px solid #334155', background: 'none', color: '#94a3b8', cursor: 'pointer' },
  submitBtn: { padding: '9px 20px', borderRadius: '8px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600' },
};
