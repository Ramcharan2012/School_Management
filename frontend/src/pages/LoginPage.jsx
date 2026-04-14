import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Bus, GraduationCap, Lock, User, Eye, EyeOff, Loader2 } from 'lucide-react';

export default function LoginPage() {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(identifier, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      {/* Animated background blobs */}
      <div style={styles.blob1} />
      <div style={styles.blob2} />

      <div style={styles.card}>
        {/* Header */}
        <div style={styles.header}>
          <div style={styles.iconWrap}>
            <GraduationCap size={32} color="#6366f1" />
          </div>
          <h1 style={styles.title}>School Management</h1>
          <p style={styles.subtitle}>Sign in to your account</p>
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          {/* Identifier */}
          <div style={styles.fieldGroup}>
            <label style={styles.label}>Email / Username / Roll Number</label>
            <div style={styles.inputWrap}>
              <User size={16} color="#64748b" style={styles.inputIcon} />
              <input
                style={styles.input}
                type="text"
                placeholder="Enter email, username or roll number"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
                autoFocus
              />
            </div>
          </div>

          {/* Password */}
          <div style={styles.fieldGroup}>
            <label style={styles.label}>Password</label>
            <div style={styles.inputWrap}>
              <Lock size={16} color="#64748b" style={styles.inputIcon} />
              <input
                style={{ ...styles.input, paddingRight: '42px' }}
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                style={styles.eyeBtn}
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {/* Error */}
          {error && <div style={styles.error}>{error}</div>}

          {/* Submit */}
          <button type="submit" style={styles.btn} disabled={loading}>
            {loading ? (
              <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} />
            ) : (
              'Sign In'
            )}
          </button>
        </form>

        {/* Footer note */}
        <div style={{...styles.footer, flexDirection: 'column', gap: '16px'}}>
          <button 
            type="button" 
            onClick={() => navigate('/apply')}
            style={{...styles.btn, background: 'transparent', border: '1px solid #6366f1', color: '#818cf8', width: '100%'}}
          >
            Apply for Admission
          </button>
          
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Bus size={14} color="#64748b" />
            <span style={styles.footerText}>&nbsp;Real-time Bus Tracking Enabled</span>
          </div>
        </div>
      </div>

      <style>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#0f172a',
    position: 'relative',
    overflow: 'hidden',
    padding: '20px',
  },
  blob1: {
    position: 'fixed', top: '-100px', left: '-100px',
    width: '400px', height: '400px', borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(99,102,241,0.3), transparent)',
    filter: 'blur(60px)', pointerEvents: 'none',
  },
  blob2: {
    position: 'fixed', bottom: '-100px', right: '-100px',
    width: '400px', height: '400px', borderRadius: '50%',
    background: 'radial-gradient(circle, rgba(6,182,212,0.25), transparent)',
    filter: 'blur(60px)', pointerEvents: 'none',
  },
  card: {
    background: 'rgba(30,41,59,0.85)',
    backdropFilter: 'blur(20px)',
    border: '1px solid rgba(99,102,241,0.2)',
    borderRadius: '20px',
    padding: '48px 40px',
    width: '100%',
    maxWidth: '420px',
    boxShadow: '0 25px 60px rgba(0,0,0,0.5)',
    position: 'relative',
    zIndex: 1,
  },
  header: { textAlign: 'center', marginBottom: '36px' },
  iconWrap: {
    width: '64px', height: '64px', borderRadius: '16px',
    background: 'rgba(99,102,241,0.15)', display: 'flex',
    alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px',
    border: '1px solid rgba(99,102,241,0.3)',
  },
  title: { fontSize: '24px', fontWeight: '700', color: '#f1f5f9', marginBottom: '6px' },
  subtitle: { fontSize: '14px', color: '#64748b' },
  form: { display: 'flex', flexDirection: 'column', gap: '20px' },
  fieldGroup: { display: 'flex', flexDirection: 'column', gap: '8px' },
  label: { fontSize: '13px', fontWeight: '500', color: '#94a3b8' },
  inputWrap: { position: 'relative', display: 'flex', alignItems: 'center' },
  inputIcon: { position: 'absolute', left: '14px', pointerEvents: 'none' },
  input: {
    width: '100%', padding: '12px 14px 12px 38px',
    background: 'rgba(15,23,42,0.6)', border: '1px solid #334155',
    borderRadius: '10px', color: '#f1f5f9', fontSize: '14px', outline: 'none',
    transition: 'border-color 0.2s',
  },
  eyeBtn: {
    position: 'absolute', right: '12px', background: 'none',
    border: 'none', cursor: 'pointer', color: '#64748b', display: 'flex',
  },
  error: {
    background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.3)',
    borderRadius: '8px', padding: '10px 14px', color: '#f87171', fontSize: '13px',
  },
  btn: {
    padding: '13px', borderRadius: '10px',
    background: 'linear-gradient(135deg, #6366f1, #4f46e5)',
    color: '#fff', fontWeight: '600', fontSize: '15px',
    border: 'none', cursor: 'pointer', display: 'flex',
    alignItems: 'center', justifyContent: 'center', gap: '8px',
    transition: 'transform 0.1s',
  },
  footer: {
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    marginTop: '28px',
  },
  footerText: { fontSize: '12px', color: '#64748b' },
};
