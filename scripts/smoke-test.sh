#!/usr/bin/env bash
# End-to-end smoke test for the running Spring Boot backend.
# 1. Start the backend first (mvn spring-boot:run or Run As > Java Application in Eclipse).
# 2. Then run:  bash scripts/smoke-test.sh
# It calls every REST route with curl and checks the HTTP status codes.

BASE="${1:-http://localhost:8080}/api/computers"
PASS=0
FAIL=0

check() { # check <description> <expected status> <actual status>
  if [ "$2" = "$3" ]; then
    echo "  PASS  $1 -> $3"
    PASS=$((PASS + 1))
  else
    echo "  FAIL  $1 -> expected $2, got $3"
    FAIL=$((FAIL + 1))
  fi
}

status() { # prints only the HTTP status code of a curl call
  curl -s -o /dev/null -w "%{http_code}" "$@"
}

echo "Smoke test against $BASE"

check "GET page 0 (size 5)"            200 "$(status "$BASE?page=0&size=5")"
check "GET last page (page 2)"         200 "$(status "$BASE?page=2&size=5")"
check "GET bad size (size=0)"          400 "$(status "$BASE?page=0&size=0")"

# POST returns the body and, on the last line, the status code.
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE" -H "Content-Type: application/json" \
  -d '{"name":"Smoke Test PC","brand":"Asus","serialNumber":"SMOKE-0001","status":"AVAILABLE"}')
CODE=$(echo "$RESPONSE" | tail -n 1)
BODY=$(echo "$RESPONSE" | sed '$d')
ID=$(echo "$BODY" | sed -E 's/.*"id":([0-9]+).*/\1/')
[[ "$ID" =~ ^[0-9]+$ ]] || ID=999999
check "POST new computer (new id $ID)" 201 "$CODE"

check "POST duplicate serial"          409 "$(status -X POST "$BASE" -H 'Content-Type: application/json' \
  -d '{"name":"Copy","brand":"Asus","serialNumber":"SMOKE-0001","status":"AVAILABLE"}')"
check "POST invalid body"              400 "$(status -X POST "$BASE" -H 'Content-Type: application/json' \
  -d '{"name":"","brand":"A","serialNumber":"x","status":"AVAILABLE"}')"
check "GET by id $ID"                  200 "$(status "$BASE/$ID")"
check "GET id that is not a number"    400 "$(status "$BASE/abc")"
check "PUT update id $ID"              200 "$(status -X PUT "$BASE/$ID" -H 'Content-Type: application/json' \
  -d '{"name":"Smoke Test PC","brand":"Asus","serialNumber":"SMOKE-0001","status":"IN_REPAIR"}')"
check "DELETE id $ID"                  204 "$(status -X DELETE "$BASE/$ID")"
check "GET deleted id $ID"             404 "$(status "$BASE/$ID")"
check "PUT missing id"                 404 "$(status -X PUT "$BASE/999999" -H 'Content-Type: application/json' \
  -d '{"name":"Nobody","brand":"Asus","serialNumber":"SMOKE-0002","status":"AVAILABLE"}')"
check "DELETE missing id"              404 "$(status -X DELETE "$BASE/999999")"

echo "Result: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
