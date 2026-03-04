import React, {useState, useEffect, useRef, useCallback} from 'react';
import {View, Text, StyleSheet} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';

type Props = NativeStackScreenProps<MyTicketsStackParamList, 'QrCheckin'>;

const INITIAL_TIMER = 30;
const MOCK_QUEUE_POSITION = 42;

export function QrCheckinScreen({route, navigation}: Props) {
  const {ticketId} = route.params;
  const tickets = useTicketStore(state => state.tickets);
  const updateTicketStatus = useTicketStore(state => state.updateTicketStatus);
  const ticket = tickets.find(t => t.id === ticketId);

  const [timer, setTimer] = useState(INITIAL_TIMER);
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const startTimer = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
    setTimer(INITIAL_TIMER);
    intervalRef.current = setInterval(() => {
      setTimer(prev => {
        if (prev <= 1) {
          if (intervalRef.current) {
            clearInterval(intervalRef.current);
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  }, []);

  useEffect(() => {
    startTimer();
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [startTimer]);

  const handleRefresh = () => {
    startTimer();
  };

  const handleCheckin = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
    const now = new Date();
    const dateStr = `${now.getFullYear()}.${String(now.getMonth() + 1).padStart(2, '0')}.${String(now.getDate()).padStart(2, '0')}`;
    updateTicketStatus(ticketId, {
      status: 'USED',
      attendedDate: dateStr,
    });
    setIsCheckedIn(true);
  };

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="QR 체크인"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.centered}>
          <Text style={styles.errorText}>티켓을 찾을 수 없습니다.</Text>
        </View>
      </View>
    );
  }

  if (isCheckedIn) {
    return (
      <View style={styles.container}>
        <AppHeader title="QR 체크인" />
        <View style={styles.centered}>
          <View style={styles.successIcon}>
            <Text style={styles.successIconText}>V</Text>
          </View>
          <Text style={styles.successTitle}>체크인 완료!</Text>
          <Text style={styles.successSubtitle}>
            {ticket.eventName}에 입장되었습니다.
          </Text>
          <View style={styles.successButtonContainer}>
            <PrimaryButton
              title="내 티켓으로"
              onPress={() =>
                navigation.navigate(MY_TICKETS_ROUTES.MY_TICKETS)
              }
            />
          </View>
        </View>
      </View>
    );
  }

  const formatTimer = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  return (
    <View style={styles.container}>
      <AppHeader
        title="QR 체크인"
        showBack
        onBack={() => navigation.goBack()}
      />
      <View style={styles.content}>
        <View style={styles.qrSection}>
          <View style={styles.qrCode}>
            <Text style={styles.qrText}>QR</Text>
            <Text style={styles.qrTicketId}>{ticket.id}</Text>
          </View>
          <Text
            style={[
              styles.timerText,
              timer <= 10 && styles.timerTextWarning,
            ]}>
            {formatTimer(timer)}
          </Text>
          {timer === 0 && (
            <Text style={styles.expiredText}>QR 코드가 만료되었습니다.</Text>
          )}
        </View>

        <View style={styles.ticketInfo}>
          <Text style={styles.ticketName}>{ticket.eventName}</Text>
          <Text style={styles.ticketDetail}>
            {ticket.seatLabel} / {ticket.grade}
          </Text>
          <Text style={styles.ticketDetail}>{ticket.eventDate}</Text>
        </View>

        <View style={styles.queueSection}>
          <Text style={styles.queueLabel}>대기 순번</Text>
          <Text style={styles.queuePosition}>{MOCK_QUEUE_POSITION}번</Text>
        </View>

        <View style={styles.buttonContainer}>
          <SecondaryButton title="새로고침" onPress={handleRefresh} />
          <PrimaryButton
            title="체크인"
            onPress={handleCheckin}
            disabled={timer === 0}
            style={styles.buttonSpacing}
          />
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  content: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 24,
  },
  errorText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  qrSection: {
    alignItems: 'center',
    marginBottom: 24,
  },
  qrCode: {
    width: 200,
    height: 200,
    borderWidth: 3,
    borderColor: colors.foreground,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.card,
    marginBottom: 16,
  },
  qrText: {
    ...typography.priceLg,
    color: colors.foreground,
  },
  qrTicketId: {
    ...typography.bodySm,
    color: colors.mutedForeground,
    marginTop: 4,
  },
  timerText: {
    ...typography.h1,
    color: colors.foreground,
  },
  timerTextWarning: {
    color: colors.danger,
  },
  expiredText: {
    ...typography.bodySm,
    color: colors.danger,
    marginTop: 4,
  },
  ticketInfo: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginBottom: 16,
  },
  ticketName: {
    ...typography.h3,
    color: colors.foreground,
    marginBottom: 4,
  },
  ticketDetail: {
    ...typography.body,
    color: colors.mutedForeground,
    marginTop: 2,
  },
  queueSection: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  queueLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  queuePosition: {
    ...typography.h2,
    color: colors.primary,
  },
  buttonContainer: {
    marginTop: 'auto',
    paddingBottom: 32,
  },
  buttonSpacing: {
    marginTop: 10,
  },
  successIcon: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.success,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  successIconText: {
    fontSize: 36,
    fontWeight: '700',
    color: colors.white,
  },
  successTitle: {
    ...typography.h1,
    color: colors.foreground,
    marginBottom: 8,
  },
  successSubtitle: {
    ...typography.body,
    color: colors.mutedForeground,
    textAlign: 'center',
  },
  successButtonContainer: {
    width: '100%',
    marginTop: 32,
  },
});
