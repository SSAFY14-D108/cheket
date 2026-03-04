import React, {useState, useMemo} from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Pressable,
  Image,
} from 'react-native';
import {useNavigation, useRoute} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RouteProp} from '@react-navigation/native';
import type {HomeStackParamList} from '../../../core/navigation/types';
import {HOME_ROUTES} from '../../../core/navigation/routes';
import {colors} from '../../../core/theme/colors';
import {MOCK_EVENTS, generateSeats} from '../../../core/data/mockData';
import type {EventDate, Grade, Seat} from '../../../core/types/common';
import {AppHeader} from '../../../shared/components/AppHeader';
import {PrimaryButton} from '../../../shared/components/PrimaryButton';

type NavigationProp = NativeStackNavigationProp<HomeStackParamList, 'SeatSelection'>;
type SeatSelectionRouteProp = RouteProp<HomeStackParamList, 'SeatSelection'>;

export function SeatSelectionScreen(): React.JSX.Element {
  const navigation = useNavigation<NavigationProp>();
  const route = useRoute<SeatSelectionRouteProp>();
  const {eventId} = route.params;

  const event = MOCK_EVENTS.find(e => e.id === eventId);
  const hasDates = Boolean(event?.dates && event.dates.length > 0);

  // Local step state: 1 = date, 2 = grade, 3 = seats
  const [step, setStep] = useState<1 | 2 | 3>(hasDates ? 1 : 2);
  const [selectedDate, setSelectedDate] = useState<EventDate | null>(null);
  const [selectedGrade, setSelectedGrade] = useState<Grade | null>(null);
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);

  const maxSeats = event?.maxPerUser ?? 4;

  // Generate seats based on selected grade
  const seats = useMemo(() => {
    if (!selectedGrade) {
      return [];
    }
    return generateSeats(eventId, selectedGrade.name);
  }, [eventId, selectedGrade]);

  // Group seats by row
  const seatsByRow = useMemo(() => {
    const map = new Map<string, Seat[]>();
    seats.forEach(seat => {
      const existing = map.get(seat.row) || [];
      existing.push(seat);
      map.set(seat.row, existing);
    });
    return map;
  }, [seats]);

  const selectedSeats = useMemo(
    () => seats.filter(s => selectedSeatIds.includes(s.id)),
    [seats, selectedSeatIds],
  );

  const totalPrice = useMemo(
    () => selectedSeats.reduce((sum, s) => sum + s.price, 0),
    [selectedSeats],
  );

  const handleDateSelect = (date: EventDate) => {
    setSelectedDate(date);
    setStep(2);
  };

  const handleGradeSelect = (grade: Grade) => {
    if (grade.remaining === 0) {
      return;
    }
    setSelectedGrade(grade);
    setSelectedSeatIds([]);
    setStep(3);
  };

  const handleSeatToggle = (seat: Seat) => {
    if (seat.status !== 'AVAILABLE') {
      return;
    }
    setSelectedSeatIds(prev => {
      if (prev.includes(seat.id)) {
        return prev.filter(id => id !== seat.id);
      }
      if (prev.length >= maxSeats) {
        return prev;
      }
      return [...prev, seat.id];
    });
  };

  const handlePayment = () => {
    navigation.navigate(HOME_ROUTES.PAYMENT, {
      eventId,
      seatIds: selectedSeatIds,
    });
  };

  const stepTitle = () => {
    if (step === 1) {
      return '날짜 선택';
    }
    if (step === 2) {
      return '등급 선택';
    }
    return '좌석 선택';
  };

  const handleBack = () => {
    if (step === 3) {
      setSelectedSeatIds([]);
      setStep(2);
    } else if (step === 2 && hasDates) {
      setSelectedGrade(null);
      setStep(1);
    } else {
      navigation.goBack();
    }
  };

  return (
    <View style={styles.container}>
      <AppHeader title={stepTitle()} showBack onBack={handleBack} />

      {/* Step indicator */}
      <View style={styles.stepBar}>
        <StepDot label="날짜" active={step >= 1} visible={hasDates} />
        {hasDates && <View style={styles.stepLine} />}
        <StepDot label="등급" active={step >= 2} visible />
        <View style={styles.stepLine} />
        <StepDot label="좌석" active={step >= 3} visible />
      </View>

      {/* Event info mini-bar */}
      <View style={styles.eventBar}>
        <Image source={{uri: event?.poster}} style={styles.eventPoster} />
        <View style={styles.eventInfo}>
          <Text style={styles.eventName} numberOfLines={1}>
            {event?.name}
          </Text>
          <Text style={styles.eventMeta} numberOfLines={1}>
            {selectedDate ? selectedDate.label : event?.date}
          </Text>
          <Text style={styles.eventMeta} numberOfLines={1}>
            {event?.venue}
          </Text>
        </View>
      </View>

      <ScrollView
        style={styles.scrollArea}
        contentContainerStyle={styles.scrollContent}>
        {/* Step 1: Date selection */}
        {step === 1 && event?.dates && (
          <View>
            <Text style={styles.sectionTitle}>공연 일정을 선택하세요</Text>
            {event.dates.map(date => (
              <Pressable
                key={date.id}
                style={[
                  styles.dateCard,
                  selectedDate?.id === date.id && styles.dateCardSelected,
                ]}
                onPress={() => handleDateSelect(date)}>
                <Text
                  style={[
                    styles.dateDay,
                    selectedDate?.id === date.id && styles.dateDaySelected,
                  ]}>
                  {date.day}
                </Text>
                <Text
                  style={[
                    styles.dateLabel,
                    selectedDate?.id === date.id && styles.dateLabelSelected,
                  ]}>
                  {date.label}
                </Text>
              </Pressable>
            ))}
          </View>
        )}

        {/* Step 2: Grade selection */}
        {step === 2 && (
          <View>
            <Text style={styles.sectionTitle}>등급을 선택하세요</Text>
            <View style={styles.gradeGrid}>
              {event?.grades.map(grade => {
                const isSoldOut = grade.remaining === 0;
                const isSelected = selectedGrade?.name === grade.name;
                return (
                  <Pressable
                    key={grade.name}
                    style={[
                      styles.gradeCard,
                      isSelected && styles.gradeCardSelected,
                      isSoldOut && styles.gradeCardDisabled,
                    ]}
                    onPress={() => handleGradeSelect(grade)}
                    disabled={isSoldOut}>
                    <View style={styles.gradeHeader}>
                      <View
                        style={[
                          styles.gradeDot,
                          {backgroundColor: grade.color || colors.primary},
                        ]}
                      />
                      <Text
                        style={[
                          styles.gradeName,
                          isSoldOut && styles.gradeTextDisabled,
                        ]}>
                        {grade.name}
                      </Text>
                    </View>
                    <Text
                      style={[
                        styles.gradePrice,
                        isSoldOut && styles.gradeTextDisabled,
                      ]}>
                      {grade.price.toLocaleString('ko-KR')} CTK
                    </Text>
                    <Text
                      style={[
                        styles.gradeRemaining,
                        isSoldOut && styles.gradeSoldOut,
                      ]}>
                      {isSoldOut
                        ? '매진'
                        : `잔여 ${grade.remaining.toLocaleString('ko-KR')}석`}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
          </View>
        )}

        {/* Step 3: Seat selection */}
        {step === 3 && (
          <View>
            <Text style={styles.sectionTitle}>
              좌석을 선택하세요 (최대 {maxSeats}석)
            </Text>

            {/* Legend */}
            <View style={styles.legend}>
              <LegendItem color={colors.secondary} label="선택 가능" />
              <LegendItem color={colors.primary} label="선택됨" />
              <LegendItem color="#E5E7EB" label="판매완료" />
              <LegendItem color={colors.warning} label="잠금" />
            </View>

            {/* Stage label */}
            <View style={styles.stageContainer}>
              <Text style={styles.stageLabel}>STAGE</Text>
            </View>

            {/* Seat grid */}
            <View style={styles.seatGrid}>
              {Array.from(seatsByRow.entries()).map(([row, rowSeats]) => (
                <View key={row} style={styles.seatRow}>
                  <Text style={styles.rowLabel}>{row}</Text>
                  <View style={styles.seatRowSeats}>
                    {rowSeats.map(seat => {
                      const isSelected = selectedSeatIds.includes(seat.id);
                      let bgColor: string = colors.secondary;
                      if (seat.status === 'SOLD') {
                        bgColor = '#E5E7EB';
                      } else if (seat.status === 'LOCKED') {
                        bgColor = colors.warning;
                      } else if (isSelected) {
                        bgColor = colors.primary;
                      }

                      return (
                        <Pressable
                          key={seat.id}
                          style={[styles.seatCell, {backgroundColor: bgColor}]}
                          onPress={() => handleSeatToggle(seat)}
                          disabled={
                            seat.status === 'SOLD' || seat.status === 'LOCKED'
                          }>
                          <Text
                            style={[
                              styles.seatNumber,
                              isSelected && styles.seatNumberSelected,
                              (seat.status === 'SOLD' ||
                                seat.status === 'LOCKED') &&
                                styles.seatNumberDisabled,
                            ]}>
                            {seat.number}
                          </Text>
                        </Pressable>
                      );
                    })}
                  </View>
                  <Text style={styles.rowLabel}>{row}</Text>
                </View>
              ))}
            </View>

            {/* Selected seats summary */}
            {selectedSeats.length > 0 && (
              <View style={styles.selectionSummary}>
                <Text style={styles.summaryTitle}>선택한 좌석</Text>
                {selectedSeats.map(seat => (
                  <View key={seat.id} style={styles.summaryRow}>
                    <Text style={styles.summaryLabel}>
                      {seat.row}열 {seat.number}번 ({seat.grade})
                    </Text>
                    <Text style={styles.summaryPrice}>
                      {seat.price.toLocaleString('ko-KR')} CTK
                    </Text>
                  </View>
                ))}
                <View style={styles.summaryDivider} />
                <View style={styles.summaryRow}>
                  <Text style={styles.summaryTotalLabel}>합계</Text>
                  <Text style={styles.summaryTotalPrice}>
                    {totalPrice.toLocaleString('ko-KR')} CTK
                  </Text>
                </View>
              </View>
            )}
          </View>
        )}
      </ScrollView>

      {/* Bottom bar for step 3 */}
      {step === 3 && (
        <View style={styles.bottomBar}>
          <PrimaryButton
            title={`결제하기 (${selectedSeats.length}석 선택)`}
            onPress={handlePayment}
            disabled={selectedSeats.length === 0}
          />
        </View>
      )}
    </View>
  );
}

/* ---- Small helper components ---- */

function StepDot({
  label,
  active,
  visible,
}: {
  label: string;
  active: boolean;
  visible: boolean;
}) {
  if (!visible) {
    return null;
  }
  return (
    <View style={styles.stepDotContainer}>
      <View
        style={[styles.stepDot, active ? styles.stepDotActive : undefined]}
      />
      <Text
        style={[
          styles.stepDotLabel,
          active ? styles.stepDotLabelActive : undefined,
        ]}>
        {label}
      </Text>
    </View>
  );
}

function LegendItem({color, label}: {color: string; label: string}) {
  return (
    <View style={styles.legendItem}>
      <View style={[styles.legendDot, {backgroundColor: color}]} />
      <Text style={styles.legendLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },

  /* Step bar */
  stepBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 12,
    paddingHorizontal: 24,
    backgroundColor: colors.card,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  stepDotContainer: {
    alignItems: 'center',
    gap: 4,
  },
  stepDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: colors.border,
  },
  stepDotActive: {
    backgroundColor: colors.primary,
  },
  stepDotLabel: {
    fontSize: 11,
    color: colors.mutedForeground,
  },
  stepDotLabelActive: {
    color: colors.primary,
    fontWeight: '600',
  },
  stepLine: {
    width: 40,
    height: 1,
    backgroundColor: colors.border,
    marginHorizontal: 8,
    marginBottom: 16,
  },

  /* Event bar */
  eventBar: {
    flexDirection: 'row',
    padding: 12,
    backgroundColor: colors.card,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    alignItems: 'center',
    gap: 12,
  },
  eventPoster: {
    width: 44,
    height: 60,
    borderRadius: 6,
    backgroundColor: colors.muted,
  },
  eventInfo: {
    flex: 1,
  },
  eventName: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 2,
  },
  eventMeta: {
    fontSize: 12,
    color: colors.mutedForeground,
    marginTop: 1,
  },

  /* Scroll area */
  scrollArea: {
    flex: 1,
  },
  scrollContent: {
    padding: 16,
    paddingBottom: 100,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 16,
  },

  /* Date cards (Step 1) */
  dateCard: {
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: 16,
    marginBottom: 10,
  },
  dateCardSelected: {
    borderColor: colors.primary,
    backgroundColor: colors.secondary,
  },
  dateDay: {
    fontSize: 13,
    fontWeight: '600',
    color: colors.mutedForeground,
    marginBottom: 4,
  },
  dateDaySelected: {
    color: colors.primary,
  },
  dateLabel: {
    fontSize: 15,
    fontWeight: '600',
    color: colors.foreground,
  },
  dateLabelSelected: {
    color: colors.primary,
  },

  /* Grade grid (Step 2) */
  gradeGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  gradeCard: {
    width: '48%',
    backgroundColor: colors.card,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 12,
    padding: 14,
  },
  gradeCardSelected: {
    borderColor: colors.primary,
    backgroundColor: colors.secondary,
  },
  gradeCardDisabled: {
    opacity: 0.5,
  },
  gradeHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 8,
  },
  gradeDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
  gradeName: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.foreground,
  },
  gradePrice: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.foreground,
    marginBottom: 4,
  },
  gradeRemaining: {
    fontSize: 12,
    color: colors.mutedForeground,
  },
  gradeSoldOut: {
    color: colors.destructive,
    fontWeight: '600',
  },
  gradeTextDisabled: {
    color: colors.mutedForeground,
  },

  /* Legend (Step 3) */
  legend: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 16,
    marginBottom: 16,
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  legendDot: {
    width: 12,
    height: 12,
    borderRadius: 3,
  },
  legendLabel: {
    fontSize: 11,
    color: colors.mutedForeground,
  },

  /* Stage */
  stageContainer: {
    alignItems: 'center',
    marginBottom: 20,
  },
  stageLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: colors.mutedForeground,
    letterSpacing: 2,
    paddingHorizontal: 24,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 4,
  },

  /* Seat grid */
  seatGrid: {
    alignItems: 'center',
    gap: 4,
  },
  seatRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  rowLabel: {
    width: 16,
    fontSize: 11,
    fontWeight: '600',
    color: colors.mutedForeground,
    textAlign: 'center',
  },
  seatRowSeats: {
    flexDirection: 'row',
    gap: 3,
  },
  seatCell: {
    width: 28,
    height: 28,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  seatNumber: {
    fontSize: 9,
    fontWeight: '600',
    color: colors.foreground,
  },
  seatNumberSelected: {
    color: colors.white,
  },
  seatNumberDisabled: {
    color: '#9CA3AF',
  },

  /* Selection summary */
  selectionSummary: {
    marginTop: 24,
    backgroundColor: colors.card,
    borderRadius: 12,
    padding: 16,
    borderWidth: 1,
    borderColor: colors.border,
  },
  summaryTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.foreground,
    marginBottom: 12,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  summaryLabel: {
    fontSize: 13,
    color: colors.mutedForeground,
  },
  summaryPrice: {
    fontSize: 13,
    fontWeight: '600',
    color: colors.foreground,
  },
  summaryDivider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: 8,
  },
  summaryTotalLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.foreground,
  },
  summaryTotalPrice: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.primary,
  },

  /* Bottom bar */
  bottomBar: {
    padding: 16,
    backgroundColor: colors.card,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
});
