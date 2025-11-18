#!/bin/bash

# Generate proto descriptor for Envoy gRPC-JSON transcoding

PROTO_DIR="../backend/src/main/proto"
OUT_FILE="proto_descriptor.pb"

echo "Generating proto descriptor for Envoy..."

protoc \
  --proto_path=$PROTO_DIR \
  --include_imports \
  --include_source_info \
  --descriptor_set_out=$OUT_FILE \
  $PROTO_DIR/*.proto

if [ $? -eq 0 ]; then
  echo "✅ Proto descriptor generated: $OUT_FILE"
else
  echo "❌ Failed to generate proto descriptor"
  exit 1
fi
