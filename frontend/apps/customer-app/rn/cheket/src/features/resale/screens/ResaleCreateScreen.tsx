import React, {useCallback, useMemo, useState} from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  TextInput,
  StyleSheet,
} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';
import {useTicketStore} from '../../../store/ticketStore';
import {useResaleStore} from '../../../store/resaleStore';
import {useAuthStore} from '../../../store/authStore';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {AppHeader} from '../../../shared/components/AppHeader';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';

type NavigationProp = NativeStackNavigationProp<MyTicketsStackParamList>;
type CreateRouteProp = RouteProp<MyTicketsStackParamList, 'ResaleCreate'>;

const USAGE_NOTICES = [
  '양도 가격은 원래 구매 가격 이하로만 설정할 수 있습니다.',
  '양도 등록 후에는 티켓 사용이 불가합니다.',
  '구매자가 나타나기 전까지 양도를 취소할 수 있습니다.',
  '거래 완료 시 수수료가 차감된 금액이 지급됩니다.',
  '부정 거래 적발 시 이용이 제한될 수 있습니다.',
];

export function ResaleCreateScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<CreateRouteProp>();
  const {ticketId} = route.params;

  const ticket = useTicketStore(state =>
    state.tickets.find(t => t.id === ticketId),
  );
  const updateTicketStatus = useTicketStore(state => state.updateTicketStatus);
  const addResaleItem = useResaleStore(state => state.addResaleItem);
  const user = useAuthStore(state => state.user);

  const [priceText, setPriceText] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);

  const price = useMemo(() => {
    const parsed = parseInt(priceText, 10);
    return isNaN(parsed) ? 0 : parsed;
  }, [priceText]);

  const validation = useMemo(() => {
    if (!ticket) {
      return {isValid: false, message: '', discountPercent: 0};
    }
    if (price <= 0) {
      return {isValid: false, message: '', discountPercent: 0};
    }
    if (price > ticket.originalPrice) {
      return {
        isValid: false,
        message: '양도 가격은 원래 가격을 초과할 수 없습니다.',
        discountPercent: 0,
      };
    }
    const discountPercent =
      price < ticket.originalPrice
        ? Math.round(
            ((ticket.originalPrice - price) / ticket.originalPrice) * 100,
          )
        : 0;
    return {isValid: true, message: '', discountPercent};
  }, [price, ticket]);

  const handleSubmit = useCallback(() => {
    if (!ticket || !user || !validation.isValid) {
      return;
    }

    const newResaleItem = {
      id: `rs_${Date.now()}`,
      ticketId: ticket.id,
      eventName: ticket.eventName,
      eventDate: ticket.eventDate,
      venue: ticket.venue,
      poster: ticket.poster,
      seatLabel: ticket.seatLabel,
      grade: ticket.grade,
      originalPrice: ticket.originalPrice,
      resalePrice: price,
      sellerId: user.id,
    };

    addResaleItem(newResaleItem);
    updateTicketStatus(ticketId, {status: 'LISTED', resalePrice: price});
    setIsSuccess(true);
  }, [ticket, user, validation.isValid, price, addResaleItem, updateTicketStatus, ticketId]);

  const handleGoToMyTickets = useCallback(() => {
    navigation.navigate(MY_TICKETS_ROUTES.MY_TICKETS);
  }, [navigation]);

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="양도 등록"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.notFound}>
          <Text style={styles.notFoundText}>
            티켓 정보를 찾을 수 없습니다.
          </Text>
        </View>
      </View>
    );
  }

  // Success view
  if (isSuccess) {
    return (
      <View style={styles.container}>
        <AppHeader title="양도 등록" />
        <View style={styles.successContainer}>
          <View style={styles.successIcon}>
            <Text style={styles.successCheckmark}>V</Text>
          </View>
          <Text style={styles.successTitle}>등록 완료!</Text>
          <Text style={styles.successDescription}>
            양도 티켓이 마켓에 등록되었습니다.{'\n'}
            구매자가 나타나면 알려드릴게요.
          </Text>

          <View style={styles.successTicketCard}>
            <Image
              source={{uri: ticket.poster}}
              style={styles.successPoster}
            />
            <View style={styles.successTicketInfo}>
              <Text style={styles.successEventName} numberOfLines={1}>
                {ticket.eventName}
              </Text>
              <Text style={styles.successDetail}>
                {ticket.seatLabel} / {ticket.grade}
              </Text>
              <Text style={styles.successPrice}>
                {price.toLocaleString()}원
              </Text>
            </View>
          </View>

          <View style={styles.successButtonContainer}>
            <PrimaryButton
              title="내 티켓으로"
              onPress={handleGoToMyTickets}
            />
          </View>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <AppHeader
        title="양도 등록"
        showBack
        onBack={() => navigation.goBack()}
      />

      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Ticket Summary with Gradient */}
        <View style={styles.ticketSummary}>
          <Image
            source={{uri: ticket.poster}}
            style={styles.ticketPoster}
          />
          <View style={styles.posterOverlay} />
          <View style={styles.ticketOverlayContent}>
            <Text style={styles.overlayEventName} numberOfLines={2}>
              {ticket.eventName}
            </Text>
            <Text style={styles.overlayDetail}>{ticket.eventDate}</Text>
            <Text style={styles.overlayDetail}>{ticket.venue}</Text>
            <Text style={styles.overlaySeat}>
              {ticket.seatLabel} / {ticket.grade}
            </Text>
          </View>
        </View>

        <View style={styles.contentContainer}>
          {/* Original Price */}
          <View style={styles.originalPriceRow}>
            <Text style={styles.originalPriceLabel}>원래 가격</Text>
            <Text style={styles.originalPriceValue}>
              {ticket.originalPrice.toLocaleString()}원
            </Text>
          </View>

          {/* Price Input */}
          <View style={styles.priceInputSection}>
            <Text style={styles.sectionTitle}>양도 가격 설정</Text>
            <View
              style={[
                styles.inputWrapper,
                !validation.isValid && price > 0 && styles.inputWrapperError,
              ]}>
              <TextInput
                style={styles.priceInput}
                value={priceText}
                onChangeText={setPriceText}
                placeholder="양도 가격을 입력하세요"
                placeholderTextColor={colors.mutedForeground}
                keyboardType="number-pad"
                maxLength={10}
              />
              <Text style={styles.currencySuffix}>원</Text>
            </View>
            {!validation.isValid && price > 0 && (
              <Text style={styles.errorMessage}>{validation.message}</Text>
            )}
            {validation.isValid && validation.discountPercent > 0 && (
              <Text style={styles.discountMessage}>
                정가 대비 {validation.discountPercent}% 할인
              </Text>
            )}
          </View>

          {/* Usage Notices */}
          <View style={styles.noticeSection}>
            <Text style={styles.sectionTitle}>양도 안내</Text>
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
          title="양도 등록"
          onPress={handleSubmit}
          disabled={!validation.isValid}
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

  // ---- Ticket Summary ----
  ticketSummary: {
    position: 'relative',
    width: '100%',
    height: 200,
  },
  ticketPoster: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  posterOverlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0,0,0,0.55)',
  },
  ticketOverlayContent: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    padding: 16,
    gap: 3,
  },
  overlayEventName: {
    ...typography.h2,
    color: colors.white,
    marginBottom: 4,
  },
  overlayDetail: {
    ...typography.bodySm,
    color: 'rgba(255,255,255,0.8)',
  },
  overlaySeat: {
    ...typography.label,
    color: colors.white,
    marginTop: 4,
  },

  // ---- Content ----
  contentContainer: {
    padding: 16,
    gap: 20,
  },

  // ---- Original Price ----
  originalPriceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
  },
  originalPriceLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  originalPriceValue: {
    ...typography.price,
    color: colors.foreground,
  },

  // ---- Price Input ----
  priceInputSection: {
    gap: 8,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 14,
  },
  inputWrapperError: {
    borderColor: colors.destructive,
  },
  priceInput: {
    flex: 1,
    height: 48,
    ...typography.bodyLg,
    color: colors.foreground,
  },
  currencySuffix: {
    ...typography.label,
    color: colors.mutedForeground,
    marginLeft: 4,
  },
  errorMessage: {
    ...typography.bodySm,
    color: colors.destructive,
  },
  discountMessage: {
    ...typography.bodySm,
    color: colors.primary,
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

  // ---- Success View ----
  successContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  successIcon: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colors.success,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  successCheckmark: {
    fontSize: 32,
    fontWeight: '700',
    color: colors.white,
  },
  successTitle: {
    ...typography.h1,
    color: colors.foreground,
    marginBottom: 8,
  },
  successDescription: {
    ...typography.body,
    color: colors.mutedForeground,
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 24,
  },
  successTicketCard: {
    flexDirection: 'row',
    width: '100%',
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: 32,
  },
  successPoster: {
    width: 56,
    height: 56,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  successTicketInfo: {
    flex: 1,
    marginLeft: 12,
    gap: 2,
  },
  successEventName: {
    ...typography.label,
    color: colors.foreground,
  },
  successDetail: {
    ...typography.bodySm,
    color: colors.mutedForeground,
  },
  successPrice: {
    ...typography.price,
    color: colors.primary,
    marginTop: 2,
  },
  successButtonContainer: {
    width: '100%',
  },
});
