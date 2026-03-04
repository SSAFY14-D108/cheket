import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import {TicketStatus, EventStatus} from '../../core/types/common';
import {colors} from '../../core/theme/colors';

const TICKET_STATUS_MAP: Record<TicketStatus, {color: string; label: string}> =
  {
    SOLD: {color: colors.badgeSold, label: '보유중'},
    LISTED: {color: colors.badgeListed, label: '판매중'},
    USED: {color: colors.badgeUsed, label: '사용됨'},
    EXPIRED: {color: colors.badgeExpired, label: '만료됨'},
  };

const EVENT_STATUS_MAP: Record<EventStatus, {color: string; label: string}> = {
  ON_SALE: {color: colors.badgeOnSale, label: '판매중'},
  SOLD_OUT: {color: colors.badgeSoldOut, label: '매진'},
  ENDED: {color: colors.badgeEnded, label: '종료'},
};

interface TicketStatusBadgeProps {
  status: TicketStatus;
}

export function TicketStatusBadge({status}: TicketStatusBadgeProps) {
  const {color, label} = TICKET_STATUS_MAP[status];
  return (
    <View style={[styles.badge, {backgroundColor: color}]}>
      <Text style={styles.text}>{label}</Text>
    </View>
  );
}

interface EventStatusBadgeProps {
  status: EventStatus;
}

export function EventStatusBadge({status}: EventStatusBadgeProps) {
  const {color, label} = EVENT_STATUS_MAP[status];
  return (
    <View style={[styles.badge, {backgroundColor: color}]}>
      <Text style={styles.text}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 4,
    alignSelf: 'flex-start',
  },
  text: {
    color: colors.white,
    fontSize: 10,
    fontWeight: '600',
  },
});
