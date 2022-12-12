.PHONY: pre-push
pre-push: test-ui test-app

.PHONY: test-ui
test-ui:
	cd ui && npm run test:nw

.PHONY: test-app
test-app:
	./gradlew test
