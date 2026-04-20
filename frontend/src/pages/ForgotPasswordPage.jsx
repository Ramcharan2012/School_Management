import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { GraduationCap, Mail, Lock, ShieldCheck, ArrowLeft, Eye, EyeOff, CheckCircle2, Loader2 } from 'lucide-react';
import api from '../services/api';

const STEPS = { EMAIL: 'EMAIL', OTP: 'OTP', NEW_PW: 'NEW_PW', DONE: 'DONE' };

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(STEPS.EMAIL);
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPw, setNewPw] = useState('');
  const [confirmPw, setConfirmPw] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const requestOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/password-reset/request', { email });
      setStep(STEPS.OTP);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not send OTP. Please check your email.');
    } finally {
      setLoading(false);
    }
  };

  const verifyOtp = (e) => {
    e.preventDefault();
    if (otp.trim().length !== 6) {
      setError('Please enter the 6-digit OTP sent to your email.');
      return;
    }
    setError('');
    setStep(STEPS.NEW_PW);
  };

  const resetPassword = async (e) => {
    e.preventDefault();
    if (newPw !== confirmPw) { setError('Passwords do not match.'); return; }
    if (newPw.length < 8) { setError('Password must be at least 8 characters.'); return; }
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/password-reset/confirm', { email, otp, newPassword: newPw });
      setStep(STEPS.DONE);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid or expired OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const stepInfo = {
    [STEPS.EMAIL]: { title: 'Forgot Password', subtitle: 'Enter your email to receive a reset OTP', icon: Mail },
    [STEPS.OTP]: { title: 'Check Your Email', subtitle: `OTP sent to ${email}`, icon: ShieldCheck },
    [STEPS.NEW_PW]: { title: 'Set New Password', subtitle: 'Choose a strong new password', icon: Lock },
    [STEPS.DONE]: { title: 'Password Reset!', subtitle: 'You can now log in with your new password', icon: CheckCircle2 },
  };
  const current = stepInfo[step];
  const Icon = current.icon;

  return (
    <div style={s.page}>
      <div style={s.blob1} />
      <div style={s.blob2} />

      <div style={s.card}>
        {/* Back to login */}
        <button onClick={() => navigate('/login')} style={s.backBtn}>
          <ArrowLeft size={15} /> Back to login
        </button>

        {/* Header */}
        <div style={s.header}>
          <div style={s.iconWrap}>
            <GraduationCap size={24} color="#6366f1" />
          </div>
          <h1 style={s.title}>{current.title}</h1>
          <p style={s.subtitle}>{current.subtitle}</p>
        </div>

        {/* Step Progress */}
        {step !== STEPS.DONE && (
          <div style={s.progress}>
            {['Email', 'OTP', 'New Password'].map((label, i) => {
              const stepOrder = [STEPS.EMAIL, STEPS.OTP, STEPS.NEW_PW];
              const currentIdx = stepOrder.indexOf(step);
              const done = i < currentIdx;
              const active = i === currentIdx;
              return (
                <div key={label} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <div style={{
                    width: '24px', height: '24px', borderRadius: '50%', fontSize: '11px', fontWeight: '700',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    background: done ? '#22c55e' : active ? '#6366f1' : '#334155',
                    color: done || active ? '#fff' : '#475569',
                  }}>
                    {done ? '✓' : i + 1}
                  </div>
                  <span style={{ fontSize: '11px', color: active ? '#f1f5f9' : done ? '#22c55e' : '#475569', fontWeight: active ? '700' : '500' }}>{label}</span>
                  {i < 2 && <div style={{ flex: 1, height: '1px', background: done ? '#22c55e44' : '#334155', margin: '0 8px', minWidth: '20px' }} />}
                </div>
              );
            })}
          </div>
        )}

        {/* Error */}
        {error && (
          <div style={s.error}>{error}</div>
        )}

        {/* ── STEP 1: Enter Email ── */}
        {step === STEPS.EMAIL && (
          <form onSubmit={requestOtp} style={s.form}>
            <div style={s.fieldGroup}>
              <label style={s.label}>Registered Email Address</label>
              <div style={s.inputWrap}>
                <Mail size={16} color="#64748b" style={s.inputIcon} />
                <input
                  style={s.input}
                  type="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                  autoFocus
                />
              </div>
            </div>
            <button type="submit" style={s.btn} disabled={loading}>
              {loading ? <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} /> : 'Send Reset OTP'}
            </button>
          </form>
        )}

        {/* ── STEP 2: Enter OTP ── */}
        {step === STEPS.OTP && (
          <form onSubmit={verifyOtp} style={s.form}>
            <div style={s.otpInfo}>
              <Mail size={18} color="#6366f1" />
              <p style={{ color: '#94a3b8', fontSize: '13px', margin: 0 }}>
                We sent a <strong style={{ color: '#f1f5f9' }}>6-digit code</strong> to <strong style={{ color: '#6366f1' }}>{email}</strong>. Check your inbox (and spam folder).
              </p>
            </div>
            <div style={s.fieldGroup}>
              <label style={s.label}>Enter OTP Code</label>
              <input
                style={{ ...s.input, textAlign: 'center', fontSize: '22px', letterSpacing: '12px', fontWeight: '700', paddingLeft: '14px' }}
                type="text"
                maxLength={6}
                placeholder="• • • • • •"
                value={otp}
                onChange={e => setOtp(e.target.value.replace(/\D/g, ''))}
                required
                autoFocus
              />
            </div>
            <button type="submit" style={s.btn}>Verify OTP</button>
            <button type="button" style={s.resendBtn} onClick={() => { setStep(STEPS.EMAIL); setOtp(''); setError(''); }}>
              Resend OTP
            </button>
          </form>
        )}

        {/* ── STEP 3: New Password ── */}
        {step === STEPS.NEW_PW && (
          <form onSubmit={resetPassword} style={s.form}>
            <div style={s.fieldGroup}>
              <label style={s.label}>New Password</label>
              <div style={s.inputWrap}>
                <Lock size={16} color="#64748b" style={s.inputIcon} />
                <input
                  style={{ ...s.input, paddingRight: '42px' }}
                  type={showPw ? 'text' : 'password'}
                  placeholder="At least 8 characters"
                  value={newPw}
                  onChange={e => setNewPw(e.target.value)}
                  required
                  autoFocus
                />
                <button type="button" style={s.eyeBtn} onClick={() => setShowPw(!showPw)}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div style={s.fieldGroup}>
              <label style={s.label}>Confirm New Password</label>
              <div style={s.inputWrap}>
                <Lock size={16} color="#64748b" style={s.inputIcon} />
                <input
                  style={s.input}
                  type={showPw ? 'text' : 'password'}
                  placeholder="Re-enter new password"
                  value={confirmPw}
                  onChange={e => setConfirmPw(e.target.value)}
                  required
                />
              </div>
            </div>
            {/* Password strength hints */}
            <div style={s.hints}>
              {[
                { ok: newPw.length >= 8, text: 'At least 8 characters' },
                { ok: /[A-Z]/.test(newPw), text: 'Uppercase letter' },
                { ok: /[0-9]/.test(newPw), text: 'Number' },
                { ok: /[^A-Za-z0-9]/.test(newPw), text: 'Special character' },
              ].map(h => (
                <span key={h.text} style={{ ...s.hint, color: h.ok ? '#22c55e' : '#475569' }}>
                  {h.ok ? '✓' : '○'} {h.text}
                </span>
              ))}
            </div>
            <button type="submit" style={s.btn} disabled={loading}>
              {loading ? <Loader2 size={18} style={{ animation: 'spin 1s linear infinite' }} /> : 'Reset Password'}
            </button>
          </form>
        )}

        {/* ── STEP 4: Done ── */}
        {step === STEPS.DONE && (
          <div style={s.doneBox}>
            <div style={s.doneIcon}>
              <CheckCircle2 size={40} color="#22c55e" />
            </div>
            <p style={{ color: '#94a3b8', fontSize: '14px', textAlign: 'center', margin: '0 0 24px' }}>
              Your password has been reset successfully. You can now log in with your new credentials.
            </p>
            <button style={s.btn} onClick={() => navigate('/login')}>
              Go to Login
            </button>
          </div>
        )}
      </div>

      <style>{`@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}

const s = {
  page: {
    minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center',
    background: '#0f172a', position: 'relative', overflow: 'hidden', padding: '20px',
  },
  blob1: {
    position: 'fixed', top: '-100px', left: '-100px', width: '400px', height: '400px',
    borderRadius: '50%', background: 'radial-gradient(circle, rgba(99,102,241,0.3), transparent)',
    filter: 'blur(60px)', pointerEvents: 'none',
  },
  blob2: {
    position: 'fixed', bottom: '-100px', right: '-100px', width: '400px', height: '400px',
    borderRadius: '50%', background: 'radial-gradient(circle, rgba(6,182,212,0.25), transparent)',
    filter: 'blur(60px)', pointerEvents: 'none',
  },
  card: {
    background: 'rgba(30,41,59,0.9)', backdropFilter: 'blur(20px)',
    border: '1px solid rgba(99,102,241,0.2)', borderRadius: '20px',
    padding: '40px', width: '100%', maxWidth: '440px',
    boxShadow: '0 25px 60px rgba(0,0,0,0.5)', position: 'relative', zIndex: 1,
  },
  backBtn: {
    display: 'flex', alignItems: 'center', gap: '6px',
    background: 'none', border: 'none', cursor: 'pointer',
    color: '#64748b', fontSize: '13px', fontWeight: '500',
    padding: '0 0 20px', fontFamily: 'Inter, sans-serif',
  },
  header: { textAlign: 'center', marginBottom: '24px' },
  iconWrap: {
    width: '56px', height: '56px', borderRadius: '14px',
    background: 'rgba(99,102,241,0.15)', display: 'flex',
    alignItems: 'center', justifyContent: 'center', margin: '0 auto 14px',
    border: '1px solid rgba(99,102,241,0.3)',
  },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: '0 0 6px' },
  subtitle: { fontSize: '13px', color: '#64748b', margin: 0 },
  progress: {
    display: 'flex', alignItems: 'center', marginBottom: '24px',
    background: 'rgba(15,23,42,0.5)', borderRadius: '10px', padding: '12px 16px',
  },
  error: {
    background: 'rgba(239,68,68,0.12)', border: '1px solid rgba(239,68,68,0.3)',
    borderRadius: '8px', padding: '10px 14px', color: '#f87171', fontSize: '13px',
    marginBottom: '16px',
  },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  fieldGroup: { display: 'flex', flexDirection: 'column', gap: '7px' },
  label: { fontSize: '12px', fontWeight: '600', color: '#94a3b8' },
  inputWrap: { position: 'relative', display: 'flex', alignItems: 'center' },
  inputIcon: { position: 'absolute', left: '14px', pointerEvents: 'none' },
  input: {
    width: '100%', padding: '12px 14px 12px 38px',
    background: 'rgba(15,23,42,0.7)', border: '1px solid #334155',
    borderRadius: '10px', color: '#f1f5f9', fontSize: '14px', outline: 'none',
    fontFamily: 'Inter, sans-serif', boxSizing: 'border-box',
  },
  eyeBtn: {
    position: 'absolute', right: '12px', background: 'none',
    border: 'none', cursor: 'pointer', color: '#64748b', display: 'flex',
  },
  btn: {
    padding: '13px', borderRadius: '10px',
    background: 'linear-gradient(135deg, #6366f1, #4f46e5)',
    color: '#fff', fontWeight: '700', fontSize: '14px',
    border: 'none', cursor: 'pointer',
    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
    fontFamily: 'Inter, sans-serif',
  },
  resendBtn: {
    background: 'none', border: '1px solid #334155', borderRadius: '10px',
    color: '#64748b', fontSize: '13px', padding: '10px', cursor: 'pointer',
    fontFamily: 'Inter, sans-serif',
  },
  otpInfo: {
    display: 'flex', gap: '12px', alignItems: 'flex-start',
    background: 'rgba(99,102,241,0.08)', border: '1px solid rgba(99,102,241,0.2)',
    borderRadius: '10px', padding: '12px 14px',
  },
  hints: { display: 'flex', flexWrap: 'wrap', gap: '8px' },
  hint: { fontSize: '11px', fontWeight: '500' },
  doneBox: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' },
  doneIcon: {
    width: '80px', height: '80px', borderRadius: '50%',
    background: 'rgba(34,197,94,0.1)', border: '2px solid rgba(34,197,94,0.3)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
};
