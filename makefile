default: help

.PHONY: pre-push
pre-push: format lint test-ui test-app test-e2e ## Format, lint, & test

.PHONY: format
format: format-ui format-app format-auth format-e2e-test ## Format files

.PHONY: format-ui
format-ui: ## Format /ui files
	cd ui && npm run format

.PHONY: format-app
format-app: ## Format /app files
	./gradlew :app:spotlessApply

.PHONY: format-auth
format-auth: ## Format /authoriser files
	./gradlew :authoriser:spotlessApply

.PHONY: format-e2e-test
format-e2e-test: ## Format /e2eTest files
	cd e2eTest && npm run format

.PHONY: lint
lint: lint-ui lint-e2e-test ## Lint /ui & /e2eTest files.  TODO: /app, /authoriser linting

.PHONY: lint-ui
lint-ui: ## Lint /ui files
	cd ui && npm run lint

.PHONY: lint-e2e-test
lint-e2e-test: ## Lint /e2eTest files
	cd e2eTest && npm run lint

.PHONY: test
test: test-ui test-app test-e2e ## Run all unit, integration, & E2E tests

.PHONY: test-ui
test-ui: ## Run /ui tests
	cd ui && npm run test:nw

.PHONY: test-app
test-app: test-app-unit test-app-integration ## Run /app unit & integration tests

.PHONY: test-app-unit
test-app-unit: ## Run /app unit tests (no logs)
	./gradlew test --rerun-tasks

.PHONY: test-app-unit-with-logs
test-app-with-logs: ## Run /app unit tests (with logs)
	./gradlew test --rerun-tasks --info

.PHONY: test-app-integration
test-app-integration: ## Run /app integration tests (no logs)
	AWS_ENDPOINT="http://localhost:4566" ./gradlew integrationTest

.PHONY: test-app-integration-with-logs
test-app-integration-with-logs: ## Run /app integration tests (with logs)
	AWS_ENDPOINT="http://localhost:4566" ./gradlew integrationTest --info

.PHONY: test-e2e
test-e2e: ## Run E2E tests (without visible browser)
	cd e2eTest && npm run test

.PHONY: test-e2e-open
test-e2e-open: ## Run E2E tests (with visible browser)
	cd e2eTest && npm run test:open

.PHONY: install
install: install-ui install-app install-e2e-test  ## Install dependencies

.PHONY: install-ui
install-ui: ## Install /ui dependencies
	cd ui && npm i

.PHONY: install-app
install-app: ## Install /app dependencies
	./gradlew build --refresh-dependencies

.PHONY: install-e2e-test
install-e2e-test: ## Install /e2eTest dependencies
	cd e2eTest && npm i

.PHONY: build-and-deploy-to-localstack
build-and-deploy-to-localstack: build-api-jars deploy-to-localstack ## Build & deploy to LocalStack

.PHONY: build-ui
build-ui: ## Build the UI
	cd ui && npm run build

.PHONY: build-api-jars
build-api-jars: ## Build API JARs
	./tasks _build-api-jars

.PHONY: deploy-to-localstack
deploy-to-localstack: ## Deploy to LocalStack
	./tasks _deploy-to-localstack

.PHONY: start-ui
start-ui: ## Start the UI
	cd ui && npm start

.PHONY: start-localstack
start-localstack: ## Start LocalStack
	./tasks start-localstack

.PHONY: view-localstack-logs
view-localstack-logs: ## View LocalStack logs
	./tasks view-localstack-logs

.PHONY: help
help: ## Show help
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
