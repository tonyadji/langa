#!/bin/bash

# T247: Quickstart Guide Validation

# Helper functions (defined first)
check_command() {
  local cmd=$1
  local name=$2
  local required=$3
  
  if command -v $(echo $cmd | awk '{print $1}') &> /dev/null; then
    VERSION=$($cmd 2>&1 || echo "installed")
    echo "  ✅ $name: $VERSION"
  else
    if [ "$required" = "required" ]; then
      echo "  ❌ $name not installed"
      ERRORS=$((ERRORS + 1))
    else
      echo "  ⚠️  $name not installed (optional)"
      WARNINGS=$((WARNINGS + 1))
    fi
  fi
}

check_script() {
  local script=$1
  local required=$2
  
  if grep -q "\"$script\":" package.json; then
    SCRIPT_CMD=$(grep "\"$script\":" package.json | head -1 | sed 's/.*: "//' | sed 's/".*//')
    echo "  ✅ $script: $SCRIPT_CMD"
  else
    if [ "$required" = "required" ]; then
      echo "  ❌ Missing script: $script"
      ERRORS=$((ERRORS + 1))
    else
      echo "  ⚠️  Optional script: $script"
      WARNINGS=$((WARNINGS + 1))
    fi
  fi
}

check_file() {
  local file=$1
  local required=$2
  
  if [ -f "$file" ]; then
    SIZE=$(ls -lh "$file" | awk '{print $5}')
    echo "  ✅ $file ($SIZE)"
  else
    if [ "$required" = "required" ]; then
      echo "  ❌ Missing: $file"
      ERRORS=$((ERRORS + 1))
    else
      echo "  ⚠️  Optional: $file"
      WARNINGS=$((WARNINGS + 1))
    fi
  fi
}

echo "📖 Quickstart Guide Validation"
echo "=============================="
echo ""

QUICKSTART_FILE="docs/QUICKSTART.md"
README_FILE="README.md"

if [ ! -f "$QUICKSTART_FILE" ]; then
  if [ -f "$README_FILE" ]; then
    echo "Using README.md as quickstart guide"
    QUICKSTART_FILE="$README_FILE"
  else
    echo "❌ No quickstart guide found (docs/QUICKSTART.md or README.md)"
    exit 1
  fi
fi

echo "Validating: $QUICKSTART_FILE"
echo ""

# Validation checks
ERRORS=0
WARNINGS=0

echo "Required Sections:"
echo "-----------------"

# Check for essential sections
check_section() {
  local section=$1
  local required=$2
  
  if grep -qi "^##\? .*$section" "$QUICKSTART_FILE"; then
    echo "  ✅ $section"
  else
    if [ "$required" = "required" ]; then
      echo "  ❌ Missing: $section"
      ERRORS=$((ERRORS + 1))
    else
      echo "  ⚠️  Optional: $section"
      WARNINGS=$((WARNINGS + 1))
    fi
  fi
}

check_section "Installation\|Getting Started\|Setup" "required"
check_section "Prerequisites\|Requirements" "required"
check_section "Usage\|Quick Start" "required"
check_section "Configuration\|Environment" "optional"
check_section "API\|Features" "optional"
check_section "Examples" "optional"
check_section "Troubleshooting" "optional"

echo ""
echo "Code Examples:"
echo "-------------"

# Count code blocks
CODE_BLOCKS=$(grep -c '```' "$QUICKSTART_FILE")
if [ "$CODE_BLOCKS" -gt 0 ]; then
  echo "  ✅ Found $CODE_BLOCKS code blocks"
  
  # Check for language specifiers
  UNSPECIFIED=$(grep -c '```$' "$QUICKSTART_FILE" || echo "0")
  if [ "$UNSPECIFIED" -gt 0 ]; then
    echo "  ⚠️  $UNSPECIFIED code blocks without language specifiers"
    WARNINGS=$((WARNINGS + 1))
  fi
else
  echo "  ⚠️  No code examples found"
  WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "Installation Commands:"
echo "--------------------"

# Check for package manager commands
if grep -q "npm install\|yarn add\|pnpm install" "$QUICKSTART_FILE"; then
  echo "  ✅ Installation commands present"
else
  echo "  ❌ Missing installation commands"
  ERRORS=$((ERRORS + 1))
fi

# Check for setup commands
if grep -q "npm run\|yarn\|pnpm" "$QUICKSTART_FILE"; then
  echo "  ✅ Run commands present"
else
  echo "  ⚠️  Missing run commands"
  WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "Prerequisites Check:"
echo "-------------------"

# Check for Node.js version
if grep -qi "node\|nodejs" "$QUICKSTART_FILE"; then
  echo "  ✅ Node.js mentioned"
  
  # Extract version requirement
  NODE_VERSION=$(grep -i "node" "$QUICKSTART_FILE" | grep -o "[0-9]\+\.[0-9]\+" | head -1)
  if [ -n "$NODE_VERSION" ]; then
    echo "     Required: Node.js $NODE_VERSION+"
    
    # Check current Node.js version
    if command -v node &> /dev/null; then
      CURRENT_NODE=$(node -v | grep -o "[0-9]\+\.[0-9]\+" | head -1)
      echo "     Current: Node.js $CURRENT_NODE"
      
      # Simple version comparison (major version)
      REQ_MAJOR=$(echo $NODE_VERSION | cut -d. -f1)
      CUR_MAJOR=$(echo $CURRENT_NODE | cut -d. -f1)
      
      if [ "$CUR_MAJOR" -ge "$REQ_MAJOR" ]; then
        echo "     ✅ Version meets requirements"
      else
        echo "     ⚠️  Version may be too old"
      fi
    fi
  else
    echo "     ⚠️  No version requirement specified"
    WARNINGS=$((WARNINGS + 1))
  fi
else
  echo "  ⚠️  Node.js not mentioned"
  WARNINGS=$((WARNINGS + 1))
fi

echo ""
echo "Validation Tests:"
echo "----------------"

# Test 1: Can we clone and install?
echo ""
echo "Test 1: Installation Steps"
echo "  Prerequisites:"
check_command "node -v" "Node.js" "required"
check_command "npm -v" "npm" "required"

# Test 2: Environment setup
echo ""
echo "Test 2: Environment Configuration"
if [ -f ".env.example" ]; then
  echo "  ✅ .env.example exists"
  
  # Check if documented in quickstart
  if grep -qi "\.env\|environment" "$QUICKSTART_FILE"; then
    echo "  ✅ Environment variables documented"
  else
    echo "  ⚠️  Environment variables not documented"
    WARNINGS=$((WARNINGS + 1))
  fi
else
  echo "  ⚠️  No .env.example file"
  WARNINGS=$((WARNINGS + 1))
fi

# Test 3: Scripts availability
echo ""
echo "Test 3: Available Scripts"
if [ -f "package.json" ]; then
  echo "  ✅ package.json exists"
  
  # Check for essential scripts
  check_script "dev" "required"
  check_script "build" "required"
  check_script "test" "required"
  check_script "lint" "optional"
else
  echo "  ❌ package.json not found"
  ERRORS=$((ERRORS + 1))
fi

# Test 4: Documentation links
echo ""
echo "Test 4: Documentation Links"

# Check for common documentation files
check_file "README.md" "required"
check_file "docs/DEPLOYMENT.md" "optional"
check_file "CONTRIBUTING.md" "optional"
check_file "LICENSE" "optional"

# Test 5: Quickstart accuracy
echo ""
echo "Test 5: Quickstart Accuracy Validation"
echo ""

# Create a temporary test script from the quickstart
TEMP_TEST="/tmp/quickstart_test_$$.sh"

echo "Extracting commands from quickstart..."

# Extract bash/shell code blocks
awk '/```(bash|sh|shell)/{flag=1;next}/```/{flag=0}flag' "$QUICKSTART_FILE" > "$TEMP_TEST"

if [ -s "$TEMP_TEST" ]; then
  LINE_COUNT=$(wc -l < "$TEMP_TEST")
  echo "  ✅ Extracted $LINE_COUNT lines of shell commands"
  echo ""
  echo "  Sample commands:"
  head -5 "$TEMP_TEST" | sed 's/^/    /'
  
  if [ "$LINE_COUNT" -gt 5 ]; then
    echo "    ..."
  fi
else
  echo "  ⚠️  No shell commands found to validate"
  WARNINGS=$((WARNINGS + 1))
fi

rm -f "$TEMP_TEST"

echo ""
echo "Summary:"
echo "--------"
echo "  Errors: $ERRORS"
echo "  Warnings: $WARNINGS"
echo ""

if [ "$ERRORS" -eq 0 ] && [ "$WARNINGS" -eq 0 ]; then
  echo "🎉 Quickstart guide is complete and validated!"
  exit 0
elif [ "$ERRORS" -eq 0 ]; then
  echo "✅ Quickstart guide is functional with minor warnings"
  exit 0
else
  echo "❌ Quickstart guide has critical issues"
  echo ""
  echo "Recommendations:"
  echo "  1. Add missing required sections"
  echo "  2. Include installation commands"
  echo "  3. Document prerequisites with versions"
  echo "  4. Add usage examples"
  echo "  5. Include troubleshooting section"
  exit 1
fi
