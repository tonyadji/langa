#!/bin/bash

# T232: Bundle Size Verification Script
# Ensures main bundle < 200KB gzipped per constitution Section IV

set -e

echo "🔍 Building production bundle..."
npm run build > /dev/null 2>&1

echo ""
echo "📦 Analyzing bundle sizes..."
echo ""

# Check if dist directory exists
if [ ! -d "dist" ]; then
  echo "❌ Error: dist directory not found. Build may have failed."
  exit 1
fi

# Find JavaScript files and calculate gzipped sizes
total_js_size=0
max_size=$((200 * 1024)) # 200KB in bytes

echo "JavaScript bundles:"
echo "-------------------"

for file in dist/assets/*.js; do
  if [ -f "$file" ]; then
    # Get original size
    original_size=$(wc -c < "$file")
    
    # Gzip the file and get size
    gzipped_size=$(gzip -c "$file" | wc -c)
    
    # Convert to KB
    original_kb=$(echo "scale=2; $original_size / 1024" | bc)
    gzipped_kb=$(echo "scale=2; $gzipped_size / 1024" | bc)
    
    filename=$(basename "$file")
    echo "  $filename:"
    echo "    Original: ${original_kb} KB"
    echo "    Gzipped:  ${gzipped_kb} KB"
    
    total_js_size=$((total_js_size + gzipped_size))
  fi
done

echo ""
echo "CSS bundles:"
echo "------------"

for file in dist/assets/*.css; do
  if [ -f "$file" ]; then
    original_size=$(wc -c < "$file")
    gzipped_size=$(gzip -c "$file" | wc -c)
    
    original_kb=$(echo "scale=2; $original_size / 1024" | bc)
    gzipped_kb=$(echo "scale=2; $gzipped_size / 1024" | bc)
    
    filename=$(basename "$file")
    echo "  $filename:"
    echo "    Original: ${original_kb} KB"
    echo "    Gzipped:  ${gzipped_kb} KB"
  fi
done

echo ""
echo "Summary:"
echo "--------"

total_kb=$(echo "scale=2; $total_js_size / 1024" | bc)
threshold_kb=200

echo "Total JS (gzipped): ${total_kb} KB"
echo "Threshold:          ${threshold_kb} KB"

# Check if within threshold
if [ $(echo "$total_js_size > $max_size" | bc) -eq 1 ]; then
  echo ""
  echo "❌ FAILED: Bundle size exceeds 200KB threshold!"
  echo "   Reduce bundle size by:"
  echo "   - Enabling code splitting"
  echo "   - Lazy loading routes"
  echo "   - Tree-shaking unused code"
  echo "   - Using smaller dependencies"
  exit 1
else
  echo ""
  echo "✅ PASSED: Bundle size is within threshold"
  remaining=$(echo "scale=2; ($max_size - $total_js_size) / 1024" | bc)
  echo "   Remaining budget: ${remaining} KB"
fi

echo ""
echo "📊 Detailed breakdown saved to: dist/stats.txt"

# Save detailed stats
{
  echo "Build Date: $(date)"
  echo ""
  echo "JavaScript Files:"
  find dist/assets -name "*.js" -exec sh -c 'echo "  $(basename {}): $(wc -c < {}) bytes"' \;
  echo ""
  echo "CSS Files:"
  find dist/assets -name "*.css" -exec sh -c 'echo "  $(basename {}): $(wc -c < {}) bytes"' \;
} > dist/stats.txt

echo "✨ Done!"
