import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Box, Toolbar, Snackbar, Alert } from '@mui/material';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Devices } from './pages/Devices';
import { DeviceDetails } from './pages/DeviceDetails';
import { Vulnerabilities } from './pages/Vulnerabilities';
import { NetworkActivity } from './pages/NetworkActivity';
import { Scans } from './pages/Scans';
import { Settings } from './pages/Settings';
import { TestPage } from './pages/TestPage';
import { Reports } from './pages/Reports';
import { Alerts } from './pages/Alerts';
import { SnmpSettings } from './pages/SnmpSettings';
import { useNotification } from './hooks/useNotification';
import { Applications } from './pages/Applications';

const Layout = ({ children }) => (
  <Box sx={{ display: 'flex' }}>
    <Navbar />
    <Sidebar />
    <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
      <Toolbar />
      {children}
    </Box>
  </Box>
);

const AppContent = () => {
  const { notification, hideNotification } = useNotification();

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/test" element={<TestPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<ProtectedRoute><Layout><Dashboard /></Layout></ProtectedRoute>} />
          <Route path="/devices" element={<ProtectedRoute><Layout><Devices /></Layout></ProtectedRoute>} />
          <Route path="/devices/:id" element={<ProtectedRoute><Layout><DeviceDetails /></Layout></ProtectedRoute>} />
          <Route path="/vulnerabilities" element={<ProtectedRoute><Layout><Vulnerabilities /></Layout></ProtectedRoute>} />
          <Route path="/network-activity" element={<ProtectedRoute><Layout><NetworkActivity /></Layout></ProtectedRoute>} />
          <Route path="/applications" element={<ProtectedRoute><Layout><Applications /></Layout></ProtectedRoute>} />
          <Route path="/reports" element={<ProtectedRoute><Layout><Reports /></Layout></ProtectedRoute>} />
          <Route path="/alerts" element={<ProtectedRoute><Layout><Alerts /></Layout></ProtectedRoute>} />
          <Route path="/scans" element={<ProtectedRoute><Layout><Scans /></Layout></ProtectedRoute>} />
          <Route path="/settings" element={<ProtectedRoute><Layout><Settings /></Layout></ProtectedRoute>} />
          <Route path="/settings/snmp" element={<ProtectedRoute><Layout><SnmpSettings /></Layout></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      <Snackbar 
        open={notification.open} 
        autoHideDuration={6000}
        onClose={hideNotification}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert onClose={hideNotification} severity={notification.severity} sx={{ width: '100%' }}>
          {notification.message}
        </Alert>
      </Snackbar>
    </>
  );
};

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
