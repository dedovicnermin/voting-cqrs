#!/usr/bin/env bash

COMMANDS="election.commands"
LEGAL_COMMANDS="election.events"

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$COMMANDS" \
  --partitions 1 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$LEGAL_COMMANDS" \
  --partitions 1 \
  --replication-factor 1

