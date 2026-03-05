#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# ─── Tool paths ───────────────────────────────────────────────────────────────
TOOLS_DIR="/workspace/tools"
JDK_DIR="$TOOLS_DIR/jdk-17.0.11+9"
MAVEN_DIR="$TOOLS_DIR/apache-maven-3.9.6"

# Install JDK if not present
if [ ! -d "$JDK_DIR" ]; then
  echo "[INFO] Downloading JDK 17..."
  mkdir -p "$TOOLS_DIR"
  ARCH=$(uname -m)
  if [ "$ARCH" = "aarch64" ]; then
    JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.11_9.tar.gz"
  else
    JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9.tar.gz"
  fi
  curl -L "$JDK_URL" -o "$TOOLS_DIR/jdk17.tar.gz"
  tar -xzf "$TOOLS_DIR/jdk17.tar.gz" -C "$TOOLS_DIR"
fi

# Install Maven if not present
if [ ! -d "$MAVEN_DIR" ]; then
  echo "[INFO] Downloading Maven 3.9.6..."
  curl -L "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz" \
    -o "$TOOLS_DIR/maven.tar.gz"
  tar -xzf "$TOOLS_DIR/maven.tar.gz" -C "$TOOLS_DIR"
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$MAVEN_DIR/bin:$PATH"

echo "[INFO] Java: $(java -version 2>&1 | head -1)"
echo "[INFO] Maven: $(mvn -version 2>&1 | head -1)"

# ─── Build backend ─────────────────────────────────────────────────────────────
echo "[INFO] Building backend..."
cd "$SCRIPT_DIR/backend"
mvn clean package -DskipTests -q

# ─── Build frontend ────────────────────────────────────────────────────────────
echo "[INFO] Building frontend..."
cd "$SCRIPT_DIR/frontend"
npm install --silent
npm run build --silent

# ─── Start backend ─────────────────────────────────────────────────────────────
echo "[INFO] Starting backend on port 3000..."
mkdir -p "$SCRIPT_DIR/backend/data"
cd "$SCRIPT_DIR/backend"
nohup java -jar target/cms-1.0.0.jar \
  --spring.datasource.url="jdbc:h2:file:$SCRIPT_DIR/backend/data/campus;DB_CLOSE_ON_EXIT=FALSE" \
  > "$SCRIPT_DIR/backend/backend.log" 2>&1 &
BACKEND_PID=$!
echo "[INFO] Backend PID: $BACKEND_PID"

# Wait for backend to start
echo "[INFO] Waiting for backend to start..."
for i in $(seq 1 30); do
  if curl -s http://localhost:3000/api/auth/login -X POST -H "Content-Type: application/json" \
    -d '{"username":"x","password":"x"}' > /dev/null 2>&1; then
    echo "[INFO] Backend is ready!"
    break
  fi
  sleep 2
done

# ─── Start frontend dev server (optional) ─────────────────────────────────────
echo "[INFO] Starting frontend dev server on port 5173..."
cd "$SCRIPT_DIR/frontend"
nohup npm run dev > "$SCRIPT_DIR/frontend/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "[INFO] Frontend PID: $FRONTEND_PID"

echo ""
echo "========================================="
echo "  Campus Management System Started!"
echo "  Backend API: http://localhost:3000/api"
echo "  Frontend:    http://localhost:5173"
echo "  H2 Console:  http://localhost:3000/api/h2-console"
echo "========================================="
echo ""
echo "Default accounts:"
echo "  Admin:   admin / admin123"
echo "  Teacher: teacher_wang / teacher123"
echo "  Student: student_zhang / student123"
echo ""
echo "To stop: kill $BACKEND_PID $FRONTEND_PID"
