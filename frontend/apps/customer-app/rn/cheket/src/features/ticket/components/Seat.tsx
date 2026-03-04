import React from 'react';
import {Pressable, Text} from 'react-native';

type Props = {
  label: string;
  selected?: boolean;
  onPress?: () => void;
};

export function Seat({label, selected = false, onPress}: Props): React.JSX.Element {
  return (
    <Pressable
      onPress={onPress}
      style={{
        width: 52,
        height: 52,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: selected ? '#0EA5E9' : '#E5E7EB',
        borderRadius: 8,
      }}>
      <Text>{label}</Text>
    </Pressable>
  );
}
