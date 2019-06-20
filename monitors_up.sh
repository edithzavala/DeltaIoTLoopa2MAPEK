#!/bin/bash

docker build -f Dockerfile_mn -t iotmonitorl1mn ../DeltaIoTLoopaMonitor
docker-compose -f docker-compose_monitor.yml up