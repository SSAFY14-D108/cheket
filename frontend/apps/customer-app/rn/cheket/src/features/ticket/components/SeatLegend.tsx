import React from 'react';
import {Text, View} from 'react-native';

export function SeatLegend(): React.JSX.Element {
  return (
    <View style={{padding: 16}}>
      <Text>Available: Gray</Text>
      <Text>Selected: Blue</Text>
    </View>
  );
}
