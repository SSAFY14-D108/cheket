import React from 'react';
import {SafeAreaView, Text} from 'react-native';
import {HomeScreen} from '../../features/home/screens/HomeScreen';

export function RootNavigator(): React.JSX.Element {
  return (
    <SafeAreaView style={{flex: 1}}>
      <Text style={{padding: 16, fontWeight: '600'}}>RootNavigator Placeholder</Text>
      <HomeScreen />
    </SafeAreaView>
  );
}
