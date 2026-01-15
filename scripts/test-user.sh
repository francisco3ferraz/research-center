#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080/research-center/api}
USER=${USER:-joao}
PASS=${PASS:-joao123}
TMPDIR=/tmp/research_tests_user
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
  TOKEN=$(echo "$BODY" | sed -E 's/^[[:space:]]*"(.*)"[[:space:]]*$/\1/; s/.*"token"[[:space:]]*:[[:space:]]*"(.*)".*/\1/; s/\r//g;')
  TOKEN=$(echo "$TOKEN" | tr -d '"\r\n')
  if [ -z "$TOKEN" ]; then
    echo "Failed to parse token from login response: $BODY" >&2
    return 1
  fi
  AUTH=( -H "Authorization: Bearer $TOKEN" )
  echo "Obtained token (length=${#TOKEN})"
}

run(){
  DESC=$1; shift
  printf "\n== %s ==\n" "$DESC"
  HTTP=$(curl -s -w "%{http_code}" -o "$TMPDIR/last_resp" "$@")
  BODY=$(cat "$TMPDIR/last_resp" || true)
  echo "HTTP/$HTTP"
  echo "$BODY"
  return 0
}

# login as user
auth_login "$USER" "$PASS"

# 1) get current user
run "GET /auth/user" curl -sS "${AUTH[@]}" -H "Accept: application/json" "$BASE/auth/user"

# 2) list publications
run "GET /publications" curl -sS "$BASE/publications?page=0&size=10"

# 3) create publication (JSON)
cat > "$TMPDIR/pub.json" <<'JSON'
{
  "title": "User Test Publication",
  "authors": ["User Author"],
  "type": "ARTICLE",
  "areaScientific": "User Area",
  "year": 2026,
  "publisher": "UserPub",
  "doi": "10.1000/user",
  "abstract": "User created publication for tests",
  "confidential": false
}
JSON
run "POST /publications (create JSON)" curl -sS -X POST "${AUTH[@]}" -H "Content-Type: application/json" -d @"$TMPDIR/pub.json" "$BASE/publications"
CREATED_ID=$(cat "$TMPDIR/last_resp" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p' | head -n1 || true)
if [ -n "$CREATED_ID" ]; then echo "Created publication id=$CREATED_ID"; fi

# 4) upload file via multipart if sample.pdf exists
if [ -f sample.pdf ] && [ -n "$CREATED_ID" ]; then
  run "POST /publications (multipart)" curl -sS -X POST "${AUTH[@]}" -F "file=@sample.pdf" -F "metadata=$(cat $TMPDIR/pub.json);type=application/json" "$BASE/publications"
fi

# 5) add tag by path
if [ -n "$CREATED_ID" ]; then
  run "POST /publications/$CREATED_ID/tags/1 (add tag path)" curl -sS -X POST "${AUTH[@]}" "$BASE/publications/$CREATED_ID/tags/1"
fi

# 6) try adding tag by body (permission should allow own additions)
if [ -n "$CREATED_ID" ]; then
  run "POST /publications/$CREATED_ID/tags (body)" curl -sS -X POST "${AUTH[@]}" -H "Content-Type: application/json" -d '{"tagId":2}' "$BASE/publications/$CREATED_ID/tags"
fi

# 7) download file if present on DTO
if [ -n "$CREATED_ID" ]; then
  FILEURL=$(cat "$TMPDIR/last_resp" | sed -n 's/.*"fileUrl"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' || true)
  if [ -n "$FILEURL" ]; then
    run "GET file URL" curl -sS "${AUTH[@]}" "$FILEURL" -o "$TMPDIR/downloaded_file" -w "%{http_code}"
    echo "Downloaded to $TMPDIR/downloaded_file"
  else
    echo "No fileUrl present in response; fetching publication details to find file"
    run "GET publication details" curl -sS "$BASE/publications/$CREATED_ID"
    FILEURL=$(cat "$TMPDIR/last_resp" | sed -n 's/.*"fileUrl"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' || true)
    if [ -n "$FILEURL" ]; then
      run "GET file URL" curl -sS "${AUTH[@]}" "$FILEURL" -o "$TMPDIR/downloaded_file" -w "%{http_code}"
      echo "Downloaded to $TMPDIR/downloaded_file"
    fi
  fi
fi

# 8) update own publication
if [ -n "$CREATED_ID" ]; then
  cat > "$TMPDIR/pub_update.json" <<JSON
{
  "title": "User Updated Title",
  "abstract": "Updated by user"
}
JSON
  run "PUT /publications/$CREATED_ID" curl -sS -X PUT "${AUTH[@]}" -H "Content-Type: application/json" -d @"$TMPDIR/pub_update.json" "$BASE/publications/$CREATED_ID"
fi

# 9) attempt to delete another user's publication (expect 403 or 404)
# we attempt delete on a likely existing id=1 (change if needed)
run "DELETE /publications/1 (attempt)" curl -sS -X DELETE "${AUTH[@]}" "$BASE/publications/1"

# 10) list publications for user
run "GET /publications (after ops)" curl -sS "$BASE/publications?page=0&size=10"

echo "User tests finished. Temp dir: $TMPDIR"
