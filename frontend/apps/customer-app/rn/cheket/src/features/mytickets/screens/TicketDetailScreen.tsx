import React from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  StyleSheet,
  Alert,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {TicketStatusBadge} from '../../../shared/components/StatusBadge';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';

type Props = NativeStackScreenProps<MyTicketsStackParamList, 'TicketDetail'>;

export function TicketDetailScreen({route, navigation}: Props) {
  const {ticketId} = route.params;
  const tickets = useTicketStore(state => state.tickets);
  const updateTicketStatus = useTicketStore(state => state.updateTicketStatus);
  const ticket = tickets.find(t => t.id === ticketId);

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="티켓 상세"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>티켓을 찾을 수 없습니다.</Text>
        </View>
      </View>
    );
  }

  const handleCancelListing = () => {
    Alert.alert('리스팅 취소', '판매 등록을 취소하시겠습니까?', [
      {text: '아니오', style: 'cancel'},
      {
        text: '예',
        onPress: () => {
          updateTicketStatus(ticket.id, {
            status: 'SOLD',
            resalePrice: undefined,
          });
          navigation.goBack();
        },
      },
    ]);
  };

  return (
    <View style={styles.container}>
      <AppHeader
        title="티켓 상세"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.posterContainer}>
          <Image source={{uri: ticket.poster}} style={styles.poster} />
          <View style={styles.badgeOverlay}>
            <TicketStatusBadge status={ticket.status} />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.eventName}>{ticket.eventName}</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>날짜</Text>
            <Text style={styles.infoValue}>{ticket.eventDate}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>장소</Text>
            <Text style={styles.infoValue}>{ticket.venue}</Text>
          </View>
        </View>

        <View style={styles.divider} />

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>좌석 정보</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>좌석</Text>
            <Text style={styles.infoValue}>{ticket.seatLabel}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>등급</Text>
            <Text style={styles.infoValue}>{ticket.grade}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>원가</Text>
            <Text style={styles.infoValue}>
              {ticket.originalPrice.toLocaleString()}원
            </Text>
          </View>
          {ticket.status === 'LISTED' && ticket.resalePrice != null && (
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>판매가</Text>
              <Text style={[styles.infoValue, styles.resalePrice]}>
                {ticket.resalePrice.toLocaleString()}원
              </Text>
            </View>
          )}
        </View>

        <View style={styles.divider} />

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>NFT 정보</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Token ID</Text>
            <Text style={styles.infoValueMono}>#TKT-{ticket.id.slice(-3)}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Owner</Text>
            <Text style={styles.infoValueMono}>0x3a9F...dE42</Text>
          </View>
        </View>

        <View style={styles.divider} />

        <View style={styles.actionContainer}>
          {ticket.status === 'SOLD' && (
            <>
              <PrimaryButton
                title="QR 체크인"
                onPress={() =>
                  navigation.navigate(MY_TICKETS_ROUTES.QR_CHECKIN, {ticketId})
                }
              />
              <SecondaryButton
                title="양도하기"
                onPress={() =>
                  navigation.navigate(MY_TICKETS_ROUTES.TRANSFER, {ticketId})
                }
                style={styles.buttonSpacing}
              />
              <SecondaryButton
                title="판매하기"
                onPress={() =>
                  navigation.navigate(MY_TICKETS_ROUTES.RESALE_CREATE, {
                    ticketId,
                  })
                }
                style={styles.buttonSpacing}
              />
            </>
          )}
          {ticket.status === 'LISTED' && (
            <PrimaryButton
              title="리스팅 취소"
              onPress={handleCancelListing}
            />
          )}
          {ticket.status === 'USED' && (
            <PrimaryButton
              title="체크인 완료"
              onPress={() => {}}
              disabled
            />
          )}
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  scrollContent: {
    paddingBottom: 40,
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  posterContainer: {
    width: '100%',
    aspectRatio: 2 / 1,
    position: 'relative',
  },
  poster: {
    width: '100%',
    height: '100%',
    backgroundColor: colors.muted,
  },
  badgeOverlay: {
    position: 'absolute',
    top: 12,
    left: 12,
  },
  section: {
    paddingHorizontal: 16,
    paddingVertical: 16,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
    marginBottom: 12,
  },
  eventName: {
    ...typography.h2,
    color: colors.foreground,
    marginBottom: 12,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 6,
  },
  infoLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  infoValue: {
    ...typography.body,
    color: colors.foreground,
    fontWeight: '600',
  },
  infoValueMono: {
    ...typography.bodySm,
    color: colors.foreground,
    fontFamily: 'monospace',
  },
  resalePrice: {
    color: colors.primary,
  },
  divider: {
    height: 8,
    backgroundColor: colors.muted,
  },
  actionContainer: {
    paddingHorizontal: 16,
    paddingVertical: 16,
  },
  buttonSpacing: {
    marginTop: 10,
  },
});
