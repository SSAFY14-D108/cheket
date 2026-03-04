import React from 'react';
import {
  View,
  Text,
  ScrollView,
  Pressable,
  Alert,
  StyleSheet,
} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../../../core/navigation/types';
import {ROOT_ROUTES} from '../../../core/navigation/routes';
import {useAuthStore} from '../../../store/authStore';
import {useTicketStore} from '../../../store/ticketStore';
import {useWishlistStore} from '../../../store/wishlistStore';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';

type NavigationProp = NativeStackNavigationProp<RootStackParamList>;

const CTK_TO_KRW = 1.2;

function MenuItem({label, onPress}: {label: string; onPress: () => void}) {
  return (
    <Pressable style={styles.menuItem} onPress={onPress}>
      <Text style={styles.menuLabel}>{label}</Text>
      <Text style={styles.menuArrow}>{'>'}</Text>
    </Pressable>
  );
}

export function MyPageScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const user = useAuthStore(state => state.user);
  const logout = useAuthStore(state => state.logout);
  const tickets = useTicketStore(state => state.tickets);
  const wishlist = useWishlistStore(state => state.wishlist);

  const ownedCount = tickets.filter(
    t => t.status === 'SOLD' || t.status === 'LISTED',
  ).length;
  const attendedCount = tickets.filter(t => t.status === 'USED').length;
  const wishlistedCount = wishlist.length;

  const ctkBalance = user?.ctkBalance ?? 0;
  const krwValue = Math.round(ctkBalance * CTK_TO_KRW);

  const handleCopyWallet = () => {
    Alert.alert('복사 완료', '지갑 주소가 복사되었습니다.');
  };

  const handleLogout = () => {
    Alert.alert('로그아웃', '정말 로그아웃 하시겠습니까?', [
      {text: '취소', style: 'cancel'},
      {
        text: '로그아웃',
        style: 'destructive',
        onPress: () => logout(),
      },
    ]);
  };

  const handleMenuPress = (label: string) => {
    Alert.alert(label, '해당 기능은 준비 중입니다.');
  };

  return (
    <View style={styles.container}>
      <AppHeader
        title="마이페이지"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}>
        {/* Profile Card */}
        <View style={styles.profileCard}>
          <View style={styles.avatarCircle}>
            <Text style={styles.avatarIcon}>U</Text>
          </View>
          <View style={styles.profileInfo}>
            <Text style={styles.userName}>{user?.name ?? '-'}</Text>
            <Text style={styles.memberLabel}>Cheket 회원</Text>
            <Text style={styles.profileDetail}>{user?.phone ?? '-'}</Text>
            <Pressable
              style={styles.walletRow}
              onPress={handleCopyWallet}>
              <Text style={styles.walletAddress} numberOfLines={1}>
                {user?.walletAddress ?? '-'}
              </Text>
              <Text style={styles.copyIcon}>복사</Text>
            </Pressable>
          </View>
        </View>

        {/* CTK Balance */}
        <View style={styles.balanceCard}>
          <View style={styles.balanceHeader}>
            <Text style={styles.balanceLabel}>CTK 잔액</Text>
            <Pressable
              style={styles.walletButton}
              onPress={() => navigation.navigate(ROOT_ROUTES.WALLET)}>
              <Text style={styles.walletButtonText}>지갑 관리</Text>
            </Pressable>
          </View>
          <Text style={styles.balanceAmount}>
            {ctkBalance.toLocaleString()} CTK
          </Text>
          <Text style={styles.balanceKrw}>
            ~ {krwValue.toLocaleString()} KRW
          </Text>
        </View>

        {/* Stats Grid */}
        <View style={styles.statsGrid}>
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{ownedCount}</Text>
            <Text style={styles.statLabel}>보유 티켓</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{attendedCount}</Text>
            <Text style={styles.statLabel}>관람 완료</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statNumber}>{wishlistedCount}</Text>
            <Text style={styles.statLabel}>찜 목록</Text>
          </View>
        </View>

        {/* Menu Items */}
        <View style={styles.menuSection}>
          <MenuItem
            label="공지사항"
            onPress={() => handleMenuPress('공지사항')}
          />
          <MenuItem
            label="이용약관"
            onPress={() => handleMenuPress('이용약관')}
          />
          <MenuItem
            label="개인정보처리방침"
            onPress={() => handleMenuPress('개인정보처리방침')}
          />
          <MenuItem
            label="고객센터"
            onPress={() => handleMenuPress('고객센터')}
          />
        </View>

        {/* Logout */}
        <Pressable style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutText}>로그아웃</Text>
        </Pressable>
      </ScrollView>
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
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    margin: 16,
    padding: 20,
    backgroundColor: colors.card,
    borderRadius: 16,
    gap: 16,
  },
  avatarCircle: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.secondary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarIcon: {
    fontSize: 22,
    fontWeight: '700',
    color: colors.secondaryForeground,
  },
  profileInfo: {
    flex: 1,
    gap: 2,
  },
  userName: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.foreground,
  },
  memberLabel: {
    fontSize: 12,
    color: colors.primary,
    fontWeight: '600',
  },
  profileDetail: {
    fontSize: 13,
    color: colors.mutedForeground,
    marginTop: 4,
  },
  walletRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 2,
  },
  walletAddress: {
    fontSize: 12,
    color: colors.mutedForeground,
    flex: 1,
  },
  copyIcon: {
    fontSize: 11,
    color: colors.primary,
    fontWeight: '600',
  },
  balanceCard: {
    marginHorizontal: 16,
    padding: 20,
    backgroundColor: colors.card,
    borderRadius: 16,
  },
  balanceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  balanceLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.mutedForeground,
  },
  walletButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: colors.secondary,
    borderRadius: 8,
  },
  walletButtonText: {
    fontSize: 12,
    fontWeight: '600',
    color: colors.primary,
  },
  balanceAmount: {
    fontSize: 28,
    fontWeight: '800',
    color: colors.foreground,
  },
  balanceKrw: {
    fontSize: 14,
    color: colors.mutedForeground,
    marginTop: 2,
  },
  statsGrid: {
    flexDirection: 'row',
    marginHorizontal: 16,
    marginTop: 16,
    backgroundColor: colors.card,
    borderRadius: 16,
    padding: 20,
    alignItems: 'center',
  },
  statItem: {
    flex: 1,
    alignItems: 'center',
    gap: 4,
  },
  statDivider: {
    width: 1,
    height: 32,
    backgroundColor: colors.border,
  },
  statNumber: {
    fontSize: 22,
    fontWeight: '800',
    color: colors.foreground,
  },
  statLabel: {
    fontSize: 12,
    color: colors.mutedForeground,
    fontWeight: '500',
  },
  menuSection: {
    margin: 16,
    backgroundColor: colors.card,
    borderRadius: 16,
    overflow: 'hidden',
  },
  menuItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  menuLabel: {
    fontSize: 15,
    color: colors.foreground,
    fontWeight: '500',
  },
  menuArrow: {
    fontSize: 16,
    color: colors.mutedForeground,
  },
  logoutButton: {
    marginHorizontal: 16,
    marginTop: 8,
    marginBottom: 40,
    alignItems: 'center',
    paddingVertical: 14,
  },
  logoutText: {
    fontSize: 15,
    fontWeight: '600',
    color: colors.destructive,
  },
});
