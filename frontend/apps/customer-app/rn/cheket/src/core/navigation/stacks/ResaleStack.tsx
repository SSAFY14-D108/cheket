import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {ResaleStackParamList} from '../types';
import {RESALE_ROUTES} from '../routes';
import {ResaleListScreen} from '../../../features/resale/screens/ResaleListScreen';
import {ResaleDetailScreen} from '../../../features/resale/screens/ResaleDetailScreen';
import {ResalePurchaseCompleteScreen} from '../../../features/resale/screens/ResalePurchaseCompleteScreen';

const Stack = createNativeStackNavigator<ResaleStackParamList>();

export function ResaleStack(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen
        name={RESALE_ROUTES.RESALE_LIST}
        component={ResaleListScreen}
      />
      <Stack.Screen
        name={RESALE_ROUTES.RESALE_DETAIL}
        component={ResaleDetailScreen}
      />
      <Stack.Screen
        name={RESALE_ROUTES.RESALE_PURCHASE_COMPLETE}
        component={ResalePurchaseCompleteScreen}
      />
    </Stack.Navigator>
  );
}
