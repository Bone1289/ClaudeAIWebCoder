#!/bin/bash

# Script to generate TypeScript code from proto files for gRPC-Web

PROTO_DIR="./proto"
OUT_DIR="./src/app/generated"

# Create output directory if it doesn't exist
mkdir -p $OUT_DIR

# Get the path to protoc-gen-grpc-web
PROTOC_GEN_GRPC_WEB_PATH="./node_modules/.bin/protoc-gen-grpc-web"

# Generate TypeScript code
protoc \
  --plugin=protoc-gen-ts=./node_modules/.bin/protoc-gen-ts \
  --plugin=protoc-gen-grpc-web=$PROTOC_GEN_GRPC_WEB_PATH \
  --js_out=import_style=commonjs:$OUT_DIR \
  --grpc-web_out=import_style=typescript,mode=grpcwebtext:$OUT_DIR \
  --ts_out=service=grpc-web:$OUT_DIR \
  --proto_path=$PROTO_DIR \
  $PROTO_DIR/*.proto

echo "✅ Proto files generated successfully in $OUT_DIR"
