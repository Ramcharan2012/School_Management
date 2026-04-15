import React, { useState, useEffect } from 'react';
import { Bus, Map, MapPin, Plus, Trash2, Crosshair } from 'lucide-react';
import { transportAPI } from '../services/api';

export default function TransportAdminPage() {
  const [routes, setRoutes] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [activeTab, setActiveTab] = useState('ROUTES'); // ROUTES or VEHICLES
  const [loading, setLoading] = useState(false);

  // Forms
  const [routeForm, setRouteForm] = useState({ routeName: '', description: '' });
  const [vehicleForm, setVehicleForm] = useState({ vehicleNumber: '', capacity: 40, driverName: '', driverPhone: '', routeId: '' });
  const [stopForm, setStopForm] = useState({ stopName: '', latitude: '', longitude: '', stopOrder: 1, pickupTime: '07:30', dropTime: '16:30' });
  const [selectedRouteForStops, setSelectedRouteForStops] = useState(null);
  const [routeStops, setRouteStops] = useState([]);

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [rRes, vRes] = await Promise.all([
        transportAPI.getRoutes().catch(() => ({ data: { data: [] } })),
        transportAPI.getVehicles().catch(() => ({ data: { data: [] } }))
      ]);
      setRoutes(rRes.data.data || []);
      setVehicles(vRes.data.data || []);
    } finally { setLoading(false); }
  };

  const loadStops = async (routeId) => {
    try {
      const res = await transportAPI.getStops(routeId);
      setRouteStops(res.data.data || []);
    } catch {}
  };

  const createRoute = async (e) => {
    e.preventDefault();
    try {
      await transportAPI.createRoute(routeForm);
      setRouteForm({ routeName: '', description: '' });
      loadData();
    } catch (err) { alert('Failed to create route'); }
  };

  const createVehicle = async (e) => {
    e.preventDefault();
    try {
      await transportAPI.createVehicle({ ...vehicleForm, routeId: vehicleForm.routeId || null });
      setVehicleForm({ vehicleNumber: '', capacity: 40, driverName: '', driverPhone: '', routeId: '' });
      loadData();
    } catch (err) { alert('Failed to create vehicle'); }
  };

  const createStop = async (e) => {
    e.preventDefault();
    try {
      await transportAPI.addStop(selectedRouteForStops.id, stopForm);
      setStopForm({ stopName: '', latitude: '', longitude: '', stopOrder: routeStops.length + 2, pickupTime: '07:30', dropTime: '16:30' });
      loadStops(selectedRouteForStops.id);
    } catch (err) { alert('Failed to add stop'); }
  };

  return (
    <div style={s.page}>
      <div style={s.header}>
        <div style={s.headerLeft}>
          <div style={s.iconWrap}><Bus size={24} color="#6366f1" /></div>
          <div>
            <h1 style={s.title}>Transport Setup</h1>
            <p style={s.subtitle}>Manage bus routes, stops, and fleet</p>
          </div>
        </div>
      </div>

      <div style={s.tabs}>
        <button style={{...s.tabBtn, ...(activeTab === 'ROUTES' ? s.tabActive : {})}} onClick={() => setActiveTab('ROUTES')}><Map size={16} /> Routes & Stops</button>
        <button style={{...s.tabBtn, ...(activeTab === 'VEHICLES' ? s.tabActive : {})}} onClick={() => setActiveTab('VEHICLES')}><Bus size={16} /> Fleet Management</button>
      </div>

      {activeTab === 'ROUTES' && (
        <div style={s.grid}>
          {/* Create Route */}
          <div style={s.card}>
            <h2 style={s.cardTitle}>Create New Route</h2>
            <form onSubmit={createRoute} style={s.form}>
              <input style={s.input} placeholder="Route Name (e.g. Route A - Kukatpally)" value={routeForm.routeName} onChange={e => setRouteForm({...routeForm, routeName: e.target.value})} required />
              <input style={s.input} placeholder="Description" value={routeForm.description} onChange={e => setRouteForm({...routeForm, description: e.target.value})} required />
              <button style={s.btnPrimary} type="submit"><Plus size={16} /> Create Route</button>
            </form>

            <h3 style={{...s.cardTitle, marginTop: '24px'}}>Existing Routes</h3>
            <div style={s.list}>
              {routes.map(r => (
                <div key={r.id} style={{...s.listItem, ...(selectedRouteForStops?.id === r.id ? s.listActive : {})}} onClick={() => { setSelectedRouteForStops(r); loadStops(r.id); }}>
                  <Map size={16} color="#818cf8" />
                  <span style={{ fontSize: '13px', fontWeight: '600' }}>{r.routeName}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Manage Stops */}
          <div style={s.card}>
            <h2 style={s.cardTitle}>Route Stops</h2>
            {!selectedRouteForStops ? (
              <p style={{ color: '#64748b', fontSize: '13px' }}>Select a route from the left to manage stops.</p>
            ) : (
              <>
                <p style={{ fontSize: '13px', color: '#f1f5f9', marginBottom: '16px', fontWeight: '600' }}>Stops for {selectedRouteForStops.routeName}</p>
                <form onSubmit={createStop} style={{...s.form, background: '#0f172a', padding: '16px', borderRadius: '12px'}}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 60px', gap: '8px' }}>
                    <input style={s.input} placeholder="Stop Name (e.g. KPHB Metro)" value={stopForm.stopName} onChange={e => setStopForm({...stopForm, stopName: e.target.value})} required />
                    <input style={s.input} type="number" placeholder="Order" value={stopForm.stopOrder} onChange={e => setStopForm({...stopForm, stopOrder: e.target.value})} required />
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                    <input style={s.input} step="0.0001" placeholder="Latitude" value={stopForm.latitude} onChange={e => setStopForm({...stopForm, latitude: e.target.value})} required />
                    <input style={s.input} step="0.0001" placeholder="Longitude" value={stopForm.longitude} onChange={e => setStopForm({...stopForm, longitude: e.target.value})} required />
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                    <input style={s.input} type="time" value={stopForm.pickupTime} onChange={e => setStopForm({...stopForm, pickupTime: e.target.value})} />
                    <input style={s.input} type="time" value={stopForm.dropTime} onChange={e => setStopForm({...stopForm, dropTime: e.target.value})} />
                  </div>
                  <button style={s.btnPrimary} type="submit"><MapPin size={16} /> Add Stop</button>
                </form>

                <div style={{...s.list, marginTop: '20px'}}>
                  {routeStops.map(s => (
                    <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', background: '#1e293b', border: '1px solid #334155', borderRadius: '8px', fontSize: '12px' }}>
                      <div>
                        <strong style={{ color: '#f1f5f9' }}>{s.stopOrder}. {s.stopName}</strong><br/>
                        <span style={{ color: '#64748b' }}>Pickup: {s.pickupTime} | Lat/Lng: {s.latitude}, {s.longitude}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {activeTab === 'VEHICLES' && (
        <div style={s.grid}>
          <div style={s.card}>
            <h2 style={s.cardTitle}>Register Vehicle</h2>
            <form onSubmit={createVehicle} style={s.form}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <input style={s.input} placeholder="Vehicle Number (e.g. AP-09-1234)" value={vehicleForm.vehicleNumber} onChange={e => setVehicleForm({...vehicleForm, vehicleNumber: e.target.value})} required />
                <input style={s.input} type="number" placeholder="Capacity" value={vehicleForm.capacity} onChange={e => setVehicleForm({...vehicleForm, capacity: Number(e.target.value)})} required />
              </div>
              <input style={s.input} placeholder="Driver Name" value={vehicleForm.driverName} onChange={e => setVehicleForm({...vehicleForm, driverName: e.target.value})} required />
              <input style={s.input} placeholder="Driver Phone" value={vehicleForm.driverPhone} onChange={e => setVehicleForm({...vehicleForm, driverPhone: e.target.value})} required />
              <select style={s.input} value={vehicleForm.routeId} onChange={e => setVehicleForm({...vehicleForm, routeId: e.target.value})}>
                <option value="">-- Assign to Route (Optional) --</option>
                {routes.map(r => <option key={r.id} value={r.id}>{r.routeName}</option>)}
              </select>
              <button style={s.btnPrimary} type="submit"><Bus size={16} /> Register Fleet</button>
            </form>
          </div>

          <div style={s.card}>
            <h2 style={s.cardTitle}>Active Fleet</h2>
            <div style={s.list}>
              {vehicles.map(v => (
                <div key={v.id} style={{ padding: '14px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                    <strong style={{ fontSize: '15px', color: '#6366f1' }}>{v.vehicleNumber}</strong>
                    <span style={{ fontSize: '11px', color: '#94a3b8', background: '#1e293b', padding: '2px 8px', borderRadius: '10px' }}>{v.capacity} seats</span>
                  </div>
                  <div style={{ fontSize: '12px', color: '#cbd5e1', lineHeight: '1.6' }}>
                    Driver: {v.driverName} ({v.driverPhone})<br/>
                    Route: {v.route?.routeName || 'Unassigned'}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '14px' },
  iconWrap: { width: '46px', height: '46px', borderRadius: '12px', background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: 0 },
  subtitle: { fontSize: '13px', color: '#64748b', margin: '4px 0 0' },
  tabs: { display: 'flex', gap: '12px', marginBottom: '20px', borderBottom: '1px solid #1e293b', paddingBottom: '12px' },
  tabBtn: { display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px', background: 'transparent', border: 'none', color: '#64748b', fontSize: '14px', fontWeight: '600', cursor: 'pointer', borderRadius: '8px' },
  tabActive: { background: '#1e293b', color: '#f1f5f9' },
  grid: { display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1fr)', gap: '20px' },
  card: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '24px' },
  cardTitle: { fontSize: '16px', fontWeight: '700', color: '#f1f5f9', marginBottom: '16px' },
  form: { display: 'flex', flexDirection: 'column', gap: '12px' },
  input: { padding: '10px 14px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', fontSize: '13px', outline: 'none' },
  btnPrimary: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', padding: '12px', background: 'linear-gradient(135deg, #6366f1, #4f46e5)', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', marginTop: '8px' },
  list: { display: 'flex', flexDirection: 'column', gap: '8px' },
  listItem: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 16px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', cursor: 'pointer', transition: 'all 0.15s' },
  listActive: { borderColor: '#6366f1', background: 'rgba(99,102,241,0.1)' }
};
