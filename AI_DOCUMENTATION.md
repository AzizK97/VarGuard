# 🤖 AI Security Assistant - Complete Integration Documentation

## 📋 Table of Contents
1. [What We Did](#what-we-did)
2. [Where Changes Were Made](#where-changes-were-made)
3. [How to Access & Use](#how-to-access--use)
4. [Technical Architecture](#technical-architecture)
5. [Testing & Verification](#testing--verification)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 What We Did

### Overview
We integrated an **AI-powered Security Assistant** into the existing Security Scanning Framework. This AI analyzes security alerts detected by Suricata and provides:
- **Intelligent threat analysis** with context
- **Specific remediation steps** tailored to each attack
- **Investigation commands** ready to copy and execute
- **Beautiful, organized interface** with tabs and interactive elements

### Key Achievements
1. ✅ **Smart Fallback System**: Works even without internet (air-gapped environments)
2. ✅ **Dynamic Content**: Extracts IPs, ports, and attack types to provide specific advice
3. ✅ **Advanced UI**: Tabbed interface with "Copy to Clipboard" for commands
4. ✅ **Auto-Analysis**: Automatically analyzes HIGH and CRITICAL severity alerts
5. ✅ **Manual Trigger**: Button to analyze any alert on-demand

---

## 📂 Where Changes Were Made

### Backend Changes (`scan-app`)

#### 1. **New Service Interface**
**File**: `/scan/core/service-api/src/main/java/tn/rnu/eniso/fwk/scan/core/service/api/AiAssistantService.java`
- **What**: Defines the contract for AI operations
- **Methods**:
  - `analyzeAlert(Alert alert)` - Analyze a single alert
  - `generateRemediation(Long alertId)` - Generate fix steps
  - `analyzeAlerts(List<Alert>)` - Batch analysis
  - `generateSecuritySummary(start, end)` - Executive summary
  - `isAvailable()` - Check if AI is ready

#### 2. **Service Implementation**
**File**: `/scan/core/service-impl/src/main/java/tn/rnu/eniso/fwk/scan/core/service/impl/AiAssistantServiceImpl.java`
- **What**: Implements the AI logic
- **Key Features**:
  - Formats alert data for AI consumption
  - Calls HuggingFaceClient for analysis
  - Handles errors gracefully
  - Provides batch processing

#### 3. **Hugging Face Client (The Brain)**
**File**: `/scan/core/service-impl/src/main/java/tn/rnu/eniso/fwk/scan/core/service/impl/HuggingFaceClient.java`
- **What**: Communicates with Hugging Face API + Smart Fallback
- **Key Features**:
  - **Primary Mode**: Calls Mistral-7B model via Hugging Face API
  - **Fallback Mode**: Local intelligent engine that:
    - Extracts Source IP, Destination IP, Ports from alerts using Regex
    - Detects attack type (Port Scan, DoS, SQL Injection, SSH Brute Force)
    - Generates specific commands like `grep 192.168.1.5 /var/log/auth.log`
  - **Prompt Engineering**: Structures requests for optimal AI responses
  - **Markdown Formatting**: Returns properly formatted text

#### 4. **REST Controller**
**File**: `/scan/core/ws-rest/src/main/java/tn/rnu/eniso/fwk/scan/core/ws/rest/AiAssistantController.java`
- **What**: Exposes AI functionality via HTTP endpoints
- **Endpoints**:
  - `POST /api/ai/analyze/{alertId}` - Analyze specific alert
  - `POST /api/ai/remediate/{alertId}` - Get remediation steps
  - `POST /api/ai/summary?start=...&end=...` - Get security summary
  - `GET /api/ai/status` - Check AI availability

#### 5. **Dependencies Added**
**Files**: 
- `/scan/core/service-impl/pom.xml`
- `/scan/app/app-jar/pom.xml`

**What**: Added `spring-boot-starter-webflux` for WebClient (HTTP client for Hugging Face API)

#### 6. **Build Optimization**
**File**: `/scan/Dockerfile`
- **What**: Commented out `mvnw clean package` inside Docker
- **Why**: We build locally first, then copy artifacts (faster: 10s vs 15min)

---

### Frontend Changes (`dashboard`)

#### 1. **AI API Service**
**File**: `/dashboard/src/services/aiApi.ts`
- **What**: TypeScript service to call backend AI endpoints
- **Functions**:
  - `analyzeAlert(alertId)` - Fetch AI analysis
  - `getRemediation(alertId)` - Fetch remediation steps

#### 2. **Enhanced Alert Card Component**
**File**: `/dashboard/src/components/AlertCard.tsx`
- **What**: Main component displaying alerts with AI integration
- **New Features**:
  - **Auto-trigger**: `useEffect` hook automatically calls AI for HIGH/CRITICAL alerts
  - **Manual button**: "🤖 Analyze with AI" for other alerts
  - **Tabbed Interface**: 
    - `Overview` tab: Threat analysis and impact
    - `Remediation` tab: Step-by-step fixes
    - `Investigation` tab: Commands to run
  - **Code Block Component**: 
    - Displays bash commands in styled blocks
    - "Copy" button with visual feedback (✓ Copied)
  - **Markdown Renderer**: 
    - Parses `###` headers, `**bold**`, lists, code blocks
    - Applies custom styling

#### 3. **Premium Styling**
**File**: `/dashboard/src/components/AlertCard.css`
- **What**: Complete visual overhaul of AI section
- **Features**:
  - Dark mode gradient background (`linear-gradient(145deg, #1a1f2c, #2d3748)`)
  - Tab buttons with hover effects and active states
  - Code blocks with header (language label + copy button)
  - Color-coded elements (blue for headers, green for code)
  - Smooth animations (`fadeIn` on load)
  - Responsive list items with custom bullets

---

## 🚀 How to Access & Use

### Step 1: Access the Dashboard
1. Open your browser
2. Navigate to: **http://localhost:30081**
3. You'll see the main dashboard with alert cards

### Step 2: View AI Analysis (Automatic)
1. Wait for a **HIGH** or **CRITICAL** severity alert to appear
2. The AI section will **automatically appear** at the bottom of the alert card
3. You'll see:
   - 🛡️ AI Security Assistant header
   - Three tabs: Overview | Remediation | Investigation
   - Loading spinner while analyzing

### Step 3: View AI Analysis (Manual)
1. Click on any alert card (any severity)
2. If AI hasn't analyzed it yet, you'll see a button: **"🤖 Analyze with AI"**
3. Click the button
4. AI section appears with analysis

### Step 4: Navigate the Tabs
- **Overview Tab** (default):
  - Threat type and description
  - Severity assessment
  - Business impact
  
- **Remediation Tab**:
  - Numbered list of fix steps
  - Specific to your alert (mentions actual IPs)
  - Example: "1. **Block Source**: Immediately block the source IP **192.168.1.5**"

- **Investigation Tab**:
  - Bash commands in code blocks
  - "Copy" button for each command block
  - Example:
    ```bash
    # Check SSH authentication logs
    grep "Failed password" /var/log/auth.log | grep 192.168.1.5
    ```

### Step 5: Copy Commands
1. Go to the **Investigation** tab
2. Hover over the code block
3. Click the **"Copy"** button in the top-right
4. Button changes to **"✓ Copied"** for 2 seconds
5. Paste into your terminal

---

## 🏗️ Technical Architecture

### Data Flow
```
1. Suricata detects attack → Alert saved to PostgreSQL
2. Dashboard fetches alerts → Displays in AlertCard
3. AlertCard (if HIGH/CRITICAL) → Calls aiApi.analyzeAlert(id)
4. aiApi → HTTP POST to scan-app:30082/api/ai/analyze/{id}
5. AiAssistantController → Calls AiAssistantServiceImpl
6. AiAssistantServiceImpl → Formats alert data → Calls HuggingFaceClient
7. HuggingFaceClient → Tries Hugging Face API
   - If success: Returns AI response
   - If fail: Generates smart fallback (extracts IPs, detects type, builds response)
8. Response flows back → Dashboard renders in Markdown with tabs
```

### Component Hierarchy
```
Backend (scan-app):
├── AiAssistantController (REST endpoints)
├── AiAssistantServiceImpl (Business logic)
└── HuggingFaceClient (AI communication + Fallback)

Frontend (dashboard):
├── AlertCard.tsx (Main component)
│   ├── MarkdownRenderer (Parses markdown)
│   ├── CodeBlock (Displays commands with copy button)
│   └── Tabs (Overview, Remediation, Investigation)
├── aiApi.ts (HTTP client)
└── AlertCard.css (Styling)
```

---

## ✅ Testing & Verification

### 1. Check Backend is Running
```bash
sudo /usr/local/bin/k3s kubectl get pods -n project-fwk -l app=scan-app
```
Expected output: `1/1 Running`

### 2. Check Backend Logs
```bash
sudo /usr/local/bin/k3s kubectl logs -n project-fwk -l app=scan-app --tail=50
```
Look for: `INFO` messages about alerts being saved

### 3. Test AI Endpoint Directly
```bash
curl -X POST http://localhost:30082/api/ai/analyze/1
```
Expected: JSON response with `analysis` field containing markdown text

### 4. Check Frontend is Running
```bash
sudo /usr/local/bin/k3s kubectl get pods -n project-fwk -l app=dashboard
```
Expected output: `1/1 Running`

### 5. Visual Test
1. Open http://localhost:30081
2. Click on an alert
3. Click "Analyze with AI"
4. Verify:
   - AI section appears
   - Tabs are clickable
   - Content changes when switching tabs
   - Copy button works in Investigation tab
   - IPs in the text match the alert's IPs

---

## 🔧 Troubleshooting

### Problem: "Failed to generate AI analysis"
**Possible Causes**:
- Backend pod not running
- Network issue between frontend and backend

**Solution**:
```bash
# Check backend status
sudo /usr/local/bin/k3s kubectl get pods -n project-fwk

# Restart backend if needed
sudo /usr/local/bin/k3s kubectl rollout restart deployment scan-app -n project-fwk

# Check logs for errors
sudo /usr/local/bin/k3s kubectl logs -l app=scan-app -n project-fwk
```

### Problem: AI section not appearing automatically
**Possible Causes**:
- Alert severity is not HIGH or CRITICAL
- Frontend not updated

**Solution**:
- Use the manual "Analyze with AI" button
- Or restart dashboard:
```bash
sudo /usr/local/bin/k3s kubectl rollout restart deployment dashboard -n project-fwk
```

### Problem: Formatting looks broken (literal `\n` or `*` visible)
**Possible Causes**:
- Old backend version

**Solution**:
Rebuild and redeploy backend:
```bash
cd /home/hedil/Desktop/Projet_fwk/scan
./mvnw clean package -DskipTests
docker build -t scan-app:latest .
cd ..
docker save -o scan-app.tar scan-app:latest
sudo /usr/local/bin/k3s ctr images import scan-app.tar
sudo /usr/local/bin/k3s kubectl rollout restart deployment scan-app -n project-fwk
```

### Problem: Copy button doesn't work
**Possible Causes**:
- Browser doesn't support Clipboard API
- HTTPS required (some browsers)

**Solution**:
- Use a modern browser (Chrome, Firefox, Edge)
- Manually select and copy the text

### Problem: Tabs not switching
**Possible Causes**:
- JavaScript error in console
- Old frontend version

**Solution**:
1. Open browser console (F12)
2. Look for errors
3. Rebuild frontend:
```bash
cd /home/hedil/Desktop/Projet_fwk
docker build -t dashboard:latest dashboard/
docker save -o dashboard.tar dashboard:latest
sudo /usr/local/bin/k3s ctr images import dashboard.tar
sudo /usr/local/bin/k3s kubectl rollout restart deployment dashboard -n project-fwk
```

---

## 📊 Configuration

### Backend Configuration
**File**: `/scan/app/app-jar/src/main/resources/application.properties`

```properties
# Hugging Face API Configuration
huggingface.api.url=https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2
huggingface.api.token=${HUGGINGFACE_API_TOKEN:}
huggingface.api.enabled=true
huggingface.api.timeout=30000
huggingface.api.max-tokens=500
huggingface.api.temperature=0.7
```

**Note**: If `HUGGINGFACE_API_TOKEN` is not set or API is unreachable, the system automatically uses the Smart Fallback mode.

### Kubernetes Secret (Optional)
To use real Hugging Face API:
```bash
kubectl create secret generic huggingface-secret \
  --from-literal=api-token=YOUR_TOKEN_HERE \
  -n project-fwk
```

---

## 📈 Future Enhancements

Possible improvements:
- [ ] Add more attack type detections (XSS, CSRF, etc.)
- [ ] Implement caching for repeated analyses
- [ ] Add export functionality (PDF reports)
- [ ] Multi-language support
- [ ] Real-time streaming analysis
- [ ] Integration with SIEM systems

---

## 📝 Summary

### What Was Changed
- **Backend**: 4 new Java files, 2 POM updates, 1 Dockerfile optimization
- **Frontend**: 1 new service file, 2 updated components (TSX + CSS)

### Where to See Changes
- **Dashboard**: http://localhost:30081 (visual changes in alert cards)
- **API**: http://localhost:30082/api/ai/* (backend endpoints)

### What It Does
- Analyzes security alerts with AI
- Provides specific, actionable advice
- Displays in a beautiful, organized interface
- Works offline with smart fallback

---

*Documentation created: 2025-12-16*  
*Project: Security Scanning Framework with AI Integration*  
*Author: AI Agent (Antigravity)*
