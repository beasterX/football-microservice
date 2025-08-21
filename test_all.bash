#!/usr/bin/env bash
#
# Sample usage:
#   ./test_all.bash start stop
#   start and stop are optional
#
: ${HOST=localhost}
: ${PORT=8080}

# last response body (without the HTTP code)
RESPONSE=""

function assertCurl() {
  local expected=$1
  local cmd="$2 -w \"%{http_code}\""
  local result
  result=$(eval $cmd)
  local httpCode="${result: -3}"
  local body="${result%???}"
  RESPONSE="$body"

  if [ -n "$body" ]; then
    echo "$body" | jq .
  fi

  if [ "$httpCode" = "$expected" ]; then
    echo "OK HTTP $httpCode"
  else
    echo "FAIL: expected HTTP $expected, got $httpCode"
    echo "  cmd: $cmd"
    exit 1
  fi
}

function assertEqual() {
  if [ "$1" = "$2" ]; then
    echo "OK value: $2"
  else
    echo "FAIL: expected '$1', got '$2'"
    exit 1
  fi
}

function testUrl() {
  curl -ks -f -o /dev/null "$@" && return 0 || return 1
}

function waitForGateway() {
  echo -n "Waiting for API gateway… "
  until testUrl http://$HOST:$PORT/api/v1/customers; do
    sleep 2; echo -n "."
  done
  echo " up!"
}

set -e

if [[ $@ == *start* ]]; then
  echo "Bringing services up…"
  docker-compose down
  docker-compose up -d
fi

waitForGateway

echo
echo "─────────────────────────────────"
echo " Downstream services via API Gateway"
echo "─────────────────────────────────"

echo "- GET all customers"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers"

echo "- POST new customer"
cust_body='{
  "firstName":"Zinedine","lastName":"Zidane",
  "email":"zidne.z@gmail.com","phone":"3334445555",
  "preferredContact":"EMAIL",
  "street":"Rue de Rivoli 50","city":"Paris",
  "state":"Ile-de-France","postalCode":"75001",
  "country":"France"
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers \
  -H 'Content-Type: application/json' -d '$cust_body'"
custId=$(echo "$RESPONSE" | jq -r .customerId)

echo "- GET customer by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId"

echo "- PUT update customer"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId"
update_cust=$(echo "$RESPONSE" | jq -c '
  del(._links)
  | .firstName = "Z."
  | .lastName  = "Zidane"
')
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/customers/$custId \
  -H 'Content-Type: application/json' -d '$update_cust'"

echo "- DELETE customer"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/customers/$custId"

echo
echo "- GET all apparels"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/apparels"

echo "- POST new apparel"
app_body='{
  "itemName":"Test Shirt","description":"T","brand":"B",
  "price":5.99,"cost":2.50,"stock":10,
  "apparelType":"JERSEY","sizeOption":"M"
}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/apparels \
  -H 'Content-Type: application/json' -d '$app_body'"
appId=$(echo "$RESPONSE" | jq -r .apparelId)

echo "- GET apparel by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/apparels/$appId"

echo "- PUT update apparel"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/apparels/$appId"
update_app=$(echo "$RESPONSE" | jq -c '
  del(._links)
  | .description = "Updated"
')
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/apparels/$appId \
  -H 'Content-Type: application/json' -d '$update_app'"

echo "- DELETE apparel"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/apparels/$appId"

echo
echo "- GET all warehouses"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/warehouses"

echo "- POST new warehouse"
wh_body='{"locationName":"WhTest","address":"Addr","capacity":150}'
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/warehouses \
  -H 'Content-Type: application/json' -d '$wh_body'"
whId=$(echo "$RESPONSE" | jq -r .warehouseId)

echo "- GET warehouse by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/warehouses/$whId"

echo "- PUT update warehouse"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/warehouses/$whId"
update_wh=$(echo "$RESPONSE" | jq -c '
  del(._links)
  | .capacity = 200
')
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/warehouses/$whId \
  -H 'Content-Type: application/json' -d '$update_wh'"

echo "- DELETE warehouse"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/warehouses/$whId"

echo
echo "- Re-create apparel for orders"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/apparels \
  -H 'Content-Type: application/json' -d '$app_body'"
appId=$(echo "$RESPONSE" | jq -r .apparelId)

echo "- Re-create warehouse for orders"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/warehouses \
  -H 'Content-Type: application/json' -d '$wh_body'"
whId=$(echo "$RESPONSE" | jq -r .warehouseId)

echo
echo "─────────────────────────────────"
echo " Orders‐service CRUD "
echo "─────────────────────────────────"

echo "- Re-create customer for orders"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers \
  -H 'Content-Type: application/json' -d '$cust_body'"
custId=$(echo "$RESPONSE" | jq -r .customerId)

echo "- GET all orders (empty)"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId/orders"
assertEqual 0 "$(echo "$RESPONSE" | jq '.|length')"

echo "- GET orders with bad customerId → 422"
assertCurl 422 "curl -s -X GET http://$HOST:$PORT/api/v1/customers/bad-id/orders"

echo "- GET missing order → 404"
assertCurl 404 "curl -s -X GET http://$HOST:$PORT/api/v1/customers/$custId/orders/00000000-0000-0000-0000-000000000000"

echo "- POST new order"
order_body="{
  \"warehouseId\":\"$whId\",
  \"items\":[
    {\"apparelId\":\"$appId\",\"quantity\":1,\"unitPrice\":5.99,\"discount\":0,\"currency\":\"USD\"}
  ]
}"
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers/$custId/orders \
  -H 'Content-Type: application/json' -d '$order_body'"
orderId=$(echo "$RESPONSE" | jq -r .orderId)

echo "- GET all orders (1)"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId/orders"
assertEqual 1 "$(echo "$RESPONSE" | jq '.|length')"

echo "- GET order by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId/orders/$orderId"
assertEqual "$orderId" "$(echo "$RESPONSE" | jq -r .orderId)"

echo "- PUT update order → quantity 2"
update_body="{
  \"warehouseId\":\"$whId\",
  \"items\":[
    {\"apparelId\":\"$appId\",\"quantity\":2,\"unitPrice\":5.99,\"discount\":0,\"currency\":\"USD\"}
  ],
  \"orderStatus\":\"PROCESSING\",
  \"paymentStatus\":\"AUTHORIZED\"
}"
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/customers/$custId/orders/$orderId \
  -H 'Content-Type: application/json' -d '$update_body'"

echo "- GET updated order, check quantity"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/$custId/orders/$orderId"
assertEqual 2 "$(echo "$RESPONSE" | jq '.items[0].quantity')"

echo "- DELETE order"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/customers/$custId/orders/$orderId"

echo
echo "- GET all orders (post-deletion, verbose)"
curl -v http://$HOST:$PORT/api/v1/customers/$custId/orders | jq .

if [[ $@ == *stop* ]]; then
  echo "Tearing down…"
  docker-compose down
fi
