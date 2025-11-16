import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) { }

  /**
   * Get a personalized hello message from the backend
   * @param name - The name to include in the greeting
   * @returns Observable<string> - The greeting message
   */
  getHello(name: string): Observable<string> {
    const params = new HttpParams().set('name', name);

    return this.http.get(`${this.apiUrl}/hello`, {
      params,
      responseType: 'text'
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Check the health status of the backend
   * @returns Observable<string> - The health status message
   */
  getHealth(): Observable<string> {
    return this.http.get(`${this.apiUrl}/health`, {
      responseType: 'text'
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   * @param error - The error object
   * @returns Observable<never> - An observable that errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
