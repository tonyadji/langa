# Getting Started with Langa Backend

**Document Type:** Tutorial  
**Audience:** Dashboard Developers (Beginner)  
**Duration:** 30-45 minutes  
**Goal:** Build your first Langa-enabled application and create a simple dashboard to visualize logs

---

## What You'll Learn

By the end of this tutorial, you will:
1. Set up and run the Langa Backend locally
2. Register a user account and authenticate
3. Create your first monitored application
4. Send logs from your application to Langa
5. Query and retrieve logs via the API
6. Build a basic React dashboard component to display logs

---

## Prerequisites

Before starting, ensure you have:
- **Java 21** installed ([Download](https://adoptium.net/))
- **MongoDB** running locally or access to MongoDB Atlas
- **Node.js 18+** and npm (for dashboard development)
- **curl** or **Postman** for API testing
- Basic knowledge of REST APIs and React

---

## Part 1: Setting Up Langa Backend

### Step 1.1: Clone and Configure

```bash
# Clone the repository
git clone https://github.com/your-repo/langa-backend.git
cd langa-backend

# Create environment configuration
cat > .env << EOF
MONGODB_URI=mongodb://localhost:27017/langa
JWT_KEY=your-super-secret-jwt-key-change-this-in-production
JWT_KID=langa-key-id
JWT_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
BASE_URL=http://localhost:8080
CORS_ALLOWED_ORIGINS=http://localhost:3000
CORS_ALLOW_CREDENTIALS=true
EOF
```

### Step 1.2: Build and Run

```bash
# Build the application
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run
```

**Expected Output:**
```
Started LangaBackendApplication in 5.123 seconds
```

**Verify:** Open http://localhost:8080/actuator/health - you should see:
```json
{"status":"UP"}
```

---

## Part 2: Creating Your First Account

### Step 2.1: Register a User

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "developer@example.com",
    "password": "SecurePass123!",
    "confirmationPassword": "SecurePass123!"
  }'
```

**Expected Response:**
```
User registered
```

### Step 2.2: Login and Get Tokens

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "developer@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "8f7e6d5c4b3a2d1e...",
  "email": "developer@example.com"
}
```

**Save your access token** - you'll need it for all subsequent requests.

### Step 2.3: Store Token for Convenience

```bash
# Export as environment variable
export LANGA_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Part 3: Creating Your First Application

### Step 3.1: Create an Application

**Request:**
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $LANGA_TOKEN" \
  -d '{
    "name": "My First App"
  }'
```

**Expected Response:**
```json
{
  "id": "67712345abcdef123456",
  "name": "My First App",
  "key": "app_1a2b3c4d",
  "accountKey": "acc_9z8y7x6w",
  "owner": "developer@example.com",
  "sharedWith": []
}
```

**Save these values:**
- `id` - Application ID (for querying logs)
- `key` - Application Key (for ingestion)
- `accountKey` - Account Key (for ingestion)

### Step 3.2: Get Ingestion Credentials

```bash
curl -X GET http://localhost:8080/api/applications/67712345abcdef123456/secured-details \
  -H "Authorization: Bearer $LANGA_TOKEN"
```

**Expected Response:**
```json
{
  "id": "67712345abcdef123456",
  "name": "My First App",
  "key": "app_1a2b3c4d",
  "accountKey": "acc_9z8y7x6w",
  "secret": "sec_a1b2c3d4e5f6",
  "httpIngestionUrl": "http://localhost:8080/api/ingestion",
  "kafkaIngestionUrl": "localhost:9092",
  "kafkaTopic": "langa-ingestion"
}
```

**Save the secret** - you'll need it to sign ingestion requests.

---

## Part 4: Sending Your First Logs

### Step 4.1: Understanding Ingestion

Langa uses **HMAC signature authentication** for ingestion to prevent unauthorized access. The signature is calculated as:

```
Signature = HMAC-SHA256(
  secret,
  X-USER-AGENT + X-APP-KEY + X-ACCOUNT-KEY + X-TIMESTAMP + request_body
)
```

### Step 4.2: Create a Simple Ingestion Script

Save this as `send-logs.sh`:

```bash
#!/bin/bash

APP_KEY="app_1a2b3c4d"
ACCOUNT_KEY="acc_9z8y7x6w"
SECRET="sec_a1b2c3d4e5f6"
USER_AGENT="tutorial-script/1.0"
TIMESTAMP=$(date +%s%3N)

# Request body
BODY=$(cat <<EOF
{
  "logs": [
    {
      "message": "Application started successfully",
      "level": "INFO",
      "loggerName": "com.example.Main",
      "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
      "threadName": "main",
      "stackTrace": null,
      "mdc": {}
    }
  ],
  "metrics": []
}
EOF
)

# Calculate signature
PAYLOAD="$USER_AGENT$APP_KEY$ACCOUNT_KEY$TIMESTAMP$BODY"
SIGNATURE=$(echo -n "$PAYLOAD" | openssl dgst -sha256 -hmac "$SECRET" -binary | base64)

# Send request
curl -X POST http://localhost:8080/api/ingestion \
  -H "Content-Type: application/json" \
  -H "X-USER-AGENT: $USER_AGENT" \
  -H "X-APP-KEY: $APP_KEY" \
  -H "X-ACCOUNT-KEY: $ACCOUNT_KEY" \
  -H "X-TIMESTAMP: $TIMESTAMP" \
  -H "X-AGENT-SIGNATURE: $SIGNATURE" \
  -d "$BODY"
```

### Step 4.3: Send Logs

```bash
chmod +x send-logs.sh
./send-logs.sh
```

**Expected Response:**
```
HTTP/1.1 202 Accepted
```

**Troubleshooting:**
- `401 Unauthorized` - Check your signature calculation
- `400 Bad Request` - Verify timestamp is recent
- `404 Not Found` - Verify app key and account key

---

## Part 5: Querying Logs via API

### Step 5.1: Retrieve All Logs

```bash
curl -X GET "http://localhost:8080/api/applications/67712345abcdef123456/logs" \
  -H "Authorization: Bearer $LANGA_TOKEN"
```

**Expected Response:**
```json
{
  "content": [
    {
      "message": "Application started successfully",
      "level": "INFO",
      "loggerName": "com.example.Main",
      "timestamp": "2025-12-28T10:30:00Z",
      "threadName": "main",
      "stackTrace": null,
      "mdc": {}
    }
  ],
  "totalElements": 1,
  "currentPage": 0,
  "pageSize": 100
}
```

### Step 5.2: Filter Logs by Level

```bash
curl -X GET "http://localhost:8080/api/applications/67712345abcdef123456/logs?level=ERROR" \
  -H "Authorization: Bearer $LANGA_TOKEN"
```

### Step 5.3: Filter by Time Range

```bash
curl -X GET "http://localhost:8080/api/applications/67712345abcdef123456/logs?startTime=2025-12-28T00:00:00Z&endTime=2025-12-28T23:59:59Z" \
  -H "Authorization: Bearer $LANGA_TOKEN"
```

### Step 5.4: Pagination

```bash
curl -X GET "http://localhost:8080/api/applications/67712345abcdef123456/logs?page=0&size=50" \
  -H "Authorization: Bearer $LANGA_TOKEN"
```

---

## Part 6: Building a Basic Dashboard

### Step 6.1: Create React App

```bash
npx create-react-app langa-dashboard
cd langa-dashboard
npm install axios date-fns
```

### Step 6.2: Create Langa API Client

Create `src/api/langaClient.js`:

```javascript
import axios from 'axios';

const LANGA_BASE_URL = 'http://localhost:8080/api';

class LangaClient {
  constructor() {
    this.token = null;
  }

  async login(username, password) {
    const response = await axios.post(`${LANGA_BASE_URL}/auth/login`, {
      username,
      password
    });
    this.token = response.data.accessToken;
    return response.data;
  }

  async getApplications() {
    const response = await axios.get(`${LANGA_BASE_URL}/applications`, {
      headers: { Authorization: `Bearer ${this.token}` }
    });
    return response.data;
  }

  async getLogs(appId, params = {}) {
    const response = await axios.get(
      `${LANGA_BASE_URL}/applications/${appId}/logs`,
      {
        headers: { Authorization: `Bearer ${this.token}` },
        params
      }
    );
    return response.data;
  }

  async getMetrics(appId, params = {}) {
    const response = await axios.get(
      `${LANGA_BASE_URL}/applications/${appId}/metrics`,
      {
        headers: { Authorization: `Bearer ${this.token}` },
        params
      }
    );
    return response.data;
  }
}

export default new LangaClient();
```

### Step 6.3: Create Login Component

Create `src/components/Login.jsx`:

```jsx
import React, { useState } from 'react';
import langaClient from '../api/langaClient';

function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const data = await langaClient.login(username, password);
      onLoginSuccess(data);
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px' }}>
      <h2>Langa Dashboard Login</h2>
      {error && <div style={{ color: 'red' }}>{error}</div>}
      <form onSubmit={handleLogin}>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="email"
            placeholder="Email"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '8px' }}
            required
          />
        </div>
        <div style={{ marginBottom: '10px' }}>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '8px' }}
            required
          />
        </div>
        <button type="submit" style={{ width: '100%', padding: '10px' }}>
          Login
        </button>
      </form>
    </div>
  );
}

export default Login;
```

### Step 6.4: Create Log Viewer Component

Create `src/components/LogViewer.jsx`:

```jsx
import React, { useState, useEffect } from 'react';
import { format } from 'date-fns';
import langaClient from '../api/langaClient';

function LogViewer({ appId }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({
    level: '',
    loggerName: ''
  });

  useEffect(() => {
    loadLogs();
  }, [appId, filter]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const data = await langaClient.getLogs(appId, filter);
      setLogs(data.content);
    } catch (err) {
      console.error('Failed to load logs', err);
    }
    setLoading(false);
  };

  const getLevelColor = (level) => {
    switch (level) {
      case 'ERROR': return '#ff4444';
      case 'WARN': return '#ffaa00';
      case 'INFO': return '#00aaff';
      case 'DEBUG': return '#888888';
      default: return '#000000';
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h2>Logs</h2>
      
      {/* Filters */}
      <div style={{ marginBottom: '20px' }}>
        <select
          value={filter.level}
          onChange={(e) => setFilter({ ...filter, level: e.target.value })}
          style={{ marginRight: '10px', padding: '5px' }}
        >
          <option value="">All Levels</option>
          <option value="ERROR">ERROR</option>
          <option value="WARN">WARN</option>
          <option value="INFO">INFO</option>
          <option value="DEBUG">DEBUG</option>
        </select>

        <input
          type="text"
          placeholder="Logger name"
          value={filter.loggerName}
          onChange={(e) => setFilter({ ...filter, loggerName: e.target.value })}
          style={{ padding: '5px' }}
        />
      </div>

      {/* Log entries */}
      {loading ? (
        <div>Loading...</div>
      ) : (
        <div style={{ fontFamily: 'monospace', fontSize: '12px' }}>
          {logs.map((log, index) => (
            <div
              key={index}
              style={{
                padding: '10px',
                borderBottom: '1px solid #eee',
                backgroundColor: index % 2 === 0 ? '#f9f9f9' : '#ffffff'
              }}
            >
              <div>
                <span style={{ color: getLevelColor(log.level), fontWeight: 'bold' }}>
                  {log.level}
                </span>
                {' | '}
                <span style={{ color: '#666' }}>
                  {format(new Date(log.timestamp), 'yyyy-MM-dd HH:mm:ss')}
                </span>
                {' | '}
                <span style={{ color: '#999' }}>
                  {log.loggerName}
                </span>
              </div>
              <div style={{ marginTop: '5px' }}>
                {log.message}
              </div>
              {log.stackTrace && (
                <pre style={{ 
                  marginTop: '5px', 
                  padding: '5px', 
                  backgroundColor: '#ffe0e0',
                  overflow: 'auto'
                }}>
                  {log.stackTrace}
                </pre>
              )}
            </div>
          ))}
          
          {logs.length === 0 && (
            <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
              No logs found
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default LogViewer;
```

### Step 6.5: Create App Selector Component

Create `src/components/AppSelector.jsx`:

```jsx
import React, { useState, useEffect } from 'react';
import langaClient from '../api/langaClient';

function AppSelector({ onSelectApp }) {
  const [apps, setApps] = useState([]);
  const [selectedAppId, setSelectedAppId] = useState('');

  useEffect(() => {
    loadApps();
  }, []);

  const loadApps = async () => {
    try {
      const data = await langaClient.getApplications();
      setApps(data);
      if (data.length > 0) {
        setSelectedAppId(data[0].id);
        onSelectApp(data[0].id);
      }
    } catch (err) {
      console.error('Failed to load applications', err);
    }
  };

  const handleChange = (e) => {
    const appId = e.target.value;
    setSelectedAppId(appId);
    onSelectApp(appId);
  };

  return (
    <div style={{ padding: '20px', borderBottom: '1px solid #ddd' }}>
      <label>
        Select Application:{' '}
        <select value={selectedAppId} onChange={handleChange} style={{ padding: '5px' }}>
          {apps.map(app => (
            <option key={app.id} value={app.id}>
              {app.name}
            </option>
          ))}
        </select>
      </label>
    </div>
  );
}

export default AppSelector;
```

### Step 6.6: Main App Component

Update `src/App.js`:

```jsx
import React, { useState } from 'react';
import Login from './components/Login';
import AppSelector from './components/AppSelector';
import LogViewer from './components/LogViewer';

function App() {
  const [user, setUser] = useState(null);
  const [selectedAppId, setSelectedAppId] = useState(null);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
  };

  const handleSelectApp = (appId) => {
    setSelectedAppId(appId);
  };

  if (!user) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div>
      <div style={{ 
        padding: '10px 20px', 
        backgroundColor: '#333', 
        color: 'white',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <h1 style={{ margin: 0 }}>Langa Dashboard</h1>
        <span>{user.email}</span>
      </div>
      
      <AppSelector onSelectApp={handleSelectApp} />
      
      {selectedAppId && <LogViewer appId={selectedAppId} />}
    </div>
  );
}

export default App;
```

### Step 6.7: Run Your Dashboard

```bash
npm start
```

Open http://localhost:3000 and:
1. Login with `developer@example.com` / `SecurePass123!`
2. Select your application from the dropdown
3. View your logs in real-time!

---

## Part 7: Next Steps

🎉 **Congratulations!** You've successfully:
- ✅ Set up Langa Backend
- ✅ Created an account and application
- ✅ Sent logs to Langa
- ✅ Built a basic dashboard

### What's Next?

1. **Add Metrics Visualization:** Extend your dashboard to show metrics with charts
2. **Real-time Updates:** Implement polling or WebSockets for live logs
3. **Team Collaboration:** Create teams and share applications
4. **Advanced Filtering:** Add date range pickers and full-text search
5. **Alerting:** Build alert rules for critical errors

### Recommended Reading

- [How-to Guides](./03-HOW-TO.md) - Specific recipes for common tasks
- [Reference Documentation](./04-REFERENCE.md) - Complete API documentation
- [Explanation Docs](./05-EXPLANATION.md) - Deep dive into architecture

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

## Troubleshooting

### MongoDB Connection Issues
```
Error: MongoTimeoutError
```
**Solution:** Ensure MongoDB is running on port 27017

### JWT Errors
```
Error: JWT expired
```
**Solution:** Use the refresh token endpoint to get a new access token

### CORS Errors in Dashboard
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solution:** Verify `CORS_ALLOWED_ORIGINS=http://localhost:3000` in backend configuration

### Ingestion Signature Fails
```
401 Unauthorized
```
**Solution:** Verify signature calculation includes all headers in correct order

---

**Happy Monitoring! 🚀**
