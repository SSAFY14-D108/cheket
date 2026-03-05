import React, {useState, useCallback, useEffect, useRef} from 'react';
import {
  View,
  Text,
  FlatList,
  Image,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
  Modal,
  Pressable,
} from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  withRepeat,
  withSequence,
  interpolate,
  Easing,
} from 'react-native-reanimated';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {CollectionStackParamList} from '../../../core/navigation/types';
import {COLLECTION_ROUTES} from '../../../core/navigation/routes';
import {useTicketStore} from '../../../store/ticketStore';
import {colors} from '../../../core/theme/colors';
import {AppHeader} from '../../../shared/components/AppHeader';
import {EmptyState} from '../../../shared/components/EmptyState';
import type {Ticket} from '../../../core/types/common';

type NavigationProp = NativeStackNavigationProp<CollectionStackParamList>;

const SCREEN_WIDTH = Dimensions.get('window').width;
const CARD_GAP = 12;
const CARD_PADDING = 16;
const COMPACT_WIDTH = (SCREEN_WIDTH - CARD_PADDING * 2 - CARD_GAP) / 2;
const COMPACT_HEIGHT = COMPACT_WIDTH * 1.7;
const FULL_WIDTH = SCREEN_WIDTH - 64;
const FULL_HEIGHT = FULL_WIDTH * 1.7;
const SCALLOP_R = 5;
const SCALLOP_COUNT = 10;

type HoloVariant = 'rainbow' | 'aurora' | 'prism' | 'cosmos' | 'sunset' | 'neon';
const HOLO_VARIANTS: HoloVariant[] = ['rainbow', 'aurora', 'prism', 'cosmos', 'sunset', 'neon'];

const HOLO_COLORS: Record<HoloVariant, string[]> = {
  rainbow: ['#ff6b6b', '#ffd93d', '#6bcb77', '#4d96ff', '#9b59b6', '#ff6b9d'],
  aurora: ['#00d2ff', '#3a7bd5', '#00d2ff', '#22c55e', '#06b6d4'],
  prism: ['#ff6b9d', '#ffd93d', '#22c55e', '#4d96ff', '#9b59b6'],
  cosmos: ['#9b59b6', '#4d96ff', '#ff6b9d', '#06b6d4', '#9b59b6'],
  sunset: ['#ff6b6b', '#ffd93d', '#ff9a56', '#ff6b9d', '#9b59b6'],
  neon: ['#06b6d4', '#4d96ff', '#9b59b6', '#ff6b9d', '#06b6d4'],
};

const GRADE_COLORS: Record<string, {bg: string; text: string}> = {
  VIP: {bg: '#f59e0b', text: '#000'},
  'VIP PIT': {bg: '#f59e0b', text: '#000'},
  'R석': {bg: '#ef4444', text: '#fff'},
  FLOOR: {bg: '#3b82f6', text: '#fff'},
  'GA PIT': {bg: '#ef4444', text: '#fff'},
  'S석': {bg: '#3b82f6', text: '#fff'},
  'A석': {bg: '#22c55e', text: '#fff'},
  STAND: {bg: '#22c55e', text: '#fff'},
  GA: {bg: '#3b82f6', text: '#fff'},
  '1일권': {bg: '#06b6d4', text: '#fff'},
  '2일권': {bg: '#3b82f6', text: '#fff'},
};

function getGrade(grade: string) {
  return GRADE_COLORS[grade] ?? {bg: '#6b7280', text: '#fff'};
}

function getVariant(ticket: Ticket): HoloVariant {
  const num = Number(ticket.id.replace(/\D/g, '')) || 0;
  return HOLO_VARIANTS[num % HOLO_VARIANTS.length];
}

// ── Scallop Edge ──
function ScallopEdge({position, width}: {position: 'top' | 'bottom'; width: number}) {
  const scallops = [];
  const spacing = width / SCALLOP_COUNT;
  for (let i = 0; i < SCALLOP_COUNT; i++) {
    scallops.push(
      <View
        key={i}
        style={[
          styles.scallop,
          {
            left: spacing * i + spacing / 2 - SCALLOP_R,
            width: SCALLOP_R * 2,
            height: SCALLOP_R * 2,
            borderRadius: SCALLOP_R,
            [position === 'top' ? 'top' : 'bottom']: -SCALLOP_R,
          },
        ]}
      />,
    );
  }
  return (
    <View style={[styles.scallopContainer, {width}]} pointerEvents="none">
      {scallops}
    </View>
  );
}

// ── Holographic Shimmer ──
function HoloShimmer({variant, width, height}: {variant: HoloVariant; width: number; height: number}) {
  const shimmerX = useSharedValue(-width);

  useEffect(() => {
    shimmerX.value = withRepeat(
      withTiming(width * 2, {duration: 3000, easing: Easing.linear}),
      -1,
      false,
    );
  }, [shimmerX, width]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{translateX: shimmerX.value}],
  }));

  const holoColors = HOLO_COLORS[variant];

  return (
    <View style={[StyleSheet.absoluteFill, {overflow: 'hidden', opacity: 0.18}]} pointerEvents="none">
      <Animated.View
        style={[
          {
            position: 'absolute',
            top: 0,
            left: -width,
            width: width * 2,
            height,
            opacity: 0.7,
            backgroundColor: holoColors[0],
          },
          animatedStyle,
        ]}
      >
        <View
          style={{
            flex: 1,
            flexDirection: 'row',
          }}>
          {holoColors.map((c, i) => (
            <View
              key={i}
              style={{
                flex: 1,
                backgroundColor: c,
                opacity: 0.6,
              }}
            />
          ))}
        </View>
      </Animated.View>
    </View>
  );
}

// ── Gold Foil Overlay ──
function GoldFoilOverlay({width, height}: {width: number; height: number}) {
  const sheenX = useSharedValue(-width);

  useEffect(() => {
    sheenX.value = withRepeat(
      withSequence(
        withTiming(width * 2, {duration: 5800, easing: Easing.linear}),
        withTiming(-width, {duration: 0}),
      ),
      -1,
      false,
    );
  }, [sheenX, width]);

  const sheenStyle = useAnimatedStyle(() => ({
    transform: [{translateX: sheenX.value}],
  }));

  return (
    <View style={[StyleSheet.absoluteFill, {overflow: 'hidden'}]} pointerEvents="none">
      {/* Gold base gradient simulation */}
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: '#d5b45a',
            opacity: 0.12,
          },
        ]}
      />
      {/* Grain pattern */}
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: '#c9972d',
            opacity: 0.06,
          },
        ]}
      />
      {/* Moving sheen */}
      <Animated.View
        style={[
          {
            position: 'absolute',
            top: 0,
            width: width * 0.4,
            height,
            backgroundColor: 'rgba(255, 248, 208, 0.35)',
          },
          sheenStyle,
        ]}
      />
    </View>
  );
}

// ── Ticket Front Face ──
function TicketFront({
  ticket,
  width,
  height,
  isGold,
  variant,
  showHolo,
}: {
  ticket: Ticket;
  width: number;
  height: number;
  isGold: boolean;
  variant: HoloVariant;
  showHolo: boolean;
}) {
  const bodyH = height - SCALLOP_R * 4;
  const ticketNo = `No.${ticket.id.replace('tkt_', '').padStart(4, '0')}`;

  return (
    <View style={[styles.ticketFace, {width, height}]}>
      <ScallopEdge position="top" width={width} />
      <View style={[styles.ticketBody, {height: bodyH, backgroundColor: isGold ? '#d5b45a' : '#0b0f1a'}]}>
        <Image
          source={{uri: ticket.poster}}
          style={[StyleSheet.absoluteFill, {opacity: isGold ? 0.85 : 1}]}
          resizeMode="cover"
        />
        {isGold && <GoldFoilOverlay width={width} height={bodyH} />}
        {showHolo && <HoloShimmer variant={variant} width={width} height={bodyH} />}
        {/* Dark gradient overlay */}
        <View
          style={[
            StyleSheet.absoluteFill,
            {
              backgroundColor: isGold
                ? 'rgba(49,34,8,0.45)'
                : 'rgba(11,15,26,0.55)',
            },
          ]}
        />
        {/* Top label */}
        <View style={styles.frontTopLabel}>
          <Text
            style={[
              styles.frontLabelText,
              {color: isGold ? 'rgba(24,20,10,0.72)' : 'rgba(255,255,255,0.82)'},
            ]}>
            CONCERT TICKET
          </Text>
        </View>
        {/* CHEKET + event name */}
        <View style={styles.frontTitle}>
          <Text
            style={[
              styles.frontCheketText,
              {
                color: isGold ? 'rgba(125,26,26,0.86)' : 'rgba(255,255,255,0.95)',
                fontSize: width > 200 ? 36 : 20,
              },
            ]}>
            CHEKET
          </Text>
          <Text
            style={[
              styles.frontEventName,
              {
                color: isGold ? 'rgba(100,22,22,0.84)' : 'rgba(255,255,255,0.95)',
                fontSize: width > 200 ? 16 : 9,
              },
            ]}
            numberOfLines={2}>
            {ticket.eventName}
          </Text>
        </View>
        {/* Bottom info */}
        <View style={styles.frontBottom}>
          <View>
            <Text
              style={[
                styles.frontSmallLabel,
                {color: isGold ? 'rgba(24,20,10,0.62)' : 'rgba(255,255,255,0.7)'},
              ]}>
              CHEKET
            </Text>
            <Text
              style={[
                styles.frontTicketNo,
                {color: isGold ? 'rgba(24,20,10,0.7)' : 'rgba(255,255,255,0.82)'},
              ]}>
              {ticketNo}
            </Text>
          </View>
          <Text
            style={[
              styles.frontTapLabel,
              {color: isGold ? 'rgba(24,20,10,0.58)' : 'rgba(255,255,255,0.68)'},
            ]}>
            TAP
          </Text>
        </View>
      </View>
      <ScallopEdge position="bottom" width={width} />
    </View>
  );
}

// ── Ticket Back Face ──
function TicketBack({
  ticket,
  width,
  height,
  isGold,
}: {
  ticket: Ticket;
  width: number;
  height: number;
  isGold: boolean;
}) {
  const bodyH = height - SCALLOP_R * 4;
  const ticketNo = `No.${ticket.id.replace('tkt_', '').padStart(4, '0')}`;
  const g = getGrade(ticket.grade);

  return (
    <View style={[styles.ticketFace, {width, height}]}>
      <ScallopEdge position="top" width={width} />
      <View style={[styles.ticketBody, {height: bodyH, backgroundColor: isGold ? '#d5b45a' : '#0b0f1a'}]}>
        <Image
          source={{uri: ticket.poster}}
          style={[StyleSheet.absoluteFill, {opacity: 0.15}]}
          resizeMode="cover"
        />
        {isGold && <GoldFoilOverlay width={width} height={bodyH} />}
        <View
          style={[
            StyleSheet.absoluteFill,
            {backgroundColor: isGold ? 'rgba(55,36,11,0.7)' : 'rgba(9,13,24,0.88)'},
          ]}
        />
        {/* Content */}
        <View style={styles.backContent}>
          {/* Header */}
          <View style={styles.backHeader}>
            <Text style={styles.backHeaderLabel}>CONCERT TICKET</Text>
            <Text style={styles.backHeaderNo}>{ticketNo}</Text>
          </View>
          {/* Event name */}
          <Text style={styles.backEventName} numberOfLines={2}>
            {ticket.eventName}
          </Text>
          {/* Divider + info */}
          <View style={styles.backDivider} />
          <BackRow label="Date" value={ticket.eventDate} />
          <BackRow label="Venue" value={ticket.venue} />
          {/* Grade & Seat */}
          <View style={styles.backDivider} />
          <View style={styles.backSeatRow}>
            <SeatBox label="Grade" value={ticket.grade} accent={isGold ? undefined : g.bg} />
            <SeatBox label="Seat" value={ticket.seatLabel} accent={isGold ? undefined : '#f8e28a'} />
          </View>
          {/* Price */}
          <View style={{flex: 1}} />
          <View style={styles.backPriceDivider} />
          <View style={styles.backPriceRow}>
            <Text style={styles.backPrice}>
              {ticket.originalPrice.toLocaleString('ko-KR')}
              <Text style={styles.backPriceUnit}> 원</Text>
            </Text>
            <Text style={styles.backTap}>TAP</Text>
          </View>
        </View>
      </View>
      <ScallopEdge position="bottom" width={width} />
    </View>
  );
}

function BackRow({label, value}: {label: string; value: string}) {
  return (
    <View style={styles.backInfoRow}>
      <Text style={styles.backInfoLabel}>{label}</Text>
      <Text style={styles.backInfoValue} numberOfLines={1}>
        {value}
      </Text>
    </View>
  );
}

function SeatBox({label, value, accent}: {label: string; value: string; accent?: string}) {
  return (
    <View
      style={[
        styles.seatBox,
        {
          backgroundColor: accent ? `${accent}20` : 'rgba(255,255,255,0.04)',
          borderColor: accent ? `${accent}40` : 'rgba(255,255,255,0.07)',
        },
      ]}>
      <Text style={styles.seatBoxLabel}>{label}</Text>
      <Text style={[styles.seatBoxValue, accent ? {color: accent} : null]}>
        {value}
      </Text>
    </View>
  );
}

// ── Flippable Ticket Card (Full Size) ──
function FlippableTicketCard({
  ticket,
  isGold,
  variant,
  width,
  height,
}: {
  ticket: Ticket;
  isGold: boolean;
  variant: HoloVariant;
  width: number;
  height: number;
}) {
  const flipProgress = useSharedValue(0);
  const [isFlipped, setIsFlipped] = useState(false);

  const handleFlip = useCallback(() => {
    const target = isFlipped ? 0 : 1;
    flipProgress.value = withTiming(target, {duration: 700, easing: Easing.inOut(Easing.ease)});
    setIsFlipped(!isFlipped);
  }, [isFlipped, flipProgress]);

  const frontStyle = useAnimatedStyle(() => {
    const rotateY = interpolate(flipProgress.value, [0, 1], [0, 180]);
    return {
      transform: [{perspective: 1200}, {rotateY: `${rotateY}deg`}],
      backfaceVisibility: 'hidden' as const,
      position: 'absolute' as const,
    };
  });

  const backStyle = useAnimatedStyle(() => {
    const rotateY = interpolate(flipProgress.value, [0, 1], [180, 360]);
    return {
      transform: [{perspective: 1200}, {rotateY: `${rotateY}deg`}],
      backfaceVisibility: 'hidden' as const,
      position: 'absolute' as const,
    };
  });

  // Pulse glow animation
  const glowOpacity = useSharedValue(0.3);
  useEffect(() => {
    glowOpacity.value = withRepeat(
      withSequence(
        withTiming(0.7, {duration: 2000}),
        withTiming(0.3, {duration: 2000}),
      ),
      -1,
      false,
    );
  }, [glowOpacity]);

  const glowStyle = useAnimatedStyle(() => ({
    opacity: glowOpacity.value,
  }));

  return (
    <Pressable onPress={handleFlip} style={{width, height}}>
      {/* Glow */}
      <Animated.View
        style={[
          {
            position: 'absolute',
            top: -8,
            left: -8,
            right: -8,
            bottom: -8,
            borderRadius: 20,
            backgroundColor: isGold ? '#d5b45a' : HOLO_COLORS[variant][0],
          },
          glowStyle,
        ]}
        pointerEvents="none"
      />
      <Animated.View style={frontStyle}>
        <TicketFront
          ticket={ticket}
          width={width}
          height={height}
          isGold={isGold}
          variant={variant}
          showHolo
        />
      </Animated.View>
      <Animated.View style={backStyle}>
        <TicketBack ticket={ticket} width={width} height={height} isGold={isGold} />
      </Animated.View>
    </Pressable>
  );
}

// ── Compact Card (Grid Item) ──
function CompactCard({
  ticket,
  isGold,
  variant,
  onPress,
}: {
  ticket: Ticket;
  isGold: boolean;
  variant: HoloVariant;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity onPress={onPress} activeOpacity={0.85} style={[styles.compactCard, {width: COMPACT_WIDTH}]}>
      <TicketFront
        ticket={ticket}
        width={COMPACT_WIDTH}
        height={COMPACT_HEIGHT}
        isGold={isGold}
        variant={variant}
        showHolo={false}
      />
    </TouchableOpacity>
  );
}

// ── Main Screen ──
export function CollectionScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const tickets = useTicketStore(state => state.tickets);
  const usedTickets = tickets.filter(t => t.status === 'USED');
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);

  const renderItem = useCallback(
    ({item, index}: {item: Ticket; index: number}) => (
      <CompactCard
        ticket={item}
        isGold={index === 0}
        variant={getVariant(item)}
        onPress={() => setSelectedTicket(item)}
      />
    ),
    [],
  );

  return (
    <View style={styles.container}>
      <AppHeader
        title="컬렉션"
        rightElement={
          <Text style={styles.countBadge}>{usedTickets.length}장</Text>
        }
      />
      {usedTickets.length === 0 ? (
        <EmptyState
          title="아직 컬렉션이 없어요"
          description="공연을 관람하면 티켓이 여기에 모입니다"
        />
      ) : (
        <>
          <View style={styles.descriptionWrap}>
            <Text style={styles.descriptionText}>
              관람한 공연의 소장 티켓을 모아보세요. 카드를 탭하면 상세 정보를 볼 수 있어요.
            </Text>
          </View>
          <FlatList
            data={usedTickets}
            renderItem={renderItem}
            keyExtractor={item => item.id}
            numColumns={2}
            contentContainerStyle={styles.listContent}
            columnWrapperStyle={styles.row}
            showsVerticalScrollIndicator={false}
          />
        </>
      )}

      {/* Full-size Modal */}
      <Modal
        visible={selectedTicket !== null}
        transparent
        animationType="fade"
        statusBarTranslucent
        onRequestClose={() => setSelectedTicket(null)}>
        <Pressable
          style={styles.modalBackdrop}
          onPress={() => setSelectedTicket(null)}>
          <Pressable
            onPress={e => e.stopPropagation()}
            style={styles.modalContent}>
            {selectedTicket && (
              <FlippableTicketCard
                ticket={selectedTicket}
                isGold={selectedTicket.id === usedTickets[0]?.id}
                variant={getVariant(selectedTicket)}
                width={FULL_WIDTH}
                height={FULL_HEIGHT}
              />
            )}
            <TouchableOpacity
              style={styles.closeButton}
              onPress={() => setSelectedTicket(null)}>
              <Text style={styles.closeButtonText}>닫기</Text>
            </TouchableOpacity>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  countBadge: {
    fontSize: 12,
    color: colors.mutedForeground,
    fontWeight: '500',
  },
  descriptionWrap: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 8,
  },
  descriptionText: {
    fontSize: 12,
    color: colors.mutedForeground,
    lineHeight: 18,
  },
  listContent: {
    padding: CARD_PADDING,
    paddingBottom: 100,
  },
  row: {
    gap: CARD_GAP,
    marginBottom: CARD_GAP,
  },
  // Scallop
  scallopContainer: {
    height: SCALLOP_R * 2,
    position: 'relative',
    overflow: 'visible',
    zIndex: 10,
  },
  scallop: {
    position: 'absolute',
    backgroundColor: colors.background,
  },
  // Ticket faces
  ticketFace: {
    overflow: 'hidden',
    borderRadius: 0,
  },
  ticketBody: {
    flex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
  // Front face
  frontTopLabel: {
    position: 'absolute',
    top: 10,
    left: 12,
    right: 12,
    zIndex: 5,
  },
  frontLabelText: {
    fontSize: 7,
    letterSpacing: 2,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  frontTitle: {
    position: 'absolute',
    top: 30,
    left: 12,
    right: 12,
    zIndex: 5,
  },
  frontCheketText: {
    fontWeight: '900',
    lineHeight: 38,
    letterSpacing: -1,
    textTransform: 'uppercase',
  },
  frontEventName: {
    marginTop: 2,
    fontWeight: '800',
    lineHeight: 18,
    textTransform: 'uppercase',
  },
  frontBottom: {
    position: 'absolute',
    bottom: 10,
    left: 12,
    right: 12,
    zIndex: 5,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
  },
  frontSmallLabel: {
    fontSize: 6,
    letterSpacing: 2,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  frontTicketNo: {
    fontFamily: 'monospace',
    fontSize: 8,
  },
  frontTapLabel: {
    fontSize: 7,
    letterSpacing: 1,
  },
  // Back face
  backContent: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    padding: 12,
    zIndex: 5,
  },
  backHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  backHeaderLabel: {
    fontSize: 7,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: 'rgba(255,255,255,0.6)',
    fontWeight: '700',
  },
  backHeaderNo: {
    fontSize: 7,
    fontFamily: 'monospace',
    color: 'rgba(255,255,255,0.6)',
  },
  backEventName: {
    fontSize: 12,
    fontWeight: '900',
    lineHeight: 15,
    color: 'rgba(255,255,255,0.95)',
    textTransform: 'uppercase',
    marginTop: 8,
  },
  backDivider: {
    height: 1,
    backgroundColor: 'rgba(255,255,255,0.2)',
    marginTop: 10,
    marginBottom: 8,
  },
  backInfoRow: {
    flexDirection: 'row',
    gap: 8,
    alignItems: 'baseline',
    marginBottom: 5,
  },
  backInfoLabel: {
    fontSize: 6,
    letterSpacing: 1,
    textTransform: 'uppercase',
    color: 'rgba(255,255,255,0.25)',
    fontWeight: '700',
    width: 32,
  },
  backInfoValue: {
    fontSize: 9,
    fontWeight: '600',
    color: 'rgba(255,255,255,0.65)',
    flex: 1,
  },
  backSeatRow: {
    flexDirection: 'row',
    gap: 6,
    marginTop: 4,
  },
  seatBox: {
    flex: 1,
    alignItems: 'center',
    gap: 3,
    paddingVertical: 5,
    borderRadius: 7,
    borderWidth: 1,
  },
  seatBoxLabel: {
    fontSize: 6,
    textTransform: 'uppercase',
    letterSpacing: 1,
    color: 'rgba(255,255,255,0.25)',
    fontWeight: '600',
  },
  seatBoxValue: {
    fontSize: 10,
    fontWeight: '800',
    color: 'rgba(255,255,255,0.82)',
  },
  backPriceDivider: {
    height: 1,
    borderStyle: 'dashed',
    borderTopWidth: 1,
    borderColor: 'rgba(255,255,255,0.24)',
    marginBottom: 8,
  },
  backPriceRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  backPrice: {
    fontSize: 16,
    fontWeight: '900',
    color: 'rgba(255,255,255,0.95)',
  },
  backPriceUnit: {
    fontSize: 9,
    opacity: 0.8,
  },
  backTap: {
    fontSize: 7,
    letterSpacing: 1,
    color: 'rgba(255,255,255,0.5)',
  },
  // Compact card
  compactCard: {
    overflow: 'hidden',
    borderRadius: 4,
  },
  // Modal
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.75)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    alignItems: 'center',
    gap: 16,
  },
  closeButton: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 10,
    backgroundColor: colors.white,
  },
  closeButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.black,
  },
});
