#!/usr/bin/env bash

REQUESTS_RAW="election.requests.raw"
REQUESTS="election.requests"

VOTES_RAW="election.votes.raw"
VOTES="election.votes"


kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$REQUESTS_RAW" \
  --partitions 1 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$REQUESTS" \
  --partitions 1 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$VOTES_RAW" \
  --partitions 1 \
  --replication-factor 1

kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic "$VOTES" \
  --partitions 1 \
  --replication-factor 1