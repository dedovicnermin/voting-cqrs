#!/usr/bin/env bash

REQUESTS_RAW="election.requests.raw"


kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic "$REQUESTS_RAW" \
  --property "parse.key=true" \
  --property "key.separator=|" \
  < ./data/elections.json


