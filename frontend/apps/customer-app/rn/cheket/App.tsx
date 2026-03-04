import React, {useEffect} from 'react';
import {AppProviders} from './src/app/providers/AppProviders';
import {initApp} from './src/app/bootstrap/initApp';
import {RootNavigator} from './src/core/navigation/RootNavigator';

function App(): React.JSX.Element {
  useEffect(() => {
    initApp();
  }, []);

  return (
    <AppProviders>
      <RootNavigator />
    </AppProviders>
  );
}

export default App;
