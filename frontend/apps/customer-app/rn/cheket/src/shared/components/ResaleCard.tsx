import React from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  Image,
  StyleSheet,
} from 'react-native';
import {ResaleItem} from '../../core/types/common';
import {colors} from '../../core/theme/colors';

interface ResaleCardProps {
  item: ResaleItem;
  onPress: () => void;
}

export function ResaleCard({item, onPress}: ResaleCardProps) {
  const hasDiscount = item.resalePrice < item.originalPrice;
  const discountPercent = hasDiscount
    ? Math.round(
        ((item.originalPrice - item.resalePrice) / item.originalPrice) * 100,
      )
    : 0;

  return (
    <TouchableOpacity
      style={styles.container}
      onPress={onPress}
      activeOpacity={0.7}>
      <Image source={{uri: item.poster}} style={styles.poster} />
      <View style={styles.info}>
        <Text style={styles.name} numberOfLines={1}>
          {item.eventName}
        </Text>
        <Text style={styles.detail}>
          {item.seatLabel} / {item.grade}
        </Text>
        <Text style={styles.detail}>{item.venue}</Text>
        <View style={styles.priceRow}>
          <Text style={styles.resalePrice}>
            {item.resalePrice.toLocaleString()}원
          </Text>
          {hasDiscount && (
            <View style={styles.discountBadge}>
              <Text style={styles.discountText}>{discountPercent}%</Text>
            </View>
          )}
        </View>
        {hasDiscount && (
          <Text style={styles.originalPrice}>
            {item.originalPrice.toLocaleString()}원
          </Text>
        )}
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
    width: 56,
    height: 56,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  info: {
    flex: 1,
    marginLeft: 12,
    gap: 2,
  },
  name: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
  },
  detail: {
    fontSize: 12,
    color: colors.mutedForeground,
  },
  priceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 2,
  },
  resalePrice: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
  },
  discountBadge: {
    backgroundColor: colors.destructive,
    paddingHorizontal: 4,
    paddingVertical: 1,
    borderRadius: 4,
  },
  discountText: {
    color: colors.white,
    fontSize: 10,
    fontWeight: '700',
  },
  originalPrice: {
    fontSize: 12,
    color: colors.mutedForeground,
    textDecorationLine: 'line-through',
  },
});
