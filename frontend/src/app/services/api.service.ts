import { Injectable } from '@angular/core';
import { Observable, map, catchError, throwError } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { GET_HELLO, GET_HEALTH } from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  constructor(private apollo: Apollo) { }

  /**
   * Safely extract data from GraphQL result with null checking
   */
  private extractData<T>(result: any, key: string): T {
    // If result or result.data is null/undefined, return the data anyway
    // Apollo's error handling will catch any actual errors
    if (!result || !result.data) {
      return null as any;
    }
    return result.data[key];
  }

  /**
   * Get a personalized hello message from the backend
   */
  getHello(name: string): Observable<string> {
    return this.apollo.query({
      query: GET_HELLO,
      variables: { name }
    }).pipe(
      map(result => this.extractData<string>(result, 'hello')),
      catchError(this.handleError)
    );
  }

  /**
   * Check the health status of the backend
   */
  getHealth(): Observable<any> {
    return this.apollo.query({
      query: GET_HEALTH
    }).pipe(
      map(result => this.extractData<any>(result, 'health')),
      catchError(this.handleError)
    );
  }

  /**
   * Handle GraphQL errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.graphQLErrors && error.graphQLErrors.length > 0) {
      errorMessage = error.graphQLErrors.map((e: any) => e.message).join(', ');
    } else if (error.networkError) {
      errorMessage = `Network error: ${error.networkError.message}`;
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('GraphQL Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
