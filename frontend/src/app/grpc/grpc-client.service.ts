import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

/**
 * gRPC Client Service
 *
 * Simple HTTP-based gRPC client that communicates with gRPC services
 * via Envoy proxy. Envoy handles the HTTP/JSON to gRPC translation.
 */
@Injectable({
  providedIn: 'root'
})
export class GrpcClientService {
  // Use /grpc path which nginx proxies to Envoy
  private readonly GRPC_WEB_URL = '/grpc';
  private readonly TOKEN_KEY = 'auth_token';

  constructor(private http: HttpClient) {}

  /**
   * Make a gRPC unary call
   */
  call<TRequest, TResponse>(
    serviceName: string,
    methodName: string,
    request: TRequest,
    requiresAuth: boolean = true
  ): Observable<TResponse> {
    const url = `${this.GRPC_WEB_URL}/${serviceName}/${methodName}`;
    const headers = this.buildHeaders(requiresAuth);

    return this.http.post<TResponse>(url, request, { headers }).pipe(
      catchError(error => {
        console.error('gRPC call failed:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Build HTTP headers for gRPC-Web request
   */
  private buildHeaders(requiresAuth: boolean): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    if (requiresAuth) {
      const token = localStorage.getItem(this.TOKEN_KEY);
      if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
      }
    }

    return headers;
  }
}
