import React from 'react';
import {View, Text, StyleSheet} from 'react-native';

export const createPlaceholder = (name: string) => () => (
  <View style={styles.container}>
    <Text style={styles.text}>{name}</Text>
  </View>
);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FAFAFA',
  },
  text: {fontSize: 18, fontWeight: '600', color: '#0D1F1A'},
});
