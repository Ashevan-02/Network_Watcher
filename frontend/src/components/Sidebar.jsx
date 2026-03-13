import { Drawer, List, ListItem, ListItemIcon, ListItemText, ListItemButton, Toolbar } from '@mui/material';
import { Home, Devices, Warning, NetworkCheck, Search, Settings, Assessment, Notifications, Apps } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';

const menuItems = [
  { text: 'Dashboard', icon: <Home />, path: '/' },
  { text: 'Devices', icon: <Devices />, path: '/devices' },
  { text: 'Vulnerabilities', icon: <Warning />, path: '/vulnerabilities' },
  { text: 'Network Activity', icon: <NetworkCheck />, path: '/network-activity' },
  { text: 'Applications', icon: <Apps />, path: '/applications' },
  { text: 'Alerts', icon: <Notifications />, path: '/alerts' },
  { text: 'Reports', icon: <Assessment />, path: '/reports' },
  { text: 'Scans', icon: <Search />, path: '/scans' },
  { text: 'Settings', icon: <Settings />, path: '/settings' }
];

export const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': { width: 240, boxSizing: 'border-box' }
      }}
    >
      <Toolbar />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Drawer>
  );
};
