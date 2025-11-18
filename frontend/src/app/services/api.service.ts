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
   * Get a personalized hello message from the backend
   */
  getHello(name: string): Observable<string> {
    return this.apollo.query({
      query: GET_HELLO,
      variables: { name }
    }).pipe(
      map(result => (result.data as any).hello),
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
      map(result => (result.data as any).health),
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
