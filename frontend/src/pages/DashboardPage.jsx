import React, { useEffect, useState } from 'react';
import { dashboardAPI } from '../services/api';
import {
  Users, GraduationCap, BookOpen, Bus,
  TrendingUp, CheckCircle, AlertCircle, Activity,
  Briefcase, Building, DollarSign, Calendar
} from 'lucide-react';

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardAPI.getStats()
      .then((res) => setStats(res.data.data))
      .catch(() => setStats(null))
      .finally(() => setLoading(false));
  }, []);

  const cards = stats ? [
    { label: 'Total Students', value: stats.totalStudents ?? '—', icon: Users, color: '#6366f1' },
    { label: 'Total Teachers', value: stats.totalTeachers ?? '—', icon: GraduationCap, color: '#06b6d4' },
    { label: 'Total Staff', value: stats.totalStaff ?? '—', icon: Briefcase, color: '#8b5cf6' },
    { label: 'Total Classes', value: stats.totalClasses ?? '—', icon: BookOpen, color: '#22c55e' },
    { label: 'Departments', value: stats.totalDepartments ?? '—', icon: Building, color: '#3b82f6' },
    { label: 'Pending Admissions', value: stats.pendingAdmissions ?? '—', icon: AlertCircle, color: '#f59e0b' },
    { label: 'Active Users', value: stats.activeUsers ?? '—', icon: Activity, color: '#10b981' },
    { label: 'Active Vehicles', value: stats.activeVehicles ?? '—', icon: Bus, color: '#a78bfa' },
    { label: 'Monthly Fees', value: stats.monthlyFeeCollection != null ? `₹${stats.monthlyFeeCollection}` : '—', icon: DollarSign, color: '#14b8a6' },
    { label: 'Leave Requests', value: stats.pendingLeaveRequests ?? '—', icon: Calendar, color: '#ef4444' },
  ] : [];

  return (
    <div style={styles.page}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <h1 style={styles.title}>Dashboard</h1>
          <p style={styles.subtitle}>Welcome back! Here's what's happening today.</p>
        </div>
        <div style={styles.badge}>
          <Activity size={14} color="#22c55e" />
          <span style={{ fontSize: '12px', color: '#22c55e', fontWeight: '600' }}>System Online</span>
        </div>
      </div>

      {/* Stats Grid */}
      {loading ? (
        <div style={styles.loadingGrid}>
          {[...Array(10)].map((_, i) => <div key={i} style={styles.skeleton} />)}
        </div>
      ) : (
        <div style={styles.grid}>
          {cards.map(({ label, value, icon: Icon, color }) => (
            <div key={label} style={styles.card}>
              <div style={{ ...styles.cardIcon, background: `${color}20`, border: `1px solid ${color}40` }}>
                <Icon size={20} color={color} />
              </div>
              <div style={styles.cardValue}>{value}</div>
              <div style={styles.cardLabel}>{label}</div>
              <div style={{ ...styles.cardBar, background: color }} />
            </div>
          ))}
        </div>
      )}

      {/* Info Banner */}
      <div style={styles.banner}>
        <div style={styles.bannerIcon}>
          <TrendingUp size={20} color="#6366f1" />
        </div>
        <div>
          <div style={styles.bannerTitle}>School Management System — Phase 6 Active</div>
          <div style={styles.bannerDesc}>
            Live Bus Tracking via Kafka + WebSockets is fully operational.
            Navigate to <strong style={{color:'#818cf8'}}>Bus Tracking</strong> to watch the live map.
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '32px', flex: 1, overflow: 'auto' },
  header: {
    display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
    marginBottom: '32px', flexWrap: 'wrap', gap: '12px',
  },
  title: { fontSize: '26px', fontWeight: '800', color: '#f1f5f9' },
  subtitle: { fontSize: '14px', color: '#64748b', marginTop: '4px' },
  badge: {
    display: 'flex', alignItems: 'center', gap: '6px',
    background: 'rgba(34,197,94,0.1)', border: '1px solid rgba(34,197,94,0.3)',
    borderRadius: '20px', padding: '6px 14px',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
    gap: '20px', marginBottom: '32px',
  },
  loadingGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
    gap: '20px', marginBottom: '32px',
  },
  skeleton: {
    height: '140px', borderRadius: '14px',
    background: 'linear-gradient(90deg, #1e293b 25%, #263348 50%, #1e293b 75%)',
    backgroundSize: '200% 100%', animation: 'shimmer 1.5s infinite',
  },
  card: {
    background: '#1e293b', borderRadius: '14px', padding: '24px',
    border: '1px solid #334155', position: 'relative', overflow: 'hidden',
    transition: 'transform 0.2s, box-shadow 0.2s',
  },
  cardIcon: {
    width: '44px', height: '44px', borderRadius: '12px',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    marginBottom: '16px',
  },
  cardValue: { fontSize: '32px', fontWeight: '800', color: '#f1f5f9', lineHeight: 1 },
  cardLabel: { fontSize: '13px', color: '#64748b', marginTop: '6px' },
  cardBar: {
    position: 'absolute', bottom: 0, left: 0, right: 0,
    height: '3px', borderRadius: '0 0 14px 14px', opacity: 0.6,
  },
  banner: {
    display: 'flex', gap: '16px', background: 'rgba(99,102,241,0.08)',
    border: '1px solid rgba(99,102,241,0.25)', borderRadius: '14px',
    padding: '20px 24px', alignItems: 'flex-start',
  },
  bannerIcon: {
    width: '40px', height: '40px', borderRadius: '10px',
    background: 'rgba(99,102,241,0.15)', display: 'flex',
    alignItems: 'center', justifyContent: 'center', flexShrink: 0,
  },
  bannerTitle: { fontSize: '15px', fontWeight: '600', color: '#f1f5f9', marginBottom: '4px' },
  bannerDesc: { fontSize: '13px', color: '#64748b', lineHeight: 1.6 },
};
