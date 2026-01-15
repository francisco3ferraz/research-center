#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080/research-center/api}
AUTH_USER=${AUTH_USER:-admin}
AUTH_PASS=${AUTH_PASS:-admin}

# Login and capture token (if available)
TOKEN=$(curl -s -H "Content-Type: application/json" -d "{\"username\":\"$AUTH_USER\",\"password\":\"$AUTH_PASS\"}" "$BASE/auth/login" || true)
if [ -n "$TOKEN" ]; then
  AUTH_HEADER=( -H "Authorization: Bearer $TOKEN" )
  echo "Obtained token (length=${#TOKEN})"
else
  AUTH_HEADER=()
  echo "No token obtained; continuing without Authorization header"
fi

echo "BASE=$BASE"

echo "\n1) GET /publications/ (list)"
curl -i -sS "$BASE/publications/"

echo "\n2) GET /publications/{id} (details) - example id=1"
curl -i -sS "$BASE/publications/1"

echo "\n3) POST /publications/ (create JSON)"
cat <<'JSON' > /tmp/pub_metadata.json
{
  "title": "Test Publication from curl",
  "authors": ["Tester"],
  "type": "ARTICLE",
  "areaScientific": "Ciência de Dados",
  "year": 2026,
  "publisher": "ACME",
  "doi": "10.0000/example",
  "abstract": "Sample abstract",
  "confidential": false,
  "uploadedById": 1
}
JSON

curl -i -sS "${AUTH_HEADER[@]}" -H "Content-Type: application/json" -d @/tmp/pub_metadata.json "$BASE/publications/"

echo "\n4) POST /publications/ (create multipart: metadata + file)"
if [ -f sample.pdf ]; then
  curl -i -sS "${AUTH_HEADER[@]}" -F "file=@sample.pdf" -F "metadata=$(cat /tmp/pub_metadata.json);type=application/json" "$BASE/publications/"
else
  echo "skipping multipart test (sample.pdf not found in current dir)"
fi

echo "\n5) PUT /publications/{id} (update) - example id=1"
cat <<'JSON' > /tmp/pub_update.json
{
  "title": "Updated Title",
  "authors": ["Tester", "Coauthor"],
  "abstract": "Updated abstract",
  "aiGeneratedSummary": "AI summary",
  "year": 2026,
  "publisher": "ACME",
  "doi": "10.0000/example"
}
JSON
curl -i -sS "${AUTH_HEADER[@]}" -X PUT -H "Content-Type: application/json" -d @/tmp/pub_update.json "$BASE/publications/1"

echo "\n6) DELETE /publications/{id} (delete) - example id=2"
curl -i -sS "${AUTH_HEADER[@]}" -X DELETE "$BASE/publications/2"

echo "\n7) POST /publications/{id}/tags (add tag by body)"
curl -i -sS "${AUTH_HEADER[@]}" -X POST -H "Content-Type: application/json" -d '{"tagId":5}' "$BASE/publications/1/tags"

echo "\n8) POST /publications/{id}/tags/{tagId} (add tag by path)"
curl -i -sS "${AUTH_HEADER[@]}" -X POST "$BASE/publications/1/tags/5"

echo "\n9) DELETE /publications/{id}/tags/{tagId} (remove tag)"
curl -i -sS "${AUTH_HEADER[@]}" -X DELETE "$BASE/publications/1/tags/5"

echo "\n10) POST /publications/{id}/visiblity (set visibility - POST fallback)"
curl -i -sS "${AUTH_HEADER[@]}" -X POST -H "Content-Type: application/json" -d '{"visible":false}' "$BASE/publications/1/visiblity"

echo "\n11) GET /publications/{id}/file (download) - example id=1"
echo "(This will print binary to stdout; saving to /tmp/pub_1_file)
"
curl -sS "$BASE/publications/1/file" -o /tmp/pub_1_file || true
if [ -s /tmp/pub_1_file ]; then
  echo "file downloaded to /tmp/pub_1_file (size=$(wc -c < /tmp/pub_1_file) bytes)"
else
  echo "no file returned or empty"
fi

echo "\nDone."
