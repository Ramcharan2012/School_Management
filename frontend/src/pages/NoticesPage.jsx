import React, { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { noticeAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import PageHeader from '../components/PageHeader';
import Badge from '../components/Badge';
import Modal, { Field, inputStyle, selectStyle } from '../components/Modal';

function formatDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// Notice target enum from backend: ALL | STUDENTS | TEACHERS | PARENTS | STAFF
const TARGET_OPTIONS = ['ALL', 'STUDENTS', 'TEACHERS', 'PARENTS', 'STAFF'];

export default function NoticesPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  // POST /admin/notices body fields from backend CreateNoticeRequest:
  // title, content, targetAudience (NoticeTarget enum), isPinned, expiresAt, attachmentUrl, postedByUserId
  const [form, setForm] = useState({
    title: '',
    content: '',
    targetAudience: 'ALL',
    isPinned: false,
    expiresAt: '',
    attachmentUrl: '',
  });

  const load = () => {
    setLoading(true);
    // GET /notices?target=ALL&page=0
    noticeAPI.getAll('ALL', 0)
      .then(r => {
        // ApiResponse<PageResponse<Notice>>
        const pg = r.data.data;
        setNotices(pg.content ?? pg ?? []);
      })
      .catch(() => setNotices([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      // Backend expects: { title, content, targetAudience, isPinned, expiresAt, attachmentUrl, postedByUserId }
      await noticeAPI.create({
        title: form.title,
        content: form.content,
        targetAudience: form.targetAudience,
        isPinned: form.isPinned,
        expiresAt: form.expiresAt || null,
        attachmentUrl: form.attachmentUrl || null,
        postedByUserId: null,  // backend resolves from Principal / admin
      });
      setShowModal(false);
      setForm({ title: '', content: '', targetAudience: 'ALL', isPinned: false, expiresAt: '', attachmentUrl: '' });
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create notice');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this notice?')) return;
    try {
      await noticeAPI.delete(id);
      load();
    } catch (err) {
      alert('Failed to delete notice');
    }
  };

  const f = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.value }));
  const fBool = (k) => (e) => setForm(p => ({ ...p, [k]: e.target.checked }));

  return (
    <div style={{ padding: '32px', flex: 1, overflow: 'auto' }}>
      <PageHeader
        title="Notices" icon={Bell}
        subtitle="School announcements and updates"
        action={isAdmin ? 'Post Notice' : undefined}
        onAction={() => setShowModal(true)}
      />

      {loading ? (
        <div style={styles.loadingMsg}>Loading notices...</div>
      ) : notices.length === 0 ? (
        <div style={styles.emptyMsg}>
          No notices posted yet.
          {isAdmin && ' Click "Post Notice" to add the first one.'}
        </div>
      ) : (
        <div style={styles.grid}>
          {notices.map(n => (
            <div key={n.id} style={{ ...styles.card, ...(n.isPinned ? styles.pinnedCard : {}) }}>
              <div style={styles.cardTop}>
                <div style={styles.cardMeta}>
                  <Badge status={n.targetAudience || 'ALL'} />
                  {n.isPinned && <span style={styles.pinBadge}>📌 Pinned</span>}
                  <span style={styles.date}>{formatDate(n.createdAt)}</span>
                </div>
                {isAdmin && (
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button style={styles.pinBtn} onClick={() => noticeAPI.togglePin(n.id).then(load)} title="Toggle pin">
                      📌
                    </button>
                    <button style={styles.deleteBtn} onClick={() => handleDelete(n.id)} title="Delete">✕</button>
                  </div>
                )}
              </div>
              <h3 style={styles.cardTitle}>{n.title}</h3>
              <p style={styles.cardContent}>{n.content}</p>
              {n.expiresAt && (
                <div style={styles.expiry}>Expires: {formatDate(n.expiresAt)}</div>
              )}
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <Modal title="Post New Notice" onClose={() => setShowModal(false)} width="520px">
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <Field label="Title *">
              <input style={inputStyle} value={form.title} onChange={f('title')} required />
            </Field>
            <Field label="Content *">
              <textarea
                style={{ ...inputStyle, minHeight: '100px', resize: 'vertical' }}
                value={form.content} onChange={f('content')} required
              />
            </Field>
            <Field label="Target Audience">
              <select style={selectStyle} value={form.targetAudience} onChange={f('targetAudience')}>
                {TARGET_OPTIONS.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </Field>
            <Field label="Expires At (optional)">
              <input style={inputStyle} type="datetime-local" value={form.expiresAt} onChange={f('expiresAt')} />
            </Field>
            <Field label="Attachment URL (optional)">
              <input style={inputStyle} placeholder="https://..." value={form.attachmentUrl} onChange={f('attachmentUrl')} />
            </Field>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', fontSize: '13px', cursor: 'pointer' }}>
              <input type="checkbox" checked={form.isPinned} onChange={fBool('isPinned')} />
              Pin this notice (shows at top)
            </label>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '8px' }}>
              <button type="button" style={styles.cancelBtn} onClick={() => setShowModal(false)}>Cancel</button>
              <button type="submit" style={styles.submitBtn} disabled={saving}>
                {saving ? 'Posting...' : 'Post Notice'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

const styles = {
  loadingMsg: { color: '#64748b', textAlign: 'center', padding: '40px' },
  emptyMsg: { color: '#64748b', textAlign: 'center', padding: '60px', background: '#1e293b', borderRadius: '14px', border: '1px dashed #334155' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' },
  card: { background: '#1e293b', border: '1px solid #334155', borderRadius: '14px', padding: '20px', display: 'flex', flexDirection: 'column', gap: '10px' },
  pinnedCard: { border: '1px solid rgba(99,102,241,0.4)', boxShadow: '0 0 20px rgba(99,102,241,0.08)' },
  cardTop: { display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '8px' },
  cardMeta: { display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' },
  date: { fontSize: '11px', color: '#64748b' },
  pinBadge: { fontSize: '11px', color: '#6366f1' },
  deleteBtn: { background: 'none', border: 'none', cursor: 'pointer', color: '#64748b', fontSize: '13px', padding: '2px 6px' },
  pinBtn: { background: 'none', border: 'none', cursor: 'pointer', fontSize: '13px', padding: '2px 6px' },
  cardTitle: { fontSize: '15px', fontWeight: '700', color: '#f1f5f9', lineHeight: 1.3 },
  cardContent: { fontSize: '13px', color: '#94a3b8', lineHeight: 1.6, flex: 1, whiteSpace:'pre-wrap' },
  expiry: { fontSize: '11px', color: '#f59e0b', paddingTop: '6px', borderTop: '1px solid #263348' },
  cancelBtn: { padding: '9px 18px', borderRadius: '8px', border: '1px solid #334155', background: 'none', color: '#94a3b8', cursor: 'pointer' },
  submitBtn: { padding: '9px 20px', borderRadius: '8px', background: 'linear-gradient(135deg,#6366f1,#4f46e5)', color: '#fff', border: 'none', cursor: 'pointer', fontWeight: '600' },
};
