import React from 'react';
import {SafeAreaView} from 'react-native';
import {HomeBanner} from '../components/HomeBanner';

export function HomeScreen(): React.JSX.Element {
  return (
    <SafeAreaView style={{padding: 16}}>
      <HomeBanner />
    </SafeAreaView>
  );
}
