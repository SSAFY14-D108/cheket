import React, {PropsWithChildren} from 'react';
import {QueryProvider} from './QueryProvider';

export function AppProviders({children}: PropsWithChildren): React.JSX.Element {
  return <QueryProvider>{children}</QueryProvider>;
}
