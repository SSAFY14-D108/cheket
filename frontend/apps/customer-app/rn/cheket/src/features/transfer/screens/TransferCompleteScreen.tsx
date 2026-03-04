import React from 'react';
import {View, Text, ScrollView, StyleSheet} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';

type Props = NativeStackScreenProps<
  MyTicketsStackParamList,
  'TransferComplete'
>;

export function TransferCompleteScreen({route, navigation}: Props) {
  const {ticketId, recipientName} = route.params;
  const tickets = useTicketStore(state => state.tickets);
  const ticket = tickets.find(t => t.id === ticketId);

  const handleGoToMyTickets = () => {
    navigation.navigate(MY_TICKETS_ROUTES.MY_TICKETS);
  };

  const handleGoHome = () => {
    navigation.getParent()?.navigate('HomeTab');
  };

  return (
    <View style={styles.container}>
      <AppHeader title="양도 완료" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.successSection}>
          <View style={styles.successIcon}>
            <Text style={styles.successIconText}>V</Text>
          </View>
          <Text style={styles.successTitle}>양도가 완료되었습니다!</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>받는 사람</Text>
          <View style={styles.recipientRow}>
            <Text style={styles.recipientName}>{recipientName}</Text>
          </View>
        </View>

        {ticket && (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>양도 티켓 정보</Text>
            <View style={styles.detailGrid}>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>공연</Text>
                <Text style={styles.detailValue} numberOfLines={1}>
                  {ticket.eventName}
                </Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>좌석</Text>
                <Text style={styles.detailValue}>{ticket.seatLabel}</Text>
              </View>
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>등급</Text>
                <Text style={styles.detailValue}>{ticket.grade}</Text>
              </View>
            </View>
          </View>
        )}

        <View style={styles.card}>
          <Text style={styles.cardTitle}>트랜잭션</Text>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Tx Hash</Text>
            <Text style={styles.hashValue}>0x7f3a...b2c1</Text>
          </View>
        </View>

        <View style={styles.buttonContainer}>
          <PrimaryButton
            title="내 티켓 확인하기"
            onPress={handleGoToMyTickets}
          />
          <SecondaryButton
            title="홈으로 가기"
            onPress={handleGoHome}
            style={styles.buttonSpacing}
          />
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
  successSection: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  successIcon: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.success,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  successIconText: {
    fontSize: 36,
    fontWeight: '700',
    color: colors.white,
  },
  successTitle: {
    ...typography.h2,
    color: colors.foreground,
  },
  card: {
    backgroundColor: colors.card,
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 12,
    padding: 16,
  },
  cardTitle: {
    ...typography.label,
    color: colors.mutedForeground,
    marginBottom: 12,
  },
  recipientRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  recipientName: {
    ...typography.h3,
    color: colors.foreground,
  },
  detailGrid: {
    gap: 8,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 4,
  },
  detailLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  detailValue: {
    ...typography.body,
    color: colors.foreground,
    fontWeight: '600',
    flexShrink: 1,
    textAlign: 'right',
  },
  hashValue: {
    ...typography.bodySm,
    color: colors.foreground,
    fontFamily: 'monospace',
  },
  buttonContainer: {
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  buttonSpacing: {
    marginTop: 10,
  },
});
