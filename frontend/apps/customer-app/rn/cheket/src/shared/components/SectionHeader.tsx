import React from 'react';
import {View, Text, TouchableOpacity, StyleSheet} from 'react-native';
import {colors} from '../../core/theme/colors';

interface SectionHeaderProps {
  title: string;
  onMore?: () => void;
}

export function SectionHeader({title, onMore}: SectionHeaderProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      {onMore && (
        <TouchableOpacity onPress={onMore} activeOpacity={0.7}>
          <Text style={styles.more}>{'더보기 >'}</Text>
        </TouchableOpacity>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.foreground,
  },
  more: {
    fontSize: 13,
    color: colors.mutedForeground,
  },
});
