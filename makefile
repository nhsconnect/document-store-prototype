default: help

.PHONY: pre-push
pre-push: format lint test-ui test-app test-e2e ## Format, lint, & test

.PHONY: format
format: format-ui format-app format-lambdas format-auth format-e2e-test ## Format files

.PHONY: format-ui
format-ui: ## Format /ui files
	cd ui && npm run format

.PHONY: format-app
format-app: ## Format /app files
	./gradlew :app:spotlessApply

.PHONY: format-lambdas
format-lambdas: format-CreateDocumentManifestByNhsNumber format-CreateDocumentReference format-DeleteDocumentReference \
format-DocumentReferenceSearch format-FakeVirusScannedEvent format-ReRegistrationEvent format-SearchPatientDetails \
format-VirusScannedEvent ## Format /lambdas files

.PHONY: format-CreateDocumentManifestByNhsNumber
format-CreateDocumentManifestByNhsNumber: ## Format CreateDocumentManifestByNhsNumber
	./gradlew :lambdas:CreateDocumentManifestByNhsNumber:spotlessApply

.PHONY: format-CreateDocumentReference
format-CreateDocumentReference: ## Format CreateDocumentReference
	./gradlew :lambdas:CreateDocumentReference:spotlessApply

.PHONY: format-DeleteDocumentReference
format-DeleteDocumentReference: ## Format DeleteDocumentReference
	./gradlew :lambdas:DeleteDocumentReference:spotlessApply

.PHONY: format-DocumentReferenceSearch
format-DocumentReferenceSearch: ## Format DocumentReferenceSearch
	./gradlew :lambdas:DocumentReferenceSearch:spotlessApply

.PHONY: format-FakeVirusScannedEvent
format-FakeVirusScannedEvent: ## Format FakeVirusScannedEvent
	./gradlew :lambdas:FakeVirusScannedEvent:spotlessApply

.PHONY: format-ReRegistrationEvent
format-ReRegistrationEvent: ## Format ReRegistrationEvent
	./gradlew :lambdas:ReRegistrationEvent:spotlessApply

.PHONY: format-SearchPatientDetails
format-SearchPatientDetails: ## Format SearchPatientDetails
	./gradlew :lambdas:SearchPatientDetails:spotlessApply

.PHONY: format-VirusScannedEvent
format-VirusScannedEvent: ## Format VirusScannedEvent
	./gradlew :lambdas:VirusScannedEvent:spotlessApply

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
test-app-unit-with-logs: ## Run /app unit tests (with logs)
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
	bash ./scripts/tasks.sh _build-api-jars

.PHONY: deploy-to-localstack
deploy-to-localstack: ## Deploy to LocalStack
	bash ./scripts/tasks.sh _deploy-to-localstack

.PHONY: start-ui
start-ui: ## Start the UI
	cd ui && npm start

.PHONY: start-localstack
start-localstack: ## Start LocalStack
	bash ./scripts/tasks.sh start-localstack

.PHONY: view-localstack-logs
view-localstack-logs: ## View LocalStack logs
	bash ./scripts/tasks.sh view-localstack-logs

.PHONY: help
help: ## Show help
	@grep -E '^[0-9a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1,$$2}'

.PHONY: plan-and-deploy-workspace
plan-and-deploy-workspace:
	bash ./scripts/sandbox.sh deploy_workspace_amplify_app

# Builds the app and plans Terraform for Sandbox A
.PHONY: plan-app-sanda
plan-app-sanda:
	bash ./scripts/sandbox.sh plan-app-sanda
# Builds the app and plans Terraform for Sandbox B
.PHONY: plan-app-sandb
plan-app-sandb:
	bash ./scripts/sandbox.sh plan-app-sandb

# Deploy Terraform for Sandbox A
.PHONY: deploy-app-sanda
deploy-app-sanda:
	bash ./scripts/sandbox.sh deploy-app-sanda
# Deploy Terraform for Sandbox B
.PHONY: deploy-app-sandb
deploy-app-sandb:
	bash ./scripts/sandbox.sh deploy-app-sandb

# Deploy UI for Sandbox A
.PHONY: deploy-ui-sanda
deploy-ui-sanda:
	bash ./scripts/sandbox.sh deploy-ui-sanda
# Deploy UI for Sandbox B
.PHONY: deploy-ui-sandb
deploy-ui-sandb:
	bash ./scripts/sandbox.sh deploy-ui-sandb

# Destroy terraform Sandbox A
.PHONY: destroy-sanda
destroy-sanda:
	bash ./scripts/sandbox.sh destroy-sanda
# Destroy terraform Sandbox B
.PHONY: destroy-sandb
destroy-sandb:
	bash ./scripts/sandbox.sh destroy-sandb