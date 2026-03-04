import { Event, Seat, Ticket, ResaleItem, User } from './types'

export const MOCK_USER: User = {
  id: 'user_001',
  name: '김민지',
  phone: '010-1234-5678',
  walletAddress: '0x3a9F...dE42',
  ctkBalance: 2400,
}

export const MOCK_EVENTS: Event[] = [
  {
    id: 'evt_001',
    name: 'AESPA WORLD TOUR 2026',
    date: '2026.04.12 (토) ~ 04.13 (일)',
    dates: [
      { id: 'evt_001_d1', label: '2026.04.12 (토) 18:00', day: 'DAY 1' },
      { id: 'evt_001_d2', label: '2026.04.12 (토) 21:00', day: 'DAY 1 (추가)' },
      { id: 'evt_001_d3', label: '2026.04.13 (일) 18:00', day: 'DAY 2' },
    ],
    venue: '올림픽체조경기장, 서울',
    region: '서울',
    poster: '/posters/concert1.jpg',
    status: 'ON_SALE',
    maxPerUser: 4,
    openDate: '2026-04-12',
    grades: [
      { name: 'VIP', price: 180000, remaining: 12, color: '#f59e0b' },
      { name: 'R석', price: 140000, remaining: 34, color: '#ef4444' },
      { name: 'S석', price: 110000, remaining: 87, color: '#3b82f6' },
      { name: 'A석', price: 88000, remaining: 152, color: '#22c55e' },
    ],
    description: 'aespa의 월드 투어 서울 공연. 대형 LED 연출과 라이브 밴드로 구성된 프리미엄 무대를 선보입니다.',
  },
  {
    id: 'evt_002',
    name: 'METALLICA M72 WORLD TOUR',
    date: '2026.05.24 (토) ~ 05.25 (일)',
    dates: [
      { id: 'evt_002_d1', label: '2026.05.24 (토) 18:00', day: 'DAY 1' },
      { id: 'evt_002_d2', label: '2026.05.25 (일) 18:00', day: 'DAY 2' },
    ],
    venue: '고척스카이돔, 서울',
    region: '서울',
    poster: '/posters/concert2.jpg',
    status: 'ON_SALE',
    maxPerUser: 2,
    openDate: '2026-05-24',
    grades: [
      { name: 'VIP PIT', price: 250000, remaining: 0, color: '#f59e0b' },
      { name: 'GA PIT', price: 180000, remaining: 5, color: '#ef4444' },
      { name: 'FLOOR', price: 150000, remaining: 44, color: '#3b82f6' },
      { name: 'STAND', price: 120000, remaining: 103, color: '#22c55e' },
    ],
    description: 'METALLICA M72 월드투어 내한 공연. 원형 스테이지 기반의 몰입형 사운드를 제공합니다.',
  },
  {
    id: 'evt_003',
    name: '서울 필하모닉 신년 콘서트',
    date: '2026.01.01 (목) 15:00',
    venue: '예술의전당 콘서트홀, 서울',
    region: '서울',
    poster: '/posters/concert3.jpg',
    status: 'SOLD_OUT',
    maxPerUser: 6,
    openDate: '2026-01-01',
    grades: [
      { name: 'VIP', price: 120000, remaining: 0, color: '#f59e0b' },
      { name: 'R석', price: 90000, remaining: 0, color: '#ef4444' },
      { name: 'S석', price: 70000, remaining: 0, color: '#3b82f6' },
    ],
    description: '새해를 여는 서울 필하모닉의 클래식 갈라 콘서트.',
  },
  {
    id: 'evt_004',
    name: 'ULTRA KOREA 2026',
    date: '2026.06.07 (토) ~ 06.08 (일)',
    dates: [
      { id: 'evt_004_d1', label: '2026.06.07 (토) 14:00', day: 'DAY 1' },
      { id: 'evt_004_d2', label: '2026.06.08 (일) 14:00', day: 'DAY 2' },
    ],
    venue: '잠실종합운동장, 서울',
    region: '서울',
    poster: '/posters/concert4.jpg',
    status: 'ON_SALE',
    maxPerUser: 4,
    openDate: '2026-06-07',
    grades: [
      { name: 'VIP', price: 300000, remaining: 8, color: '#f59e0b' },
      { name: 'GA', price: 220000, remaining: 210, color: '#3b82f6' },
    ],
    description: '세계적인 EDM 페스티벌 ULTRA KOREA 2026.',
  },
  {
    id: 'evt_005',
    name: 'JARASUM JAZZ FESTIVAL',
    date: '2026.10.05 (월) 12:00',
    venue: '자라섬 가평군',
    region: '경기',
    poster: '/posters/concert5.jpg',
    status: 'ENDED',
    maxPerUser: 8,
    openDate: '2026-10-05',
    grades: [
      { name: '1일권', price: 88000, remaining: 0, color: '#22c55e' },
      { name: '2일권', price: 150000, remaining: 0, color: '#3b82f6' },
    ],
    description: '가을 대표 재즈 페스티벌 자라섬 재즈 페스티벌.',
  },
]

// ---------- Home screen extra data ----------

export const BANNER_SLIDES = [
  {
    id: 'b1',
    eventId: 'evt_004',
    image: '/posters/concert4.jpg',
    title: '2026 울트라 코리아',
    subtitle: 'ULTRA KOREA IS COMING',
    venue: '잠실종합운동장, 서울',
    dates: '2026.6.7 - 2026.6.8',
  },
  {
    id: 'b2',
    eventId: 'evt_001',
    image: '/posters/concert1.jpg',
    title: '2026 aespa 월드 투어',
    subtitle: 'AESPA WORLD TOUR',
    venue: '올림픽체조경기장, 서울',
    dates: '2026.4.12',
  },
  {
    id: 'b3',
    eventId: 'evt_002',
    image: '/posters/concert2.jpg',
    title: 'METALLICA 내한 공연',
    subtitle: 'METALLICA M72 WORLD TOUR',
    venue: '고척스카이돔, 서울',
    dates: '2026.5.24',
  },
]

export const CATEGORY_ICONS = [
  { id: 'musical', label: '뮤지컬', icon: '🎭' },
  { id: 'concert', label: '콘서트', icon: '🎤' },
  { id: 'sports', label: '스포츠', icon: '⚽' },
  { id: 'classic', label: '클래식', icon: '🎻' },
  { id: 'play', label: '연극', icon: '🎬' },
  { id: 'leisure', label: '레저/캠핑', icon: '🏕️' },
  { id: 'family', label: '아동/가족', icon: '👨‍👩‍👧' },
  { id: 'exhibit', label: '전시/행사', icon: '🖼️' },
  { id: 'special', label: '기획공연', icon: '✨' },
  { id: 'benefit', label: '이달의 혜택', icon: '🎁' },
]

export const RANKING_GENRES = ['콘서트', '뮤지컬', '스포츠', '전시/행사', '클래식']

export const RANKING_ITEMS = [
  { rank: 1, eventId: 'evt_001', name: 'AESPA WORLD TOUR 2026', venue: '올림픽체조경기장', poster: '/posters/concert1.jpg', genre: '콘서트' },
  { rank: 2, eventId: 'evt_002', name: 'METALLICA M72 WORLD TOUR', venue: '고척스카이돔', poster: '/posters/concert2.jpg', genre: '콘서트' },
  { rank: 3, eventId: 'evt_004', name: 'ULTRA KOREA 2026', venue: '잠실종합운동장', poster: '/posters/concert4.jpg', genre: '콘서트' },
  { rank: 4, eventId: 'evt_003', name: '서울 필하모닉 신년 콘서트', venue: '예술의전당', poster: '/posters/concert3.jpg', genre: '클래식' },
  { rank: 5, eventId: 'evt_005', name: 'JARASUM JAZZ FESTIVAL', venue: '자라섬 가평군', poster: '/posters/concert5.jpg', genre: '콘서트' },
]

export const OPEN_SCHEDULE = [
  {
    id: 'op_001',
    eventId: 'evt_001',
    name: 'AESPA WORLD TOUR 2026',
    openLabel: '오늘 18:00',
    openType: '일반예매',
    tags: ['단독판매'],
    poster: '/posters/concert1.jpg',
    isToday: true,
  },
  {
    id: 'op_002',
    eventId: 'evt_002',
    name: 'METALLICA M72 WORLD TOUR',
    openLabel: '내일 20:00',
    openType: '멤버십 선예매',
    tags: ['단독판매'],
    poster: '/posters/concert2.jpg',
    isToday: false,
  },
  {
    id: 'op_003',
    eventId: 'evt_003',
    name: '서울 필하모닉 신년 콘서트',
    openLabel: '03.03(화) 18:00',
    openType: '일반예매',
    tags: ['HOT', '단독판매'],
    poster: '/posters/concert3.jpg',
    isToday: false,
  },
  {
    id: 'op_004',
    eventId: 'evt_004',
    name: 'ULTRA KOREA 2026',
    openLabel: '03.10(화) 10:00',
    openType: '얼리버드',
    tags: ['단독판매'],
    poster: '/posters/concert4.jpg',
    isToday: false,
  },
]

export const DISCOUNT_ITEMS = [
  {
    id: 'dc_001',
    eventId: 'evt_005',
    name: 'JARASUM JAZZ FESTIVAL',
    venue: '자라섬 가평군',
    dates: '2026.10.5 ~ 10.7',
    discountType: '한정석 할인',
    discountPct: 40,
    finalPrice: 88000,
    countdownLabel: '00:00:00',
    isTimeDeal: true,
    poster: '/posters/concert5.jpg',
  },
  {
    id: 'dc_002',
    eventId: 'evt_003',
    name: '서울 필하모닉 신년 콘서트',
    venue: '예술의전당 콘서트홀',
    dates: '2026.1.1 ~ 1.3',
    discountType: 'R석 특가',
    discountPct: 50,
    finalPrice: 45000,
    countdownLabel: 'D-10 10:24:33',
    isTimeDeal: true,
    poster: '/posters/concert3.jpg',
  },
  {
    id: 'dc_003',
    eventId: 'evt_001',
    name: 'AESPA WORLD TOUR 2026',
    venue: '올림픽체조경기장',
    dates: '2026.4.12',
    discountType: 'S석 특가',
    discountPct: 20,
    finalPrice: 88000,
    countdownLabel: '10:24:33',
    isTimeDeal: false,
    poster: '/posters/concert1.jpg',
  },
]

export function generateSeats(eventId: string, gradeName?: string): Seat[] {
  const seats: Seat[] = []
  const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  const event = MOCK_EVENTS.find((e) => e.id === eventId)
  const grades = event?.grades ?? [{ name: 'R석', price: 140000, remaining: 50 }]

  // If gradeName is provided, only generate seats for that grade
  const targetGrade = gradeName ? grades.find((g) => g.name === gradeName) : null

  // Rows per grade when all grades shown; or fixed rows for single grade
  const gradeRowCount = Math.max(2, Math.floor(rows.length / grades.length))

  let seatIndex = 0
  rows.forEach((row, rowIdx) => {
    const gradeIndex = Math.min(Math.floor(rowIdx / gradeRowCount), grades.length - 1)
    const grade = targetGrade ?? grades[gradeIndex]

    // If filtering by grade, skip rows that belong to other grades
    if (targetGrade && grades[gradeIndex].name !== targetGrade.name) return

    for (let num = 1; num <= 12; num++) {
      let status: 'AVAILABLE' | 'LOCKED' | 'SOLD' = 'AVAILABLE'
      const rand = (seatIndex + rowIdx * 3) % 7
      if (rand === 2 || rand === 5) status = 'SOLD'
      else if (rand === 3) status = 'LOCKED'

      // Respect remaining count - mark extras as SOLD
      if (targetGrade && targetGrade.remaining === 0) status = 'SOLD'

      seats.push({
        id: `${eventId}_${row}${num}`,
        row,
        number: num,
        grade: grade.name,
        price: grade.price,
        status,
      })
      seatIndex++
    }
  })
  return seats
}

export const MOCK_TICKETS: Ticket[] = [
  {
    id: 'tkt_001',
    eventId: 'evt_001',
    eventName: 'AESPA WORLD TOUR 2025',
    eventDate: '2025.04.12 (토) 19:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatId: 'evt_001_C3',
    seatLabel: 'C열 3번',
    grade: 'R석',
    originalPrice: 140000,
    status: 'SOLD',
  },
  {
    id: 'tkt_002',
    eventId: 'evt_002',
    eventName: 'METALLICA M72 WORLD TOUR',
    eventDate: '2025.05.24 (토) 18:00',
    venue: '고척스카이돔, 서울',
    poster: '/posters/concert2.jpg',
    seatId: 'evt_002_B7',
    seatLabel: 'B열 7번',
    grade: 'FLOOR',
    originalPrice: 150000,
    status: 'LISTED',
    resalePrice: 145000,
  },
  {
    id: 'tkt_003',
    eventId: 'evt_005',
    eventName: 'JARASUM JAZZ FESTIVAL',
    eventDate: '2024.10.05 (토) 12:00',
    venue: '자라섬 가평군',
    poster: '/posters/concert5.jpg',
    seatId: 'evt_005_A2',
    seatLabel: 'A열 2번',
    grade: '2일권',
    originalPrice: 150000,
    status: 'USED',
    attendedDate: '2024.10.05',
  },
  {
    id: 'tkt_004',
    eventId: 'evt_003',
    eventName: '서울 필하모닉 신년 콘서트',
    eventDate: '2025.01.01 (수) 15:00',
    venue: '예술의전당 콘서트홀, 서울',
    poster: '/posters/concert3.jpg',
    seatId: 'evt_003_D5',
    seatLabel: 'D열 5번',
    grade: 'R석',
    originalPrice: 90000,
    status: 'EXPIRED',
  },
  {
    id: 'tkt_005',
    eventId: 'evt_001',
    eventName: 'AESPA WORLD TOUR 2025',
    eventDate: '2025.04.12 (토) 19:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatId: 'evt_001_A1',
    seatLabel: 'A열 1번',
    grade: 'VIP',
    originalPrice: 180000,
    status: 'USED',
    attendedDate: '2025.04.12',
  },
  {
    id: 'tkt_006',
    eventId: 'evt_002',
    eventName: 'METALLICA M72 WORLD TOUR',
    eventDate: '2025.05.24 (토) 18:00',
    venue: '고척스카이돔, 서울',
    poster: '/posters/concert2.jpg',
    seatId: 'evt_002_F11',
    seatLabel: 'F열 11번',
    grade: 'GA PIT',
    originalPrice: 180000,
    status: 'USED',
    attendedDate: '2025.05.24',
  },
  {
    id: 'tkt_007',
    eventId: 'evt_004',
    eventName: 'ULTRA KOREA 2026',
    eventDate: '2026.06.07 (토) 14:00',
    venue: '잠실종합운동장, 서울',
    poster: '/posters/concert4.jpg',
    seatId: 'evt_004_GA215',
    seatLabel: 'GA 구역 215번',
    grade: 'GA',
    originalPrice: 220000,
    status: 'USED',
    attendedDate: '2026.06.07',
  },
  {
    id: 'tkt_008',
    eventId: 'evt_001',
    eventName: 'AESPA WORLD TOUR 2026',
    eventDate: '2026.04.13 (일) 18:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatId: 'evt_001_B9',
    seatLabel: 'B열 9번',
    grade: 'S석',
    originalPrice: 110000,
    status: 'USED',
    attendedDate: '2026.04.13',
  },
  {
    id: 'tkt_009',
    eventId: 'evt_002',
    eventName: 'METALLICA M72 WORLD TOUR',
    eventDate: '2026.05.25 (일) 18:00',
    venue: '고척스카이돔, 서울',
    poster: '/posters/concert2.jpg',
    seatId: 'evt_002_C4',
    seatLabel: 'C열 4번',
    grade: 'STAND',
    originalPrice: 120000,
    status: 'USED',
    attendedDate: '2026.05.25',
  },
  {
    id: 'tkt_010',
    eventId: 'evt_003',
    eventName: '서울 필하모닉 신년 콘서트',
    eventDate: '2026.01.01 (목) 15:00',
    venue: '예술의전당 콘서트홀, 서울',
    poster: '/posters/concert3.jpg',
    seatId: 'evt_003_R12',
    seatLabel: 'R석 12번',
    grade: 'R석',
    originalPrice: 90000,
    status: 'USED',
    attendedDate: '2026.01.01',
  },
  {
    id: 'tkt_011',
    eventId: 'evt_005',
    eventName: 'JARASUM JAZZ FESTIVAL',
    eventDate: '2026.10.05 (월) 12:00',
    venue: '자라섬 가평군',
    poster: '/posters/concert5.jpg',
    seatId: 'evt_005_B5',
    seatLabel: 'B열 5번',
    grade: '1일권',
    originalPrice: 88000,
    status: 'USED',
    attendedDate: '2026.10.05',
  },
  {
    id: 'tkt_012',
    eventId: 'evt_004',
    eventName: 'ULTRA KOREA 2026',
    eventDate: '2026.06.08 (일) 14:00',
    venue: '잠실종합운동장, 서울',
    poster: '/posters/concert4.jpg',
    seatId: 'evt_004_VIP7',
    seatLabel: 'VIP 7번',
    grade: 'VIP',
    originalPrice: 300000,
    status: 'USED',
    attendedDate: '2026.06.08',
  },
  {
    id: 'tkt_013',
    eventId: 'evt_001',
    eventName: 'AESPA WORLD TOUR 2026',
    eventDate: '2026.04.12 (토) 18:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatId: 'evt_001_A6',
    seatLabel: 'A열 6번',
    grade: 'VIP',
    originalPrice: 180000,
    status: 'USED',
    attendedDate: '2026.04.12',
  },
  {
    id: 'tkt_014',
    eventId: 'evt_002',
    eventName: 'METALLICA M72 WORLD TOUR',
    eventDate: '2026.05.24 (토) 18:00',
    venue: '고척스카이돔, 서울',
    poster: '/posters/concert2.jpg',
    seatId: 'evt_002_D14',
    seatLabel: 'D열 14번',
    grade: 'GA PIT',
    originalPrice: 180000,
    status: 'USED',
    attendedDate: '2026.05.24',
  },
]

export const MOCK_RESALE_ITEMS: ResaleItem[] = [
  {
    id: 'rs_001',
    ticketId: 'tkt_002',
    eventName: 'METALLICA M72 WORLD TOUR',
    eventDate: '2025.05.24 (토) 18:00',
    venue: '고척스카이돔, 서울',
    poster: '/posters/concert2.jpg',
    seatLabel: 'B열 7번',
    grade: 'FLOOR',
    originalPrice: 150000,
    resalePrice: 145000,
    sellerId: 'user_001',
  },
  {
    id: 'rs_002',
    ticketId: 'tkt_ext_001',
    eventName: 'AESPA WORLD TOUR 2025',
    eventDate: '2025.04.12 (토) 19:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatLabel: 'E열 4번',
    grade: 'S석',
    originalPrice: 110000,
    resalePrice: 105000,
    sellerId: 'user_042',
  },
  {
    id: 'rs_003',
    ticketId: 'tkt_ext_002',
    eventName: 'ULTRA KOREA 2025',
    eventDate: '2025.06.07 (토) 14:00',
    venue: '잠실종합운동장, 서울',
    poster: '/posters/concert4.jpg',
    seatLabel: 'GA 구역 215번',
    grade: 'GA',
    originalPrice: 220000,
    resalePrice: 210000,
    sellerId: 'user_088',
  },
  {
    id: 'rs_004',
    ticketId: 'tkt_ext_003',
    eventName: 'AESPA WORLD TOUR 2025',
    eventDate: '2025.04.12 (토) 19:00',
    venue: '올림픽체조경기장, 서울',
    poster: '/posters/concert1.jpg',
    seatLabel: 'A열 1번',
    grade: 'VIP',
    originalPrice: 180000,
    resalePrice: 175000,
    sellerId: 'user_019',
  },
]
