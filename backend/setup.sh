#!/usr/bin/env bash
# Creates the FastTrack competition collections in PocketBase.
# Run AFTER starting `./pocketbase serve` and creating your first superuser
# via http://127.0.0.1:8090/_/

set -e

PB_URL="${PB_URL:-http://127.0.0.1:8090}"
echo "PocketBase: $PB_URL"

read -p "Superuser email: " EMAIL
read -s -p "Superuser password: " PASS
echo ""

TOKEN=$(curl -sf -X POST "$PB_URL/api/collections/_superusers/auth-with-password" \
  -H "Content-Type: application/json" \
  -d "{\"identity\":\"$EMAIL\",\"password\":\"$PASS\"}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

[ -z "$TOKEN" ] && echo "❌ Auth failed" && exit 1
echo "✅ Authenticated"

USERS_ID=$(curl -sf -H "Authorization: $TOKEN" "$PB_URL/api/collections/users" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "  users collection id: $USERS_ID"

# --- profiles collection ---
curl -sf -X POST "$PB_URL/api/collections" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"profiles\",
    \"type\": \"base\",
    \"listRule\": \"@request.auth.id != ''\",
    \"viewRule\": \"@request.auth.id != ''\",
    \"createRule\": \"@request.auth.id = userId\",
    \"updateRule\": \"@request.auth.id = userId\",
    \"deleteRule\": \"@request.auth.id = userId\",
    \"fields\": [
      {\"name\":\"userId\",\"type\":\"relation\",\"required\":true,\"unique\":true,\"collectionId\":\"$USERS_ID\",\"maxSelect\":1,\"cascadeDelete\":true},
      {\"name\":\"friend_code\",\"type\":\"text\",\"required\":true,\"unique\":true,\"min\":6,\"max\":6},
      {\"name\":\"total_fasting_hours\",\"type\":\"number\",\"min\":0},
      {\"name\":\"weekly_fasting_hours\",\"type\":\"number\",\"min\":0},
      {\"name\":\"streak_days\",\"type\":\"number\",\"min\":0}
    ]
  }" > /dev/null
echo "✅ Created profiles collection"

# --- friendships collection ---
curl -sf -X POST "$PB_URL/api/collections" \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"friendships\",
    \"type\": \"base\",
    \"listRule\": \"@request.auth.id = userId\",
    \"viewRule\": \"@request.auth.id = userId\",
    \"createRule\": \"@request.auth.id != ''\",
    \"updateRule\": null,
    \"deleteRule\": \"@request.auth.id = userId\",
    \"fields\": [
      {\"name\":\"userId\",\"type\":\"relation\",\"required\":true,\"collectionId\":\"$USERS_ID\",\"maxSelect\":1},
      {\"name\":\"friendId\",\"type\":\"relation\",\"required\":true,\"collectionId\":\"$USERS_ID\",\"maxSelect\":1}
    ]
  }" > /dev/null
echo "✅ Created friendships collection"
echo ""
echo "🎉 Setup complete! Start the Android app and open the Competition tab."
