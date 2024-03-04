#!/usr/bin/env bash

kubectl taint nodes playground-worker app=infra:NoSchedule
