import React from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  Image,
  StyleSheet,
} from 'react-native';
import {Ticket} from '../../core/types/common';
import {colors} from '../../core/theme/colors';
import {TicketStatusBadge} from './StatusBadge';

interface TicketCardProps {
  ticket: Ticket;
  onPress: () => void;
}

export function TicketCard({ticket, onPress}: TicketCardProps) {
  return (
    <TouchableOpacity
      style={styles.container}
      onPress={onPress}
      activeOpacity={0.7}>
      <Image source={{uri: ticket.poster}} style={styles.poster} />
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={2}>
          {ticket.eventName}
        </Text>
        <Text style={styles.detail}>
          {ticket.seatLabel} / {ticket.grade}
        </Text>
        <Text style={styles.detail}>{ticket.eventDate}</Text>
        <Text style={styles.detail}>{ticket.venue}</Text>
        <TicketStatusBadge status={ticket.status} />
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
    width: 64,
    height: 85,
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
