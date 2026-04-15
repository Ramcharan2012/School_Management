import React, { useState, useEffect, useRef } from 'react';
import { Bus, Map, Play, Square, FastForward, Activity } from 'lucide-react';
import { transportAPI } from '../services/api';

// Demo route in Hyderabad (Kukatpally -> Ameerpet -> Jubilee Hills -> HITEC City)
const DEMO_ROUTE_COORDS = [
  [17.4948, 78.3996],
  [17.4850, 78.4050],
  [17.4700, 78.4200],
  [17.4375, 78.4482],
  [17.4350, 78.4300],
  [17.4325, 78.4070],
  [17.4400, 78.3900],
  [17.4474, 78.3762]
];

// Helper to interpolate between two points to make it smooth
function interpolate(p1, p2, steps) {
  const points = [];
  for (let i = 0; i <= steps; i++) {
    const lat = p1[0] + (p2[0] - p1[0]) * (i / steps);
    const lng = p1[1] + (p2[1] - p1[1]) * (i / steps);
    points.push([lat, lng]);
  }
  return points;
}

// Generate a smooth path with 30 steps between each major coordinate
const smoothPath = [];
for (let i = 0; i < DEMO_ROUTE_COORDS.length - 1; i++) {
  smoothPath.push(...interpolate(DEMO_ROUTE_COORDS[i], DEMO_ROUTE_COORDS[i + 1], 20));
}

export default function BusSimulatorPage() {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [speed, setSpeed] = useState(1); // 1x, 2x, etc
  const [currentStep, setCurrentStep] = useState(0);
  const [logs, setLogs] = useState([]);

  const timerRef = useRef(null);

  useEffect(() => {
    transportAPI.getVehicles().then(res => setVehicles(res.data.data || []));
    return stopSimulator;
  }, []);

  const addLog = (msg) => {
    setLogs(prev => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev].slice(0, 15));
  };

  const startSimulator = () => {
    if (!selectedVehicle) return alert('Select a vehicle first');
    setIsRunning(true);
    addLog('Simulator started.');
    
    timerRef.current = setInterval(() => {
      setCurrentStep(prev => {
        let next = prev + 1;
        if (next >= smoothPath.length) {
          next = 0; // Loop around
          addLog('Reached destination. Looping route.');
        }
        sendGpsPoint(next);
        return next;
      });
    }, 2000 / speed); // send every 2 seconds by default
  };

  const stopSimulator = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    setIsRunning(false);
    addLog('Simulator stopped.');
  };

  const sendGpsPoint = async (stepIndex) => {
    const v = vehicles.find(v => v.id.toString() === selectedVehicle);
    if (!v) return;
    
    const [lat, lng] = smoothPath[stepIndex];
    // Random fake speed around 40kmph
    const fakeSpeed = 35 + (Math.random() * 15);
    
    try {
      await transportAPI.sendLocation({
        vehicleId: v.id,
        vehicleNumber: v.vehicleNumber,
        latitude: lat,
        longitude: lng,
        speed: fakeSpeed
      });
      addLog(`GPS sent (Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)})`);
    } catch (e) {
      addLog(`Failed to send GPS: ${e.message}`);
    }
  };

  return (
    <div style={s.page}>
      <div style={s.header}>
        <div style={s.headerLeft}>
          <div style={s.iconWrap}><Activity size={24} color="#ec4899" /></div>
          <div>
            <h1 style={s.title}>Driver Simulator</h1>
            <p style={s.subtitle}>Mock live GPS data for demo purposes</p>
          </div>
        </div>
        <div style={s.headerAlert}>
          <strong style={{color:"#f1f5f9"}}>Demo Instructions:</strong> Open "Live Tracking" in another window.<br/>Select a vehicle here and click Start. The bus will start moving on the other window automatically!
        </div>
      </div>

      <div style={s.grid}>
        <div style={s.card}>
          <h2 style={s.cardTitle}>Simulator Controls</h2>
          
          <div style={{ marginBottom: '20px' }}>
            <label style={s.label}>Select Vehicle to Simulate</label>
            <select style={s.input} value={selectedVehicle} onChange={e => setSelectedVehicle(e.target.value)} disabled={isRunning}>
              <option value="">-- Choose Vehicle --</option>
              {vehicles.map(v => <option key={v.id} value={v.id}>{v.vehicleNumber}</option>)}
            </select>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label style={s.label}>Simulation Speed</label>
            <select style={s.input} value={speed} onChange={e => {
               setSpeed(Number(e.target.value));
               if (isRunning) { stopSimulator(); setTimeout(startSimulator, 100); }
            }}>
              <option value="1">1x (Every 2 seconds)</option>
              <option value="2">2x (Every 1 second)</option>
              <option value="4">4x (Every 0.5 seconds)</option>
            </select>
          </div>

          <div style={{ display: 'flex', gap: '10px' }}>
            {!isRunning ? (
              <button style={{...s.btn, background: '#22c55e'}} onClick={startSimulator}>
                <Play size={18} /> Start Driving
              </button>
            ) : (
              <button style={{...s.btn, background: '#ef4444'}} onClick={stopSimulator}>
                <Square size={18} /> Stop Driving
              </button>
            )}
          </div>
          
          <div style={{ marginTop: '20px', padding: '12px', background: 'rgba(99,102,241,0.1)', borderRadius: '8px', border: '1px solid rgba(99,102,241,0.2)', fontSize: '13px', color: '#818cf8' }}>
            <strong>Route info:</strong> {smoothPath.length} coordinate points. Following path from Kukatpally to HITEC City.
            <div style={{ marginTop: '5px' }}>Current Progress: {Math.round((currentStep / smoothPath.length) * 100)}%</div>
          </div>
        </div>

        <div style={s.card}>
          <h2 style={s.cardTitle}>Live GPS Logs</h2>
          <div style={s.logger}>
            {logs.length === 0 && <div style={{ color: '#64748b', fontStyle: 'italic' }}>Waiting for simulator to start...</div>}
            {logs.map((L, i) => (
              <div key={i} style={{ padding: '8px 0', borderBottom: '1px dashed #334155', fontSize: '12px', color: '#cbd5e1', fontFamily: 'monospace' }}>
                {L}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  header: { display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '24px', flexWrap: 'wrap', gap: '20px' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '14px' },
  iconWrap: { width: '46px', height: '46px', borderRadius: '12px', background: 'rgba(236,72,153,0.1)', border: '1px solid rgba(236,72,153,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', margin: 0 },
  subtitle: { fontSize: '13px', color: '#64748b', margin: '4px 0 0' },
  headerAlert: { flex: 1, minWidth: '300px', background: 'rgba(245,158,11,0.1)', border: '1px solid rgba(245,158,11,0.2)', padding: '14px', borderRadius: '12px', fontSize: '13px', color: '#fcd34d', lineHeight: 1.5 },
  grid: { display: 'grid', gridTemplateColumns: 'minmax(0, 400px) minmax(0, 1fr)', gap: '20px' },
  card: { background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', padding: '24px' },
  cardTitle: { fontSize: '16px', fontWeight: '700', color: '#f1f5f9', marginBottom: '20px' },
  label: { display: 'block', fontSize: '13px', color: '#94a3b8', marginBottom: '6px', fontWeight: '500' },
  input: { width: '100%', padding: '12px 14px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', fontSize: '14px', outline: 'none' },
  btn: { flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', padding: '14px', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', transition: 'transform 0.1s' },
  logger: { background: '#020617', padding: '16px', borderRadius: '12px', minHeight: '300px', overflowY: 'auto', border: '1px solid #1e293b' }
};
