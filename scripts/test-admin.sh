#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080/research-center/api}
USER=${USER:-admin}
PASS=${PASS:-admin}
TMPDIR=/tmp/research_tests_admin
mkdir -p "$TMPDIR"

echo "BASE=$BASE"

auth_login(){
  printf '%s' "{\"username\":\"$1\",\"password\":\"$2\"}" > "$TMPDIR/login.json"
  HTTP=$(curl -s -w "%{http_code}" -o "$TMPDIR/login_resp" -H "Content-Type: application/json" -d @$TMPDIR/login.json "$BASE/auth/login")
  BODY=$(cat "$TMPDIR/login_resp")
  if [ "$HTTP" -ne 200 ]; then
    echo "Login failed ($HTTP): $BODY" >&2
    return 1
  fi
  # token may be raw or JSON {"token":"..."}
  TOKEN=$(echo "$BODY" | sed -E 's/^[[:space:]]*"(.*)"[[:space:]]*$/\1/; s/.*"token"[[:space:]]*:[[:space:]]*"(.*)".*/\1/; s/\r//g;')
  TOKEN=$(echo "$TOKEN" | tr -d '"\r\n')
  if [ -z "$TOKEN" ]; then
    echo "Failed to parse token from login response: $BODY" >&2
    return 1
  fi
  AUTH=( -H "Authorization: Bearer $TOKEN" )
  echo "Obtained token (length=${#TOKEN})"
}

# helper: run curl and print HTTP and body
run(){
  DESC=$1; shift
  printf "\n== %s ==\n" "$DESC"
  HTTP=$(curl -s -w "%{http_code}" -o "$TMPDIR/last_resp" "$@")
  BODY=$(cat "$TMPDIR/last_resp" || true)
  echo "HTTP/$HTTP"
  echo "$BODY"
  return 0
}

# Login as admin
auth_login "$USER" "$PASS"

# 1) Inspect current user info
run "GET /auth/user" curl -sS "${AUTH[@]}" -H "Accept: application/json" "$BASE/auth/user"

# 2) List publications (paged)
run "GET /publications (list)" curl -sS "$BASE/publications?page=0&size=10"

# 3) Create a publication (JSON)
cat > "$TMPDIR/pub.json" <<'JSON'
{
  "title": "Admin Test Publication",
  "authors": ["Admin Author"],
  "type": "ARTICLE",
  "areaScientific": "Admin Area",
  "year": 2026,
  "publisher": "AdminPub",
  "doi": "10.1000/admin",
  "abstract": "Admin created publication for tests",
  "confidential": false
}
JSON
run "POST /publications (create JSON)" curl -sS -X POST "${AUTH[@]}" -H "Content-Type: application/json" -d @"$TMPDIR/pub.json" "$BASE/publications"

# Capture created id
CREATED_ID=$(cat "$TMPDIR/last_resp" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p' | head -n1 || true)
if [ -n "$CREATED_ID" ]; then echo "Created publication id=$CREATED_ID"; fi

# 4) Create multipart with sample.pdf if present
if [ -f sample.pdf ]; then
  run "POST /publications (multipart metadata+file)" curl -sS -X POST "${AUTH[@]}" -F "file=@sample.pdf" -F "metadata=$(cat $TMPDIR/pub.json);type=application/json" "$BASE/publications"
else
  echo "sample.pdf not present — skipping multipart test"
fi

# 5) Update the created publication
if [ -n "$CREATED_ID" ]; then
  cat > "$TMPDIR/pub_update.json" <<JSON
{
  "title": "Admin Updated Title",
  "authors": ["Admin Author","Coauthor"],
  "abstract": "Updated by admin",
  "aiGeneratedSummary": "Admin summary",
  "year": 2026,
  "publisher": "AdminPub"
}
JSON
  run "PUT /publications/$CREATED_ID" curl -sS -X PUT "${AUTH[@]}" -H "Content-Type: application/json" -d @"$TMPDIR/pub_update.json" "$BASE/publications/$CREATED_ID"
fi

# 6) Add tag by body and by path
if [ -n "$CREATED_ID" ]; then
  run "POST /publications/$CREATED_ID/tags (body)" curl -sS -X POST "${AUTH[@]}" -H "Content-Type: application/json" -d '{"tagId":1}' "$BASE/publications/$CREATED_ID/tags"
  run "POST /publications/$CREATED_ID/tags/2 (path)" curl -sS -X POST "${AUTH[@]}" "$BASE/publications/$CREATED_ID/tags/2"
fi

# 7) Remove tag (admin privilege) — expect success
if [ -n "$CREATED_ID" ]; then
  run "DELETE /publications/$CREATED_ID/tags/2" curl -sS -X DELETE "${AUTH[@]}" "$BASE/publications/$CREATED_ID/tags/2"
fi

# 8) Set visibility (POST fallback)
if [ -n "$CREATED_ID" ]; then
  run "POST /publications/$CREATED_ID/visiblity (set visible=false)" curl -sS -X POST "${AUTH[@]}" -H "Content-Type: application/json" -d '{"visible":false}' "$BASE/publications/$CREATED_ID/visiblity"
fi

# 9) Delete publication (admin)
if [ -n "$CREATED_ID" ]; then
  run "DELETE /publications/$CREATED_ID" curl -sS -X DELETE "${AUTH[@]}" "$BASE/publications/$CREATED_ID"
fi

# 10) Try admin-only operations if any (example: get all users via /auth/user not available), list publications again
run "GET /publications (after ops)" curl -sS "$BASE/publications"

echo "Admin tests finished. Temp dir: $TMPDIR"
