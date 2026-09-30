SHELL := /bin/bash
DC := docker compose
TOOLS := --profile tools

# If the user cannot access the Docker socket, fall back to the rootless
# Podman socket (docker-compatible API) automatically.
PODMAN_SOCKET := unix://$(or $(XDG_RUNTIME_DIR),/run/user/$(shell id -u))/podman/podman.sock
DOCKER_HOST ?= $(shell if [ -w /var/run/docker.sock ]; then echo ""; else systemctl --user start podman.socket 2>/dev/null; echo "$(PODMAN_SOCKET)"; fi)
export DOCKER_HOST

.DEFAULT_GOAL := help

# ─────────────────────────────────────────────────────────────────────────────
# ERP Core - everything runs in Docker. No local Java/Node required.
# ─────────────────────────────────────────────────────────────────────────────

.PHONY: help up down logs ps build test verify fe-install fe-build fe-lint fe-typecheck e2e reset clean bootstrap-mvnw

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

up: ## Start the full stack (postgres, pgadmin, backend, frontend)
	$(DC) up --build -d

down: ## Stop the stack (keeps volumes)
	$(DC) down

logs: ## Tail logs
	$(DC) logs -f --tail=100

ps: ## Show container status
	$(DC) ps

build: ## Compile + package backend (no tests)
	$(DC) $(TOOLS) run --rm maven clean package -DskipTests

test: ## Run backend unit + integration tests (Testcontainers)
	$(DC) $(TOOLS) run --rm maven clean test

verify: ## Full backend verification (tests + coverage report)
	$(DC) $(TOOLS) run --rm maven clean verify

fe-install: ## Install frontend dependencies
	$(DC) $(TOOLS) run --rm node install

fe-build: ## Production build of the frontend
	$(DC) $(TOOLS) run --rm node run build

fe-lint: ## Lint frontend
	$(DC) $(TOOLS) run --rm node run lint

fe-typecheck: ## Typecheck frontend
	$(DC) $(TOOLS) run --rm node run typecheck

e2e: ## Run Playwright end-to-end tests (installs Chromium deps in-container)
	$(DC) $(TOOLS) run --rm --entrypoint sh e2e -c "npx playwright install --with-deps chromium && npx playwright test"

reset: ## Reset demo data via API (requires backend running)
	curl -s -X POST http://localhost:$${BACKEND_PORT:-8080}/api/v1/demo/reset -H "Authorization: Bearer $$(curl -s -X POST http://localhost:$${BACKEND_PORT:-8080}/api/v1/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')" | python3 -m json.tool

clean: ## Stop stack and DELETE volumes (fresh start)
	$(DC) down -v --remove-orphans

bootstrap-mvnw: ## Generate the Maven wrapper using a container
	docker run --rm -v $(PWD):/workspace:Z -w /workspace maven:3.9-eclipse-temurin-21 mvn -N -ntp wrapper:wrapper -Dmaven=3.9.11
