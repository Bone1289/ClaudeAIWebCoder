export const GRPC_CONFIG = {
  // Backend gRPC endpoint (via nginx proxy to Envoy)
  GRPC_WEB_URL: '/grpc',

  // Service names
  AUTH_SERVICE: 'com.example.demo.grpc.AuthService',
  ADMIN_SERVICE: 'com.example.demo.grpc.AdminService',

  // Storage keys
  TOKEN_KEY: 'admin_auth_token',
  USER_KEY: 'admin_current_user'
};
