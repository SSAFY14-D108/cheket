import React, {useCallback, useMemo} from 'react';
import {View, Text, Image, ScrollView, StyleSheet} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {useTicketStore} from '../../../store/ticketStore';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {AppHeader} from '../../../shared/components/AppHeader';
import type {
  ResaleStackParamList,
  MainTabParamList,
} from '../../../core/navigation/types';
import {
  RESALE_ROUTES,
  TAB_ROUTES,
  MY_TICKETS_ROUTES,
} from '../../../core/navigation/routes';

type NavigationProp = NativeStackNavigationProp<ResaleStackParamList>;
type CompleteRouteProp = RouteProp<
  ResaleStackParamList,
  'ResalePurchaseComplete'
>;

// Mock blockchain data
const MOCK_TX_HASH = '0x7a3b...e4f1d92c';
const MOCK_ESCROW = '0xEscrow...Contract';
const NETWORK_NAME = 'Polygon';

export function ResalePurchaseCompleteScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<CompleteRouteProp>();
  const {resaleId, purchasedTicketId} = route.params;

  const ticket = useTicketStore(state =>
    state.tickets.find(t => t.id === purchasedTicketId),
  );

  const discountInfo = useMemo(() => {
    if (!ticket || !ticket.originalPrice) {
      return {hasDiscount: false, discountPercent: 0, savedAmount: 0};
    }
    // The ticket was purchased at originalPrice from the resale item;
    // we stored it as ticket.originalPrice. The actual resale price can be
    // inferred since the ticket was just created from the resale.
    // For display, we look at the ticket data.
    return {hasDiscount: false,
      discountPercent: 0,
      savedAmount: 0,
    };
  }, [ticket]);

  const handleGoToMyTickets = useCallback(() => {
    // Navigate to MyTickets tab
    const parent = navigation.getParent<NativeStackNavigationProp<MainTabParamList>>();
    if (parent) {
      parent.navigate(TAB_ROUTES.MY_TICKETS_TAB, {
        screen: MY_TICKETS_ROUTES.MY_TICKETS,
      });
    }
  }, [navigation]);

  const handleGoToResaleList = useCallback(() => {
    navigation.navigate(RESALE_ROUTES.RESALE_LIST);
  }, [navigation]);

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader title="구매 완료" />
        <View style={styles.notFound}>
          <Text style={styles.notFoundText}>
            구매 정보를 찾을 수 없습니다.
          </Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <AppHeader title="구매 완료" />

      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}>
        {/* Success Banner */}
        <View style={styles.successBanner}>
          <View style={styles.successIcon}>
            <Text style={styles.successCheckmark}>V</Text>
          </View>
          <Text style={styles.successTitle}>구매 완료!</Text>
          <Text style={styles.successDescription}>
            양도 티켓 구매가 완료되었습니다.
          </Text>
        </View>

        {/* Payment Summary Card */}
        <View style={styles.summaryCard}>
          <View style={styles.summaryHeader}>
            <Image
              source={{uri: ticket.poster}}
              style={styles.summaryPoster}
            />
            <View style={styles.summaryInfo}>
              <Text style={styles.summaryEventName} numberOfLines={2}>
                {ticket.eventName}
              </Text>
              <Text style={styles.summaryDetail}>{ticket.eventDate}</Text>
              <Text style={styles.summaryDetail}>{ticket.venue}</Text>
              <Text style={styles.summarySeat}>
                {ticket.seatLabel} / {ticket.grade}
              </Text>
            </View>
          </View>

          <View style={styles.summaryDivider} />

          {/* Price Info */}
          <View style={styles.priceSection}>
            <View style={styles.priceRow}>
              <Text style={styles.priceLabel}>정가</Text>
              <Text style={styles.originalPrice}>
                {ticket.originalPrice.toLocaleString()}원
              </Text>
            </View>
            <View style={styles.priceRow}>
              <Text style={styles.priceLabel}>결제 금액</Text>
              <Text style={styles.paidPrice}>
                {ticket.originalPrice.toLocaleString()}원
              </Text>
            </View>
          </View>
        </View>

        {/* Blockchain Info */}
        <View style={styles.blockchainSection}>
          <Text style={styles.sectionTitle}>블록체인 거래 정보</Text>
          <View style={styles.blockchainCard}>
            <View style={styles.blockchainRow}>
              <Text style={styles.blockchainLabel}>Escrow</Text>
              <Text style={styles.blockchainValue}>{MOCK_ESCROW}</Text>
            </View>
            <View style={styles.blockchainDivider} />
            <View style={styles.blockchainRow}>
              <Text style={styles.blockchainLabel}>TX Hash</Text>
              <Text style={styles.blockchainValue}>{MOCK_TX_HASH}</Text>
            </View>
            <View style={styles.blockchainDivider} />
            <View style={styles.blockchainRow}>
              <Text style={styles.blockchainLabel}>Network</Text>
              <View style={styles.networkBadge}>
                <Text style={styles.networkText}>{NETWORK_NAME}</Text>
              </View>
            </View>
          </View>
        </View>
      </ScrollView>

      {/* Bottom Buttons */}
      <View style={styles.bottomBar}>
        <PrimaryButton
          title="내 티켓 확인하기"
          onPress={handleGoToMyTickets}
        />
        <SecondaryButton
          title="목록으로 돌아가기"
          onPress={handleGoToResaleList}
          style={styles.secondaryBtn}
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
  scrollContent: {
    padding: 16,
    gap: 20,
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

  // ---- Success Banner ----
  successBanner: {
    alignItems: 'center',
    paddingVertical: 24,
  },
  successIcon: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colors.success,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  successCheckmark: {
    fontSize: 32,
    fontWeight: '700',
    color: colors.white,
  },
  successTitle: {
    ...typography.h1,
    color: colors.foreground,
    marginBottom: 6,
  },
  successDescription: {
    ...typography.body,
    color: colors.mutedForeground,
    textAlign: 'center',
  },

  // ---- Summary Card ----
  summaryCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
  },
  summaryHeader: {
    flexDirection: 'row',
  },
  summaryPoster: {
    width: 72,
    height: 96,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  summaryInfo: {
    flex: 1,
    marginLeft: 14,
    gap: 3,
  },
  summaryEventName: {
    ...typography.h3,
    color: colors.foreground,
  },
  summaryDetail: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  summarySeat: {
    ...typography.label,
    color: colors.foreground,
    marginTop: 4,
  },
  summaryDivider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: 14,
  },

  // ---- Price ----
  priceSection: {
    gap: 8,
  },
  priceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
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
  paidPrice: {
    ...typography.priceLg,
    color: colors.primary,
  },

  // ---- Blockchain ----
  blockchainSection: {
    gap: 8,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
  },
  blockchainCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },
  blockchainRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 6,
  },
  blockchainDivider: {
    height: 1,
    backgroundColor: colors.border,
  },
  blockchainLabel: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  blockchainValue: {
    ...typography.labelSm,
    color: colors.foreground,
  },
  networkBadge: {
    backgroundColor: '#7B3FE4',
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 8,
  },
  networkText: {
    ...typography.labelSm,
    color: colors.white,
  },

  // ---- Bottom ----
  bottomBar: {
    padding: 16,
    paddingBottom: 32,
    backgroundColor: colors.card,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    gap: 10,
  },
  secondaryBtn: {
    marginTop: 0,
  },
});
