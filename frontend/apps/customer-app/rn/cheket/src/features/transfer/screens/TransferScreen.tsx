import React, {useState, useCallback} from 'react';
import {
  View,
  Text,
  TextInput,
  Image,
  ScrollView,
  StyleSheet,
} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {MOCK_PHONE_BOOK} from '../../../core/data/mockPhoneBook';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';

type Props = NativeStackScreenProps<MyTicketsStackParamList, 'Transfer'>;

export function TransferScreen({route, navigation}: Props) {
  const {ticketId} = route.params;
  const tickets = useTicketStore(state => state.tickets);
  const updateTicketStatus = useTicketStore(state => state.updateTicketStatus);
  const ticket = tickets.find(t => t.id === ticketId);

  const [phone, setPhone] = useState('');
  const [verifiedName, setVerifiedName] = useState<string | null>(null);
  const [verifyError, setVerifyError] = useState<string | null>(null);
  const [isVerified, setIsVerified] = useState(false);

  const formatPhoneNumber = useCallback((text: string) => {
    const digits = text.replace(/\D/g, '');
    if (digits.length <= 3) {
      return digits;
    }
    if (digits.length <= 7) {
      return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    }
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7, 11)}`;
  }, []);

  const handlePhoneChange = (text: string) => {
    const formatted = formatPhoneNumber(text);
    setPhone(formatted);
    setVerifiedName(null);
    setVerifyError(null);
    setIsVerified(false);
  };

  const handleVerify = () => {
    const name = MOCK_PHONE_BOOK[phone];
    if (name) {
      setVerifiedName(name);
      setVerifyError(null);
      setIsVerified(true);
    } else {
      setVerifiedName(null);
      setVerifyError('등록되지 않은 사용자입니다');
      setIsVerified(false);
    }
  };

  const handleTransfer = () => {
    if (!isVerified || !verifiedName) {
      return;
    }
    updateTicketStatus(ticketId, {status: 'USED'});
    navigation.navigate(MY_TICKETS_ROUTES.TRANSFER_COMPLETE, {
      ticketId,
      recipientName: verifiedName,
    });
  };

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="양도하기"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.centered}>
          <Text style={styles.errorText}>티켓을 찾을 수 없습니다.</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <AppHeader
        title="양도하기"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.ticketSummary}>
          <Image source={{uri: ticket.poster}} style={styles.thumbnail} />
          <View style={styles.ticketInfo}>
            <Text style={styles.ticketName} numberOfLines={2}>
              {ticket.eventName}
            </Text>
            <Text style={styles.ticketDetail}>
              {ticket.seatLabel} / {ticket.grade}
            </Text>
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>받는 사람</Text>
          <View style={styles.phoneRow}>
            <TextInput
              style={styles.phoneInput}
              value={phone}
              onChangeText={handlePhoneChange}
              placeholder="010-XXXX-XXXX"
              placeholderTextColor={colors.mutedForeground}
              keyboardType="phone-pad"
              maxLength={13}
            />
            <SecondaryButton
              title="확인"
              onPress={handleVerify}
              disabled={phone.length < 13}
              style={styles.verifyButton}
            />
          </View>

          {isVerified && verifiedName && (
            <View style={styles.verifiedCard}>
              <Text style={styles.verifiedLabel}>확인된 사용자</Text>
              <Text style={styles.verifiedName}>{verifiedName}</Text>
            </View>
          )}

          {verifyError && (
            <Text style={styles.errorMessage}>{verifyError}</Text>
          )}
        </View>

        <View style={styles.warningBox}>
          <Text style={styles.warningTitle}>주의사항</Text>
          <Text style={styles.warningText}>
            양도는 되돌릴 수 없습니다. 양도 후에는 해당 티켓에 대한 모든 권리가
            상대방에게 이전됩니다. 신중하게 진행해 주세요.
          </Text>
        </View>

        <View style={styles.buttonContainer}>
          <PrimaryButton
            title="양도하기"
            onPress={handleTransfer}
            disabled={!isVerified}
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
  centered: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scrollContent: {
    paddingBottom: 40,
  },
  errorText: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  ticketSummary: {
    flexDirection: 'row',
    backgroundColor: colors.card,
    padding: 16,
    margin: 16,
    borderRadius: 12,
  },
  thumbnail: {
    width: 56,
    height: 74,
    borderRadius: 8,
    backgroundColor: colors.muted,
  },
  ticketInfo: {
    flex: 1,
    marginLeft: 12,
    justifyContent: 'center',
  },
  ticketName: {
    ...typography.h3,
    color: colors.foreground,
    marginBottom: 4,
  },
  ticketDetail: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  section: {
    paddingHorizontal: 16,
    marginBottom: 16,
  },
  sectionTitle: {
    ...typography.h3,
    color: colors.foreground,
    marginBottom: 12,
  },
  phoneRow: {
    flexDirection: 'row',
    gap: 8,
  },
  phoneInput: {
    flex: 1,
    height: 48,
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 16,
    ...typography.body,
    color: colors.foreground,
  },
  verifyButton: {
    width: 72,
  },
  verifiedCard: {
    marginTop: 12,
    padding: 16,
    backgroundColor: colors.card,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.success,
  },
  verifiedLabel: {
    ...typography.bodySm,
    color: colors.success,
    marginBottom: 4,
  },
  verifiedName: {
    ...typography.h3,
    color: colors.foreground,
  },
  errorMessage: {
    ...typography.bodySm,
    color: colors.danger,
    marginTop: 8,
  },
  warningBox: {
    marginHorizontal: 16,
    padding: 16,
    backgroundColor: '#FFF8E1',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.warning,
    marginBottom: 24,
  },
  warningTitle: {
    ...typography.label,
    color: colors.warning,
    marginBottom: 4,
  },
  warningText: {
    ...typography.bodySm,
    color: '#7A6200',
    lineHeight: 18,
  },
  buttonContainer: {
    paddingHorizontal: 16,
  },
});
