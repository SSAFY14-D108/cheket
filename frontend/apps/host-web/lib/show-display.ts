interface ShowDisplayTarget {
  status: string
  show: {
    showStartDate: string
    showEndDate: string
  }
  reservation: {
    startDate: string
    endDate: string
  }
}

type ReservationPhase = "TICKETING" | "UPCOMING" | "CLOSED"
type PerformancePhase = "UPCOMING" | "LIVE" | "ENDED" | "CANCELLED"

interface StatusBadgeMeta<TPhase extends string> {
  phase: TPhase
  label: string
}

interface ShowDisplayMeta {
  reservation: StatusBadgeMeta<ReservationPhase>
  performance: StatusBadgeMeta<PerformancePhase>
  badges: Array<StatusBadgeMeta<ReservationPhase | PerformancePhase>>
  priority: number
  sortTimestamp: number
}

function toDate(value: string, fallbackTime: "start" | "end") {
  if (!value) {
    return new Date(0)
  }

  if (value.includes("T")) {
    return new Date(value)
  }

  return new Date(`${value}T${fallbackTime === "start" ? "00:00:00" : "23:59:59"}`)
}

function getReservationStatus(
  reservationStartAt: Date,
  reservationEndAt: Date,
  now: Date
): StatusBadgeMeta<ReservationPhase> {
  if (now.getTime() >= reservationStartAt.getTime() && now.getTime() <= reservationEndAt.getTime()) {
    return { phase: "TICKETING", label: "예매 중" }
  }

  if (now.getTime() < reservationStartAt.getTime()) {
    return { phase: "UPCOMING", label: "예매 예정" }
  }

  return { phase: "CLOSED", label: "예매 완료" }
}

function getPerformanceStatus(
  rawStatus: string,
  showStartAt: Date,
  showEndAt: Date,
  now: Date
): StatusBadgeMeta<PerformancePhase> {
  if (rawStatus === "CANCELLED") {
    return { phase: "CANCELLED", label: "취소됨" }
  }

  if (now.getTime() > showEndAt.getTime()) {
    return { phase: "ENDED", label: "공연 종료" }
  }

  if (now.getTime() >= showStartAt.getTime() && now.getTime() <= showEndAt.getTime()) {
    return { phase: "LIVE", label: "공연 중" }
  }

  return { phase: "UPCOMING", label: "공연 예정" }
}

export function getShowDisplayMeta(show: ShowDisplayTarget, now = new Date()): ShowDisplayMeta {
  const reservationStartAt = toDate(show.reservation.startDate, "start")
  const reservationEndAt = toDate(show.reservation.endDate, "end")
  const showStartAt = toDate(show.show.showStartDate, "start")
  const showEndAt = toDate(show.show.showEndDate, "end")
  const reservation = getReservationStatus(reservationStartAt, reservationEndAt, now)
  const performance = getPerformanceStatus(show.status, showStartAt, showEndAt, now)

  if (performance.phase === "CANCELLED") {
    return {
      reservation,
      performance,
      badges: [performance],
      priority: 5,
      sortTimestamp: showEndAt.getTime(),
    }
  }

  if (performance.phase === "ENDED") {
    return {
      reservation,
      performance,
      badges: [performance, reservation],
      priority: 4,
      sortTimestamp: showEndAt.getTime(),
    }
  }

  if (reservation.phase === "TICKETING") {
    return {
      reservation,
      performance,
      badges: [reservation, performance],
      priority: 0,
      sortTimestamp: reservationEndAt.getTime(),
    }
  }

  if (performance.phase === "LIVE") {
    return {
      reservation,
      performance,
      badges: [performance, reservation],
      priority: 1,
      sortTimestamp: showEndAt.getTime(),
    }
  }

  if (reservation.phase === "CLOSED") {
    return {
      reservation,
      performance,
      badges: [reservation, performance],
      priority: 2,
      sortTimestamp: showStartAt.getTime(),
    }
  }

  return {
    reservation,
    performance,
    badges: [reservation, performance],
    priority: 3,
    sortTimestamp: reservationStartAt.getTime(),
  }
}

export function compareShowsByDisplayOrder<T extends ShowDisplayTarget>(left: T, right: T) {
  const leftMeta = getShowDisplayMeta(left)
  const rightMeta = getShowDisplayMeta(right)

  if (leftMeta.priority !== rightMeta.priority) {
    return leftMeta.priority - rightMeta.priority
  }

  if (leftMeta.performance.phase === "ENDED" || leftMeta.performance.phase === "CANCELLED") {
    return rightMeta.sortTimestamp - leftMeta.sortTimestamp
  }

  return leftMeta.sortTimestamp - rightMeta.sortTimestamp
}
