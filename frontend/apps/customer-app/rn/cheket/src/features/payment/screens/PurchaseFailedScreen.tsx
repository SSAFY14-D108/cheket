import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {TAB_ROUTES} from '../../../core/navigation/routes';
import type {PurchaseFailureReason} from '../../../core/types/common';
import {colors} from '../../../core/theme/colors';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';
import {SecondaryButton} from '../../../shared/components/SecondaryButton';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList, 'PurchaseFailed'>;
type PurchaseFailedRouteProp = RouteProp<HomeStackParamList, 'PurchaseFailed'>;

type FailureConfig = {
  icon: string;
  message: string;
  description: string;
};

const FAILURE_MAP: Record<PurchaseFailureReason, FailureConfig> = {
  SOLD_OUT: {
    icon: '!',
    message: '선택한 좌석이 매진되었습니다',
    description: '다른 좌석을 선택하거나 다른 공연을 확인해보세요.',
  },
  LOCK_FAILED: {
    icon: '!',
    message: '좌석 잠금에 실패했습니다',
    description: '다른 사용자가 먼저 선택했을 수 있습니다. 다시 시도해주세요.',
  },
  LIMIT_EXCEEDED: {
    icon: '!',
    message: '1인당 구매 제한을 초과했습니다',
    description: '구매 가능한 수량을 확인 후 다시 시도해주세요.',
  },
  INSUFFICIENT_BALANCE: {
    icon: '!',
    message: 'CTK 잔액이 부족합니다',
    description: 'CTK를 충전한 후 다시 시도해주세요.',
  },
  NETWORK: {
    icon: '!',
    message: '네트워크 오류가 발생했습니다',
    description: '인터넷 연결을 확인하고 다시 시도해주세요.',
  },
};

export function PurchaseFailedScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<PurchaseFailedRouteProp>();
  const {reason} = route.params;

  const config = FAILURE_MAP[reason];

  const handleRetry = () => {
    navigation.goBack();
  };

  const handleGoHome = () => {
    navigation.getParent()?.navigate(TAB_ROUTES.HOME_TAB);
  };

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        {/* Error icon circle */}
        <View style={styles.iconCircle}>
          <Text style={styles.iconText}>{config.icon}</Text>
        </View>

        <Text style={styles.message}>{config.message}</Text>
        <Text style={styles.description}>{config.description}</Text>
      </View>

      <View style={styles.buttonContainer}>
        <PrimaryButton title="좌석 재선택" onPress={handleRetry} />
        <SecondaryButton
          title="홈으로 가기"
          onPress={handleGoHome}
          style={styles.homeButton}
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
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  iconCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colors.destructive,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 24,
  },
  iconText: {
    color: colors.white,
    fontSize: 36,
    fontWeight: '700',
  },
  message: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.foreground,
    textAlign: 'center',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    color: colors.mutedForeground,
    textAlign: 'center',
    lineHeight: 20,
  },
  buttonContainer: {
    padding: 16,
    gap: 10,
  },
  homeButton: {
    marginTop: 0,
  },
});
