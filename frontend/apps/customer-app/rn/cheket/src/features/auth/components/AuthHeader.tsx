import React from 'react';
import {Text, View} from 'react-native';

type Props = {
  title: string;
};

export function AuthHeader({title}: Props): React.JSX.Element {
  return (
    <View style={{padding: 16}}>
      <Text style={{fontSize: 24, fontWeight: '700'}}>{title}</Text>
    </View>
  );
}
