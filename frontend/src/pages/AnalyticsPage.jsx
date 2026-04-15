import React, { useState, useEffect } from 'react';
import { BarChart2, Users, TrendingUp, CreditCard, FileText, CheckSquare } from 'lucide-react';
import api from '../services/api';

// Simple bar chart using divs (no external library needed)
function BarChart({ data, colorFn, labelKey, valueKey, height = 160 }) {
  if (!data || data.length === 0) return <p style={{ color: '#475569', textAlign: 'center', padding: '20px' }}>No data</p>;
  const max = Math.max(...data.map(d => d[valueKey] || 0), 1);
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', height: `${height}px`, padding: '0 4px' }}>
      {data.map((d, i) => {
        const pct = ((d[valueKey] || 0) / max) * 100;
        const color = colorFn ? colorFn(d, i) : '#6366f1';
        return (
          <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px', height: '100%', justifyContent: 'flex-end' }}>
            <span style={{ fontSize: '11px', fontWeight: '700', color }}>{d[valueKey] || 0}</span>
            <div style={{ width: '100%', background: `${color}20`, borderRadius: '6px 6px 0 0', height: `${pct}%`, minHeight: pct > 0 ? '4px' : '0', background: `linear-gradient(to top, ${color}, ${color}88)`, transition: 'height 0.5s ease' }} />
            <span style={{ fontSize: '10px', color: '#64748b', textAlign: 'center', lineHeight: 1.2, maxWidth: '60px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{d[labelKey]}</span>
          </div>
        );
      })}
    </div>
  );
}

function DonutChart({ segments, size = 120 }) {
  const total = segments.reduce((s, g) => s + g.value, 0) || 1;
  let offset = 25;
  const r = 40, cx = 60, cy = 60, circ = 2 * Math.PI * r;
  return (
    <svg width={size} height={size} viewBox="0 0 120 120">
      {segments.map((seg, i) => {
        const pct = seg.value / total;
        const dash = pct * circ;
        const el = (
          <circle key={i} cx={cx} cy={cy} r={r}
            fill="none" stroke={seg.color} strokeWidth="18"
            strokeDasharray={`${dash} ${circ - dash}`}
            strokeDashoffset={-((offset / 100) * circ)}
            style={{ transition: 'stroke-dasharray 0.6s ease' }}
          />
        );
        offset += pct * 100;
        return el;
      })}
      <circle cx={cx} cy={cy} r={30} fill="#1e293b" />
      <text x={cx} y={cy + 5} textAnchor="middle" fill="#f1f5f9" fontSize="14" fontWeight="700">{total}</text>
    </svg>
  );
}

export default function AnalyticsPage() {
  const [stats, setStats] = useState(null);
  const [admStats, setAdmStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [classes, setClasses] = useState([]);
  const [feeData, setFeeData] = useState([]);

  useEffect(() => {
    loadAll();
  }, []);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [dashR, admR] = await Promise.all([
        api.get('/admin/dashboard/stats').catch(() => ({ data: {} })),
        api.get('/admin/admissions/stats').catch(() => ({ data: {} })),
      ]);
      setStats(dashR.data.data || dashR.data);
      setAdmStats(admR.data.data || admR.data);

      // Load classes for per-class student chart
      const yr = await api.get('/admin/academic/years/active').catch(() => ({ data: {} }));
      const yId = yr.data.data?.id;
      if (yId) {
        const classR = await api.get('/admin/academic/classes', { params: { academicYearId: yId, page: 0, size: 50 } });
        const cls = classR.data.data?.content || [];
        setClasses(cls.map(c => ({ name: c.gradeName, value: c.currentStrength || 0 })));
      }

      // Fee summary
      const feeR = await api.get('/admin/fees/structures').catch(() => ({ data: {} }));
      const structs = feeR.data.data || [];
      setFeeData(structs.slice(0, 5).map(f => ({ name: f.feeType?.replace('_', ' '), value: f.amount || 0 })));
    } catch {} finally { setLoading(false); }
  };

  if (loading) return (
    <div style={{ ...s.page, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={s.spinner} />
    </div>
  );

  const admissionSegments = [
    { label: 'Approved', value: admStats?.approved || 0, color: '#22c55e' },
    { label: 'Pending', value: admStats?.pending || 0, color: '#f59e0b' },
    { label: 'Rejected', value: admStats?.rejected || 0, color: '#ef4444' },
  ];

  return (
    <div style={s.page}>
      {/* Header */}
      <div style={s.header}>
        <div style={s.headerLeft}>
          <div style={s.headerIcon}><BarChart2 size={20} color="#8b5cf6" /></div>
          <div>
            <h1 style={s.title}>Analytics</h1>
            <p style={s.subtitle}>Live insights from your school data</p>
          </div>
        </div>
        <button style={s.refreshBtn} onClick={loadAll}>Refresh</button>
      </div>

      {/* KPI Cards */}
      <div style={s.kpiGrid}>
        <KpiCard icon={<Users size={18} />} label="Total Students" value={stats?.totalStudents ?? '—'} color="#6366f1" sub="Enrolled & Active" />
        <KpiCard icon={<Users size={18} />} label="Total Teachers" value={stats?.totalTeachers ?? '—'} color="#06b6d4" sub="Active faculty" />
        <KpiCard icon={<CreditCard size={18} />} label="Fee Collected" value={stats?.totalFeeCollected ? `₹${stats.totalFeeCollected.toLocaleString()}` : '—'} color="#22c55e" sub="This academic year" />
        <KpiCard icon={<FileText size={18} />} label="Admissions" value={(admStats?.approved || 0) + (admStats?.pending || 0) + (admStats?.rejected || 0)} color="#f59e0b" sub="Total applications" />
        <KpiCard icon={<CheckSquare size={18} />} label="Pending Leaves" value={stats?.pendingLeaves ?? '—'} color="#ec4899" sub="Awaiting approval" />
        <KpiCard icon={<TrendingUp size={18} />} label="Notices Posted" value={stats?.totalNotices ?? '—'} color="#8b5cf6" sub="Active announcements" />
      </div>

      {/* Charts Row 1 */}
      <div style={s.chartGrid2}>
        {/* Students per class */}
        <div style={s.chartCard}>
          <div style={s.chartTitle}><Users size={15} color="#6366f1" /> Students per Class</div>
          <BarChart
            data={classes}
            labelKey="name"
            valueKey="value"
            colorFn={(_, i) => ['#6366f1', '#06b6d4', '#22c55e', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6', '#f97316', '#6366f1', '#06b6d4'][i % 10]}
            height={180}
          />
        </div>

        {/* Admission Donut */}
        <div style={s.chartCard}>
          <div style={s.chartTitle}><FileText size={15} color="#f59e0b" /> Admissions Breakdown</div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '28px', padding: '16px 0' }}>
            <DonutChart segments={admissionSegments} size={140} />
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {admissionSegments.map(seg => (
                <div key={seg.label} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div style={{ width: '12px', height: '12px', borderRadius: '3px', background: seg.color }} />
                  <span style={{ fontSize: '13px', color: '#94a3b8' }}>{seg.label}</span>
                  <span style={{ fontSize: '14px', fontWeight: '700', color: seg.color, marginLeft: 'auto' }}>{seg.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div style={s.chartGrid3}>
        {/* Quick Stats */}
        <div style={s.chartCard}>
          <div style={s.chartTitle}><TrendingUp size={15} color="#8b5cf6" /> Quick Summary</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
            {[
              { label: 'Total Classes', value: classes.length, color: '#6366f1' },
              { label: 'Avg Students / Class', value: classes.length ? Math.round(classes.reduce((a, c) => a + c.value, 0) / classes.length) : 0, color: '#06b6d4' },
              { label: 'Admission Pass Rate', value: admStats ? `${Math.round(((admStats.approved || 0) / Math.max((admStats.approved || 0) + (admStats.rejected || 0), 1)) * 100)}%` : '—', color: '#22c55e' },
              { label: 'Pending Applications', value: admStats?.pending || 0, color: '#f59e0b' },
              { label: 'Pending Leave Requests', value: stats?.pendingLeaves || 0, color: '#ec4899' },
            ].map(item => (
              <div key={item.label} style={s.summaryRow}>
                <span style={{ fontSize: '13px', color: '#94a3b8' }}>{item.label}</span>
                <span style={{ fontSize: '15px', fontWeight: '800', color: item.color }}>{item.value}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Fee structures bar */}
        <div style={{ ...s.chartCard, gridColumn: 'span 2' }}>
          <div style={s.chartTitle}><CreditCard size={15} color="#22c55e" /> Fee Structures (₹)</div>
          <BarChart
            data={feeData}
            labelKey="name"
            valueKey="value"
            colorFn={(_, i) => ['#22c55e', '#06b6d4', '#6366f1', '#f59e0b', '#ec4899'][i % 5]}
            height={180}
          />
        </div>
      </div>
    </div>
  );
}

function KpiCard({ icon, label, value, color, sub }) {
  return (
    <div style={{ ...s.kpiCard, borderColor: `${color}33` }}>
      <div style={{ ...s.kpiIcon, background: `${color}18`, color }}>{icon}</div>
      <div style={s.kpiValue}>{value}</div>
      <div style={s.kpiLabel}>{label}</div>
      <div style={s.kpiSub}>{sub}</div>
    </div>
  );
}

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  spinner: { width: '40px', height: '40px', borderRadius: '50%', border: '3px solid #334155', borderTopColor: '#8b5cf6', animation: 'spin 0.8s linear infinite' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '28px' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '14px' },
  headerIcon: { width: '46px', height: '46px', borderRadius: '12px', background: 'rgba(139,92,246,0.1)', border: '1px solid rgba(139,92,246,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: 0 },
  subtitle: { fontSize: '13px', color: '#64748b', margin: '4px 0 0' },
  refreshBtn: { padding: '8px 16px', borderRadius: '8px', background: '#1e293b', border: '1px solid #334155', color: '#94a3b8', fontSize: '13px', fontWeight: '600', cursor: 'pointer' },
  kpiGrid: { display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: '14px', marginBottom: '20px' },
  kpiCard: { background: '#1e293b', borderRadius: '14px', border: '1px solid', padding: '18px 16px', display: 'flex', flexDirection: 'column', gap: '6px' },
  kpiIcon: { width: '36px', height: '36px', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '4px' },
  kpiValue: { fontSize: '24px', fontWeight: '800', color: '#f1f5f9', lineHeight: 1 },
  kpiLabel: { fontSize: '12px', fontWeight: '600', color: '#94a3b8' },
  kpiSub: { fontSize: '11px', color: '#475569' },
  chartGrid2: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' },
  chartGrid3: { display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '16px' },
  chartCard: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '20px' },
  chartTitle: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', fontWeight: '700', color: '#f1f5f9', marginBottom: '16px' },
  summaryRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid #334155' },
};
