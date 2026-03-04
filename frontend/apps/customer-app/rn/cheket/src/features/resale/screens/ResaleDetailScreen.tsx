import React, {useCallback, useMemo} from 'react';
import {View, Text, Image, ScrollView, StyleSheet} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {useResaleStore} from '../../../store/resaleStore';
import {useAuthStore} from '../../../store/authStore';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {AppHeader} from '../../../shared/components/AppHeader';
import type {ResaleStackParamList} from '../../../core/navigation/types';
import {RESALE_ROUTES} from '../../../core/navigation/routes';

type NavigationProp = NativeStackNavigationProp<ResaleStackParamList>;
type DetailRouteProp = RouteProp<ResaleStackParamList, 'ResaleDetail'>;

const USAGE_NOTICES = [
  '양도 티켓은 블록체인 기반 에스크로를 통해 안전하게 거래됩니다.',
  '구매 후 취소 및 환불이 불가합니다.',
  '티켓 양도는 1회에 한하여 가능합니다.',
  '본인 인증 후 입장이 가능합니다.',
  '부정 거래 적발 시 이용이 제한될 수 있습니다.',
];

export function ResaleDetailScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<DetailRouteProp>();
  const {resaleId} = route.params;

  const resaleItem = useResaleStore(state =>
    state.resaleItems.find(item => item.id === resaleId),
  );
  const buyResaleTicket = useResaleStore(state => state.buyResaleTicket);
  const user = useAuthStore(state => state.user);

  const discountInfo = useMemo(() => {
    if (!resaleItem) {
      return {hasDiscount: false, discountPercent: 0};
    }
    const hasDiscount = resaleItem.resalePrice < resaleItem.originalPrice;
    const discountPercent = hasDiscount
      ? Math.round(
          ((resaleItem.originalPrice - resaleItem.resalePrice) /
            resaleItem.originalPrice) *
            100,
        )
      : 0;
    return {hasDiscount, discountPercent};
  }, [resaleItem]);

  const ctkBalance = user?.ctkBalance ?? 0;
  const insufficientBalance = resaleItem
    ? ctkBalance < resaleItem.resalePrice
    : true;

  const handlePurchase = useCallback(() => {
    if (!resaleItem) {
      return;
    }
    const newTicketId = buyResaleTicket(resaleId);
    if (newTicketId) {
      navigation.navigate(RESALE_ROUTES.RESALE_PURCHASE_COMPLETE, {
        resaleId,
        purchasedTicketId: newTicketId,
      });
    }
  }, [resaleItem, resaleId, buyResaleTicket, navigation]);

  if (!resaleItem) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="양도 상세"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.notFound}>
          <Text style={styles.notFoundText}>
            양도 정보를 찾을 수 없습니다.
          </Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <AppHeader
        title="양도 상세"
        showBack
        onBack={() => navigation.goBack()}
      />

      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Poster */}
        <Image source={{uri: resaleItem.poster}} style={styles.poster} />

        <View style={styles.contentContainer}>
          {/* Event Name */}
          <Text style={styles.eventName}>{resaleItem.eventName}</Text>

          {/* Event Info */}
          <View style={styles.infoSection}>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>날짜</Text>
              <Text style={styles.infoValue}>{resaleItem.eventDate}</Text>
            </View>
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>장소</Text>
              <Text style={styles.infoValue}>{resaleItem.venue}</Text>
            </View>
          </View>

          {/* Seat Info */}
          <View style={styles.seatSection}>
            <Text style={styles.sectionTitle}>좌석 정보</Text>
            <View style={styles.seatCard}>
              <View style={styles.seatRow}>
                <Text style={styles.seatLabel}>좌석</Text>
                <Text style={styles.seatValue}>{resaleItem.seatLabel}</Text>
              </View>
              <View style={styles.seatRow}>
                <Text style={styles.seatLabel}>등급</Text>
                <View style={styles.gradeBadge}>
                  <Text style={styles.gradeText}>{resaleItem.grade}</Text>
                </View>
              </View>
            </View>
          </View>

          {/* Price Section */}
          <View style={styles.priceSection}>
            <Text style={styles.sectionTitle}>가격 정보</Text>
            <View style={styles.priceCard}>
              <View style={styles.priceRow}>
                <Text style={styles.priceLabel}>정가</Text>
                <Text style={styles.originalPrice}>
                  {resaleItem.originalPrice.toLocaleString()}원
                </Text>
              </View>
              <View style={styles.divider} />
              <View style={styles.resalePriceRow}>
                <Text style={styles.priceLabel}>양도가</Text>
                <View style={styles.resalePriceContainer}>
                  <Text style={styles.resalePrice}>
                    {resaleItem.resalePrice.toLocaleString()}원
                  </Text>
                  {discountInfo.hasDiscount && (
                    <View style={styles.discountBadge}>
                      <Text style={styles.discountText}>
                        {discountInfo.discountPercent}% OFF
                      </Text>
                    </View>
                  )}
                </View>
              </View>
            </View>
          </View>

          {/* CTK Balance */}
          <View style={styles.balanceSection}>
            <Text style={styles.sectionTitle}>내 CTK 잔액</Text>
            <View
              style={[
                styles.balanceCard,
                insufficientBalance && styles.balanceCardWarning,
              ]}>
              <Text
                style={[
                  styles.balanceAmount,
                  insufficientBalance && styles.balanceAmountWarning,
                ]}>
                {ctkBalance.toLocaleString()} CTK
              </Text>
              {insufficientBalance && (
                <Text style={styles.balanceWarning}>
                  잔액이 부족합니다. 충전 후 이용해주세요.
                </Text>
              )}
            </View>
          </View>

          {/* Usage Notices */}
          <View style={styles.noticeSection}>
            <Text style={styles.sectionTitle}>이용 안내</Text>
            <View style={styles.noticeCard}>
              {USAGE_NOTICES.map((notice, index) => (
                <View key={index} style={styles.noticeRow}>
                  <Text style={styles.noticeBullet}>*</Text>
                  <Text style={styles.noticeText}>{notice}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Bottom CTA */}
      <View style={styles.bottomBar}>
        <PrimaryButton
          title={insufficientBalance ? '잔액 부족' : '구매하기'}
          onPress={handlePurchase}
          disabled={insufficientBalance}
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
  },
  notFoundText: {
    ...typography.bodyLg,
    color: colors.mutedForeground,
  },

  // ---- Poster ----
  poster: {
    width: '100%',
    aspectRatio: 3 / 2,
    backgroundColor: colors.muted,
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
    gap: 8,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  infoLabel: {
    ...typography.label,
    color: colors.mutedForeground,
    width: 48,
  },
  infoValue: {
    ...typography.body,
    color: colors.foreground,
    flex: 1,
  },

  // ---- Seat ----
  seatSection: {
    gap: 8,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
  },
  seatCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    gap: 10,
    borderWidth: 1,
    borderColor: colors.border,
  },
  seatRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  seatLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  seatValue: {
    ...typography.label,
    color: colors.foreground,
  },
  gradeBadge: {
    backgroundColor: colors.secondary,
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 8,
  },
  gradeText: {
    ...typography.labelSm,
    color: colors.secondaryForeground,
  },

  // ---- Price ----
  priceSection: {
    gap: 8,
  },
  priceCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },
  priceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: 10,
  },
  priceLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  originalPrice: {
    ...typography.body,
    color: colors.mutedForeground,
    textDecorationLine: 'line-through',
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
  },
  resalePriceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingTop: 10,
  },
  resalePriceContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  resalePrice: {
    ...typography.priceLg,
    color: colors.foreground,
  },
  discountBadge: {
    backgroundColor: colors.destructive,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  discountText: {
    ...typography.caption,
    color: colors.white,
    fontWeight: '700',
  },

  // ---- Balance ----
  balanceSection: {
    gap: 8,
  },
  balanceCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
    alignItems: 'center',
    gap: 6,
  },
  balanceCardWarning: {
    borderColor: colors.warning,
    backgroundColor: '#FFFBEB',
  },
  balanceAmount: {
    ...typography.priceLg,
    color: colors.primary,
  },
  balanceAmountWarning: {
    color: colors.warning,
  },
  balanceWarning: {
    ...typography.bodySm,
    color: colors.warning,
  },

  // ---- Notices ----
  noticeSection: {
    gap: 8,
    marginBottom: 16,
  },
  noticeCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    gap: 8,
    borderWidth: 1,
    borderColor: colors.border,
  },
  noticeRow: {
    flexDirection: 'row',
    gap: 6,
  },
  noticeBullet: {
    ...typography.bodySm,
    color: colors.mutedForeground,
    lineHeight: 18,
  },
  noticeText: {
    ...typography.bodySm,
    color: colors.mutedForeground,
    flex: 1,
    lineHeight: 18,
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
