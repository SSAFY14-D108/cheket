import React, {useEffect} from 'react';
import {StatusBar} from 'react-native';
import {SafeAreaProvider, SafeAreaView} from 'react-native-safe-area-context';
import {AppProviders} from './src/app/providers/AppProviders';
import {initApp} from './src/app/bootstrap/initApp';
import {RootNavigator} from './src/core/navigation/RootNavigator';
import {colors} from './src/core/theme/colors';

function App(): React.JSX.Element {
  useEffect(() => {
    initApp();
  }, []);

  return (
    <SafeAreaProvider>
      <AppProviders>
        <StatusBar
          barStyle="dark-content"
          backgroundColor={colors.background}
        />
        <SafeAreaView
          style={{flex: 1, backgroundColor: colors.background}}
          edges={['top']}>
          <RootNavigator />
        </SafeAreaView>
      </AppProviders>
    </SafeAreaProvider>
  );
}

export default App;
