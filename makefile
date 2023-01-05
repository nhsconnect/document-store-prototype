default: help

.PHONY: pre-push
pre-push: format-ui lint-ui test-ui test-app ## Format and lint UI files and run all unit tests. Todo: BE formatting & linting.

.PHONY: test-ui
test-ui: ## Run FE unit tests
	cd ui && npm run test:nw

.PHONY: format-ui
format-ui: ## Format .js[x] files within the UI package
	cd ui && npm run format

.PHONY: lint-ui
lint-ui: ## Lint .js[x] files within the UI package
	cd ui && npm run lint

.PHONY: test-app
test-app: ## Run BE unit tests (no logs)
	./gradlew test --rerun-tasks

.PHONY: test-app-with-logs
test-app-with-logs: ## Run BE unit tests (with logs)
	./gradlew test --rerun-tasks --info

.PHONY: build-and-deploy-to-local-stack
build-and-deploy-to-local-stack: ## Build and deploy to LocalStack
	./tasks build-api-jars && ./tasks deploy-to-localstack

.PHONY: help
help: ## Show help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
