#!/usr/bin/env bash
# Records the Playwright golden path (video) and converts it to docs/demo.gif.
# Runs inside containers - no local Node/ffmpeg needed.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Running Playwright golden path (records video + screenshots)..."
docker compose --profile tools run --rm e2e test

VIDEO=$(find docs/screenshots -name "video.webm" | head -1 || true)
if [ -n "${VIDEO:-}" ]; then
  echo "Converting $VIDEO to GIF with ffmpeg..."
  docker run --rm -v "$PWD:/work:Z" linuxserver/ffmpeg:latest \
    -i "/work/$VIDEO" -vf "fps=12,scale=1200:-1:flags=lanczos" /work/docs/demo.gif
else
  echo "No video found; screenshots are in docs/screenshots/"
fi

echo "Done. Artifacts:"
ls -la docs/screenshots docs/demo.gif 2>/dev/null || true
