import React, {useCallback} from 'react';
import {
  View,
  Text,
  FlatList,
  Image,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {CollectionStackParamList} from '../../../core/navigation/types';
import {COLLECTION_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';
import {EmptyState} from '../../../shared/components/EmptyState';
import type {Ticket} from '../../../core/types/common';

type NavigationProp = NativeStackNavigationProp<CollectionStackParamList>;

const SCREEN_WIDTH = Dimensions.get('window').width;
const CARD_GAP = 12;
const CARD_PADDING = 16;
const CARD_WIDTH = (SCREEN_WIDTH - CARD_PADDING * 2 - CARD_GAP) / 2;

function ArchiveCard({
  ticket,
  onPress,
}: {
  ticket: Ticket;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      activeOpacity={0.8}>
      <Image source={{uri: ticket.poster}} style={styles.poster} />
      <View style={styles.badge}>
        <Text style={styles.badgeText}>소장됨</Text>
      </View>
      <View style={styles.infoContainer}>
        <Text style={styles.eventName} numberOfLines={2}>
          {ticket.eventName}
        </Text>
        <Text style={styles.detail}>{ticket.eventDate}</Text>
        <Text style={styles.detail}>{ticket.seatLabel}</Text>
      </View>
    </TouchableOpacity>
  );
}

export function ArchiveScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const tickets = useTicketStore(state => state.tickets);
  const usedTickets = tickets.filter(t => t.status === 'USED');

  const handlePress = useCallback(
    (ticketId: string) => {
      navigation.navigate(COLLECTION_ROUTES.COLLECTIBLE_TICKET_DETAIL, {
        ticketId,
      });
    },
    [navigation],
  );

  const renderItem = useCallback(
    ({item}: {item: Ticket}) => (
      <ArchiveCard ticket={item} onPress={() => handlePress(item.id)} />
    ),
    [handlePress],
  );

  const keyExtractor = useCallback((item: Ticket) => item.id, []);

  return (
    <View style={styles.container}>
      <AppHeader
        title="아카이브"
        showBack
        onBack={() => navigation.goBack()}
      />
      {usedTickets.length === 0 ? (
        <EmptyState
          title="아카이브가 비어있습니다"
          description="관람한 공연 티켓이 여기에 보관됩니다"
        />
      ) : (
        <FlatList
          data={usedTickets}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          numColumns={2}
          contentContainerStyle={styles.listContent}
          columnWrapperStyle={styles.row}
          showsVerticalScrollIndicator={false}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  listContent: {
    padding: CARD_PADDING,
    paddingBottom: 32,
  },
  row: {
    gap: CARD_GAP,
    marginBottom: CARD_GAP,
  },
  card: {
    width: CARD_WIDTH,
    borderRadius: 12,
    overflow: 'hidden',
    backgroundColor: colors.card,
  },
  poster: {
    width: '100%',
    aspectRatio: 2 / 3,
    backgroundColor: colors.muted,
  },
  badge: {
    position: 'absolute',
    top: 8,
    left: 8,
    backgroundColor: colors.primary,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
  },
  badgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: colors.white,
  },
  infoContainer: {
    padding: 10,
    gap: 2,
  },
  eventName: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 2,
  },
  detail: {
    fontSize: 11,
    color: colors.mutedForeground,
  },
});
