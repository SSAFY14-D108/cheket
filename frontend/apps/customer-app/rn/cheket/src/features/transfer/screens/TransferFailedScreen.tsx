import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {MyTicketsStackParamList} from '../../../core/navigation/types';
import {MY_TICKETS_ROUTES} from '../../../core/navigation/routes';
import type {TransferFailureReason} from '../../../core/types/common';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';
import {colors} from '../../../core/theme/colors';
import {typography} from '../../../core/theme/typography';

type Props = NativeStackScreenProps<MyTicketsStackParamList, 'TransferFailed'>;

const FAILURE_MESSAGES: Record<TransferFailureReason, string> = {
  LIMIT_EXCEEDED: '양도 횟수 제한을 초과했습니다.',
  USER_NOT_FOUND: '받는 사람을 찾을 수 없습니다.',
  NETWORK: '네트워크 오류가 발생했습니다. 다시 시도해 주세요.',
};

export function TransferFailedScreen({route, navigation}: Props) {
  const {reason} = route.params;

  const handleRetry = () => {
    navigation.goBack();
  };

  const handleGoToMyTickets = () => {
    navigation.navigate(MY_TICKETS_ROUTES.MY_TICKETS);
  };

  return (
    <View style={styles.container}>
      <AppHeader title="양도 실패" />
      <View style={styles.content}>
        <View style={styles.iconSection}>
          <View style={styles.errorIcon}>
            <Text style={styles.errorIconText}>!</Text>
          </View>
          <Text style={styles.failureTitle}>양도에 실패했습니다</Text>
          <Text style={styles.failureMessage}>
            {FAILURE_MESSAGES[reason]}
          </Text>
        </View>

        <View style={styles.infoCard}>
          <Text style={styles.infoLabel}>오류 코드</Text>
          <Text style={styles.infoValue}>{reason}</Text>
        </View>

        <View style={styles.buttonContainer}>
          <PrimaryButton title="다시 시도" onPress={handleRetry} />
          <SecondaryButton
            title="내 티켓으로"
            onPress={handleGoToMyTickets}
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
  content: {
    flex: 1,
    paddingHorizontal: 16,
  },
  iconSection: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  errorIcon: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: colors.danger,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  errorIconText: {
    fontSize: 36,
    fontWeight: '700',
    color: colors.white,
  },
  failureTitle: {
    ...typography.h2,
    color: colors.foreground,
    marginBottom: 8,
  },
  failureMessage: {
    ...typography.body,
    color: colors.mutedForeground,
    textAlign: 'center',
    lineHeight: 22,
  },
  infoCard: {
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  infoLabel: {
    ...typography.body,
    color: colors.mutedForeground,
  },
  infoValue: {
    ...typography.bodySm,
    color: colors.foreground,
    fontFamily: 'monospace',
  },
  buttonContainer: {
    marginTop: 'auto',
    paddingBottom: 32,
  },
  buttonSpacing: {
    marginTop: 10,
  },
});
