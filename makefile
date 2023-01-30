default: help

.PHONY: pre-push
pre-push: format lint test-ui test-app test-e2e ## Format, lint, & run unit, integration, & E2E tests

.PHONY: format
format: format-ui format-app format-e2e-test ## Format compatible files in /ui, /app, & /e2eTest

.PHONY: format-ui
format-ui: ## Format /ui Prettier-compatible files
	cd ui && npm run format

.PHONY: format-app
format-app: ## Format /app .java files
	./gradlew goJF

.PHONY: format-e2e-test
format-e2e-test: ## Format /e2eTest Prettier-compatible files
	cd e2eTest && npm run format

.PHONY: lint
lint: lint-ui lint-e2e-test ## Lint compatible files in /ui & /e2eTest.  Todo: BE linting

.PHONY: lint-ui
lint-ui: ## Lint /ui .js[x] files
	cd ui && npm run lint

.PHONY: lint-e2e-test
lint-e2e-test: ## Lint /e2eTest .js files
	cd e2eTest && npm run lint

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
test-app-integration: ## Run /app integration tests
	./gradlew e2eTest

.PHONY: test-e2e
test-e2e: ## Run E2E test (without visible browser)
	cd e2eTest && npm run test

.PHONY: test-e2e-open
test-e2e-open: ## Run E2E test (with visible browser)
	cd e2eTest && npm run test:open

.PHONY: install
install: install-ui install-app install-e2e-test  ## Install dependencies in /ui, /app, & /e2eTest

.PHONY: install-ui
install-ui: ## Install dependencies in /ui
	cd ui && npm i

.PHONY: install-app
install-app: ## Install dependencies in /app
	./gradlew build --refresh-dependencies

.PHONY: install-e2e-test
install-e2e-test: ## Install dependencies in /e2eTest
	cd e2eTest && npm i

.PHONY: build-and-deploy-to-localstack
build-and-deploy-to-localstack: build-api-jars deploy-to-localstack ## Build & deploy to LocalStack

.PHONY: build-api-jars
build-api-jars: ## Build API JARs
	./tasks build-api-jars

.PHONY: deploy-to-localstack
deploy-to-localstack: ## Deploy to LocalStack
	./tasks deploy-to-localstack

.PHONY: start-localstack
start-localstack: ## Start LocalStack
	./tasks start-localstack

.PHONY: start-ui
start-ui: ## Start the UI
	cd ui && npm start

.PHONY: help
help: ## Show help
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
