import React, {useState, useMemo, useCallback} from 'react';
import {
  View,
  Text,
  TextInput,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {MOCK_EVENTS} from '../../../core/data/mockData';
import {EventCard} from '../../../shared/components/EventCard';
import {EmptyState} from '../../../shared/components/EmptyState';
import type {ConcertsStackParamList} from '../../../core/navigation/types';
import type {Event} from '../../../core/types/common';
import {CONCERTS_ROUTES} from '../../../core/navigation/routes';

type NavigationProp = NativeStackNavigationProp<ConcertsStackParamList>;

type SortKey = 'popular' | 'latest' | 'closing';

const SORT_TABS: {key: SortKey; label: string}[] = [
  {key: 'popular', label: '인기순'},
  {key: 'latest', label: '최신순'},
  {key: 'closing', label: '마감임박순'},
];

const REGIONS = [
  '전체',
  '서울',
  '경기',
  '부산',
  '대구',
  '광주',
  '대전',
  '제주',
  '기타',
];

export function ConcertsScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();

  const [searchQuery, setSearchQuery] = useState('');
  const [sortKey, setSortKey] = useState<SortKey>('popular');
  const [showFilter, setShowFilter] = useState(false);
  const [selectedRegion, setSelectedRegion] = useState('전체');
  const [includeSoldOut, setIncludeSoldOut] = useState(false);

  const filteredEvents = useMemo(() => {
    let events = [...MOCK_EVENTS];

    // Search filter
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      events = events.filter(
        e =>
          e.name.toLowerCase().includes(q) ||
          e.venue.toLowerCase().includes(q),
      );
    }

    // Region filter
    if (selectedRegion !== '전체') {
      events = events.filter(e => e.region === selectedRegion);
    }

    // Sold out filter
    if (!includeSoldOut) {
      events = events.filter(e => e.status !== 'SOLD_OUT');
    }

    // Sort
    switch (sortKey) {
      case 'latest':
        events.sort((a, b) => {
          const dateA = a.openDate || '';
          const dateB = b.openDate || '';
          return dateB.localeCompare(dateA);
        });
        break;
      case 'closing':
        events.sort((a, b) => {
          const dateA = a.openDate || '';
          const dateB = b.openDate || '';
          return dateA.localeCompare(dateB);
        });
        break;
      case 'popular':
      default:
        // Keep original order (assumed popularity order)
        break;
    }

    return events;
  }, [searchQuery, sortKey, selectedRegion, includeSoldOut]);

  const handleEventPress = useCallback(
    (eventId: string) => {
      navigation.navigate(CONCERTS_ROUTES.EVENT_DETAIL, {eventId});
    },
    [navigation],
  );

  const renderEventItem = useCallback(
    ({item}: {item: Event}) => (
      <View style={styles.cardWrapper}>
        <EventCard event={item} onPress={() => handleEventPress(item.id)} />
      </View>
    ),
    [handleEventPress],
  );

  return (
    <SafeAreaView style={styles.container}>
      {/* Search Bar */}
      <View style={styles.searchContainer}>
        <View style={styles.searchBar}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={styles.searchInput}
            placeholder="공연명, 아티스트 검색"
            placeholderTextColor={colors.mutedForeground}
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
        </View>
      </View>

      {/* Sort Tabs */}
      <View style={styles.sortRow}>
        <View style={styles.sortTabs}>
          {SORT_TABS.map(tab => (
            <TouchableOpacity
              key={tab.key}
              style={[
                styles.sortTab,
                sortKey === tab.key && styles.sortTabActive,
              ]}
              onPress={() => setSortKey(tab.key)}
              activeOpacity={0.7}>
              <Text
                style={[
                  styles.sortTabText,
                  sortKey === tab.key && styles.sortTabTextActive,
                ]}>
                {tab.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity
          style={[
            styles.filterToggle,
            showFilter && styles.filterToggleActive,
          ]}
          onPress={() => setShowFilter(prev => !prev)}
          activeOpacity={0.7}>
          <Text
            style={[
              styles.filterToggleText,
              showFilter && styles.filterToggleTextActive,
            ]}>
            필터
          </Text>
        </TouchableOpacity>
      </View>

      {/* Filter Panel */}
      {showFilter && (
        <View style={styles.filterPanel}>
          <Text style={styles.filterLabel}>지역</Text>
          <View style={styles.chipsRow}>
            {REGIONS.map(region => (
              <TouchableOpacity
                key={region}
                style={[
                  styles.chip,
                  selectedRegion === region && styles.chipActive,
                ]}
                onPress={() => setSelectedRegion(region)}
                activeOpacity={0.7}>
                <Text
                  style={[
                    styles.chipText,
                    selectedRegion === region && styles.chipTextActive,
                  ]}>
                  {region}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.toggleRow}>
            <Text style={styles.filterLabel}>매진 포함</Text>
            <TouchableOpacity
              style={[
                styles.toggleTrack,
                includeSoldOut && styles.toggleTrackActive,
              ]}
              onPress={() => setIncludeSoldOut(prev => !prev)}
              activeOpacity={0.7}>
              <View
                style={[
                  styles.toggleThumb,
                  includeSoldOut && styles.toggleThumbActive,
                ]}
              />
            </TouchableOpacity>
          </View>
        </View>
      )}

      {/* Event List */}
      <FlatList
        data={filteredEvents}
        renderItem={renderEventItem}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <EmptyState
            title="검색 결과가 없습니다"
            description="다른 검색어나 필터를 시도해 보세요."
          />
        }
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },

  // ---- Search ----
  searchContainer: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 8,
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.input,
    borderRadius: 10,
    paddingHorizontal: 12,
    height: 42,
  },
  searchIcon: {
    fontSize: 16,
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    ...typography.body,
    color: colors.foreground,
    padding: 0,
  },

  // ---- Sort ----
  sortRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  sortTabs: {
    flexDirection: 'row',
    gap: 4,
  },
  sortTab: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderBottomWidth: 2,
    borderBottomColor: colors.transparent,
  },
  sortTabActive: {
    borderBottomColor: colors.primary,
  },
  sortTabText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  sortTabTextActive: {
    color: colors.primary,
    fontWeight: '600',
  },
  filterToggle: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.border,
  },
  filterToggleActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  filterToggleText: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  filterToggleTextActive: {
    color: colors.white,
  },

  // ---- Filter Panel ----
  filterPanel: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: colors.card,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: colors.border,
    gap: 12,
  },
  filterLabel: {
    ...typography.label,
    color: colors.foreground,
  },
  chipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    backgroundColor: colors.muted,
  },
  chipActive: {
    backgroundColor: colors.primary,
  },
  chipText: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  chipTextActive: {
    color: colors.white,
    fontWeight: '600',
  },
  toggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  toggleTrack: {
    width: 44,
    height: 24,
    borderRadius: 12,
    backgroundColor: colors.border,
    justifyContent: 'center',
    paddingHorizontal: 2,
  },
  toggleTrackActive: {
    backgroundColor: colors.primary,
  },
  toggleThumb: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: colors.white,
  },
  toggleThumbActive: {
    alignSelf: 'flex-end',
  },

  // ---- List ----
  listContent: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    gap: 12,
    flexGrow: 1,
  },
  cardWrapper: {
    marginBottom: 0,
  },
});
