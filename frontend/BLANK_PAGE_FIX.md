# 🐛 Blank Page Troubleshooting

## Quick Fix Steps

### 1. Check Browser Console
Press `F12` or `Ctrl+Shift+I` to open Developer Tools, then check the Console tab for errors.

### 2. Common Issues & Solutions

#### Issue: "Cannot find module" errors
**Solution**: Reinstall dependencies
```bash
cd "c:\Users\Cordial space\Desktop\networkwatcher_Frontend\network-watcher-frontend"
rm -rf node_modules package-lock.json
npm install
npm run dev
```

#### Issue: Port already in use
**Solution**: Kill the process or use different port
```bash
# Kill process on port 5173
netstat -ano | findstr :5173
taskkill /PID <PID_NUMBER> /F

# Or change port in vite.config.js
```

#### Issue: Import errors
**Check these files exist**:
- `src/hooks/useNotification.js`
- `src/components/BandwidthChart.jsx`
- `src/services/bandwidthService.js`

### 3. Verify Files

Run this to check critical files:
```bash
cd "c:\Users\Cordial space\Desktop\networkwatcher_Frontend\network-watcher-frontend\src"
dir hooks\useNotification.js
dir components\BandwidthChart.jsx
dir services\bandwidthService.js
```

### 4. Test Simple Version

Create a test file to verify React works:

**src/App.test.jsx**:
```jsx
function App() {
  return <div>Hello World</div>;
}
export default App;
```

Then in **src/main.jsx**, temporarily change:
```jsx
import App from './App.test.jsx';
```

If "Hello World" appears, the issue is in App.jsx imports.

### 5. Check Vite Output

When you run `npm run dev`, you should see:
```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 6. Check Backend

Verify backend is running:
```bash
curl http://localhost:8080/api/auth/login
```

Should return 401 or 400, not connection refused.

## Most Likely Causes

1. **Missing dependencies** → Run `npm install`
2. **Import errors** → Check console for "Cannot find module"
3. **Port conflict** → Change port or kill process
4. **Backend not running** → Start backend first

## Step-by-Step Debug

1. Stop frontend (`Ctrl+C`)
2. Clear cache: `npm cache clean --force`
3. Reinstall: `npm install`
4. Start: `npm run dev`
5. Open: http://localhost:5173
6. Check console (F12)
7. Share error message if still blank

## Emergency Simple Version

If nothing works, use this minimal App.jsx:

```jsx
import { useState } from 'react';

function App() {
  const [count, setCount] = useState(0);
  
  return (
    <div style={{ padding: '20px' }}>
      <h1>Network Watcher</h1>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>Click me</button>
    </div>
  );
}

export default App;
```

If this works, gradually add back features.

## Get Help

Share the console errors (F12 → Console tab) for specific help.
