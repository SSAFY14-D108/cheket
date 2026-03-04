import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import {colors} from '../../core/theme/colors';

interface EmptyStateProps {
  title: string;
  description?: string;
}

export function EmptyState({title, description}: EmptyStateProps) {
  return (
    <View style={styles.container}>
      <View style={styles.iconPlaceholder}>
        <Text style={styles.iconText}>-</Text>
      </View>
      <Text style={styles.title}>{title}</Text>
      {description && <Text style={styles.description}>{description}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 48,
    paddingHorizontal: 24,
  },
  iconPlaceholder: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: colors.muted,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  iconText: {
    fontSize: 28,
    color: colors.mutedForeground,
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.foreground,
    textAlign: 'center',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    color: colors.mutedForeground,
    textAlign: 'center',
    lineHeight: 20,
  },
});
