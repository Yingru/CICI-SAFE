#!/bin/bash

if [ "$#" -ne 1 ]; then
   echo "illegal number of parameters"
   echo "usage: "$0" SafeServerIP"
   exit 1
fi

SAFESERVER_IP=$1



echo "IDSet"

curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"iCqAs3PcsfGWjF3Ywcr47-0Jfd15Z7EQ3v6sSVza8KY\", \"otherValues\": [\"as0\"] }"
curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"eZz9xWGKCwBdf8VjRtuSXYfaO1jLgjwOfrX8N3s14f8\", \"otherValues\": [\"as1\"] }"
curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"GA6A1fkLhsWUB7SKaPSCw8DO_ozLVM-Q8l0NdsqwRiA\", \"otherValues\": [\"as2\"] }"
curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"t8UXUgNmiZY8jloIIvCxi9jSjzXUaokqdwtzvyoy95c\", \"otherValues\": [\"as0\"] }"
curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"eQ-b5wEKqzjI1PSCuqZY6EVQdMWyGAZob2JzkW5pLsg\", \"otherValues\": [\"as1\"] }"
curl  -v -X POST http://${SAFESERVER_IP}:7777/postIdSet -H "Content-Type: application/json" -d "{ \"principal\": \"XLajumPCpZWScMJUoxrApXMCxRllpAVnFIwzv7CFpsU\", \"otherValues\": [\"as2\"] }"