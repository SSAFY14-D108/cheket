import React, {useCallback} from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {MOCK_EVENTS} from '../../../core/data/mockData';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {AppHeader} from '../../../shared/components/AppHeader';
import {useWishlistStore} from '../../../store/wishlistStore';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {HOME_ROUTES} from '../../../core/navigation/routes';

type DetailNavigationProp = NativeStackNavigationProp<HomeStackParamList>;
type DetailRouteProp = RouteProp<HomeStackParamList, 'EventDetail'>;

export function EventDetailScreen(): React.JSX.Element {
  const navigation = useNavigation<DetailNavigationProp>();
  const route = useRoute<DetailRouteProp>();
  const {eventId} = route.params;

  const event = MOCK_EVENTS.find(e => e.id === eventId);
  const {toggleWishlist, isWishlisted} = useWishlistStore();
  const wishlisted = isWishlisted(eventId);

  const isDisabled = event?.status === 'SOLD_OUT' || event?.status === 'ENDED';

  const handleBook = useCallback(() => {
    if (event && !isDisabled) {
      navigation.navigate(HOME_ROUTES.WAITING_QUEUE, {eventId: event.id});
    }
  }, [event, isDisabled, navigation]);

  if (!event) {
    return (
      <View style={styles.notFound}>
        <Text style={styles.notFoundText}>이벤트를 찾을 수 없습니다.</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <AppHeader
        title="공연 상세"
        showBack
        onBack={() => navigation.goBack()}
      />

      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}>
        {/* Poster Image */}
        <View style={styles.posterWrapper}>
          <Image source={{uri: event.poster}} style={styles.poster} />
          <TouchableOpacity
            style={styles.heartButton}
            onPress={() => toggleWishlist(eventId)}
            activeOpacity={0.7}>
            <Text style={styles.heartIcon}>{wishlisted ? '❤️' : '🤍'}</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.contentContainer}>
          {/* Event Name */}
          <Text style={styles.eventName}>{event.name}</Text>

          {/* Info Rows */}
          <View style={styles.infoSection}>
            <View style={styles.infoRow}>
              <Text style={styles.infoIcon}>📅</Text>
              <Text style={styles.infoText}>{event.date}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoIcon}>📍</Text>
              <Text style={styles.infoText}>{event.venue}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoIcon}>👤</Text>
              <Text style={styles.infoText}>
                1인당 최대 {event.maxPerUser}매
              </Text>
            </View>
          </View>

          {/* Description */}
          {event.description && (
            <View style={styles.descriptionSection}>
              <Text style={styles.sectionTitle}>공연 소개</Text>
              <Text style={styles.descriptionText}>{event.description}</Text>
            </View>
          )}

          {/* Grade Prices Table */}
          <View style={styles.gradesSection}>
            <Text style={styles.sectionTitle}>좌석 등급별 가격</Text>
            <View style={styles.gradesTable}>
              {event.grades.map(grade => (
                <View key={grade.name} style={styles.gradeRow}>
                  <View style={styles.gradeNameContainer}>
                    <View
                      style={[
                        styles.gradeDot,
                        {backgroundColor: grade.color || colors.primary},
                      ]}
                    />
                    <Text style={styles.gradeName}>{grade.name}</Text>
                  </View>
                  <Text style={styles.gradePrice}>
                    {grade.price.toLocaleString('ko-KR')}원
                  </Text>
                  <Text
                    style={[
                      styles.gradeRemaining,
                      grade.remaining === 0 && styles.gradeRemainingZero,
                    ]}>
                    {grade.remaining === 0
                      ? '매진'
                      : `${grade.remaining}석 남음`}
                  </Text>
                </View>
              ))}
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Bottom Fixed Button */}
      <View style={styles.bottomBar}>
        <PrimaryButton
          title={
            event.status === 'SOLD_OUT'
              ? '매진'
              : event.status === 'ENDED'
                ? '종료된 공연'
                : '예매하기'
          }
          onPress={handleBook}
          disabled={isDisabled}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  scrollView: {
    flex: 1,
  },
  notFound: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.background,
  },
  notFoundText: {
    ...typography.bodyLg,
    color: colors.mutedForeground,
  },

  // ---- Poster ----
  posterWrapper: {
    position: 'relative',
    width: '100%',
    aspectRatio: 3 / 2,
  },
  poster: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  heartButton: {
    position: 'absolute',
    top: 12,
    right: 12,
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(0,0,0,0.4)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  heartIcon: {
    fontSize: 20,
  },

  // ---- Content ----
  contentContainer: {
    padding: 16,
    gap: 20,
  },
  eventName: {
    ...typography.h1,
    color: colors.foreground,
  },

  // ---- Info ----
  infoSection: {
    gap: 10,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  infoIcon: {
    fontSize: 16,
    width: 24,
    textAlign: 'center',
  },
  infoText: {
    ...typography.body,
    color: colors.foreground,
    flex: 1,
  },

  // ---- Description ----
  descriptionSection: {
    gap: 8,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
  },
  descriptionText: {
    ...typography.body,
    color: colors.mutedForeground,
    lineHeight: 22,
  },

  // ---- Grades ----
  gradesSection: {
    gap: 12,
  },
  gradesTable: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 12,
    gap: 12,
  },
  gradeRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  gradeNameContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    gap: 8,
  },
  gradeDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
  gradeName: {
    ...typography.body,
    fontWeight: '600',
    color: colors.foreground,
  },
  gradePrice: {
    ...typography.price,
    color: colors.foreground,
    flex: 1,
    textAlign: 'center',
  },
  gradeRemaining: {
    ...typography.bodySm,
    color: colors.primary,
    width: 80,
    textAlign: 'right',
  },
  gradeRemainingZero: {
    color: colors.destructive,
  },

  // ---- Bottom ----
  bottomBar: {
    padding: 16,
    paddingBottom: 32,
    backgroundColor: colors.card,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
});
