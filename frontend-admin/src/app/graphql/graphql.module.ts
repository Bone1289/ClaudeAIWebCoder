import { NgModule } from '@angular/core';
import { HttpLink } from 'apollo-angular/http';
import { InMemoryCache, ApolloClient, ApolloLink } from '@apollo/client/core';
import { setContext } from '@apollo/client/link/context';
import { APOLLO_OPTIONS, ApolloModule } from 'apollo-angular';

const uri = 'http://localhost:8080/graphql';

export function createApollo(httpLink: HttpLink) {
  const http = httpLink.create({ uri });

  const auth = setContext((operation, context) => {
    const token = localStorage.getItem('admin_auth_token');

    if (token === null) {
      return {};
    }

    return {
      headers: {
        Authorization: `Bearer ${token}`
      }
    };
  });

  return {
    link: ApolloLink.from([auth, http]),
    cache: new InMemoryCache(),
    defaultOptions: {
      watchQuery: {
        fetchPolicy: 'network-only',
        errorPolicy: 'all',
      },
      query: {
        fetchPolicy: 'network-only',
        errorPolicy: 'all',
      },
      mutate: {
        errorPolicy: 'all',
      },
    },
  };
}

@NgModule({
  exports: [ApolloModule],
  providers: [
    {
      provide: APOLLO_OPTIONS,
      useFactory: createApollo,
      deps: [HttpLink],
    },
  ],
})
export class GraphQLModule {}
