import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../types';
import {MY_TICKETS_ROUTES} from '../routes';
import {MyTicketsScreen} from '../../../features/mytickets/screens/MyTicketsScreen';
import {TicketDetailScreen} from '../../../features/mytickets/screens/TicketDetailScreen';
import {QrCheckinScreen} from '../../../features/mytickets/screens/QrCheckinScreen';
import {TransferScreen} from '../../../features/transfer/screens/TransferScreen';
import {TransferCompleteScreen} from '../../../features/transfer/screens/TransferCompleteScreen';
import {TransferFailedScreen} from '../../../features/transfer/screens/TransferFailedScreen';
import {ResaleCreateScreen} from '../../../features/resale/screens/ResaleCreateScreen';

const Stack = createNativeStackNavigator<MyTicketsStackParamList>();

export function MyTicketsStack(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen
        name={MY_TICKETS_ROUTES.MY_TICKETS}
        component={MyTicketsScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.TICKET_DETAIL}
        component={TicketDetailScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.QR_CHECKIN}
        component={QrCheckinScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.TRANSFER}
        component={TransferScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.TRANSFER_COMPLETE}
        component={TransferCompleteScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.TRANSFER_FAILED}
        component={TransferFailedScreen}
      />
      <Stack.Screen
        name={MY_TICKETS_ROUTES.RESALE_CREATE}
        component={ResaleCreateScreen}
      />
    </Stack.Navigator>
  );
}
