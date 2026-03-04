import React, {useCallback, useState} from 'react';
import {
  View,
  Text,
  FlatList,
  Pressable,
  Alert,
  StyleSheet,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import {useAuthStore} from '../../../store/authStore';
import {MOCK_TRANSACTIONS} from '../../../core/data/mockData';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';
import type {Transaction} from '../../../core/types/common';

const CTK_TO_KRW = 1.2;
const TOP_UP_AMOUNTS = [5000, 10000, 30000, 50000] as const;

export function WalletScreen(): React.JSX.Element {
  const navigation = useNavigation();
  const user = useAuthStore(state => state.user);
  const topUpCTK = useAuthStore(state => state.topUpCTK);
  const [lastTopUp, setLastTopUp] = useState<number | null>(null);

  const ctkBalance = user?.ctkBalance ?? 0;
  const krwValue = Math.round(ctkBalance * CTK_TO_KRW);

  const handleTopUp = useCallback(
    (amount: number) => {
      topUpCTK(amount);
      setLastTopUp(amount);
      Alert.alert('충전 완료', `${amount.toLocaleString()} CTK가 충전되었습니다.`);
      setTimeout(() => setLastTopUp(null), 1500);
    },
    [topUpCTK],
  );

  const handleCopyWallet = () => {
    Alert.alert('복사 완료', '지갑 주소가 복사되었습니다.');
  };

  const renderTransaction = useCallback(
    ({item}: {item: Transaction}) => {
      const isIn = item.type === 'in';
      return (
        <View style={styles.txRow}>
          <View
            style={[
              styles.txIcon,
              {backgroundColor: isIn ? colors.success + '18' : colors.muted},
            ]}>
            <Text
              style={[
                styles.txArrow,
                {color: isIn ? colors.success : colors.mutedForeground},
              ]}>
              {isIn ? 'v' : '^'}
            </Text>
          </View>
          <View style={styles.txInfo}>
            <Text style={styles.txLabel}>{item.label}</Text>
            <Text style={styles.txDate}>{item.date}</Text>
          </View>
          <Text
            style={[
              styles.txAmount,
              {color: isIn ? colors.success : colors.foreground},
            ]}>
            {isIn ? '+' : '-'}
            {item.amount.toLocaleString()} CTK
          </Text>
        </View>
      );
    },
    [],
  );

  const keyExtractor = useCallback((item: Transaction) => item.id, []);

  const ListHeader = (
    <>
      {/* Balance Card */}
      <View style={styles.balanceCard}>
        <Text style={styles.balanceLabel}>CTK 잔액</Text>
        <Text style={styles.balanceAmount}>
          {ctkBalance.toLocaleString()} CTK
        </Text>
        <Text style={styles.balanceKrw}>
          ~ {krwValue.toLocaleString()} KRW
        </Text>
        <Pressable style={styles.walletAddressRow} onPress={handleCopyWallet}>
          <Text style={styles.walletAddress} numberOfLines={1}>
            {user?.walletAddress ?? '-'}
          </Text>
          <Text style={styles.copyText}>복사</Text>
        </Pressable>
      </View>

      {/* Top-up Section */}
      <View style={styles.topUpSection}>
        <Text style={styles.sectionTitle}>테스트 CTK 충전</Text>
        <View style={styles.topUpGrid}>
          {TOP_UP_AMOUNTS.map(amount => (
            <Pressable
              key={amount}
              style={[
                styles.topUpButton,
                lastTopUp === amount && styles.topUpButtonActive,
              ]}
              onPress={() => handleTopUp(amount)}>
              <Text
                style={[
                  styles.topUpText,
                  lastTopUp === amount && styles.topUpTextActive,
                ]}>
                {amount.toLocaleString()} CTK
              </Text>
            </Pressable>
          ))}
        </View>
      </View>

      {/* Transaction History Header */}
      <View style={styles.txHeader}>
        <Text style={styles.sectionTitle}>거래 내역</Text>
      </View>
    </>
  );

  return (
    <View style={styles.container}>
      <AppHeader
        title="지갑 관리"
        showBack
        onBack={() => navigation.goBack()}
      />
      <FlatList
        data={MOCK_TRANSACTIONS}
        renderItem={renderTransaction}
        keyExtractor={keyExtractor}
        ListHeaderComponent={ListHeader}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  listContent: {
    paddingBottom: 40,
  },
  balanceCard: {
    margin: 16,
    padding: 20,
    backgroundColor: colors.card,
    borderRadius: 16,
  },
  balanceLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.mutedForeground,
    marginBottom: 4,
  },
  balanceAmount: {
    fontSize: 32,
    fontWeight: '800',
    color: colors.foreground,
  },
  balanceKrw: {
    fontSize: 14,
    color: colors.mutedForeground,
    marginTop: 2,
    marginBottom: 12,
  },
  walletAddressRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.muted,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 8,
    gap: 8,
  },
  walletAddress: {
    flex: 1,
    fontSize: 13,
    color: colors.mutedForeground,
  },
  copyText: {
    fontSize: 12,
    fontWeight: '600',
    color: colors.primary,
  },
  topUpSection: {
    marginHorizontal: 16,
    marginBottom: 16,
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: 20,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 12,
  },
  topUpGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  topUpButton: {
    flex: 1,
    minWidth: '45%',
    paddingVertical: 14,
    backgroundColor: colors.secondary,
    borderRadius: 12,
    alignItems: 'center',
  },
  topUpButtonActive: {
    backgroundColor: colors.primary,
  },
  topUpText: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.secondaryForeground,
  },
  topUpTextActive: {
    color: colors.white,
  },
  txHeader: {
    paddingHorizontal: 16,
    paddingTop: 8,
  },
  txRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
    backgroundColor: colors.card,
    marginHorizontal: 16,
    marginBottom: 1,
    gap: 12,
  },
  txIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  txArrow: {
    fontSize: 16,
    fontWeight: '700',
  },
  txInfo: {
    flex: 1,
    gap: 2,
  },
  txLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
  },
  txDate: {
    fontSize: 12,
    color: colors.mutedForeground,
  },
  txAmount: {
    fontSize: 14,
    fontWeight: '700',
  },
});
