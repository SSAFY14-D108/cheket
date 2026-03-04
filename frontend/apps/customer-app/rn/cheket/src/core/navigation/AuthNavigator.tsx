import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {AuthStackParamList} from './types';
import {AUTH_ROUTES} from './routes';
import {LoginScreen} from '../../features/auth/screens/LoginScreen';
import {SignUpScreen} from '../../features/auth/screens/SignUpScreen';

const Stack = createNativeStackNavigator<AuthStackParamList>();

export function AuthNavigator(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen name={AUTH_ROUTES.LOGIN} component={LoginScreen} />
      <Stack.Screen name={AUTH_ROUTES.SIGN_UP} component={SignUpScreen} />
    </Stack.Navigator>
  );
}
