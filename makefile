pre-push: test-ui test-app

test-ui:
	cd ui && npm run test:nw

test-app:
	./gradlew test
