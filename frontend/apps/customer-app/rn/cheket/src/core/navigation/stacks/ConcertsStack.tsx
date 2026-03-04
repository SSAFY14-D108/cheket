import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {ConcertsStackParamList} from '../types';
import {CONCERTS_ROUTES} from '../routes';
import {createPlaceholder} from '../PlaceholderScreen';
import {ConcertsScreen} from '../../../features/concerts/screens/ConcertsScreen';

const Stack = createNativeStackNavigator<ConcertsStackParamList>();

const EventDetailScreen = createPlaceholder('ConcertsEventDetail');

export function ConcertsStack(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen
        name={CONCERTS_ROUTES.CONCERTS}
        component={ConcertsScreen}
      />
      <Stack.Screen
        name={CONCERTS_ROUTES.EVENT_DETAIL}
        component={EventDetailScreen}
      />
    </Stack.Navigator>
  );
}
