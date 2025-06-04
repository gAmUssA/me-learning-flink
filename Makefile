.PHONY: start stop wait print-url start-and-wait

# Variables
FLINK_URL := http://localhost:8081
MAX_RETRIES := 30
SLEEP_INTERVAL := 2
FLINK_VERSION := 1.19.1
SCALA_VERSION := 2.12

# Start Flink cluster and wait for it to be ready
start-and-wait: start wait print-url

# Start Flink cluster
start:
	@echo "Starting Flink cluster..."
	docker-compose up -d
	@echo "Flink cluster is starting. Use 'make wait' to wait for it to be ready."

# Stop Flink cluster
stop:
	@echo "Stopping Flink cluster..."
	docker-compose down
	@echo "Flink cluster stopped."

# Wait for Flink JobManager to be available
wait:
	@echo "Waiting for Flink JobManager to be available..."
	@for i in $$(seq 1 $(MAX_RETRIES)); do \
		if curl -s -f -o /dev/null $(FLINK_URL); then \
			echo "Flink JobManager is now available."; \
			exit 0; \
		fi; \
		echo "Attempt $$i: JobManager not ready. Retrying in $(SLEEP_INTERVAL) seconds..."; \
		sleep $(SLEEP_INTERVAL); \
	done; \
	echo "Flink JobManager did not become available in time."; \
	exit 1

# Print URL to Flink console
print-url:
	@echo "Flink console URL: $(FLINK_URL)"

# Download and extract Flink
download-flink:
	@echo "Downloading Flink $(FLINK_VERSION)..."
	@curl -L https://archive.apache.org/dist/flink/flink-$(FLINK_VERSION)/flink-$(FLINK_VERSION)-bin-scala_$(SCALA_VERSION).tgz --output flink-$(FLINK_VERSION).tgz
	@echo "Extracting Flink..."
	@tar -xzf flink-$(FLINK_VERSION).tgz
	@rm flink-$(FLINK_VERSION).tgz
	@echo "Flink $(FLINK_VERSION) has been downloaded and extracted."


