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
    if (!result || !result.data) {
      throw new Error('GraphQL response is null or undefined');
    }
    const data = result.data[key];
    if (data === undefined || data === null) {
      throw new Error(`GraphQL response missing expected field: ${key}`);
    }
    return data;
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
