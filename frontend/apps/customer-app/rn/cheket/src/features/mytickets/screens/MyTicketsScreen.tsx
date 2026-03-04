import React, {useState, useCallback} from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {TicketCard} from '../../../shared/components/TicketCard';
import {EmptyState} from '../../../shared/components/EmptyState';
import {AppHeader} from '../../../shared/components/AppHeader';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import type {TicketStatus} from '../../../core/types/common';

type Props = NativeStackScreenProps<MyTicketsStackParamList, 'MyTickets'>;

type FilterTab = 'ALL' | TicketStatus;

const FILTER_TABS: {key: FilterTab; label: string}[] = [
  {key: 'ALL', label: '전체'},
  {key: 'SOLD', label: '보유중'},
  {key: 'LISTED', label: '판매중'},
  {key: 'USED', label: '사용됨'},
  {key: 'EXPIRED', label: '만료됨'},
];

export function MyTicketsScreen({navigation}: Props) {
  const [selectedFilter, setSelectedFilter] = useState<FilterTab>('ALL');
  const tickets = useTicketStore(state => state.tickets);

  const filteredTickets =
    selectedFilter === 'ALL'
      ? tickets
      : tickets.filter(t => t.status === selectedFilter);

  const handleTicketPress = useCallback(
    (ticketId: string) => {
      navigation.navigate(MY_TICKETS_ROUTES.TICKET_DETAIL, {ticketId});
    },
    [navigation],
  );

  return (
    <View style={styles.container}>
      <AppHeader title="내 티켓" />
      <View style={styles.filterContainer}>
        {FILTER_TABS.map(tab => {
          const isActive = selectedFilter === tab.key;
          return (
            <TouchableOpacity
              key={tab.key}
              style={[styles.filterTab, isActive && styles.filterTabActive]}
              onPress={() => setSelectedFilter(tab.key)}
              activeOpacity={0.7}>
              <Text
                style={[
                  styles.filterTabText,
                  isActive && styles.filterTabTextActive,
                ]}>
                {tab.label}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
      <FlatList
        data={filteredTickets}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.listContent}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        renderItem={({item}) => (
          <TicketCard
            ticket={item}
            onPress={() => handleTicketPress(item.id)}
          />
        )}
        ListEmptyComponent={
          <EmptyState
            title="티켓이 없습니다"
            description="해당 상태의 티켓이 존재하지 않습니다."
          />
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  filterContainer: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
    backgroundColor: colors.card,
  },
  filterTab: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    backgroundColor: colors.muted,
  },
  filterTabActive: {
    backgroundColor: colors.primary,
  },
  filterTabText: {
    ...typography.labelSm,
    color: colors.mutedForeground,
  },
  filterTabTextActive: {
    color: colors.primaryForeground,
  },
  listContent: {
    padding: 16,
    paddingBottom: 32,
    flexGrow: 1,
  },
  separator: {
    height: 12,
  },
});
