#!/bin/bash

# Test signup endpoint against running backend

echo "Testing Signup Endpoint"
echo "======================"
echo ""

TEST_EMAIL="testuser_$(date +%s)@example.com"
TEST_PASSWORD="TestPassword123"
TEST_FIRSTNAME="John"
TEST_LASTNAME="Doe"

echo "Sending signup request..."
echo "Email: $TEST_EMAIL"
echo "Password: $TEST_PASSWORD"
echo "FirstName: $TEST_FIRSTNAME"
echo "LastName: $TEST_LASTNAME"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST 'http://localhost:8080/api/auth/signup' \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"confirmPassword\":\"$TEST_PASSWORD\",\"firstName\":\"$TEST_FIRSTNAME\",\"lastName\":\"$TEST_LASTNAME\"}")

# Extract HTTP status code (last line)
HTTP_STATUS=$(echo "$RESPONSE" | tail -n 1)
# Extract response body (all but last line)
BODY=$(echo "$RESPONSE" | head -n -1)

echo "HTTP Status: $HTTP_STATUS"
echo "Response Body:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
echo ""

if [ "$HTTP_STATUS" == "201" ]; then
  echo "✓ Signup successful!"
  # Extract token from response
  TOKEN=$(echo "$BODY" | jq -r '.data.token' 2>/dev/null)
  if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "✓ JWT Token generated: ${TOKEN:0:50}..."
  fi
else
  echo "✗ Signup failed with status $HTTP_STATUS"
fi
