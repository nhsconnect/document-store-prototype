default: help

.PHONY: pre-push
pre-push: test-ui test-app ## Run all unit tests. Run this before pushing. Todo: Formatting & linting.

.PHONY: test-ui
test-ui: ## Run FE unit tests (with logs)
	cd ui && npm run test:nw

.PHONY: test-app
test-app: ## Run BE unit tests (no logs)
	./gradlew test --rerun-tasks

.PHONY: test-app-with-logs
test-app-with-logs: ## Run BE unit tests (with logs)
	./gradlew test --rerun-tasks --info

.PHONY: help
help: ## Show help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
