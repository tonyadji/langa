#!/bin/bash

# T248: Responsive Design Verification Script
# Verifies application responsiveness across key breakpoints

echo "📱 Responsive Design Verification"
echo "=================================="
echo ""

BREAKPOINTS=(
  "375:Mobile (iPhone SE)"
  "768:Tablet (iPad)"
  "1024:Desktop (Small)"
  "1440:Desktop (Large)"
)

echo "✓ Breakpoints to verify:"
for bp in "${BREAKPOINTS[@]}"; do
  width="${bp%%:*}"
  name="${bp#*:}"
  echo "  - ${width}px: ${name}"
done

echo ""
echo "Manual Verification Checklist:"
echo "-----------------------------"

echo ""
echo "1. Mobile (375px):"
echo "   [ ] Navigation collapses to hamburger menu"
echo "   [ ] Tables scroll horizontally or stack"
echo "   [ ] Forms are single column"
echo "   [ ] Buttons are full-width or stacked"
echo "   [ ] Touch targets are ≥ 44x44px"
echo "   [ ] Text is readable without zooming"

echo ""
echo "2. Tablet (768px):"
echo "   [ ] Layout uses available space efficiently"
echo "   [ ] Sidebar appears or collapses appropriately"
echo "   [ ] Charts and graphs are readable"
echo "   [ ] Modal dialogs are appropriately sized"
echo "   [ ] Touch navigation works smoothly"

echo ""
echo "3. Desktop (1024px+):"
echo "   [ ] Full navigation visible"
echo "   [ ] Multi-column layouts work"
echo "   [ ] Content doesn't stretch too wide (max-width)"
echo "   [ ] Hover states work properly"
echo "   [ ] Keyboard navigation functional"

echo ""
echo "Automated Checks:"
echo "-----------------"

# Check for responsive classes in Tailwind
echo "✓ Checking for responsive Tailwind classes..."
responsive_count=$(grep -r "sm:\|md:\|lg:\|xl:\|2xl:" src/ --include="*.tsx" | wc -l | tr -d ' ')
echo "  Found ${responsive_count} responsive class usages"

if [ "$responsive_count" -gt 50 ]; then
  echo "  ✅ Good responsive class coverage"
else
  echo "  ⚠️  Consider adding more responsive variants"
fi

# Check viewport meta tag
echo ""
echo "✓ Checking viewport meta tag..."
if grep -q "width=device-width" index.html; then
  echo "  ✅ Viewport meta tag present"
else
  echo "  ❌ Viewport meta tag missing!"
fi

# Check for max-width constraints
echo ""
echo "✓ Checking for container max-width..."
if grep -rq "max-w-" src/ --include="*.tsx"; then
  echo "  ✅ Max-width constraints found"
else
  echo "  ⚠️  No max-width constraints - content may stretch too wide"
fi

echo ""
echo "Testing Instructions:"
echo "--------------------"
echo "1. Start dev server: npm run dev"
echo "2. Open Chrome DevTools (F12)"
echo "3. Toggle device toolbar (Ctrl+Shift+M)"
echo "4. Test each breakpoint:"
echo "   - 375px (iPhone SE)"
echo "   - 768px (iPad)"
echo "   - 1024px (Desktop)"
echo "   - 1440px (Large Desktop)"
echo "5. Navigate through all pages:"
echo "   - Dashboard"
echo "   - Applications"
echo "   - Application Details"
echo "   - Logs"
echo "   - Metrics"
echo "   - Teams"
echo "   - Profile"
echo "6. Check:"
echo "   - Layout doesn't break"
echo "   - Text is readable"
echo "   - Interactive elements are accessible"
echo "   - Images/charts resize properly"
echo "   - No horizontal scroll (except tables)"

echo ""
echo "✨ Responsive design verification complete!"
echo ""
echo "Note: This script provides automated checks and a manual"
echo "verification checklist. Full responsive testing requires"
echo "manual browser testing across different devices."
