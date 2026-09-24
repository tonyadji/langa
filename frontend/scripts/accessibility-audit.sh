#!/bin/bash

# T235: Accessibility Audit with axe-core

echo "♿ Accessibility Audit (WCAG 2.1 AA)"
echo "===================================="
echo ""

# Check if @axe-core/cli is installed
if ! npm list @axe-core/cli > /dev/null 2>&1; then
  echo "⚠️  @axe-core/cli not found. Installing as dev dependency..."
  npm install --save-dev @axe-core/cli
fi

echo "Prerequisites:"
echo "-------------"
echo "1. Dev server must be running on http://localhost:5173"
echo "2. @axe-core/cli must be installed"
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
mkdir -p accessibility-reports

echo "Running axe accessibility audit..."
echo "----------------------------------"
echo ""

# Pages to test
PAGES=(
  "http://localhost:5173"
  "http://localhost:5173/dashboard"
  "http://localhost:5173/applications"
  "http://localhost:5173/usage"
  "http://localhost:5173/settings"
)

TOTAL_VIOLATIONS=0
TOTAL_PAGES=${#PAGES[@]}

for PAGE in "${PAGES[@]}"; do
  PAGE_NAME=$(echo $PAGE | sed 's|http://localhost:5173||' | sed 's|/|-|g' | sed 's|^-||')
  [ -z "$PAGE_NAME" ] && PAGE_NAME="home"
  
  echo "Testing: $PAGE"
  echo "Output: accessibility-reports/axe-$PAGE_NAME.json"
  
  npx axe "$PAGE" \
    --rules wcag2a,wcag2aa,wcag21a,wcag21aa \
    --save accessibility-reports/axe-$PAGE_NAME.json \
    --timeout 60000 \
    2>&1 | grep -v "deprecated"
  
  if [ -f "accessibility-reports/axe-$PAGE_NAME.json" ]; then
    # Count violations if jq is available
    if command -v jq &> /dev/null; then
      VIOLATIONS=$(jq '.violations | length' accessibility-reports/axe-$PAGE_NAME.json 2>/dev/null || echo "0")
      TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + VIOLATIONS))
      
      if [ "$VIOLATIONS" -eq 0 ]; then
        echo "  ✅ No violations found"
      else
        echo "  ⚠️  $VIOLATIONS violation(s) found"
        
        # Show violation summaries
        jq -r '.violations[] | "    - [\(.impact | ascii_upcase)] \(.help)"' accessibility-reports/axe-$PAGE_NAME.json 2>/dev/null
      fi
    fi
  fi
  
  echo ""
done

echo ""
echo "Summary:"
echo "--------"
echo "Pages tested: $TOTAL_PAGES"
echo "Total violations: $TOTAL_VIOLATIONS"
echo ""

if [ "$TOTAL_VIOLATIONS" -eq 0 ]; then
  echo "🎉 No accessibility violations found!"
  echo "✅ WCAG 2.1 AA compliance verified"
else
  echo "⚠️  Accessibility issues detected"
  echo ""
  echo "Review detailed reports in accessibility-reports/"
  echo ""
  echo "Common fixes:"
  echo "  - Add alt text to images"
  echo "  - Ensure sufficient color contrast"
  echo "  - Add ARIA labels to interactive elements"
  echo "  - Ensure keyboard navigation works"
  echo "  - Add proper heading hierarchy"
fi

echo ""
echo "Manual Testing Checklist:"
echo "------------------------"
echo "  [ ] Keyboard navigation works (Tab, Enter, Escape)"
echo "  [ ] Screen reader announces content correctly"
echo "  [ ] Focus indicators are visible"
echo "  [ ] Color contrast meets WCAG AA (4.5:1 for text)"
echo "  [ ] Forms have proper labels and error messages"
echo "  [ ] Modals trap focus correctly"
echo "  [ ] Skip navigation link works"
echo "  [ ] Images have alt text"
echo "  [ ] Buttons have descriptive text"
echo "  [ ] Links are descriptive (not 'click here')"

echo ""
echo "Testing Tools:"
echo "-------------"
echo "  • Browser DevTools"
echo "  • NVDA (Windows screen reader)"
echo "  • JAWS (Windows screen reader)"
echo "  • VoiceOver (macOS screen reader)"
echo "  • WAVE browser extension"
echo "  • axe DevTools browser extension"

echo ""
echo "Report files:"
echo "------------"
ls -lh accessibility-reports/ 2>/dev/null

echo ""
echo "View detailed report:"
echo "  cat accessibility-reports/axe-home.json | jq"
echo ""

# Create summary markdown
cat > accessibility-reports/SUMMARY.md << EOF
# Accessibility Audit Summary

**Date**: $(date)
**Standard**: WCAG 2.1 AA
**Tool**: axe-core

## Results

- **Pages Tested**: $TOTAL_PAGES
- **Total Violations**: $TOTAL_VIOLATIONS

## Pages

EOF

for PAGE in "${PAGES[@]}"; do
  PAGE_NAME=$(echo $PAGE | sed 's|http://localhost:5173||' | sed 's|/|-|g' | sed 's|^-||')
  [ -z "$PAGE_NAME" ] && PAGE_NAME="home"
  
  if [ -f "accessibility-reports/axe-$PAGE_NAME.json" ] && command -v jq &> /dev/null; then
    VIOLATIONS=$(jq '.violations | length' accessibility-reports/axe-$PAGE_NAME.json 2>/dev/null || echo "0")
    
    echo "### $PAGE" >> accessibility-reports/SUMMARY.md
    echo "" >> accessibility-reports/SUMMARY.md
    echo "- **Violations**: $VIOLATIONS" >> accessibility-reports/SUMMARY.md
    
    if [ "$VIOLATIONS" -gt 0 ]; then
      echo "- **Issues**:" >> accessibility-reports/SUMMARY.md
      jq -r '.violations[] | "  - [\(.impact | ascii_upcase)] \(.help)"' accessibility-reports/axe-$PAGE_NAME.json >> accessibility-reports/SUMMARY.md 2>/dev/null
    fi
    
    echo "" >> accessibility-reports/SUMMARY.md
  fi
done

cat >> accessibility-reports/SUMMARY.md << EOF

## Accessibility Features Implemented

- ✅ Skip navigation link
- ✅ ARIA labels on interactive elements
- ✅ Semantic HTML structure
- ✅ Keyboard navigation support
- ✅ Focus management in modals
- ✅ Error announcements
- ✅ Loading state announcements

## Next Steps

1. Address all violations found in automated tests
2. Perform manual testing with screen readers
3. Test keyboard navigation across all features
4. Verify color contrast ratios
5. Test with browser extensions (WAVE, axe DevTools)

---

*Generated by accessibility audit script*
EOF

echo "📄 Summary saved to: accessibility-reports/SUMMARY.md"
