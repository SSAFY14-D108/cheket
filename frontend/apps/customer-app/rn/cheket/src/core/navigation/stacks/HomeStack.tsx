import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {HomeStackParamList} from '../types';
import {HOME_ROUTES} from '../routes';
import {HomeScreen} from '../../../features/home/screens/HomeScreen';
import {EventDetailScreen} from '../../../features/event/screens/EventDetailScreen';
import {WaitingQueueScreen} from '../../../features/event/screens/WaitingQueueScreen';
import {SeatSelectionScreen} from '../../../features/event/screens/SeatSelectionScreen';
import {PaymentScreen} from '../../../features/payment/screens/PaymentScreen';
import {PurchaseFailedScreen} from '../../../features/payment/screens/PurchaseFailedScreen';
import {WishlistScreen} from '../../../features/profile/screens/WishlistScreen';

const Stack = createNativeStackNavigator<HomeStackParamList>();

export function HomeStack(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen name={HOME_ROUTES.HOME} component={HomeScreen} />
      <Stack.Screen
        name={HOME_ROUTES.EVENT_DETAIL}
        component={EventDetailScreen}
      />
      <Stack.Screen
        name={HOME_ROUTES.WAITING_QUEUE}
        component={WaitingQueueScreen}
      />
      <Stack.Screen
        name={HOME_ROUTES.SEAT_SELECTION}
        component={SeatSelectionScreen}
      />
      <Stack.Screen name={HOME_ROUTES.PAYMENT} component={PaymentScreen} />
      <Stack.Screen
        name={HOME_ROUTES.PURCHASE_FAILED}
        component={PurchaseFailedScreen}
      />
      <Stack.Screen name={HOME_ROUTES.WISHLIST} component={WishlistScreen} />
    </Stack.Navigator>
  );
}
