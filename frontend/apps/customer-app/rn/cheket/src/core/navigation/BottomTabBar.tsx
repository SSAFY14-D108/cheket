import React from 'react';
import {View, Text, Pressable, StyleSheet} from 'react-native';
import type {BottomTabBarProps} from '@react-navigation/bottom-tabs';
import {TAB_ROUTES} from './routes';

const TAB_CONFIG: Record<string, {label: string; icon: string}> = {
  [TAB_ROUTES.HOME_TAB]: {label: '메인', icon: '🏠'},
  [TAB_ROUTES.CONCERTS_TAB]: {label: '공연', icon: '🎵'},
  [TAB_ROUTES.RESALE_TAB]: {label: '티켓 줍줍', icon: '🔄'},
  [TAB_ROUTES.MY_TICKETS_TAB]: {label: '내 티켓', icon: '🎫'},
  [TAB_ROUTES.COLLECTION_TAB]: {label: '컬렉션', icon: '⭐'},
};

export function BottomTabBar({
  state,
  descriptors,
  navigation,
}: BottomTabBarProps): React.JSX.Element {
  return (
    <View style={styles.container}>
      {state.routes.map((route, index) => {
        const {options} = descriptors[route.key];
        const config = TAB_CONFIG[route.name] ?? {
          label: options.title ?? route.name,
          icon: '?',
        };
        const isFocused = state.index === index;

        const onPress = () => {
          const event = navigation.emit({
            type: 'tabPress',
            target: route.key,
            canPreventDefault: true,
          });
          if (!isFocused && !event.defaultPrevented) {
            navigation.navigate(route.name, route.params);
          }
        };

        const onLongPress = () => {
          navigation.emit({type: 'tabLongPress', target: route.key});
        };

        return (
          <Pressable
            key={route.key}
            accessibilityRole="button"
            accessibilityState={isFocused ? {selected: true} : {}}
            accessibilityLabel={options.tabBarAccessibilityLabel}
            onPress={onPress}
            onLongPress={onLongPress}
            style={styles.tab}>
            <Text style={styles.icon}>{config.icon}</Text>
            <Text style={[styles.label, isFocused && styles.labelFocused]}>
              {config.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#E5E5E5',
    paddingBottom: 20,
    paddingTop: 8,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 4,
  },
  icon: {
    fontSize: 20,
    marginBottom: 2,
  },
  label: {
    fontSize: 11,
    color: '#999999',
    fontWeight: '500',
  },
  labelFocused: {
    color: '#0D1F1A',
    fontWeight: '700',
  },
});
