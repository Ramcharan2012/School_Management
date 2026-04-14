import React, { useEffect, useState } from 'react';
import { BookOpen, Calendar, Layers, MapPin, CheckCircle, Plus } from 'lucide-react';
import { academicAPI } from '../services/api';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Modal, { Field, inputStyle } from '../components/Modal';
import Badge from '../components/Badge';

export default function AcademicSetupPage() {
  const [activeTab, setActiveTab] = useState('years');
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  
  // Modals
  const [showYearModal, setShowYearModal] = useState(false);
  const [showClassModal, setShowClassModal] = useState(false);
  const [showSubjectModal, setShowSubjectModal] = useState(false);
  const [showDeptModal, setShowDeptModal] = useState(false);
  
  // Shared active academic year
  const [activeYear, setActiveYear] = useState(null);

  // Loaders
  const loadActiveYear = () => {
    academicAPI.getActiveYear().then(r => setActiveYear(r.data.data)).catch(() => {});
  }

  const loadData = () => {
    setLoading(true);
    if (activeTab === 'years') {
      academicAPI.getYears().then(r => setData(r.data.data || [])).finally(() => setLoading(false));
    } else if (activeTab === 'classes') {
      academicAPI.getClasses(activeYear?.id).then(r => setData(r.data.data?.content || [])).finally(() => setLoading(false));
    } else if (activeTab === 'subjects') {
      academicAPI.getSubjects().then(r => setData(r.data.data?.content || [])).finally(() => setLoading(false));
    } else if (activeTab === 'departments') {
      academicAPI.getDepartments().then(r => setData(r.data.data || [])).finally(() => setLoading(false));
    }
  };

  useEffect(() => { loadActiveYear(); }, []);
  useEffect(() => { loadData(); }, [activeTab, activeYear]);

  // Activate Year
  const handleActivateYear = async (id) => {
    try {
      await academicAPI.activateYear(id);
      loadActiveYear();
      loadData();
    } catch (e) { alert('Failed to activate year'); }
  };

  // Render Tabs
  const renderTab = (key, label, Icon) => (
    <button
      onClick={() => setActiveTab(key)}
      style={{
        display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 16px', borderRadius: '8px',
        background: activeTab === key ? 'rgba(99,102,241,0.15)' : 'transparent',
        border: `1px solid ${activeTab === key ? '#6366f1' : 'transparent'}`,
        color: activeTab === key ? '#818cf8' : '#94a3b8',
        fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s'
      }}
    >
      <Icon size={16} /> {label}
    </button>
  );

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto', display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <PageHeader
        title="Academic Setup" icon={BookOpen}
        subtitle={`Current Active Year: ${activeYear ? activeYear.yearLabel : 'None - Please activate one'}`}
      />

      <div style={{ display: 'flex', gap: '12px', background: '#1e293b', padding: '12px', borderRadius: '12px', border: '1px solid #334155' }}>
        {renderTab('years', 'Academic Years', Calendar)}
        {renderTab('classes', 'Classes', Layers)}
        {renderTab('departments', 'Departments', MapPin)}
        {renderTab('subjects', 'Subjects', BookOpen)}
      </div>

      <div style={{ background: '#1e293b', borderRadius: '12px', padding: '20px', border: '1px solid #334155' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
          <h2 style={{ color: '#f1f5f9', fontSize: '16px', fontWeight: '700' }}>
            {activeTab === 'years' && 'Manage Academic Years'}
            {activeTab === 'classes' && `Classes for ${activeYear?.yearLabel || '...'}`}
            {activeTab === 'departments' && 'Manage Departments'}
            {activeTab === 'subjects' && 'Manage Subjects'}
          </h2>
          <button
            onClick={() => {
              if (activeTab === 'years') setShowYearModal(true);
              if (activeTab === 'classes') setShowClassModal(true);
              if (activeTab === 'departments') setShowDeptModal(true);
              if (activeTab === 'subjects') setShowSubjectModal(true);
            }}
            style={{ padding: '8px 16px', background: '#6366f1', color: '#fff', borderRadius: '8px', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 'bold' }}
          >
            <Plus size={16} /> Add New
          </button>
        </div>

        {activeTab === 'years' && (
          <DataTable
            columns={[
              { key: 'id', label: 'ID' },
              { key: 'yearLabel', label: 'Year Label' },
              { key: 'startDate', label: 'Start Date' },
              { key: 'endDate', label: 'End Date' },
              { key: 'isActive', label: 'Status', render: r => <Badge status={r.isActive ? 'ACTIVE' : 'INACTIVE'} /> },
              { key: 'action', label: 'Action', render: r => !r.isActive && (
                <button onClick={() => handleActivateYear(r.id)} style={{ padding: '4px 10px', background: '#10b981', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Set Active</button>
              )}
            ]}
            data={data} loading={loading}
          />
        )}

        {activeTab === 'classes' && (
          <DataTable
            columns={[
              { key: 'id', label: 'ID' },
              { key: 'gradeName', label: 'Class Name' },
              { key: 'section', label: 'Section' },
              { key: 'capacity', label: 'Capacity' },
              { key: 'roomNumber', label: 'Room' },
            ]}
            data={data} loading={loading}
          />
        )}

        {activeTab === 'departments' && (
          <DataTable
            columns={[
              { key: 'id', label: 'ID' },
              { key: 'name', label: 'Department Name' },
              { key: 'code', label: 'Code' },
            ]}
            data={data} loading={loading}
          />
        )}

        {activeTab === 'subjects' && (
          <DataTable
            columns={[
              { key: 'id', label: 'ID' },
              { key: 'name', label: 'Subject Name' },
              { key: 'code', label: 'Code' },
              { key: 'creditHours', label: 'Credits' },
              { key: 'department', label: 'Department', render: r => r.department?.name },
            ]}
            data={data} loading={loading}
          />
        )}
      </div>

      {showYearModal && <YearModal onClose={() => setShowYearModal(false)} onSaved={loadData} />}
      {showClassModal && <ClassModal onClose={() => setShowClassModal(false)} activeYearId={activeYear?.id} onSaved={loadData} />}
      {showDeptModal && <DeptModal onClose={() => setShowDeptModal(false)} onSaved={loadData} />}
      {showSubjectModal && <SubjectModal onClose={() => setShowSubjectModal(false)} onSaved={loadData} />}
    </div>
  );
}

function YearModal({ onClose, onSaved }) {
  const [f, setF] = useState({ yearLabel: '', startDate: '', endDate: '' });
  const [s, setS] = useState(false);
  const submit = async (e) => {
    e.preventDefault(); setS(true);
    try { await academicAPI.createYear(f); onSaved(); onClose(); } 
    catch { alert('Error'); } finally { setS(false); }
  };
  return (
    <Modal title="Create Academic Year" onClose={onClose}>
      <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        <Field label="Year Label (e.g. 2024-2025)"><input style={inputStyle} value={f.yearLabel} onChange={e=>setF({...f, yearLabel: e.target.value})} required /></Field>
        <Field label="Start Date"><input type="date" style={inputStyle} value={f.startDate} onChange={e=>setF({...f, startDate: e.target.value})} required /></Field>
        <Field label="End Date"><input type="date" style={inputStyle} value={f.endDate} onChange={e=>setF({...f, endDate: e.target.value})} required /></Field>
        <button type="submit" style={{ padding: '10px', background: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px' }} disabled={s}>Save</button>
      </form>
    </Modal>
  );
}

function ClassModal({ onClose, onSaved, activeYearId }) {
  const [f, setF] = useState({ gradeName: '', section: '', capacity: 40, roomNumber: '' });
  const [s, setS] = useState(false);
  const submit = async (e) => {
    e.preventDefault(); setS(true);
    try { await academicAPI.createClass({ ...f, academicYearId: activeYearId }); onSaved(); onClose(); } 
    catch { alert('Error'); } finally { setS(false); }
  };
  return (
    <Modal title="Create Class" onClose={onClose}>
      <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        <Field label="Class Name (e.g. 1st Class, 10th Class)"><input style={inputStyle} value={f.gradeName} onChange={e=>setF({...f, gradeName: e.target.value})} required /></Field>
        <Field label="Section"><input style={inputStyle} value={f.section} onChange={e=>setF({...f, section: e.target.value})} required /></Field>
        <Field label="Capacity"><input type="number" style={inputStyle} value={f.capacity} onChange={e=>setF({...f, capacity: e.target.value})} required /></Field>
        <Field label="Room Number"><input style={inputStyle} value={f.roomNumber} onChange={e=>setF({...f, roomNumber: e.target.value})} /></Field>
        <button type="submit" style={{ padding: '10px', background: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px' }} disabled={s || !activeYearId}>
          {activeYearId ? 'Save' : 'Required Active Year First'}
        </button>
      </form>
    </Modal>
  );
}

function DeptModal({ onClose, onSaved }) {
    const [f, setF] = useState({ name: '', code: '', description: '' });
    const [s, setS] = useState(false);
    const submit = async (e) => {
      e.preventDefault(); setS(true);
      try { await academicAPI.createDepartment(f); onSaved(); onClose(); } 
      catch { alert('Error'); } finally { setS(false); }
    };
    return (
      <Modal title="Create Department" onClose={onClose}>
        <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <Field label="Name (e.g. Science)"><input style={inputStyle} value={f.name} onChange={e=>setF({...f, name: e.target.value})} required /></Field>
          <Field label="Code (e.g. SCI)"><input style={inputStyle} value={f.code} onChange={e=>setF({...f, code: e.target.value})} required /></Field>
          <button type="submit" style={{ padding: '10px', background: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px' }} disabled={s}>Save</button>
        </form>
      </Modal>
    );
  }

function SubjectModal({ onClose, onSaved }) {
    const [f, setF] = useState({ name: '', code: '', creditHours: 4, departmentId: '' });
    const [depts, setDepts] = useState([]);
    const [s, setS] = useState(false);
    useEffect(() => { academicAPI.getDepartments().then(r => setDepts(r.data.data)).catch(()=>{}); }, []);
    
    const submit = async (e) => {
      e.preventDefault(); setS(true);
      try { await academicAPI.createSubject({...f, departmentId: Number(f.departmentId)}); onSaved(); onClose(); } 
      catch { alert('Error'); } finally { setS(false); }
    };
    return (
      <Modal title="Create Subject" onClose={onClose}>
        <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <Field label="Subject Name (e.g. Mathematics)"><input style={inputStyle} value={f.name} onChange={e=>setF({...f, name: e.target.value})} required /></Field>
          <Field label="Code"><input style={inputStyle} value={f.code} onChange={e=>setF({...f, code: e.target.value})} required /></Field>
          <Field label="Department">
            <select style={inputStyle} value={f.departmentId} onChange={e=>setF({...f, departmentId: e.target.value})} required>
                <option value="">Select Dept</option>
                {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </Field>
          <button type="submit" style={{ padding: '10px', background: '#6366f1', color: '#fff', border: 'none', borderRadius: '8px' }} disabled={s}>Save</button>
        </form>
      </Modal>
    );
}
