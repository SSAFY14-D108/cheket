import React from 'react';
import {TextInput} from 'react-native';

type Props = {
  value: string;
  onChangeText: (text: string) => void;
  placeholder: string;
};

export function AuthTextField({value, onChangeText, placeholder}: Props): React.JSX.Element {
  return (
    <TextInput
      value={value}
      onChangeText={onChangeText}
      placeholder={placeholder}
      style={{borderWidth: 1, borderColor: '#D1D5DB', borderRadius: 8, padding: 12, marginBottom: 12}}
    />
  );
}
