import React, {useCallback, useMemo, useState} from 'react';
import {
  View,
  Text,
  FlatList,
  Image,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {useResaleStore} from '../../../store/resaleStore';
import {ResaleCard} from '../../../shared/components/ResaleCard';
import {EmptyState} from '../../../shared/components/EmptyState';
import {AppHeader} from '../../../shared/components/AppHeader';
import type {ResaleStackParamList} from '../../../core/navigation/types';
import {RESALE_ROUTES} from '../../../core/navigation/routes';
import type {ResaleItem} from '../../../core/types/common';

type NavigationProp = NativeStackNavigationProp<ResaleStackParamList>;

interface EventGroup {
  eventName: string;
  poster: string;
  venue: string;
  eventDate: string;
  count: number;
  minPrice: number;
}

export function ResaleListScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const resaleItems = useResaleStore(state => state.resaleItems);
  const [selectedEvent, setSelectedEvent] = useState<string | null>(null);

  const eventGroups = useMemo(() => {
    const groupMap = new Map<string, EventGroup>();
    resaleItems.forEach(item => {
      const existing = groupMap.get(item.eventName);
      if (existing) {
        existing.count += 1;
        existing.minPrice = Math.min(existing.minPrice, item.resalePrice);
      } else {
        groupMap.set(item.eventName, {
          eventName: item.eventName,
          poster: item.poster,
          venue: item.venue,
          eventDate: item.eventDate,
          count: 1,
          minPrice: item.resalePrice,
        });
      }
    });
    return Array.from(groupMap.values());
  }, [resaleItems]);

  const filteredItems = useMemo(() => {
    if (!selectedEvent) {
      return [];
    }
    return resaleItems.filter(item => item.eventName === selectedEvent);
  }, [resaleItems, selectedEvent]);

  const handleEventPress = useCallback((eventName: string) => {
    setSelectedEvent(eventName);
  }, []);

  const handleResalePress = useCallback(
    (resaleId: string) => {
      navigation.navigate(RESALE_ROUTES.RESALE_DETAIL, {resaleId});
    },
    [navigation],
  );

  const handleBack = useCallback(() => {
    setSelectedEvent(null);
  }, []);

  const renderEventCard = useCallback(
    ({item}: {item: EventGroup}) => (
      <TouchableOpacity
        style={styles.eventCard}
        onPress={() => handleEventPress(item.eventName)}
        activeOpacity={0.7}>
        <Image source={{uri: item.poster}} style={styles.eventPoster} />
        <View style={styles.eventInfo}>
          <Text style={styles.eventName} numberOfLines={1}>
            {item.eventName}
          </Text>
          <Text style={styles.eventDetail}>{item.venue}</Text>
          <Text style={styles.eventDetail}>{item.eventDate}</Text>
          <View style={styles.eventMeta}>
            <View style={styles.countBadge}>
              <Text style={styles.countText}>{item.count}건</Text>
            </View>
            <Text style={styles.minPrice}>
              {item.minPrice.toLocaleString()}원~
            </Text>
          </View>
        </View>
      </TouchableOpacity>
    ),
    [handleEventPress],
  );

  const renderResaleItem = useCallback(
    ({item}: {item: ResaleItem}) => (
      <View style={styles.resaleCardWrapper}>
        <ResaleCard
          item={item}
          onPress={() => handleResalePress(item.id)}
        />
      </View>
    ),
    [handleResalePress],
  );

  if (resaleItems.length === 0) {
    return (
      <View style={styles.container}>
        <AppHeader title="양도 마켓" />
        <EmptyState
          title="등록된 양도 티켓이 없습니다"
          description="현재 양도 가능한 티켓이 없습니다."
        />
      </View>
    );
  }

  // Filtered view: showing resale items for a specific event
  if (selectedEvent) {
    return (
      <View style={styles.container}>
        <AppHeader
          title={selectedEvent}
          showBack
          onBack={handleBack}
        />
        <FlatList
          data={filteredItems}
          keyExtractor={item => item.id}
          renderItem={renderResaleItem}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
          ListEmptyComponent={
            <EmptyState
              title="양도 티켓이 없습니다"
              description="이 공연의 양도 티켓이 모두 판매되었습니다."
            />
          }
        />
      </View>
    );
  }

  // Default view: grouped by event
  return (
    <View style={styles.container}>
      <AppHeader title="양도 마켓" />
      <FlatList
        data={eventGroups}
        keyExtractor={item => item.eventName}
        renderItem={renderEventCard}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  listContent: {
    padding: 16,
  },
  separator: {
    height: 12,
  },

  // ---- Event Card (grouped view) ----
  eventCard: {
    flexDirection: 'row',
    padding: 14,
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
  },
  eventPoster: {
    width: 72,
    height: 96,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  eventInfo: {
    flex: 1,
    marginLeft: 14,
    justifyContent: 'center',
    gap: 3,
  },
  eventName: {
    ...typography.h3,
    color: colors.foreground,
  },
  eventDetail: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  eventMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 4,
  },
  countBadge: {
    backgroundColor: colors.secondary,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
  },
  countText: {
    ...typography.labelSm,
    color: colors.primary,
  },
  minPrice: {
    ...typography.price,
    color: colors.foreground,
  },

  // ---- Resale Card Wrapper ----
  resaleCardWrapper: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    overflow: 'hidden',
  },
});
