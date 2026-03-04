import React from 'react';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import type {MainTabParamList} from './types';
import {TAB_ROUTES} from './routes';
import {BottomTabBar} from './BottomTabBar';
import {HomeStack} from './stacks/HomeStack';
import {ConcertsStack} from './stacks/ConcertsStack';
import {ResaleStack} from './stacks/ResaleStack';
import {MyTicketsStack} from './stacks/MyTicketsStack';
import {CollectionStack} from './stacks/CollectionStack';

const Tab = createBottomTabNavigator<MainTabParamList>();

export function MainTabNavigator(): React.JSX.Element {
  return (
    <Tab.Navigator
      tabBar={props => <BottomTabBar {...props} />}
      screenOptions={{headerShown: false}}>
      <Tab.Screen name={TAB_ROUTES.HOME_TAB} component={HomeStack} />
      <Tab.Screen name={TAB_ROUTES.CONCERTS_TAB} component={ConcertsStack} />
      <Tab.Screen name={TAB_ROUTES.RESALE_TAB} component={ResaleStack} />
      <Tab.Screen
        name={TAB_ROUTES.MY_TICKETS_TAB}
        component={MyTicketsStack}
      />
      <Tab.Screen
        name={TAB_ROUTES.COLLECTION_TAB}
        component={CollectionStack}
      />
    </Tab.Navigator>
  );
}
