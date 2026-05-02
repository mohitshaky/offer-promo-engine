# Offer Promo Engine

> **What problem this solves:** Automates promo code validation — so marketing teams skip manual approval and coupons apply in real-time.

[![CI](https://github.com/mohitshaky/offer-promo-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/mohitshaky/offer-promo-engine/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)

## Key Results
- ✅ Handles 500+ concurrent requests
- ✅ Sub-10ms Redis cache hits
- ✅ K8s autoscaled + Grafana monitored

## Tech Stack
Java 17 · Spring Boot · Redis · PostgreSQL · Kafka · Docker · Kubernetes · Grafana

## What It Does
A high-performance promotion and coupon validation engine built on Spring Boot. It validates promo codes against business rules in real-time using Redis-backed caching, publishes events to Kafka for downstream processing, and exposes a clean REST API for marketing and checkout integrations.

## Quick Start
```bash
# clone and run
git clone https://github.com/mohitshaky/offer-promo-engine.git
cd offer-promo-engine
./gradlew bootRun
```

## License
MIT — see [LICENSE](LICENSE)