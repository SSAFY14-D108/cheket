import React from 'react';
import {Text, View} from 'react-native';

export function HomeBanner(): React.JSX.Element {
  return (
    <View style={{padding: 16, backgroundColor: '#E0F2FE', borderRadius: 12}}>
      <Text style={{fontSize: 18, fontWeight: '600'}}>Welcome to Cheket</Text>
    </View>
  );
}
