import React, {PropsWithChildren} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {QueryProvider} from './QueryProvider';

export function AppProviders({children}: PropsWithChildren): React.JSX.Element {
  return (
    <QueryProvider>
      <NavigationContainer>{children}</NavigationContainer>
    </QueryProvider>
  );
}
