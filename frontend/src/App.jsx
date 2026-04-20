import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import DashboardPage from './pages/DashboardPage';
import BusTrackingPage from './pages/BusTrackingPage';
import StudentsPage from './pages/StudentsPage';
import TeachersPage from './pages/TeachersPage';
import NoticesPage from './pages/NoticesPage';
import AttendancePage from './pages/AttendancePage';
import FeePage from './pages/FeePage';
import LeavePage from './pages/LeavePage';
import AdmissionsPage from './pages/AdmissionsPage';
import AcademicSetupPage from './pages/AcademicSetupPage';
import PublicAdmissionPage from './pages/PublicAdmissionPage';
import DataSeedPage from './pages/DataSeedPage';
import ProfilePage from './pages/ProfilePage';
import MarksPage from './pages/MarksPage';
import TimetablePage from './pages/TimetablePage';
import AnalyticsPage from './pages/AnalyticsPage';
import TransportAdminPage from './pages/TransportAdminPage';
import BusSimulatorPage from './pages/BusSimulatorPage';
import Sidebar from './components/Sidebar';

// Protected layout — wraps all authenticated pages
function AppLayout({ children, allowedRoles }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={loadingStyle}>
        <div style={spinnerStyle} />
        <style>{`@keyframes spin{from{transform:rotate(0deg)}to{transform:rotate(360deg)}}`}</style>
      </div>
    );
  }

  if (!user) return <Navigate to="/login" replace />;

  // Role-based route guard
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, overflow: 'auto', background: '#0f172a' }}>
        {children}
      </main>
    </div>
  );
}

function Protected({ children, roles }) {
  return <AppLayout allowedRoles={roles}>{children}</AppLayout>;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/apply" element={<PublicAdmissionPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />

          {/* All authenticated */}
          <Route path="/dashboard"    element={<Protected><DashboardPage /></Protected>} />
          <Route path="/notices"      element={<Protected><NoticesPage /></Protected>} />
          <Route path="/leave"        element={<Protected><LeavePage /></Protected>} />
          <Route path="/bus-tracking" element={<Protected><BusTrackingPage /></Protected>} />

          {/* Admin only */}
          <Route path="/academic-setup" element={<Protected roles={['ADMIN']}><AcademicSetupPage /></Protected>} />
          <Route path="/seed-data"     element={<Protected roles={['ADMIN']}><DataSeedPage /></Protected>} />
          <Route path="/students"   element={<Protected roles={['ADMIN']}><StudentsPage /></Protected>} />
          <Route path="/admissions" element={<Protected roles={['ADMIN']}><AdmissionsPage /></Protected>} />
          <Route path="/teachers"   element={<Protected roles={['ADMIN']}><TeachersPage /></Protected>} />
          <Route path="/fee"        element={<Protected roles={['ADMIN']}><FeePage /></Protected>} />
          <Route path="/admin/transport" element={<Protected roles={['ADMIN']}><TransportAdminPage /></Protected>} />
          <Route path="/admin/simulator" element={<Protected roles={['ADMIN']}><BusSimulatorPage /></Protected>} />

          {/* Teacher + Admin */}
          <Route path="/attendance" element={<Protected roles={['TEACHER','ADMIN']}><AttendancePage /></Protected>} />
          <Route path="/marks"     element={<Protected roles={['TEACHER','ADMIN']}><MarksPage /></Protected>} />
          <Route path="/timetable" element={<Protected roles={['TEACHER','ADMIN','STUDENT','STAFF']}><TimetablePage /></Protected>} />

          {/* All authenticated */}
          <Route path="/profile"   element={<Protected><ProfilePage /></Protected>} />
          <Route path="/analytics" element={<Protected roles={['ADMIN']}><AnalyticsPage /></Protected>} />

          {/* Redirect root */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

const loadingStyle = {
  minHeight: '100vh', display: 'flex', flexDirection: 'column',
  alignItems: 'center', justifyContent: 'center', background: '#0f172a', gap: '16px',
};
const spinnerStyle = {
  width: '42px', height: '42px', borderRadius: '50%',
  border: '3px solid #334155', borderTopColor: '#6366f1',
  animation: 'spin 0.8s linear infinite',
};
