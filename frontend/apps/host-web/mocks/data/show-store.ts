export interface Grade {
  sectionId: number
  gradeName: string
  price: number
  colorCode: string
  ticketEffectId?: number
}

export interface Stakeholder {
  role: "organizer" | "artist"
  name: string
  businessNo?: string
  phone?: string
  shareBps: number
}

export interface RefundItem {
  daysRemaining: number
  refundRate: number
}

export interface SessionItem {
  sessionId: number
  sessionDate: string
  sessionStartDate: string
  capacity: number
}

export interface Event {
  showId: number
  title: string
  artistName: string
  posterUrl: string
  venue: {
    venueId: number
    name: string
    address: string
  }
  show: {
    startAt: string
    endAt: string
  }
  reservation: {
    openAt: string
    closeAt: string
  }
  description: string
  purchaseLimit: number
  grade: Grade[]
  stakeholders: Stakeholder[]
  refundPolicy: RefundItem[]
  sessionInfo: SessionItem[]
  status: string
  createdAt: string
  updatedAt: string
  capacity: number
  soldSeats: number
  enteredCount: number
  notEnteredCount: number
  emptyCount: number
  likes: number
}

export interface Venue {
  venueId: number
  name: string
  capacity: number
}

export interface Section {
  sectionId: number
  sectionName: string
}

export interface TicketEffect {
  id: number
  effect: string
}

export interface LikedShow {
  showId: number
  title: string
  posterUrl: string
  venue: string
  showDate: string
  status: string
}

export interface MyPageShowSummary {
  showId: number
  title: string
  posterUrl: string
  venue: string
  purchaseLimit: number
  region: string
  show: {
    showStartDate: string
    showEndDate: string
  }
  reservation: {
    startDate: string
    endDate: string
  }
  status: string
}

export const mockSectionsByVenue: Record<number, Section[]> = {
  1: [
    { sectionId: 1, sectionName: "가 구역" },
    { sectionId: 2, sectionName: "나 구역" },
    { sectionId: 3, sectionName: "다 구역" },
    { sectionId: 4, sectionName: "라 구역" },
  ],
  2: [
    { sectionId: 5, sectionName: "1층 스탠딩" },
    { sectionId: 6, sectionName: "2층 지정석" },
  ],
  3: [
    { sectionId: 7, sectionName: "피크닉존" },
    { sectionId: 8, sectionName: "그랜드홀" },
  ],
  4: [{ sectionId: 9, sectionName: "단일 층" }],
}

export const mockTicketEffects: TicketEffect[] = [
  { id: 1, effect: "glow1" },
  { id: 2, effect: "glow2" },
  { id: 3, effect: "glow3" },
  { id: 4, effect: "glow4" },
]

export const mockVenues: Venue[] = [
  { venueId: 1, name: "올림픽홀", capacity: 2500 },
  { venueId: 2, name: "블루노트 서울", capacity: 300 },
  { venueId: 3, name: "난지한강공원", capacity: 10000 },
  { venueId: 4, name: "홍대 롤링홀", capacity: 200 },
]

export const mockEvents: Event[] = [
  {
    showId: 42,
    title: "CHEKET LIVE: Spring Night",
    artistName: "에스파",
    posterUrl: "/images/poster-1.jpg",
    venue: {
      venueId: 1,
      name: "올림픽공원 올림픽홀",
      address: "서울특별시 송파구 올림픽로 424",
    },
    show: {
      startAt: "2026-03-20T19:30:00",
      endAt: "2026-03-28T21:30:00",
    },
    reservation: {
      openAt: "2026-03-05T12:00:00",
      closeAt: "2026-03-20T18:00:00",
    },
    description:
      "CHEKET 아티스트들과 함께하는 단독 공연입니다.\n- 전석 지정좌석\n- 만 7세 이상 관람가",
    purchaseLimit: 2,
    grade: [
      { sectionId: 1, gradeName: "VIP", price: 150000, colorCode: "#7C6EF0", ticketEffectId: 1 },
      { sectionId: 1, gradeName: "GOLD", price: 120000, colorCode: "#c8ff00ff", ticketEffectId: 2 },
      { sectionId: 2, gradeName: "S", price: 100000, colorCode: "#aaaaaa", ticketEffectId: 3 },
    ],
    stakeholders: [
      { role: "organizer", name: "뮤직페스티벌 주최사", businessNo: "123-45-67890", shareBps: 7000 },
      { role: "artist", name: "박지연", phone: "010-1234-5678", shareBps: 3000 },
    ],
    refundPolicy: [
      { daysRemaining: 14, refundRate: 100 },
      { daysRemaining: 7, refundRate: 70 },
      { daysRemaining: 3, refundRate: 50 },
      { daysRemaining: 1, refundRate: 0 },
    ],
    sessionInfo: [
      { sessionId: 1, sessionDate: "2026-03-20", sessionStartDate: "19:30", capacity: 1200 },
      { sessionId: 2, sessionDate: "2026-03-21", sessionStartDate: "19:30", capacity: 1200 },
    ],
    status: "UPCOMING",
    createdAt: "2026-02-28T14:00:00",
    updatedAt: "2026-02-28T15:30:00",
    capacity: 2400,
    soldSeats: 2000,
    enteredCount: 0,
    notEnteredCount: 0,
    emptyCount: 400,
    likes: 1240,
  },
  {
    showId: 43,
    title: "봄날의 재즈 나이트",
    artistName: "재즈 올스타즈",
    posterUrl: "/images/poster-2.jpg",
    venue: {
      venueId: 2,
      name: "블루노트 서울",
      address: "서울 마포구",
    },
    show: {
      startAt: "2026-04-20T20:00:00",
      endAt: "2026-04-20T22:30:00",
    },
    reservation: {
      openAt: "2026-03-15T14:00:00",
      closeAt: "2026-04-19T23:59:59",
    },
    description:
      "봄밤의 감성을 채워줄 재즈 공연. 국내외 최고의 재즈 뮤지션들이 펼치는 특별한 무대입니다.",
    purchaseLimit: 4,
    grade: [{ sectionId: 3, gradeName: "일반", price: 80000, colorCode: "#000000", ticketEffectId: 1 }],
    stakeholders: [
      { role: "organizer", name: "재즈협회", businessNo: "222-22", shareBps: 5000 },
      { role: "artist", name: "재즈밴드", phone: "010-1111-2222", shareBps: 5000 },
    ],
    refundPolicy: [
      { daysRemaining: 5, refundRate: 100 },
      { daysRemaining: 0, refundRate: 0 },
    ],
    sessionInfo: [
      { sessionId: 3, sessionDate: "2026-04-20", sessionStartDate: "20:00", capacity: 300 },
    ],
    status: "UPCOMING",
    createdAt: "2026-03-01T10:00:00",
    updatedAt: "2026-03-01T10:00:00",
    capacity: 300,
    soldSeats: 280,
    enteredCount: 250,
    notEnteredCount: 30,
    emptyCount: 20,
    likes: 560,
  },
  {
    showId: 44,
    title: "Rolling Indie Night",
    artistName: "실리카겔 & 잔나비",
    posterUrl: "/images/poster-3.jpg",
    venue: {
      venueId: 4,
      name: "홍대 롤링홀",
      address: "서울 마포구 어울마당로 35",
    },
    show: {
      startAt: "2026-05-10T19:00:00",
      endAt: "2026-05-10T22:00:00",
    },
    reservation: {
      openAt: "2026-04-01T14:00:00",
      closeAt: "2026-05-09T23:59:59",
    },
    description:
      "대한민국 인디 록의 중심, 롤링홀에서 펼쳐지는 뜨거운 인디 밴드들의 릴레이 공연! 스트레스를 날려버릴 강렬한 사운드가 여러분을 기다립니다.",
    purchaseLimit: 4,
    grade: [{ sectionId: 4, gradeName: "스탠딩", price: 55000, colorCode: "#FF5733", ticketEffectId: 4 }],
    stakeholders: [
      { role: "organizer", name: "인디뮤직네트워크", businessNo: "333-33-33333", shareBps: 8000 },
      { role: "artist", name: "록 밴드 연합", phone: "010-3333-3333", shareBps: 2000 },
    ],
    refundPolicy: [
      { daysRemaining: 3, refundRate: 100 },
      { daysRemaining: 0, refundRate: 0 },
    ],
    sessionInfo: [
      { sessionId: 4, sessionDate: "2026-05-10", sessionStartDate: "19:00", capacity: 200 },
    ],
    status: "UPCOMING",
    createdAt: "2026-03-02T10:00:00",
    updatedAt: "2026-03-02T10:00:00",
    capacity: 200,
    soldSeats: 180,
    enteredCount: 0,
    notEnteredCount: 180,
    emptyCount: 20,
    likes: 850,
  },
  {
    showId: 45,
    title: "2026 한강 썸머 뮤직 페스티벌",
    artistName: "Various Artists",
    posterUrl: "/images/poster-4.jpg",
    venue: {
      venueId: 3,
      name: "난지한강공원",
      address: "서울 마포구 한강난지로 162",
    },
    show: {
      startAt: "2026-07-25T13:00:00",
      endAt: "2026-07-26T22:00:00",
    },
    reservation: {
      openAt: "2026-05-01T12:00:00",
      closeAt: "2026-07-24T23:59:59",
    },
    description:
      "무더운 여름을 시원하게 날려버릴 한강 최대의 야외 음악 축제! 2일 동안 펼쳐지는 국내외 최정상급 아티스트들의 무대와 다채로운 즐길거리가 풍성하게 준비되어 있습니다.",
    purchaseLimit: 4,
    grade: [
      { sectionId: 5, gradeName: "2일권", price: 180000, colorCode: "#1D4ED8", ticketEffectId: 2 },
      { sectionId: 6, gradeName: "1일권(토)", price: 110000, colorCode: "#3B82F6", ticketEffectId: 3 },
      { sectionId: 7, gradeName: "1일권(일)", price: 110000, colorCode: "#60A5FA", ticketEffectId: 4 },
    ],
    stakeholders: [
      { role: "organizer", name: "썸머페스트 조직위", businessNo: "444-44-44444", shareBps: 6000 },
      { role: "artist", name: "참여 아티스트 전체", phone: "010-4444-4444", shareBps: 4000 },
    ],
    refundPolicy: [
      { daysRemaining: 30, refundRate: 100 },
      { daysRemaining: 14, refundRate: 70 },
      { daysRemaining: 7, refundRate: 50 },
      { daysRemaining: 1, refundRate: 0 },
    ],
    sessionInfo: [
      { sessionId: 5, sessionDate: "2026-07-25", sessionStartDate: "13:00", capacity: 10000 },
      { sessionId: 6, sessionDate: "2026-07-26", sessionStartDate: "13:00", capacity: 10000 },
    ],
    status: "UPCOMING",
    createdAt: "2026-03-03T10:00:00",
    updatedAt: "2026-03-03T10:00:00",
    capacity: 20000,
    soldSeats: 15500,
    enteredCount: 0,
    notEnteredCount: 15500,
    emptyCount: 4500,
    likes: 4200,
  },
]

export const mockLikes: LikedShow[] = [
  {
    showId: 42,
    title: "CHEKET LIVE: Spring Night",
    posterUrl: "/images/poster-1.jpg",
    venue: "올림픽공원 올림픽홀",
    showDate: "2026-03-20T19:30:00",
    status: "UPCOMING",
  },
  {
    showId: 44,
    title: "Rolling Indie Night",
    posterUrl: "/images/poster-3.jpg",
    venue: "홍대 롤링홀",
    showDate: "2026-05-10T19:00:00",
    status: "UPCOMING",
  },
]

const INITIAL_MY_PAGE_SHOWS: MyPageShowSummary[] = [
  {
    showId: 42,
    title: "CHEKET LIVE: Spring Night",
    posterUrl: "/images/poster-1.jpg",
    venue: "올림픽공원 올림픽홀",
    purchaseLimit: 2,
    region: "SEOUL",
    show: {
      showStartDate: "2026-03-20",
      showEndDate: "2026-03-28",
    },
    reservation: {
      startDate: "2026-03-05T12:00:00",
      endDate: "2026-03-20T18:00:00",
    },
    status: "DRAFT",
  },
  {
    showId: 43,
    title: "봄날의 재즈 나이트",
    posterUrl: "/images/poster-2.jpg",
    venue: "블루노트 서울",
    purchaseLimit: 4,
    region: "SEOUL",
    show: {
      showStartDate: "2026-04-20",
      showEndDate: "2026-04-20",
    },
    reservation: {
      startDate: "2026-03-15T14:00:00",
      endDate: "2026-04-19T23:59:59",
    },
    status: "UPCOMING",
  },
  {
    showId: 44,
    title: "한여름 밤의 인디페스타",
    posterUrl: "/images/poster-3.jpg",
    venue: "난지한강공원",
    purchaseLimit: 4,
    region: "SEOUL",
    show: {
      showStartDate: "2026-08-15",
      showEndDate: "2026-08-16",
    },
    reservation: {
      startDate: "2026-07-20T12:00:00",
      endDate: "2026-08-14T23:59:59",
    },
    status: "TICKETING",
  },
  {
    showId: 45,
    title: "가을 클래식 선율",
    posterUrl: "/images/poster-4.jpg",
    venue: "예술의전당 콘서트홀",
    purchaseLimit: 2,
    region: "SEOUL",
    show: {
      showStartDate: "2026-10-10",
      showEndDate: "2026-10-12",
    },
    reservation: {
      startDate: "2026-09-01T10:00:00",
      endDate: "2026-10-09T18:00:00",
    },
    status: "UPCOMING",
  },
]

export function cloneMockEvent(event: Event) {
  return {
    ...event,
    venue: { ...event.venue },
    show: { ...event.show },
    reservation: { ...event.reservation },
    grade: event.grade.map((grade) => ({ ...grade })),
    stakeholders: event.stakeholders.map((stakeholder) => ({ ...stakeholder })),
    refundPolicy: event.refundPolicy.map((policy) => ({ ...policy })),
    sessionInfo: event.sessionInfo.map((session) => ({ ...session })),
  }
}

export const mockEventStore = mockEvents.map(cloneMockEvent)
export const myPageShowsStore = INITIAL_MY_PAGE_SHOWS.map((show) => ({
  ...show,
  show: { ...show.show },
  reservation: { ...show.reservation },
}))

export function getShowDetailSnapshot(showIdValue: string | readonly string[] | undefined) {
  const showId = Number(showIdValue)

  if (!Number.isInteger(showId)) {
    return null
  }

  const event = mockEventStore.find((item) => item.showId === showId)

  if (!event) {
    return null
  }

  return {
    showId: event.showId,
    title: event.title,
    posterUrl: event.posterUrl,
    artistName: event.artistName,
    venue: {
      venueId: event.venue.venueId,
      name: event.venue.name,
      address: event.venue.address,
    },
    show: {
      showStartDate: event.show.startAt.slice(0, 10),
      showEndDate: event.show.endAt.slice(0, 10),
    },
    reservation: {
      startDate: event.reservation.openAt,
      endDate: event.reservation.closeAt,
    },
    description: event.description,
    purchaseLimit: event.purchaseLimit,
    likeCount: event.likes,
    grade: event.grade,
    stakeholders: event.stakeholders.map((stakeholder, index) => ({
      role: stakeholder.role,
      userId: 15 + index,
      shareBps: stakeholder.shareBps,
      name: stakeholder.name,
    })),
    refundPolicy: event.refundPolicy,
    sessionInfo: event.sessionInfo,
    status: event.status,
    createdAt: event.createdAt,
    updatedAt: event.updatedAt,
  }
}

export function getNextShowId() {
  const currentMaxShowId = mockEventStore.reduce(
    (maxShowId, event) => Math.max(maxShowId, event.showId),
    0
  )

  return currentMaxShowId + 1
}

export function getVenueAddress(venueId: number) {
  return (
    mockEventStore.find((event) => event.venue.venueId === venueId)?.venue.address ??
    "주소 정보 없음"
  )
}
