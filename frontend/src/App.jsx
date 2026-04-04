import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import BusTrackingPage from './pages/BusTrackingPage';
import Sidebar from './components/Sidebar';

// Protected layout — wraps all authenticated pages
function AppLayout({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={loadingStyle}>
        <div style={spinnerStyle} />
      </div>
    );
  }

  if (!user) return <Navigate to="/login" replace />;

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{ flex: 1, overflow: 'auto', background: '#0f172a' }}>
        {children}
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={
              <AppLayout>
                <DashboardPage />
              </AppLayout>
            }
          />
          <Route
            path="/bus-tracking"
            element={
              <AppLayout>
                <BusTrackingPage />
              </AppLayout>
            }
          />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

const loadingStyle = {
  minHeight: '100vh', display: 'flex',
  alignItems: 'center', justifyContent: 'center',
  background: '#0f172a',
};
const spinnerStyle = {
  width: '40px', height: '40px', borderRadius: '50%',
  border: '3px solid #334155', borderTopColor: '#6366f1',
  animation: 'spin 0.8s linear infinite',
};
