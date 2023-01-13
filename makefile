default: help

.PHONY: pre-push
pre-push: format-node-projects lint-node-projects test-ui test-app test-e2e ## Format & lint Node projects & run unit & E2E tests. Todo: BE formatting, linting, & integration tests.

.PHONY: format-node-projects
format-node-projects: format-ui format-e2e-test ## Format Prettier-compatible files for Node projects

.PHONY: format-ui
format-ui: ## Format /ui Prettier-compatible files
	cd ui && npm run format

.PHONY: format-e2e-test
format-e2e-test: ## Format /e2eTest Prettier-compatible files
	cd e2eTest && npm run format

.PHONY: lint-node-projects
lint-node-projects: lint-ui lint-e2e-test ## Lint Node projects

.PHONY: lint-ui
lint-ui: ## Lint /ui .js[x] files
	cd ui && npm run lint

.PHONY: lint-e2e-test
lint-e2e-test: ## Lint /e2eTest .js files
	cd e2eTest && npm run lint

.PHONY: test-ui
test-ui: ## Run /ui unit tests
	cd ui && npm run test:nw

.PHONY: test-app
test-app: ## Run /app unit tests (no logs)
	./gradlew test --rerun-tasks

.PHONY: test-app-with-logs
test-app-with-logs: ## Run /app unit tests (with logs)
	./gradlew test --rerun-tasks --info

.PHONY: test-e2e
test-e2e: ## Run E2E test (without visible browser)
	cd e2eTest && npm run test

.PHONY: test-e2e-open
test-e2e-open: ## Run E2E test (with visible browser)
	cd e2eTest && npm run test:open

.PHONY: install
install: install-ui install-e2e-test install-app ## Install dependencies

.PHONY: install-ui
install-ui: ## Install /ui dependencies
	cd ui && npm i

.PHONY: install-e2e-test
install-e2e-test: ## Install /e2eTest dependencies
	cd e2eTest && npm i

.PHONY: install-app
install-app: ## Install /app dependencies
	./gradlew build --refresh-dependencies

.PHONY: build-and-deploy-to-local-stack
build-and-deploy-to-local-stack: build-api-jars deploy-to-localstack ## Build & deploy to LocalStack

.PHONY: build-api-jars
build-api-jars: ## Build API JARs
	./tasks build-api-jars

.PHONY: deploy-to-localstack
deploy-to-localstack: ## Deploy to LocalStack
	./tasks deploy-to-localstack

.PHONY: help
help: ## Show help
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
