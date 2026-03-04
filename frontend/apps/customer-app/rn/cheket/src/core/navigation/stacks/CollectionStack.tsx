import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import type {CollectionStackParamList} from '../types';
import {COLLECTION_ROUTES} from '../routes';
import {CollectionScreen} from '../../../features/collection/screens/CollectionScreen';
import {ArchiveScreen} from '../../../features/collection/screens/ArchiveScreen';
import {CollectibleTicketDetailScreen} from '../../../features/collection/screens/CollectibleTicketDetailScreen';

const Stack = createNativeStackNavigator<CollectionStackParamList>();

export function CollectionStack(): React.JSX.Element {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen
        name={COLLECTION_ROUTES.COLLECTION}
        component={CollectionScreen}
      />
      <Stack.Screen
        name={COLLECTION_ROUTES.ARCHIVE}
        component={ArchiveScreen}
      />
      <Stack.Screen
        name={COLLECTION_ROUTES.COLLECTIBLE_TICKET_DETAIL}
        component={CollectibleTicketDetailScreen}
      />
    </Stack.Navigator>
  );
}
