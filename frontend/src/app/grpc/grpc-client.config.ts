/**
 * gRPC Client Configuration
 *
 * Configuration for connecting to gRPC services via Envoy proxy
 */

export const GRPC_CONFIG = {
  // Envoy proxy endpoint for gRPC-Web
  GRPC_WEB_URL: '/grpc',  // Proxied through nginx to http://envoy:8080

  // Service paths
  AUTH_SERVICE: 'com.example.demo.grpc.AuthService',
  BANKING_SERVICE: 'com.example.demo.grpc.BankingService',
  NOTIFICATION_SERVICE: 'com.example.demo.grpc.NotificationService',
  CATEGORY_SERVICE: 'com.example.demo.grpc.CategoryService',
  ADMIN_SERVICE: 'com.example.demo.grpc.AdminService',

  // Request timeout in milliseconds
  TIMEOUT: 30000,

  // Metadata/headers
  CONTENT_TYPE: 'application/grpc-web+proto',
};
