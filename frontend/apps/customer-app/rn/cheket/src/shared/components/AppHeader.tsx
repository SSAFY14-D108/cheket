import React, {ReactNode} from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
} from 'react-native';
import {colors} from '../../core/theme/colors';

interface AppHeaderProps {
  title?: string;
  showBack?: boolean;
  onBack?: () => void;
  rightElement?: ReactNode;
}

export function AppHeader({
  title,
  showBack = false,
  onBack,
  rightElement,
}: AppHeaderProps) {
  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <View style={styles.left}>
          {showBack && (
            <TouchableOpacity
              onPress={onBack}
              style={styles.backButton}
              activeOpacity={0.7}>
              <Text style={styles.backArrow}>{'<'}</Text>
            </TouchableOpacity>
          )}
        </View>
        <View style={styles.center}>
          {title && <Text style={styles.title}>{title}</Text>}
        </View>
        <View style={styles.right}>{rightElement}</View>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    backgroundColor: colors.card,
  },
  container: {
    height: 56,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    backgroundColor: colors.card,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  left: {
    width: 40,
    alignItems: 'flex-start',
  },
  center: {
    flex: 1,
    alignItems: 'center',
  },
  right: {
    width: 40,
    alignItems: 'flex-end',
  },
  backButton: {
    padding: 4,
  },
  backArrow: {
    fontSize: 22,
    fontWeight: '600',
    color: colors.foreground,
  },
  title: {
    fontSize: 17,
    fontWeight: '700',
    color: colors.foreground,
  },
});
