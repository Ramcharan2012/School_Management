import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount: restore user from localStorage
  useEffect(() => {
    const token = localStorage.getItem('token');
    const stored = localStorage.getItem('user');
    if (token && stored) {
      try {
        setUser(JSON.parse(stored));
      } catch (_) {}
    }
    setLoading(false);
  }, []);

  const login = async (identifier, password) => {
    // POST /auth/login → ApiResponse<LoginResponse>
    // LoginResponse fields: accessToken, refreshToken, userId, username,
    //                       email, fullName, role, isFirstLogin
    const res = await authAPI.login(identifier, password);
    const d = res.data.data;

    // Store token
    localStorage.setItem('token', d.accessToken);

    // Build user object with all fields needed across the app
    const userObj = {
      id: d.userId,              // used by LeavePage (applicantId), AttendancePage (teacherId)
      username: d.username,
      email: d.email,
      fullName: d.fullName,
      role: d.role,              // "ADMIN" | "TEACHER" | "STUDENT" | "STAFF"
      isFirstLogin: d.isFirstLogin,
    };
    localStorage.setItem('user', JSON.stringify(userObj));
    setUser(userObj);

    return d.role;
  };

  const logout = async () => {
    await authAPI.logout().catch(() => {});
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
