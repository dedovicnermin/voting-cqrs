#!/usr/bin/env bash

date
echo ""

echo "Spinning up kind cluster."
kind create cluster --name playground --config kind-config.yml

echo "Sleeping for 60s before applying taint."
sleep 60

echo "Applying taint."
./taint.sh

echo ""
date



