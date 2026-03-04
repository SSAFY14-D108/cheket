import React from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  Image,
  StyleSheet,
} from 'react-native';
import {Event} from '../../core/types/common';
import {colors} from '../../core/theme/colors';
import {EventStatusBadge} from './StatusBadge';

interface EventCardProps {
  event: Event;
  onPress: () => void;
}

export function EventCard({event, onPress}: EventCardProps) {
  return (
    <TouchableOpacity
      style={styles.container}
      onPress={onPress}
      activeOpacity={0.7}>
      <Image source={{uri: event.poster}} style={styles.poster} />
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={2}>
          {event.name}
        </Text>
        <EventStatusBadge status={event.status} />
        <Text style={styles.detail}>{event.date}</Text>
        <Text style={styles.detail}>{event.venue}</Text>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: colors.card,
    borderRadius: 12,
  },
  poster: {
    width: 80,
    height: 106,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  info: {
    flex: 1,
    marginLeft: 12,
    gap: 4,
  },
  name: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
  },
  detail: {
    fontSize: 12,
    color: colors.mutedForeground,
  },
});
