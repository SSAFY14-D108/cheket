import React, {useRef, useEffect, useCallback, useState} from 'react';
import {
  View,
  Text,
  ScrollView,
  FlatList,
  Image,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
  NativeSyntheticEvent,
  NativeScrollEvent,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {
  BANNER_SLIDES,
  CATEGORY_ICONS,
  RANKING_ITEMS,
  OPEN_SCHEDULE,
  DISCOUNT_ITEMS,
} from '../../../core/data/mockData';
import {SectionHeader} from '../../../shared/components/SectionHeader';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {HOME_ROUTES} from '../../../core/navigation/routes';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList>;

const {width: SCREEN_WIDTH} = Dimensions.get('window');
const BANNER_HEIGHT = 220;

const RANK_COLORS: Record<number, string> = {
  1: '#FFD700',
  2: '#C0C0C0',
  3: '#CD7F32',
};

export function HomeScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const bannerRef = useRef<FlatList>(null);
  const [currentBanner, setCurrentBanner] = useState(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const startAutoScroll = useCallback(() => {
    timerRef.current = setInterval(() => {
      setCurrentBanner(prev => {
        const next = (prev + 1) % BANNER_SLIDES.length;
        bannerRef.current?.scrollToOffset({
          offset: next * SCREEN_WIDTH,
          animated: true,
        });
        return next;
      });
    }, 3500);
  }, []);

  useEffect(() => {
    startAutoScroll();
    return () => {
      if (timerRef.current) {
        clearInterval(timerRef.current);
      }
    };
  }, [startAutoScroll]);

  const onBannerScroll = useCallback(
    (e: NativeSyntheticEvent<NativeScrollEvent>) => {
      const index = Math.round(e.nativeEvent.contentOffset.x / SCREEN_WIDTH);
      if (index !== currentBanner) {
        setCurrentBanner(index);
        if (timerRef.current) {
          clearInterval(timerRef.current);
        }
        startAutoScroll();
      }
    },
    [currentBanner, startAutoScroll],
  );

  const navigateToDetail = useCallback(
    (eventId: string) => {
      navigation.navigate(HOME_ROUTES.EVENT_DETAIL, {eventId});
    },
    [navigation],
  );

  // ---- Banner Carousel ----
  const renderBannerItem = useCallback(
    ({item}: {item: (typeof BANNER_SLIDES)[number]}) => (
      <TouchableOpacity
        activeOpacity={0.9}
        onPress={() => navigateToDetail(item.eventId)}
        style={styles.bannerSlide}>
        <Image source={{uri: item.image}} style={styles.bannerImage} />
        <View style={styles.bannerGradient} />
        <View style={styles.bannerTextContainer}>
          <Text style={styles.bannerSubtitle}>{item.subtitle}</Text>
          <Text style={styles.bannerTitle}>{item.title}</Text>
          <Text style={styles.bannerInfo}>
            {item.venue} | {item.dates}
          </Text>
        </View>
      </TouchableOpacity>
    ),
    [navigateToDetail],
  );

  // ---- Category Icons ----
  const renderCategoryItem = useCallback(
    ({item}: {item: (typeof CATEGORY_ICONS)[number]}) => (
      <TouchableOpacity style={styles.categoryItem} activeOpacity={0.7}>
        <View style={styles.categoryIconCircle}>
          <Text style={styles.categoryEmoji}>{item.icon}</Text>
        </View>
        <Text style={styles.categoryLabel}>{item.label}</Text>
      </TouchableOpacity>
    ),
    [],
  );

  // ---- Ranking Items ----
  const renderRankingItem = useCallback(
    ({item}: {item: (typeof RANKING_ITEMS)[number]}) => (
      <TouchableOpacity
        style={styles.rankingItem}
        activeOpacity={0.7}
        onPress={() => navigateToDetail(item.eventId)}>
        <View style={styles.rankingPosterWrapper}>
          <Image source={{uri: item.poster}} style={styles.rankingPoster} />
          <View
            style={[
              styles.rankBadge,
              {
                backgroundColor:
                  RANK_COLORS[item.rank] || colors.mutedForeground,
              },
            ]}>
            <Text style={styles.rankBadgeText}>{item.rank}</Text>
          </View>
        </View>
        <Text style={styles.rankingName} numberOfLines={2}>
          {item.name}
        </Text>
        <Text style={styles.rankingVenue} numberOfLines={1}>
          {item.venue}
        </Text>
      </TouchableOpacity>
    ),
    [navigateToDetail],
  );

  // ---- Open Schedule Items ----
  const renderOpenScheduleItem = useCallback(
    (item: (typeof OPEN_SCHEDULE)[number]) => (
      <TouchableOpacity
        key={item.id}
        style={styles.scheduleItem}
        activeOpacity={0.7}
        onPress={() => navigateToDetail(item.eventId)}>
        <Image source={{uri: item.poster}} style={styles.schedulePoster} />
        <View style={styles.scheduleInfo}>
          <Text style={styles.scheduleName} numberOfLines={1}>
            {item.name}
          </Text>
          <View style={styles.scheduleRow}>
            <Text
              style={[
                styles.scheduleOpenLabel,
                item.isToday && styles.scheduleTodayLabel,
              ]}>
              {item.openLabel}
            </Text>
            <Text style={styles.scheduleOpenType}>{item.openType}</Text>
          </View>
          <View style={styles.tagsRow}>
            {item.tags.map(tag => (
              <View key={tag} style={styles.tagBadge}>
                <Text style={styles.tagText}>{tag}</Text>
              </View>
            ))}
          </View>
        </View>
      </TouchableOpacity>
    ),
    [navigateToDetail],
  );

  // ---- Discount Items ----
  const renderDiscountItem = useCallback(
    ({item}: {item: (typeof DISCOUNT_ITEMS)[number]}) => (
      <TouchableOpacity
        style={styles.discountItem}
        activeOpacity={0.7}
        onPress={() => navigateToDetail(item.eventId)}>
        <View style={styles.discountPosterWrapper}>
          <Image source={{uri: item.poster}} style={styles.discountPoster} />
          <View style={styles.discountBadge}>
            <Text style={styles.discountBadgeText}>{item.discountPct}%</Text>
          </View>
        </View>
        <Text style={styles.discountName} numberOfLines={1}>
          {item.name}
        </Text>
        <Text style={styles.discountPrice}>
          {item.finalPrice.toLocaleString('ko-KR')}원
        </Text>
        <Text style={styles.discountCountdown}>{item.countdownLabel}</Text>
      </TouchableOpacity>
    ),
    [navigateToDetail],
  );

  return (
    <ScrollView
      style={styles.container}
      showsVerticalScrollIndicator={false}>
      {/* Hero Banner Carousel */}
      <View style={styles.bannerContainer}>
        <FlatList
          ref={bannerRef}
          data={BANNER_SLIDES}
          renderItem={renderBannerItem}
          keyExtractor={item => item.id}
          horizontal
          pagingEnabled
          showsHorizontalScrollIndicator={false}
          onMomentumScrollEnd={onBannerScroll}
          bounces={false}
        />
        <View style={styles.dotContainer}>
          {BANNER_SLIDES.map((slide, index) => (
            <View
              key={slide.id}
              style={[
                styles.dot,
                index === currentBanner && styles.dotActive,
              ]}
            />
          ))}
        </View>
      </View>

      {/* Category Icons */}
      <FlatList
        data={CATEGORY_ICONS}
        renderItem={renderCategoryItem}
        keyExtractor={item => item.id}
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.categoryList}
        scrollEnabled={true}
      />

      {/* Concert Ranking */}
      <SectionHeader title="콘서트 랭킹" />
      <FlatList
        data={RANKING_ITEMS}
        renderItem={renderRankingItem}
        keyExtractor={item => item.eventId}
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.rankingList}
      />

      {/* Open Schedule */}
      <SectionHeader title="오픈 스케줄" />
      <View style={styles.scheduleList}>
        {OPEN_SCHEDULE.map(renderOpenScheduleItem)}
      </View>

      {/* Discount Section */}
      <SectionHeader title="할인/특가" />
      <FlatList
        data={DISCOUNT_ITEMS}
        renderItem={renderDiscountItem}
        keyExtractor={item => item.id}
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.discountList}
      />

      <View style={styles.bottomSpacer} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },

  // ---- Banner ----
  bannerContainer: {
    height: BANNER_HEIGHT,
    position: 'relative',
  },
  bannerSlide: {
    width: SCREEN_WIDTH,
    height: BANNER_HEIGHT,
    position: 'relative',
  },
  bannerImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  bannerGradient: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.45)',
  },
  bannerTextContainer: {
    position: 'absolute',
    bottom: 32,
    left: 16,
    right: 16,
  },
  bannerSubtitle: {
    ...typography.labelSm,
    color: colors.primary,
    marginBottom: 4,
  },
  bannerTitle: {
    ...typography.h1,
    color: colors.white,
    marginBottom: 4,
  },
  bannerInfo: {
    ...typography.bodySm,
    color: 'rgba(255,255,255,0.8)',
  },
  dotContainer: {
    position: 'absolute',
    bottom: 10,
    alignSelf: 'center',
    flexDirection: 'row',
    gap: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: 'rgba(255,255,255,0.4)',
  },
  dotActive: {
    backgroundColor: colors.white,
    width: 18,
  },

  // ---- Category ----
  categoryList: {
    paddingHorizontal: 12,
    paddingVertical: 16,
    gap: 4,
  },
  categoryItem: {
    alignItems: 'center',
    width: 68,
  },
  categoryIconCircle: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  categoryEmoji: {
    fontSize: 22,
  },
  categoryLabel: {
    ...typography.caption,
    color: colors.foreground,
    textAlign: 'center',
  },

  // ---- Ranking ----
  rankingList: {
    paddingHorizontal: 16,
    gap: 12,
  },
  rankingItem: {
    width: 100,
  },
  rankingPosterWrapper: {
    position: 'relative',
    marginBottom: 6,
  },
  rankingPoster: {
    width: 100,
    height: 133,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  rankBadge: {
    position: 'absolute',
    top: 6,
    left: 6,
    width: 22,
    height: 22,
    borderRadius: 11,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rankBadgeText: {
    ...typography.caption,
    color: colors.white,
    fontWeight: '700',
  },
  rankingName: {
    ...typography.bodySm,
    fontWeight: '600',
    color: colors.foreground,
    marginBottom: 2,
  },
  rankingVenue: {
    ...typography.caption,
    color: colors.mutedForeground,
  },

  // ---- Open Schedule ----
  scheduleList: {
    paddingHorizontal: 16,
    gap: 12,
  },
  scheduleItem: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: colors.card,
    borderRadius: 12,
  },
  schedulePoster: {
    width: 56,
    height: 74,
    borderRadius: 6,
    backgroundColor: colors.muted,
  },
  scheduleInfo: {
    flex: 1,
    marginLeft: 12,
    justifyContent: 'center',
    gap: 4,
  },
  scheduleName: {
    ...typography.body,
    fontWeight: '600',
    color: colors.foreground,
  },
  scheduleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  scheduleOpenLabel: {
    ...typography.labelSm,
    color: colors.mutedForeground,
  },
  scheduleTodayLabel: {
    color: colors.primary,
  },
  scheduleOpenType: {
    ...typography.caption,
    color: colors.mutedForeground,
    backgroundColor: colors.muted,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    overflow: 'hidden',
  },
  tagsRow: {
    flexDirection: 'row',
    gap: 4,
  },
  tagBadge: {
    backgroundColor: colors.secondary,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  tagText: {
    ...typography.caption,
    color: colors.primary,
  },

  // ---- Discount ----
  discountList: {
    paddingHorizontal: 16,
    gap: 12,
  },
  discountItem: {
    width: 140,
  },
  discountPosterWrapper: {
    position: 'relative',
    marginBottom: 6,
  },
  discountPoster: {
    width: 140,
    height: 186,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  discountBadge: {
    position: 'absolute',
    top: 8,
    right: 8,
    backgroundColor: colors.destructive,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  discountBadgeText: {
    ...typography.labelSm,
    color: colors.white,
  },
  discountName: {
    ...typography.bodySm,
    fontWeight: '600',
    color: colors.foreground,
    marginBottom: 2,
  },
  discountPrice: {
    ...typography.price,
    color: colors.foreground,
    fontSize: 14,
  },
  discountCountdown: {
    ...typography.caption,
    color: colors.destructive,
    marginTop: 2,
  },

  bottomSpacer: {
    height: 32,
  },
});
