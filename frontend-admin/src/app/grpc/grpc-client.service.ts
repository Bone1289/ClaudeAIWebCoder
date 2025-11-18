import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GRPC_CONFIG } from './grpc-client.config';

/**
 * Generic gRPC-Web client service
 * Handles communication with gRPC backend via JSON over HTTP
 */
@Injectable({
  providedIn: 'root'
})
export class GrpcClientService {
  private readonly GRPC_WEB_URL = GRPC_CONFIG.GRPC_WEB_URL;
  private readonly TOKEN_KEY = GRPC_CONFIG.TOKEN_KEY;

  constructor(private http: HttpClient) { }

  /**
   * Make a gRPC call via JSON over HTTP
   * @param serviceName - Full service name (e.g., 'com.example.demo.grpc.AuthService')
   * @param methodName - Method name (e.g., 'Login')
   * @param request - Request object
   * @param requiresAuth - Whether the request requires authentication
   * @returns Observable with the response
   */
  call<TRequest, TResponse>(
    serviceName: string,
    methodName: string,
    request: TRequest,
    requiresAuth: boolean = true
  ): Observable<TResponse> {
    const url = `${this.GRPC_WEB_URL}/${serviceName}/${methodName}`;
    const headers = this.buildHeaders(requiresAuth);

    return this.http.post<TResponse>(url, request, { headers });
  }

  /**
   * Build HTTP headers for gRPC-Web request
   */
  private buildHeaders(requiresAuth: boolean): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    if (requiresAuth) {
      const token = this.getToken();
      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }
    }

    return headers;
  }

  /**
   * Get authentication token from storage
   */
  private getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
