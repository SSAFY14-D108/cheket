import React from 'react';
import {
  View,
  Text,
  Image,
  ScrollView,
  Alert,
  StyleSheet,
} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import type {CollectionStackParamList} from '../../../core/navigation/types';
import {useTicketStore} from '../../../store/ticketStore';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';

type NavigationProp = NativeStackNavigationProp<CollectionStackParamList>;
type ScreenRouteProp = RouteProp<
  CollectionStackParamList,
  'CollectibleTicketDetail'
>;

export function CollectibleTicketDetailScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<ScreenRouteProp>();
  const {ticketId} = route.params;

  const ticket = useTicketStore(state =>
    state.tickets.find(t => t.id === ticketId),
  );

  if (!ticket) {
    return (
      <View style={styles.container}>
        <AppHeader
          title="티켓 상세"
          showBack
          onBack={() => navigation.goBack()}
        />
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>티켓을 찾을 수 없습니다</Text>
        </View>
      </View>
    );
  }

  const mockTokenId = `#${ticket.id.replace('tkt_', '')}`;
  const mockWalletAddress = '0x3a9F...dE42';
  const mockContractAddress = '0x7B2c...aF19';

  const handleShare = () => {
    Alert.alert('공유하기', '공유 기능은 준비 중입니다.');
  };

  return (
    <View style={styles.container}>
      <AppHeader
        title="NFT 티켓"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}>
        {/* Poster */}
        <View style={styles.posterContainer}>
          <Image source={{uri: ticket.poster}} style={styles.poster} />
          <View style={styles.nftBadge}>
            <Text style={styles.nftBadgeText}>NFT</Text>
          </View>
        </View>

        {/* Event Info */}
        <View style={styles.section}>
          <Text style={styles.eventName}>{ticket.eventName}</Text>
          <Text style={styles.eventDetail}>{ticket.eventDate}</Text>
          <Text style={styles.eventDetail}>{ticket.venue}</Text>
        </View>

        {/* Seat & Attendance Info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>관람 정보</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>좌석</Text>
            <Text style={styles.infoValue}>{ticket.seatLabel}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>등급</Text>
            <Text style={styles.infoValue}>{ticket.grade}</Text>
          </View>
          {ticket.attendedDate && (
            <View style={styles.infoRow}>
              <Text style={styles.infoLabel}>관람일</Text>
              <Text style={styles.infoValue}>{ticket.attendedDate}</Text>
            </View>
          )}
        </View>

        {/* NFT Info */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>NFT 정보</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Token ID</Text>
            <Text style={styles.infoValue}>{mockTokenId}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Owner</Text>
            <Text style={styles.infoValue}>{mockWalletAddress}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Contract</Text>
            <Text style={styles.infoValue}>{mockContractAddress}</Text>
          </View>
        </View>

        {/* Share Button */}
        <View style={styles.buttonContainer}>
          <PrimaryButton title="공유하기" onPress={handleShare} />
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
  scrollView: {
    flex: 1,
  },
  errorContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorText: {
    fontSize: 16,
    color: colors.mutedForeground,
  },
  posterContainer: {
    position: 'relative',
    marginHorizontal: 16,
    marginTop: 16,
    borderRadius: 16,
    overflow: 'hidden',
  },
  poster: {
    width: '100%',
    aspectRatio: 2 / 3,
    backgroundColor: colors.muted,
  },
  nftBadge: {
    position: 'absolute',
    top: 12,
    right: 12,
    backgroundColor: colors.card,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: colors.primary,
  },
  nftBadgeText: {
    fontSize: 13,
    fontWeight: '800',
    color: colors.primary,
  },
  section: {
    marginHorizontal: 16,
    marginTop: 24,
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    gap: 4,
  },
  eventName: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 4,
  },
  eventDetail: {
    fontSize: 14,
    color: colors.mutedForeground,
    lineHeight: 20,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 8,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 6,
  },
  infoLabel: {
    fontSize: 14,
    color: colors.mutedForeground,
  },
  infoValue: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
  },
  buttonContainer: {
    margin: 16,
    marginBottom: 40,
  },
});
