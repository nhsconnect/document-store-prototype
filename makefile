default: help

.PHONY: pre-push
pre-push: format-node-projects lint-node-projects test-ui test-app test-e2e ## Format & lint Node projects & run all unit & E2E tests. Todo: BE formatting, linting, & integration tests.

.PHONY: format-node-projects
format-node-projects: format-ui format-e2e-test ## Format all Prettier-compatible files for all Node projects

.PHONY: format-ui
format-ui: ## Format all Prettier-compatible files within /ui
	cd ui && npm run format

.PHONY: format-e2e-test
format-e2e-test: ## Format all Prettier-compatible files within /e2eTest
	cd e2eTest && npm run format

.PHONY: lint-node-projects
lint-node-projects: lint-ui lint-e2e-test ## Lint all all Node projects

.PHONY: lint-ui
lint-ui: ## Lint .js[x] files within /ui
	cd ui && npm run lint

.PHONY: lint-e2e-test
lint-e2e-test: ## Lint .js files within /e2eTest
	cd e2eTest && npm run lint

.PHONY: test-ui
test-ui: ## Run UI unit tests
	cd ui && npm run test:nw

.PHONY: test-app
test-app: ## Run BE unit tests (no logs)
	./gradlew test --rerun-tasks

.PHONY: test-app-with-logs
test-app-with-logs: ## Run BE unit tests (with logs)
	./gradlew test --rerun-tasks --info

.PHONY: test-e2e
test-e2e: ## Run E2E test (without visible browser)
	cd e2eTest && npm run test

.PHONY: test-e2e-open
test-e2e-open: ## Run E2E test (with visible browser)
	cd e2eTest && npm run test:open

.PHONY: install-node-projects
install-node-projects: install-ui install-e2e-test ## Install dependencies for all Node projects

.PHONY: install-ui
install-ui: ## Install UI dependencies
	cd ui && npm i

.PHONY: install-e2e-test
install-e2e-test: ## Install E2E test dependencies
	cd e2e-test && npm i

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
