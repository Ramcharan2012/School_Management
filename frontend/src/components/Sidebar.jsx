import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard, Bus, GraduationCap, LogOut, ChevronRight
} from 'lucide-react';

const navItems = [
  { label: 'Dashboard', icon: LayoutDashboard, path: '/dashboard' },
  { label: 'Bus Tracking', icon: Bus, path: '/bus-tracking' },
];

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <aside style={styles.sidebar}>
      {/* Logo */}
      <div style={styles.logo}>
        <div style={styles.logoIcon}>
          <GraduationCap size={22} color="#6366f1" />
        </div>
        <div>
          <div style={styles.logoTitle}>School MS</div>
          <div style={styles.logoSub}>Management System</div>
        </div>
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
              <Icon size={18} color={active ? '#6366f1' : '#64748b'} />
              <span style={{ ...styles.navLabel, color: active ? '#f1f5f9' : '#94a3b8' }}>
                {label}
              </span>
              {active && <ChevronRight size={14} color="#6366f1" style={{ marginLeft: 'auto' }} />}
            </button>
          );
        })}
      </nav>

      {/* User Profile */}
      <div style={styles.userBlock}>
        <div style={styles.avatar}>
          {user?.fullName?.charAt(0).toUpperCase() || 'A'}
        </div>
        <div style={styles.userInfo}>
          <div style={styles.userName}>{user?.fullName || 'Admin'}</div>
          <div style={styles.userRole}>{user?.role || 'ADMIN'}</div>
        </div>
        <button style={styles.logoutBtn} onClick={handleLogout} title="Logout">
          <LogOut size={16} color="#64748b" />
        </button>
      </div>
    </aside>
  );
}

const styles = {
  sidebar: {
    width: '240px', minHeight: '100vh', background: '#1e293b',
    borderRight: '1px solid #334155', display: 'flex',
    flexDirection: 'column', padding: '24px 16px', flexShrink: 0,
  },
  logo: {
    display: 'flex', alignItems: 'center', gap: '12px',
    padding: '0 8px', marginBottom: '36px',
  },
  logoIcon: {
    width: '40px', height: '40px', borderRadius: '10px',
    background: 'rgba(99,102,241,0.15)', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    border: '1px solid rgba(99,102,241,0.3)',
  },
  logoTitle: { fontSize: '15px', fontWeight: '700', color: '#f1f5f9' },
  logoSub: { fontSize: '11px', color: '#64748b' },
  nav: { display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 },
  navItem: {
    display: 'flex', alignItems: 'center', gap: '12px',
    padding: '10px 12px', borderRadius: '10px', border: 'none',
    background: 'none', cursor: 'pointer', width: '100%', textAlign: 'left',
    transition: 'background 0.15s',
  },
  navActive: { background: 'rgba(99,102,241,0.12)' },
  navLabel: { fontSize: '14px', fontWeight: '500' },
  userBlock: {
    display: 'flex', alignItems: 'center', gap: '10px',
    padding: '12px', borderRadius: '12px', background: '#263348',
    marginTop: '16px',
  },
  avatar: {
    width: '36px', height: '36px', borderRadius: '50%',
    background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontWeight: '700', color: '#fff', fontSize: '14px', flexShrink: 0,
  },
  userInfo: { flex: 1, overflow: 'hidden' },
  userName: {
    fontSize: '13px', fontWeight: '600', color: '#f1f5f9',
    whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
  },
  userRole: { fontSize: '11px', color: '#64748b' },
  logoutBtn: {
    background: 'none', border: 'none', cursor: 'pointer',
    display: 'flex', padding: '4px',
  },
};
