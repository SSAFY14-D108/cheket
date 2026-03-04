import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {RootStackParamList} from './types';
import {ROOT_ROUTES} from './routes';
import {useAuthStore} from '../../store/authStore';
import {AuthNavigator} from './AuthNavigator';
import {MainTabNavigator} from './MainTabNavigator';
import {MyPageScreen} from '../../features/profile/screens/MyPageScreen';
import {WalletScreen} from '../../features/profile/screens/WalletScreen';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator(): React.JSX.Element {
  const isLoggedIn = useAuthStore(state => state.isLoggedIn);

  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      {isLoggedIn ? (
        <Stack.Group>
          <Stack.Screen
            name={ROOT_ROUTES.MAIN_TABS}
            component={MainTabNavigator}
          />
          <Stack.Screen
            name={ROOT_ROUTES.MY_PAGE}
            component={MyPageScreen}
            options={{presentation: 'modal'}}
          />
          <Stack.Screen
            name={ROOT_ROUTES.WALLET}
            component={WalletScreen}
            options={{presentation: 'modal'}}
          />
        </Stack.Group>
      ) : (
        <Stack.Screen name={ROOT_ROUTES.AUTH} component={AuthNavigator} />
      )}
    </Stack.Navigator>
  );
}
