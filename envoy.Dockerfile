# Custom Envoy image with proto descriptor for JSON transcoding

FROM envoyproxy/envoy:v1.28-latest AS builder

# Install protoc
USER root
RUN apt-get update && apt-get install -y \
    protobuf-compiler \
    && rm -rf /var/lib/apt/lists/*

# Copy proto files
COPY backend/src/main/proto /tmp/proto

# Generate proto descriptor
WORKDIR /tmp
RUN protoc \
    --proto_path=/tmp/proto \
    --include_imports \
    --include_source_info \
    --descriptor_set_out=/tmp/proto_descriptor.pb \
    /tmp/proto/*.proto

# Final stage
FROM envoyproxy/envoy:v1.28-latest

# Copy proto descriptor
COPY --from=builder /tmp/proto_descriptor.pb /etc/envoy/proto_descriptor.pb

# Copy envoy configuration
COPY envoy.yaml /etc/envoy/envoy.yaml

# Expose ports
EXPOSE 8080 9901

# Start envoy
CMD ["/usr/local/bin/envoy", "-c", "/etc/envoy/envoy.yaml"]
