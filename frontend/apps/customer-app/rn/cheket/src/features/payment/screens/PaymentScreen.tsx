import React, {useState, useMemo} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Image,
  Pressable,
} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {HOME_ROUTES} from '../../../core/navigation/routes';
import {TAB_ROUTES} from '../../../core/navigation/routes';
import {colors} from '../../../core/theme/colors';
import {MOCK_EVENTS, generateSeats} from '../../../core/data/mockData';
import {useAuthStore} from '../../../store/authStore';
import {useTicketStore} from '../../../store/ticketStore';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import type {Seat, Ticket} from '../../../core/types/common';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList, 'Payment'>;
type PaymentRouteProp = RouteProp<HomeStackParamList, 'Payment'>;

export function PaymentScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<PaymentRouteProp>();
  const {eventId, seatIds} = route.params;

  const user = useAuthStore(state => state.user);
  const addTicket = useTicketStore(state => state.addTicket);

  const event = MOCK_EVENTS.find(e => e.id === eventId);

  const [isApproved, setIsApproved] = useState(false);
  const [isPurchased, setIsPurchased] = useState(false);

  // Look up the actual seat objects from generated seats
  const selectedSeats: Seat[] = useMemo(() => {
    if (!event) {
      return [];
    }
    const allSeats = generateSeats(eventId);
    return allSeats.filter(s => seatIds.includes(s.id));
  }, [eventId, seatIds, event]);

  const totalPrice = useMemo(
    () => selectedSeats.reduce((sum, s) => sum + s.price, 0),
    [selectedSeats],
  );

  const ctkBalance = user?.ctkBalance ?? 0;
  const hasSufficientBalance = ctkBalance >= totalPrice;

  const handlePurchase = () => {
    if (!event || !user) {
      return;
    }

    // Create tickets for each selected seat
    selectedSeats.forEach((seat, idx) => {
      const ticket: Ticket = {
        id: `tkt_new_${Date.now()}_${idx}`,
        eventId: event.id,
        eventName: event.name,
        eventDate: event.date,
        venue: event.venue,
        poster: event.poster,
        seatId: seat.id,
        seatLabel: `${seat.row}열 ${seat.number}번`,
        grade: seat.grade,
        originalPrice: seat.price,
        status: 'SOLD',
      };
      addTicket(ticket);
    });

    setIsPurchased(true);
  };

  const handleGoToMyTickets = () => {
    // Navigate to MyTickets tab
    navigation.getParent()?.navigate(TAB_ROUTES.MY_TICKETS_TAB);
  };

  // ---- Success view ----
  if (isPurchased) {
    return (
      <View style={styles.container}>
        <View style={styles.successContent}>
          {/* Checkmark circle */}
          <View style={styles.checkCircle}>
            <Text style={styles.checkMark}>{'✓'}</Text>
          </View>

          <Text style={styles.successTitle}>구매가 완료되었습니다!</Text>
          <Text style={styles.successSubtitle}>
            티켓이 지갑에 추가되었습니다
          </Text>

          {/* Ticket info */}
          <View style={styles.successCard}>
            <Text style={styles.successEventName}>{event?.name}</Text>
            <Text style={styles.successMeta}>{event?.date}</Text>
            <Text style={styles.successMeta}>{event?.venue}</Text>
            <View style={styles.successDivider} />
            {selectedSeats.map(seat => (
              <Text key={seat.id} style={styles.successSeat}>
                {seat.grade} / {seat.row}열 {seat.number}번
              </Text>
            ))}
            <View style={styles.successDivider} />
            <Text style={styles.successTotal}>
              총 {totalPrice.toLocaleString('ko-KR')} CTK
            </Text>
          </View>

          <View style={styles.successButtons}>
            <PrimaryButton
              title="내 티켓 보기"
              onPress={handleGoToMyTickets}
            />
            <SecondaryButton
              title="홈으로 가기"
              onPress={() => navigation.popToTop()}
              style={styles.secondaryButtonSpacing}
            />
          </View>
        </View>
      </View>
    );
  }

  // ---- Payment view ----
  return (
    <View style={styles.container}>
      <AppHeader
        title="결제"
        showBack
        onBack={() => navigation.goBack()}
      />

      <ScrollView
        style={styles.scrollArea}
        contentContainerStyle={styles.scrollContent}>
        {/* Event summary card */}
        <View style={styles.card}>
          <View style={styles.eventRow}>
            <Image source={{uri: event?.poster}} style={styles.poster} />
            <View style={styles.eventDetails}>
              <Text style={styles.eventName}>{event?.name}</Text>
              <Text style={styles.eventMeta}>{event?.date}</Text>
              <Text style={styles.eventMeta}>{event?.venue}</Text>
            </View>
          </View>
        </View>

        {/* Selected seats list */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>선택 좌석</Text>
          {selectedSeats.map(seat => (
            <View key={seat.id} style={styles.seatRow}>
              <View>
                <Text style={styles.seatLabel}>
                  {seat.grade} / {seat.row}열 {seat.number}번
                </Text>
              </View>
              <Text style={styles.seatPrice}>
                {seat.price.toLocaleString('ko-KR')} CTK
              </Text>
            </View>
          ))}
          <View style={styles.divider} />
          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>총 결제 금액</Text>
            <Text style={styles.totalPrice}>
              {totalPrice.toLocaleString('ko-KR')} CTK
            </Text>
          </View>
        </View>

        {/* CTK wallet balance */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>CTK 지갑</Text>
          <View style={styles.walletRow}>
            <Text style={styles.walletLabel}>보유 잔액</Text>
            <Text
              style={[
                styles.walletBalance,
                !hasSufficientBalance && styles.walletInsufficient,
              ]}>
              {ctkBalance.toLocaleString('ko-KR')} CTK
            </Text>
          </View>
          {!hasSufficientBalance && (
            <Text style={styles.insufficientNotice}>
              잔액이 부족합니다. 충전 후 다시 시도해주세요.
            </Text>
          )}
          <View style={styles.walletRow}>
            <Text style={styles.walletLabel}>결제 후 잔액</Text>
            <Text style={styles.walletAfter}>
              {hasSufficientBalance
                ? (ctkBalance - totalPrice).toLocaleString('ko-KR')
                : '-'}{' '}
              CTK
            </Text>
          </View>
        </View>

        {/* Approval step */}
        <View style={styles.card}>
          <Text style={styles.cardTitle}>결제 승인</Text>
          <Pressable
            style={[
              styles.approvalButton,
              isApproved && styles.approvalButtonActive,
            ]}
            onPress={() => setIsApproved(!isApproved)}>
            <View
              style={[
                styles.checkbox,
                isApproved && styles.checkboxChecked,
              ]}>
              {isApproved && <Text style={styles.checkboxMark}>{'✓'}</Text>}
            </View>
            <Text style={styles.approvalText}>
              구매 내용을 확인하였으며, 결제에 동의합니다
            </Text>
          </Pressable>
        </View>
      </ScrollView>

      {/* Bottom purchase button */}
      <View style={styles.bottomBar}>
        <PrimaryButton
          title="구매 확정"
          onPress={handlePurchase}
          disabled={!isApproved || !hasSufficientBalance}
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
  scrollArea: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 100,
    gap: 12,
  },

  /* Cards */
  card: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 12,
  },

  /* Event summary */
  eventRow: {
    flexDirection: 'row',
    gap: 12,
  },
  poster: {
    width: 60,
    height: 84,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  eventDetails: {
    flex: 1,
    justifyContent: 'center',
  },
  eventName: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 4,
  },
  eventMeta: {
    fontSize: 13,
    color: colors.mutedForeground,
    marginTop: 2,
  },

  /* Seats list */
  seatRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  seatLabel: {
    fontSize: 14,
    color: colors.foreground,
  },
  seatPrice: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: 8,
  },
  totalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  totalLabel: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
  },
  totalPrice: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.primary,
  },

  /* Wallet */
  walletRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 6,
  },
  walletLabel: {
    fontSize: 14,
    color: colors.mutedForeground,
  },
  walletBalance: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
  },
  walletInsufficient: {
    color: colors.destructive,
  },
  walletAfter: {
    fontSize: 14,
    color: colors.mutedForeground,
  },
  insufficientNotice: {
    fontSize: 12,
    color: colors.destructive,
    marginTop: 4,
    marginBottom: 4,
  },

  /* Approval */
  approvalButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 8,
  },
  approvalButtonActive: {},
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 4,
    borderWidth: 2,
    borderColor: colors.border,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  checkboxMark: {
    color: colors.white,
    fontSize: 14,
    fontWeight: '700',
  },
  approvalText: {
    flex: 1,
    fontSize: 13,
    color: colors.foreground,
    lineHeight: 18,
  },

  /* Bottom bar */
  bottomBar: {
    padding: 16,
    backgroundColor: colors.card,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },

  /* Success view */
  successContent: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  checkCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  checkMark: {
    color: colors.white,
    fontSize: 36,
    fontWeight: '700',
  },
  successTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 6,
  },
  successSubtitle: {
    fontSize: 14,
    color: colors.mutedForeground,
    marginBottom: 24,
  },
  successCard: {
    width: '100%',
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 20,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 24,
  },
  successEventName: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 4,
  },
  successMeta: {
    fontSize: 13,
    color: colors.mutedForeground,
    marginTop: 2,
  },
  successDivider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: 12,
  },
  successSeat: {
    fontSize: 14,
    color: colors.foreground,
    marginBottom: 4,
  },
  successTotal: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.primary,
    textAlign: 'right',
  },
  successButtons: {
    width: '100%',
    gap: 10,
  },
  secondaryButtonSpacing: {
    marginTop: 0,
  },
});
