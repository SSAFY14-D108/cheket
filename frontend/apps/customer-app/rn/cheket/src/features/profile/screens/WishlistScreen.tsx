import React, {useCallback} from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  Image,
  StyleSheet,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {TAB_ROUTES} from '../../../core/navigation/routes';
import {useWishlistStore} from '../../../store/wishlistStore';
import {MOCK_EVENTS} from '../../../core/data/mockData';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';
import {EmptyState} from '../../../shared/components/EmptyState';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import type {Event} from '../../../core/types/common';
import {EventStatusBadge} from '../../../shared/components/StatusBadge';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList>;

function WishlistItem({
  event,
  onRemove,
}: {
  event: Event;
  onRemove: () => void;
}) {
  return (
    <View style={styles.itemContainer}>
      <Image source={{uri: event.poster}} style={styles.poster} />
      <View style={styles.itemInfo}>
        <Text style={styles.itemName} numberOfLines={2}>
          {event.name}
        </Text>
        <EventStatusBadge status={event.status} />
        <Text style={styles.itemDetail}>{event.date}</Text>
        <Text style={styles.itemDetail}>{event.venue}</Text>
      </View>
      <TouchableOpacity
        style={styles.removeButton}
        onPress={onRemove}
        activeOpacity={0.7}>
        <Text style={styles.removeIcon}>X</Text>
      </TouchableOpacity>
    </View>
  );
}

export function WishlistScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const wishlist = useWishlistStore(state => state.wishlist);
  const toggleWishlist = useWishlistStore(state => state.toggleWishlist);

  const wishlisted = MOCK_EVENTS.filter(e => wishlist.includes(e.id));

  const handleRemove = useCallback(
    (eventId: string) => {
      toggleWishlist(eventId);
    },
    [toggleWishlist],
  );

  const handleNavigateToConcerts = () => {
    navigation.getParent()?.navigate(TAB_ROUTES.CONCERTS_TAB);
  };

  const renderItem = useCallback(
    ({item}: {item: Event}) => (
      <WishlistItem event={item} onRemove={() => handleRemove(item.id)} />
    ),
    [handleRemove],
  );

  const keyExtractor = useCallback((item: Event) => item.id, []);

  return (
    <View style={styles.container}>
      <AppHeader
        title="찜 목록"
        showBack
        onBack={() => navigation.goBack()}
        rightElement={
          <Text style={styles.countBadge}>{wishlisted.length}</Text>
        }
      />
      {wishlisted.length === 0 ? (
        <View style={styles.emptyContainer}>
          <EmptyState
            title="찜한 공연이 없습니다"
            description="관심있는 공연을 찜해보세요"
          />
          <View style={styles.emptyAction}>
            <PrimaryButton
              title="공연 둘러보기"
              onPress={handleNavigateToConcerts}
            />
          </View>
        </View>
      ) : (
        <FlatList
          data={wishlisted}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          contentContainerStyle={styles.listContent}
          showsVerticalScrollIndicator={false}
          ItemSeparatorComponent={() => <View style={styles.separator} />}
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
  countBadge: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.primary,
  },
  listContent: {
    padding: 16,
    paddingBottom: 32,
  },
  itemContainer: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: colors.card,
    borderRadius: 12,
    alignItems: 'center',
  },
  poster: {
    width: 80,
    height: 106,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  itemInfo: {
    flex: 1,
    marginLeft: 12,
    gap: 4,
  },
  itemName: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
  },
  itemDetail: {
    fontSize: 12,
    color: colors.mutedForeground,
  },
  removeButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.destructive + '15',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
  },
  removeIcon: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.destructive,
  },
  separator: {
    height: 8,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
  },
  emptyAction: {
    paddingHorizontal: 40,
    marginTop: 8,
  },
});
