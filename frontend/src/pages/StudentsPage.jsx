import React, { useEffect, useState } from 'react';
import { Users, Download } from 'lucide-react';
import { studentAPI, academicAPI } from '../services/api';
import PageHeader from '../components/PageHeader';
import DataTable from '../components/DataTable';
import Badge from '../components/Badge';

// Students page shows the students list.
// New student flow uses admissions — see AdmissionsPage.jsx
// This page is ADMIN-only: GET /admin/students
export default function StudentsPage() {
  const [data, setData] = useState([]);
  const [classes, setClasses] = useState([]);
  const [selectedClassId, setSelectedClassId] = useState('');
  const [filteredData, setFilteredData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');

  const load = (p = 0, search = '') => {
    setLoading(true);
    studentAPI.getAll(p, 10, search)
      .then(res => {
        // Backend: ApiResponse<PageResponse<Student>>
        // res.data = ApiResponse  →  res.data.data = PageResponse
        const page = res.data.data;
        const rows = page.content ?? [];
        setData(rows);
        setFilteredData(rows);
        setTotalPages(page.totalPages ?? 1);
      })
      .catch(() => { setData([]); setFilteredData([]); })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load(0);
    // Load active classes for bulk download
    academicAPI.getActiveYear().then(r => {
      if (r.data.data?.id) {
        academicAPI.getClasses(r.data.data.id).then(cr => {
           const clst = cr.data.data?.content || cr.data.data || [];
           setClasses(clst);
           if (clst.length > 0) setSelectedClassId(clst[0].id);
        });
      }
    });
  }, []);

  const handleSearch = (q) => {
    setSearchTerm(q);
    if (!q.trim()) {
      setFilteredData(data);
    } else {
      load(0, q);
    }
  };

  const handleDownloadIdCard = async (student) => {
    try {
      const res = await studentAPI.downloadIdCard(student.id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `IDCard_${student.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (e) {
      alert('Failed to download ID card.');
    }
  };

  const handleDownloadReportCard = async (student) => {
    if (!student.currentClassGrade?.id) {
       alert('Student is not assigned to a class.');
       return;
    }
    try {
      const res = await studentAPI.downloadReportCard(student.id, student.currentClassGrade.id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `ReportCard_${student.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (e) {
      alert('Failed to download Report Card.');
    }
  };

  const handleBulkDownload = async () => {
    if (!selectedClassId) return;
    try {
      const res = await studentAPI.downloadBulkIdCards(selectedClassId);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `IDCards_class_${selectedClassId}.zip`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (e) {
      alert('Failed to download Bulk ID Cards');
    }
  };

  const columns = [
    { key: 'id',         label: 'ID' },
    { key: 'name',       label: 'Name',     render: r => `${r.firstName ?? ''} ${r.lastName ?? ''}`.trim() || '—' },
    { key: 'rollNumber', label: 'Roll No',  render: r => r.rollNumber ?? '—' },
    { key: 'classGrade', label: 'Class',    render: r => r.classGrade ? `${r.classGrade.gradeName} ${r.classGrade.section ?? ''}` : '—' },
    { key: 'gender',     label: 'Gender',   render: r => r.gender ? <Badge status={r.gender} /> : '—' },
    { key: 'bloodGroup', label: 'Blood',    render: r => r.bloodGroup ?? '—' },
    { key: 'parentName', label: 'Parent',   render: r => r.parentName ?? '—' },
    { key: 'parentPhone',label: 'Parent Ph',render: r => r.parentPhone ?? '—' },
    { key: 'active',     label: 'Status',   render: r => <Badge status={r.isActive === true || r.active === true ? 'ACTIVE' : 'INACTIVE'} /> },
    { key: 'actions',    label: 'Actions',  render: r => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <button onClick={() => handleDownloadIdCard(r)} style={btnStyle}>ID Card</button>
          <button onClick={() => handleDownloadReportCard(r)} style={btnStyle}>Report Card</button>
        </div>
      )
    },
  ];

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto' }}>
      <PageHeader
        title="Students"
        icon={Users}
        subtitle={`Manage student records`}
        action="New Admission →"
        onAction={() => window.location.href = '/admissions'}
      />
      
      {/* Bulk actions */}
      <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '20px', background: '#1e293b', padding: '16px', borderRadius: '12px', border: '1px solid #334155' }}>
        <span style={{ fontSize: '13px', color: '#94a3b8', fontWeight: 600 }}>Bulk Actions:</span>
        <select 
          style={{ padding: '8px 12px', borderRadius: '8px', background: '#0f172a', border: '1px solid #334155', color: '#f1f5f9', fontSize: '13px' }}
          value={selectedClassId} onChange={e => setSelectedClassId(e.target.value)}
        >
          {classes.map(c => <option key={c.id} value={c.id}>{c.gradeName} {c.section}</option>)}
        </select>
        <button 
          onClick={handleBulkDownload}
          style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '8px 16px', background: 'rgba(99,102,241,0.1)', color: '#818cf8', border: '1px solid rgba(99,102,241,0.3)', borderRadius: '8px', fontSize: '13px', fontWeight: 600, cursor: 'pointer' }}
        >
          <Download size={14} /> Download Class ID Cards (ZIP)
        </button>
      </div>

      <DataTable
        columns={columns}
        data={filteredData}
        loading={loading}
        onSearch={handleSearch}
        searchPlaceholder="Search by name or roll number..."
        page={page}
        totalPages={totalPages}
        onPageChange={p => { setPage(p); load(p, searchTerm); }}
        emptyMessage="No students found. Submit an admission application first."
      />
    </div>
  );
}

const btnStyle = {
  padding: '4px 8px', borderRadius: '4px', background: 'rgba(99,102,241,0.1)',
  border: '1px solid rgba(99,102,241,0.3)', color: '#818cf8', fontSize: '11px',
  cursor: 'pointer', fontWeight: '600'
};
