default: help

.PHONY: pre-push
pre-push: test-ui test-app ## Run all local tests, format checking & linting, etc. Run this before pushing.

.PHONY: test-ui
test-ui: ## Run front-end unit tests
	cd ui && npm run test:nw

.PHONY: test-app
test-app: ## Run back-end unit tests
	./gradlew test

.PHONY: help
help: ## Show this help
	@ grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'
