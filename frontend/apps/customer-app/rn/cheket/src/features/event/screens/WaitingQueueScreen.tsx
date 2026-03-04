import React, {useEffect, useState, useRef, useCallback} from 'react';
import {View, Text, StyleSheet, Animated, Easing} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {HOME_ROUTES} from '../../../core/navigation/routes';
import {colors} from '../../../core/theme/colors';
import {MOCK_EVENTS} from '../../../core/data/mockData';
import {AppHeader} from '../../../shared/components/AppHeader';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList, 'WaitingQueue'>;
type WaitingQueueRouteProp = RouteProp<HomeStackParamList, 'WaitingQueue'>;

export function WaitingQueueScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<WaitingQueueRouteProp>();
  const {eventId} = route.params;

  const event = MOCK_EVENTS.find(e => e.id === eventId);

  const initialPosition = useRef(Math.floor(Math.random() * 401) + 100).current;
  const [position, setPosition] = useState(initialPosition);
  const spinValue = useRef(new Animated.Value(0)).current;
  const progressAnim = useRef(new Animated.Value(0)).current;

  // Spinning animation for the circular indicator
  useEffect(() => {
    const spin = Animated.loop(
      Animated.timing(spinValue, {
        toValue: 1,
        duration: 2000,
        easing: Easing.linear,
        useNativeDriver: true,
      }),
    );
    spin.start();
    return () => spin.stop();
  }, [spinValue]);

  // Decrement queue position every second
  useEffect(() => {
    const interval = setInterval(() => {
      setPosition(prev => {
        const decrement = Math.floor(Math.random() * 30) + 10;
        const next = prev - decrement;
        return next > 0 ? next : 0;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  // Progress bar animation (fills over 5 seconds)
  useEffect(() => {
    Animated.timing(progressAnim, {
      toValue: 1,
      duration: 5000,
      easing: Easing.linear,
      useNativeDriver: false,
    }).start();
  }, [progressAnim]);

  // Auto-navigate after 5 seconds
  const handleAutoNavigate = useCallback(() => {
    navigation.replace(HOME_ROUTES.SEAT_SELECTION, {eventId});
  }, [navigation, eventId]);

  useEffect(() => {
    const timer = setTimeout(handleAutoNavigate, 5000);
    return () => clearTimeout(timer);
  }, [handleAutoNavigate]);

  const spin = spinValue.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '360deg'],
  });

  const estimatedSeconds = Math.max(1, Math.ceil(position / 80));
  const estimatedTimeLabel =
    estimatedSeconds >= 60
      ? `${Math.floor(estimatedSeconds / 60)}분 ${estimatedSeconds % 60}초`
      : `${estimatedSeconds}초`;

  const progressWidth = progressAnim.interpolate({
    inputRange: [0, 1],
    outputRange: ['0%', '100%'],
  });

  return (
    <View style={styles.container}>
      <AppHeader
        title="대기열"
        showBack
        onBack={() => navigation.goBack()}
      />

      <View style={styles.content}>
        {/* Event name */}
        <Text style={styles.eventName}>{event?.name ?? '이벤트'}</Text>

        {/* Circular progress indicator */}
        <View style={styles.circleContainer}>
          <Animated.View
            style={[styles.circleOuter, {transform: [{rotate: spin}]}]}>
            <View style={styles.circleArc} />
          </Animated.View>
          <View style={styles.circleInner}>
            <Text style={styles.positionLabel}>대기 순서</Text>
            <Text style={styles.positionNumber}>
              {position.toLocaleString('ko-KR')}
            </Text>
            <Text style={styles.positionUnit}>번째</Text>
          </View>
        </View>

        {/* Status message */}
        <Text style={styles.waitingMessage}>대기 중입니다...</Text>

        {/* Estimated time */}
        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>예상 대기 시간</Text>
          <Text style={styles.infoValue}>{estimatedTimeLabel}</Text>
        </View>

        <View style={styles.infoRow}>
          <Text style={styles.infoLabel}>내 앞 대기 인원</Text>
          <Text style={styles.infoValue}>
            {position.toLocaleString('ko-KR')}명
          </Text>
        </View>

        <Text style={styles.notice}>
          페이지를 벗어나면 대기가 취소될 수 있습니다.{'\n'}잠시만 기다려
          주세요.
        </Text>
      </View>

      {/* Bottom progress bar */}
      <View style={styles.progressBarContainer}>
        <Animated.View
          style={[styles.progressBarFill, {width: progressWidth}]}
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
    paddingHorizontal: 24,
  },
  eventName: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 32,
    textAlign: 'center',
  },
  circleContainer: {
    width: 180,
    height: 180,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 32,
  },
  circleOuter: {
    position: 'absolute',
    width: 180,
    height: 180,
    borderRadius: 90,
    borderWidth: 4,
    borderColor: colors.border,
  },
  circleArc: {
    position: 'absolute',
    top: -4,
    left: -4,
    width: 180,
    height: 180,
    borderRadius: 90,
    borderWidth: 4,
    borderColor: 'transparent',
    borderTopColor: colors.primary,
    borderRightColor: colors.primary,
  },
  circleInner: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  positionLabel: {
    fontSize: 13,
    color: colors.mutedForeground,
    marginBottom: 4,
  },
  positionNumber: {
    fontSize: 40,
    fontWeight: '800',
    color: colors.primary,
  },
  positionUnit: {
    fontSize: 14,
    color: colors.mutedForeground,
    marginTop: 2,
  },
  waitingMessage: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.foreground,
    marginBottom: 24,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    maxWidth: 260,
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
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
  notice: {
    marginTop: 32,
    fontSize: 13,
    color: colors.mutedForeground,
    textAlign: 'center',
    lineHeight: 20,
  },
  progressBarContainer: {
    height: 4,
    backgroundColor: colors.border,
  },
  progressBarFill: {
    height: 4,
    backgroundColor: colors.primary,
  },
});
