import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { transportAPI } from '../services/api';
import { subscribeToBus } from '../services/websocket';
import { Bus, MapPin, Radio, Wifi, WifiOff, RefreshCw } from 'lucide-react';

// Fix default Leaflet marker icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Custom bus icon
const busIcon = L.divIcon({
  html: `<div style="
    background: linear-gradient(135deg,#6366f1,#4f46e5);
    width: 40px; height: 40px; border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    border: 3px solid white; box-shadow: 0 4px 12px rgba(99,102,241,0.6);
    font-size: 20px;
  ">🚌</div>`,
  className: '',
  iconSize: [40, 40],
  iconAnchor: [20, 20],
});

// Custom stop icon
const stopIcon = L.divIcon({
  html: `<div style="
    background:#ef4444; width:14px; height:14px; border-radius:50%;
    border:3px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.4);
  "></div>`,
  className: '',
  iconSize: [14, 14],
  iconAnchor: [7, 7],
});

// Default map center (Chennai — change to your school's GPS)
const DEFAULT_CENTER = [13.0827, 80.2707];

export default function BusTrackingPage() {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [stops, setStops] = useState([]);
  const [busPosition, setBusPosition] = useState(null);
  const [connected, setConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);
  const [speed, setSpeed] = useState(null);
  const disconnectRef = useRef(null);

  // Fetch all vehicles on mount
  useEffect(() => {
    transportAPI.getVehicles()
      .then((res) => {
        const v = res.data.data || [];
        setVehicles(v);
        if (v.length > 0) setSelectedVehicle(v[0]);
      })
      .catch(() => setVehicles([]));
  }, []);

  // When vehicle selected: fetch stops + connect WebSocket + poll Redis
  useEffect(() => {
    if (!selectedVehicle) return;

    // Fetch route stops
    if (selectedVehicle.route?.id) {
      transportAPI.getStops(selectedVehicle.route.id)
        .then((res) => setStops(res.data.data || []))
        .catch(() => setStops([]));
    }

    // Poll Redis for initial/current location
    transportAPI.getBusLocation(selectedVehicle.id)
      .then((res) => {
        const loc = res.data.data;
        if (loc?.latitude) {
          setBusPosition([parseFloat(loc.latitude), parseFloat(loc.longitude)]);
          setSpeed(loc.speed);
          setLastUpdate(loc.timestamp);
        }
      })
      .catch(() => {});

    // Disconnect previous WebSocket
    if (disconnectRef.current) disconnectRef.current();

    // Subscribe to live WebSocket updates
    disconnectRef.current = subscribeToBus(selectedVehicle.id, (event) => {
      setBusPosition([event.latitude, event.longitude]);
      setSpeed(event.speed);
      setLastUpdate(new Date().toLocaleTimeString());
      setConnected(true);
    });
    setConnected(false); // will flip to true on first WS message

    return () => {
      if (disconnectRef.current) disconnectRef.current();
    };
  }, [selectedVehicle]);

  return (
    <div style={styles.page}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <h1 style={styles.title}>🚌 Live Bus Tracking</h1>
          <p style={styles.subtitle}>Real-time GPS via Kafka + WebSockets</p>
        </div>
        <div style={styles.statusBadge}>
          {connected
            ? <><Wifi size={14} color="#22c55e" /><span style={{color:'#22c55e', fontSize:'12px', fontWeight:'600'}}>Live Connected</span></>
            : <><WifiOff size={14} color="#f59e0b" /><span style={{color:'#f59e0b', fontSize:'12px', fontWeight:'600'}}>Waiting for GPS...</span></>
          }
        </div>
      </div>

      <div style={styles.layout}>
        {/* Left Panel — Vehicle List + Info */}
        <div style={styles.panel}>
          <div style={styles.panelTitle}>
            <Bus size={16} color="#6366f1" /> Vehicles
          </div>
          <div style={styles.vehicleList}>
            {vehicles.length === 0 && (
              <div style={styles.emptyMsg}>No vehicles registered yet.<br/>Use Swagger to create one.</div>
            )}
            {vehicles.map((v) => (
              <button
                key={v.id}
                style={{
                  ...styles.vehicleItem,
                  ...(selectedVehicle?.id === v.id ? styles.vehicleItemActive : {}),
                }}
                onClick={() => setSelectedVehicle(v)}
              >
                <div style={styles.vehicleNumBadge}>🚌</div>
                <div>
                  <div style={styles.vehicleNum}>{v.vehicleNumber}</div>
                  <div style={styles.vehicleDriver}>Driver: {v.driverName || 'N/A'}</div>
                </div>
              </button>
            ))}
          </div>

          {/* Live Info Card */}
          {selectedVehicle && (
            <div style={styles.infoCard}>
              <div style={styles.infoTitle}>Live Stats</div>
              <div style={styles.infoRow}>
                <MapPin size={13} color="#64748b" />
                <span>
                  {busPosition
                    ? `${busPosition[0].toFixed(5)}, ${busPosition[1].toFixed(5)}`
                    : 'Waiting for GPS signal...'}
                </span>
              </div>
              <div style={styles.infoRow}>
                <Radio size={13} color="#64748b" />
                <span>Speed: {speed ? `${parseFloat(speed).toFixed(1)} km/h` : '—'}</span>
              </div>
              <div style={styles.infoRow}>
                <RefreshCw size={13} color="#64748b" />
                <span>Last update: {lastUpdate || '—'}</span>
              </div>
              <div style={styles.infoRow}>
                <MapPin size={13} color="#64748b" />
                <span>Stops on route: {stops.length}</span>
              </div>
            </div>
          )}
        </div>

        {/* Map */}
        <div style={styles.mapWrap}>
          <MapContainer
            center={busPosition || DEFAULT_CENTER}
            zoom={13}
            style={{ height: '100%', width: '100%' }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {/* Bus Marker */}
            {busPosition && (
              <>
                <Marker position={busPosition} icon={busIcon}>
                  <Popup>
                    <strong>{selectedVehicle?.vehicleNumber}</strong><br />
                    Speed: {speed ? `${parseFloat(speed).toFixed(1)} km/h` : 'N/A'}<br />
                    Updated: {lastUpdate}
                  </Popup>
                </Marker>
                {/* 2km geofence ring */}
                <Circle
                  center={busPosition}
                  radius={400}
                  color="#6366f1"
                  fillColor="#6366f1"
                  fillOpacity={0.08}
                  weight={1}
                />
              </>
            )}

            {/* Route Stops */}
            {stops.map((stop) => (
              <Marker
                key={stop.id}
                position={[stop.latitude, stop.longitude]}
                icon={stopIcon}
              >
                <Popup>
                  <strong>Stop #{stop.stopOrder}</strong><br />
                  {stop.stopName}<br />
                  Pickup: {stop.pickupTime || 'N/A'}
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { padding: '32px', flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' },
  header: {
    display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between',
    marginBottom: '24px', flexWrap: 'wrap', gap: '12px',
  },
  title: { fontSize: '24px', fontWeight: '800', color: '#f1f5f9' },
  subtitle: { fontSize: '13px', color: '#64748b', marginTop: '4px' },
  statusBadge: {
    display: 'flex', alignItems: 'center', gap: '6px',
    background: '#1e293b', border: '1px solid #334155',
    borderRadius: '20px', padding: '8px 16px',
  },
  layout: { display: 'flex', gap: '20px', flex: 1, minHeight: 0 },
  panel: {
    width: '260px', flexShrink: 0, display: 'flex',
    flexDirection: 'column', gap: '16px', overflowY: 'auto',
  },
  panelTitle: {
    fontSize: '13px', fontWeight: '600', color: '#94a3b8',
    textTransform: 'uppercase', letterSpacing: '0.5px',
    display: 'flex', alignItems: 'center', gap: '8px',
  },
  vehicleList: { display: 'flex', flexDirection: 'column', gap: '8px' },
  emptyMsg: {
    fontSize: '13px', color: '#64748b', textAlign: 'center',
    padding: '20px', background: '#1e293b', borderRadius: '12px',
    border: '1px dashed #334155', lineHeight: 1.6,
  },
  vehicleItem: {
    display: 'flex', alignItems: 'center', gap: '12px',
    padding: '12px', borderRadius: '12px', border: '1px solid #334155',
    background: '#1e293b', cursor: 'pointer', width: '100%', textAlign: 'left',
    transition: 'all 0.15s',
  },
  vehicleItemActive: {
    borderColor: 'rgba(99,102,241,0.5)', background: 'rgba(99,102,241,0.1)',
  },
  vehicleNumBadge: { fontSize: '22px' },
  vehicleNum: { fontSize: '14px', fontWeight: '600', color: '#f1f5f9' },
  vehicleDriver: { fontSize: '12px', color: '#64748b' },
  infoCard: {
    background: '#1e293b', borderRadius: '12px', border: '1px solid #334155',
    padding: '16px', display: 'flex', flexDirection: 'column', gap: '10px',
  },
  infoTitle: { fontSize: '13px', fontWeight: '600', color: '#94a3b8', marginBottom: '4px' },
  infoRow: {
    display: 'flex', alignItems: 'flex-start', gap: '8px',
    fontSize: '12px', color: '#94a3b8', lineHeight: 1.5,
  },
  mapWrap: {
    flex: 1, borderRadius: '14px', overflow: 'hidden',
    border: '1px solid #334155', minHeight: '500px',
  },
};
