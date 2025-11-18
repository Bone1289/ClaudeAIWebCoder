import { Injectable } from '@angular/core';
import { GrpcWebClientBase, MethodDescriptor, RpcError } from 'grpc-web';
import { Observable, from } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { GRPC_CONFIG } from './grpc-client.config';

/**
 * Base gRPC-Web client service
 * Handles communication with gRPC services via Envoy proxy
 */
@Injectable({
  providedIn: 'root'
})
export class GrpcWebClientService {
  private client: GrpcWebClientBase;
  private readonly TOKEN_KEY = 'auth_token';

  constructor() {
    this.client = new GrpcWebClientBase({
      format: 'text'
    });
  }

  /**
   * Make a unary gRPC call
   */
  unaryCall<TRequest, TResponse>(
    serviceName: string,
    methodName: string,
    request: TRequest,
    requiresAuth: boolean = true
  ): Observable<TResponse> {
    const methodDescriptor = new MethodDescriptor(
      `/${serviceName}/${methodName}`,
      'unary',
      () => request,
      (response: any) => response as TResponse,
      (request: TRequest) => this.serializeRequest(request),
      (bytes: Uint8Array) => this.deserializeResponse<TResponse>(bytes)
    );

    const metadata = this.buildMetadata(requiresAuth);

    return new Observable<TResponse>((observer) => {
      this.client.rpcCall(
        GRPC_CONFIG.GRPC_WEB_URL,
        request,
        metadata,
        methodDescriptor,
        (err: RpcError | null, response?: TResponse) => {
          if (err) {
            observer.error(this.mapGrpcError(err));
          } else if (response) {
            observer.next(response);
            observer.complete();
          }
        }
      );
    });
  }

  /**
   * Make a server streaming gRPC call
   */
  serverStreamingCall<TRequest, TResponse>(
    serviceName: string,
    methodName: string,
    request: TRequest,
    requiresAuth: boolean = true
  ): Observable<TResponse> {
    const methodDescriptor = new MethodDescriptor(
      `/${serviceName}/${methodName}`,
      'server_streaming',
      () => request,
      (response: any) => response as TResponse,
      (request: TRequest) => this.serializeRequest(request),
      (bytes: Uint8Array) => this.deserializeResponse<TResponse>(bytes)
    );

    const metadata = this.buildMetadata(requiresAuth);

    return new Observable<TResponse>((observer) => {
      const stream = this.client.serverStreaming(
        GRPC_CONFIG.GRPC_WEB_URL,
        request,
        metadata,
        methodDescriptor
      );

      stream.on('data', (response: TResponse) => {
        observer.next(response);
      });

      stream.on('error', (err: RpcError) => {
        observer.error(this.mapGrpcError(err));
      });

      stream.on('end', () => {
        observer.complete();
      });
    });
  }

  /**
   * Build metadata/headers for gRPC request
   */
  private buildMetadata(requiresAuth: boolean): { [key: string]: string } {
    const metadata: { [key: string]: string } = {
      'content-type': 'application/grpc-web+proto',
    };

    if (requiresAuth) {
      const token = localStorage.getItem(this.TOKEN_KEY);
      if (token) {
        metadata['authorization'] = `Bearer ${token}`;
      }
    }

    return metadata;
  }

  /**
   * Serialize request to bytes
   */
  private serializeRequest<T>(request: T): Uint8Array {
    const json = JSON.stringify(request);
    return new TextEncoder().encode(json);
  }

  /**
   * Deserialize response from bytes
   */
  private deserializeResponse<T>(bytes: Uint8Array): T {
    const json = new TextDecoder().decode(bytes);
    return JSON.parse(json) as T;
  }

  /**
   * Map gRPC error to application error
   */
  private mapGrpcError(err: RpcError): Error {
    const error = new Error(err.message || 'gRPC call failed');
    (error as any).code = err.code;
    (error as any).metadata = err.metadata;
    return error;
  }
}
