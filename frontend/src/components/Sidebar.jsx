import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Bus, GraduationCap, LogOut, ChevronRight,
  Users, Bell, CreditCard, Calendar, CheckSquare, BookOpen, FileText, Settings,
  BarChart2, ClipboardList, User, Map
} from 'lucide-react';

const navConfig = {
  ADMIN: [
    { label: 'Dashboard',    icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Analytics',    icon: BarChart2,        path: '/analytics' },
    { label: 'Academic Setup',icon: Settings,        path: '/academic-setup' },
    { label: 'Students',     icon: Users,            path: '/students' },
    { label: 'Admissions',   icon: FileText,         path: '/admissions' },
    { label: 'Teachers',     icon: GraduationCap,    path: '/teachers' },
    { label: 'Marks',        icon: ClipboardList,    path: '/marks' },
    { label: 'Timetable',    icon: Calendar,         path: '/timetable' },
    { label: 'Notices',      icon: Bell,             path: '/notices' },
    { label: 'Attendance',   icon: CheckSquare,      path: '/attendance' },
    { label: 'Fee',          icon: CreditCard,       path: '/fee' },
    { label: 'Leave',        icon: Calendar,         path: '/leave' },
    { label: 'Bus Tracking', icon: Bus,              path: '/bus-tracking' },
    { label: 'Transport Setup', icon: Settings,      path: '/admin/transport' },
    { label: 'Bus Simulator', icon: Map,             path: '/admin/simulator' },
  ],
  TEACHER: [
    { label: 'Dashboard',    icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Marks',        icon: ClipboardList,   path: '/marks' },
    { label: 'Timetable',    icon: Calendar,        path: '/timetable' },
    { label: 'Attendance',   icon: CheckSquare,     path: '/attendance' },
    { label: 'Notices',      icon: Bell,            path: '/notices' },
    { label: 'Leave',        icon: Calendar,        path: '/leave' },
    { label: 'Bus Tracking', icon: Bus,             path: '/bus-tracking' },
  ],
  STUDENT: [
    { label: 'Dashboard',    icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Timetable',    icon: Calendar,        path: '/timetable' },
    { label: 'Notices',      icon: Bell,            path: '/notices' },
    { label: 'Leave',        icon: Calendar,        path: '/leave' },
    { label: 'Bus Tracking', icon: Bus,             path: '/bus-tracking' },
  ],
  STAFF: [
    { label: 'Dashboard',    icon: LayoutDashboard, path: '/dashboard' },
    { label: 'Timetable',    icon: Calendar,        path: '/timetable' },
    { label: 'Notices',      icon: Bell,            path: '/notices' },
    { label: 'Leave',        icon: Calendar,        path: '/leave' },
    { label: 'Bus Tracking', icon: Bus,             path: '/bus-tracking' },
  ],
};

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const role = user?.role || 'STUDENT';
  const navItems = navConfig[role] || navConfig.STUDENT;

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const roleColor = {
    ADMIN: '#6366f1', TEACHER: '#06b6d4', STUDENT: '#22c55e', STAFF: '#f59e0b',
  };

  return (
    <aside style={styles.sidebar}>
      {/* Logo */}
      <div style={styles.logo}>
        <div style={styles.logoIcon}>
          <BookOpen size={20} color="#6366f1" />
        </div>
        <div>
          <div style={styles.logoTitle}>School MS</div>
          <div style={styles.logoSub}>Management System</div>
        </div>
      </div>

      {/* Role Badge */}
      <div style={{ ...styles.roleBadge, borderColor: `${roleColor[role]}40`, color: roleColor[role], background: `${roleColor[role]}15` }}>
        {role}
      </div>

      {/* Nav */}
      <nav style={styles.nav}>
        {navItems.map(({ label, icon: Icon, path }) => {
          const active = location.pathname === path;
          return (
            <button
              key={path}
              style={{ ...styles.navItem, ...(active ? styles.navActive : {}) }}
              onClick={() => navigate(path)}
            >
              <Icon size={17} color={active ? '#6366f1' : '#64748b'} />
              <span style={{ ...styles.navLabel, color: active ? '#f1f5f9' : '#94a3b8' }}>
                {label}
              </span>
              {active && <ChevronRight size={13} color="#6366f1" style={{ marginLeft: 'auto' }} />}
            </button>
          );
        })}
      </nav>

      {/* User Block */}
      <div style={styles.userBlock}>
        <div style={{ ...styles.avatar, background: `linear-gradient(135deg, ${roleColor[role]}, #1e293b)` }}>
          {user?.fullName?.charAt(0).toUpperCase() || 'U'}
        </div>
        <div style={styles.userInfo} onClick={() => navigate('/profile')} title="View Profile">
          <div style={styles.userName}>{user?.fullName || 'User'}</div>
          <div style={styles.userRole}>{role}</div>
        </div>
        <button style={styles.logoutBtn} onClick={handleLogout} title="Logout">
          <LogOut size={15} color="#64748b" />
        </button>
      </div>
    </aside>
  );
}

const styles = {
  sidebar: {
    width: '230px', minHeight: '100vh', background: '#1e293b',
    borderRight: '1px solid #334155', display: 'flex',
    flexDirection: 'column', padding: '20px 14px', flexShrink: 0,
  },
  logo: { display: 'flex', alignItems: 'center', gap: '10px', padding: '0 6px', marginBottom: '16px' },
  logoIcon: {
    width: '38px', height: '38px', borderRadius: '10px',
    background: 'rgba(99,102,241,0.15)', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    border: '1px solid rgba(99,102,241,0.3)',
  },
  logoTitle: { fontSize: '14px', fontWeight: '700', color: '#f1f5f9' },
  logoSub: { fontSize: '10px', color: '#64748b' },
  roleBadge: {
    margin: '0 6px 20px', padding: '5px 10px', borderRadius: '6px',
    fontSize: '10px', fontWeight: '700', letterSpacing: '1px',
    border: '1px solid', textAlign: 'center',
  },
  nav: { display: 'flex', flexDirection: 'column', gap: '2px', flex: 1 },
  navItem: {
    display: 'flex', alignItems: 'center', gap: '10px',
    padding: '9px 10px', borderRadius: '9px', border: 'none',
    background: 'none', cursor: 'pointer', width: '100%', textAlign: 'left',
  },
  navActive: { background: 'rgba(99,102,241,0.12)' },
  navLabel: { fontSize: '13px', fontWeight: '500' },
  userBlock: {
    display: 'flex', alignItems: 'center', gap: '8px',
    padding: '10px', borderRadius: '10px', background: '#263348',
    marginTop: '12px',
  },
  avatar: {
    width: '34px', height: '34px', borderRadius: '50%',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontWeight: '700', color: '#fff', fontSize: '13px', flexShrink: 0,
  },
  userInfo: { flex: 1, overflow: 'hidden' },
  userName: {
    fontSize: '12px', fontWeight: '600', color: '#f1f5f9',
    whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
  },
  userRole: { fontSize: '10px', color: '#64748b' },
  logoutBtn: { background: 'none', border: 'none', cursor: 'pointer', display: 'flex', padding: '4px' },
};
