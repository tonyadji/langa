#!/bin/bash

# T234: Lighthouse Audit Script

echo "🔦 Lighthouse Performance Audit"
echo "================================"
echo ""

# Check if lighthouse is available
if ! command -v lighthouse &> /dev/null; then
  echo "⚠️  Lighthouse CLI not found. Installing..."
  npm install -g lighthouse
fi

echo "Prerequisites:"
echo "-------------"
echo "1. Dev server must be running on http://localhost:5173"
echo "2. Lighthouse CLI must be installed (npm install -g lighthouse)"
echo ""

# Check if dev server is running
if ! curl -s http://localhost:5173 > /dev/null 2>&1; then
  echo "❌ Dev server not running on http://localhost:5173"
  echo ""
  echo "Please start the dev server:"
  echo "  npm run dev"
  echo ""
  echo "Then run this script again."
  exit 1
fi

echo "✅ Dev server is running"
echo ""

# Create output directory
mkdir -p lighthouse-reports

echo "Running Lighthouse audit..."
echo "---------------------------"
echo ""

# Run Lighthouse with desktop configuration
lighthouse http://localhost:5173 \
  --output html \
  --output json \
  --output-path ./lighthouse-reports/report \
  --chrome-flags="--headless" \
  --preset=desktop \
  --quiet

echo ""
echo "✅ Lighthouse audit complete!"
echo ""

# Parse JSON results
if [ -f "./lighthouse-reports/report.json" ]; then
  echo "Performance Metrics:"
  echo "-------------------"
  
  # Extract scores using jq if available, otherwise use grep
  if command -v jq &> /dev/null; then
    PERFORMANCE=$(jq -r '.categories.performance.score * 100' ./lighthouse-reports/report.json)
    ACCESSIBILITY=$(jq -r '.categories.accessibility.score * 100' ./lighthouse-reports/report.json)
    BEST_PRACTICES=$(jq -r '.categories["best-practices"].score * 100' ./lighthouse-reports/report.json)
    SEO=$(jq -r '.categories.seo.score * 100' ./lighthouse-reports/report.json)
    
    FCP=$(jq -r '.audits["first-contentful-paint"].displayValue' ./lighthouse-reports/report.json)
    LCP=$(jq -r '.audits["largest-contentful-paint"].displayValue' ./lighthouse-reports/report.json)
    TBT=$(jq -r '.audits["total-blocking-time"].displayValue' ./lighthouse-reports/report.json)
    CLS=$(jq -r '.audits["cumulative-layout-shift"].displayValue' ./lighthouse-reports/report.json)
    SI=$(jq -r '.audits["speed-index"].displayValue' ./lighthouse-reports/report.json)
    
    echo "Overall Scores:"
    echo "  Performance: $PERFORMANCE/100"
    echo "  Accessibility: $ACCESSIBILITY/100"
    echo "  Best Practices: $BEST_PRACTICES/100"
    echo "  SEO: $SEO/100"
    echo ""
    echo "Core Web Vitals:"
    echo "  First Contentful Paint (FCP): $FCP"
    echo "  Largest Contentful Paint (LCP): $LCP"
    echo "  Total Blocking Time (TBT): $TBT"
    echo "  Cumulative Layout Shift (CLS): $CLS"
    echo "  Speed Index: $SI"
    echo ""
    
    # Check constitution thresholds
    echo "Constitution Compliance:"
    echo "-----------------------"
    
    # Extract numeric values for comparison
    FCP_MS=$(echo $FCP | grep -o '[0-9.]*' | head -1)
    LCP_MS=$(echo $LCP | grep -o '[0-9.]*' | head -1)
    
    # FCP < 1.5s (1500ms)
    if [ $(echo "$FCP_MS < 1.5" | bc -l) -eq 1 ] 2>/dev/null; then
      echo "  ✅ FCP < 1.5s: $FCP"
    else
      echo "  ⚠️  FCP should be < 1.5s: $FCP"
    fi
    
    # LCP < 2.5s (2500ms)
    if [ $(echo "$LCP_MS < 2.5" | bc -l) -eq 1 ] 2>/dev/null; then
      echo "  ✅ LCP < 2.5s: $LCP"
    else
      echo "  ⚠️  LCP should be < 2.5s: $LCP"
    fi
    
    # Performance score should be > 90
    if [ $(echo "$PERFORMANCE > 90" | bc -l) -eq 1 ] 2>/dev/null; then
      echo "  ✅ Performance Score > 90: $PERFORMANCE"
    else
      echo "  ⚠️  Performance Score should be > 90: $PERFORMANCE"
    fi
    
    # Accessibility score should be > 90
    if [ $(echo "$ACCESSIBILITY > 90" | bc -l) -eq 1 ] 2>/dev/null; then
      echo "  ✅ Accessibility Score > 90: $ACCESSIBILITY"
    else
      echo "  ⚠️  Accessibility Score should be > 90: $ACCESSIBILITY"
    fi
    
  else
    echo "⚠️  jq not installed - cannot parse scores"
    echo "Install jq: brew install jq"
  fi
fi

echo ""
echo "Report Files:"
echo "------------"
echo "  HTML: lighthouse-reports/report.html"
echo "  JSON: lighthouse-reports/report.json"
echo ""
echo "Open report:"
echo "  open lighthouse-reports/report.html"
echo ""

# Check for common issues
echo "Common Optimization Opportunities:"
echo "----------------------------------"

if grep -q "unused-css-rules" ./lighthouse-reports/report.json 2>/dev/null; then
  echo "  ⚠️  Unused CSS detected"
fi

if grep -q "unused-javascript" ./lighthouse-reports/report.json 2>/dev/null; then
  echo "  ⚠️  Unused JavaScript detected"
fi

if grep -q "unminified-css" ./lighthouse-reports/report.json 2>/dev/null; then
  echo "  ⚠️  Unminified CSS detected"
fi

if grep -q "unminified-javascript" ./lighthouse-reports/report.json 2>/dev/null; then
  echo "  ⚠️  Unminified JavaScript detected"
fi

echo ""
echo "Next Steps:"
echo "----------"
echo "1. Open the HTML report to see detailed recommendations"
echo "2. Address any issues with scores < 90"
echo "3. Optimize assets flagged as blocking render"
echo "4. Review accessibility issues and fix violations"
echo "5. Run production build audit for final verification"
echo ""

echo "Production Build Audit:"
echo "----------------------"
echo "# Build for production"
echo "npm run build"
echo ""
echo "# Serve production build"
echo "npm run preview"
echo ""
echo "# Run Lighthouse against production (port 4173)"
echo "lighthouse http://localhost:4173 --output html --output-path ./lighthouse-reports/production-report"
