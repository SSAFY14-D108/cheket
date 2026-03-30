import { Event, Seat, SeatStatus, Ticket, ResaleItem, User } from "./types";

export const MOCK_USER: User = {
  id: "user_001",
  name: "김민준",
  phone: "010-1234-5678",
  email: "minjun.kim@cheket.app",
  walletAddress: "0x3a9F...dE42",
  ctkBalance: 2400,
};

export const MOCK_EVENTS: Event[] = [
  {
    id: "evt_001",
    name: "AESPA WORLD TOUR 2026",
    artistName: "aespa",
    date: "2026.04.12 (토) ~ 04.13 (일)",
    dates: [
      { id: "evt_001_d1", label: "2026.04.12 (토) 18:00", day: "DAY 1" },
      { id: "evt_001_d2", label: "2026.04.12 (토) 21:00", day: "DAY 1 (야간)" },
      { id: "evt_001_d3", label: "2026.04.13 (일) 18:00", day: "DAY 2" },
    ],
    venue: "올림픽체조경기장, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert1.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-04-12",
    grades: [
      { name: "VIP", price: 180000, remaining: 12, color: "#f59e0b" },
      { name: "R석", price: 140000, remaining: 34, color: "#ef4444" },
      { name: "S석", price: 110000, remaining: 87, color: "#3b82f6" },
      { name: "A석", price: 88000, remaining: 152, color: "#22c55e" },
    ],
    refundRules: [
      { id: "evt_001_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_001_r2",
        daysBefore: 3,
        feeRate: 0.1,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_001_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_001_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description:
      "aespa의 첫 번째 월드 투어. 서울 단독 공연으로 펼쳐지는 화려한 무대.",
  },
  {
    id: "evt_002",
    name: "METALLICA M72 WORLD TOUR",
    artistName: "Metallica",
    date: "2026.05.24 (토) ~ 05.25 (일)",
    dates: [
      { id: "evt_002_d1", label: "2026.05.24 (토) 18:00", day: "DAY 1" },
      { id: "evt_002_d2", label: "2026.05.25 (일) 18:00", day: "DAY 2" },
    ],
    venue: "고척스카이돔, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert2.jpg",
    status: "ON_SALE",
    maxPerUser: 2,
    openDate: "2026-05-24",
    grades: [
      { name: "VIP PIT", price: 250000, remaining: 0, color: "#f59e0b" },
      { name: "GA PIT", price: 180000, remaining: 5, color: "#ef4444" },
      { name: "FLOOR", price: 150000, remaining: 44, color: "#3b82f6" },
      { name: "STAND", price: 120000, remaining: 103, color: "#22c55e" },
    ],
    refundRules: [
      {
        id: "evt_002_r1",
        daysBefore: 10,
        feeRate: 0.05,
        label: "공연 10일 전까지",
      },
      {
        id: "evt_002_r2",
        daysBefore: 5,
        feeRate: 0.15,
        label: "공연 5일 전까지",
      },
      {
        id: "evt_002_r3",
        daysBefore: 2,
        feeRate: 0.3,
        label: "공연 2일 전까지",
      },
      {
        id: "evt_002_r4",
        daysBefore: 0,
        feeRate: 1,
        label: "공연 1일 전 및 당일",
      },
    ],
    description:
      "메탈리카의 M72 월드투어 한국 공연. 역사적인 무대를 경험하세요.",
  },
  {
    id: "evt_003",
    name: "서울 필하모닉 뉴이어 콘서트",
    artistName: "Seoul Philharmonic Orchestra",
    date: "2026.01.01 (수) 15:00",
    venue: "예술의전당 콘서트홀, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert3.jpg",
    status: "SOLD_OUT",
    maxPerUser: 6,
    openDate: "2026-01-01",
    grades: [
      { name: "VIP", price: 120000, remaining: 0, color: "#f59e0b" },
      { name: "R석", price: 90000, remaining: 0, color: "#ef4444" },
      { name: "S석", price: 70000, remaining: 0, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_003_r1",
        daysBefore: 14,
        feeRate: 0,
        label: "공연 14일 전까지",
      },
      {
        id: "evt_003_r2",
        daysBefore: 7,
        feeRate: 0.1,
        label: "공연 7일 전까지",
      },
      {
        id: "evt_003_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_003_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "새해를 여는 서울 필하모닉의 특별 갈라 콘서트.",
  },
  {
    id: "evt_004",
    name: "ULTRA KOREA 2026",
    artistName: "Martin Garrix, Armin van Buuren 외",
    date: "2026.06.07 (토) ~ 06.08 (일)",
    dates: [
      { id: "evt_004_d1", label: "2026.06.07 (토) 14:00", day: "DAY 1" },
      { id: "evt_004_d2", label: "2026.06.08 (일) 14:00", day: "DAY 2" },
    ],
    venue: "잠실종합운동장, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert4.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-06-07",
    grades: [
      { name: "VIP", price: 300000, remaining: 8, color: "#f59e0b" },
      { name: "GA", price: 220000, remaining: 210, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_004_r1",
        daysBefore: 14,
        feeRate: 0.05,
        label: "공연 14일 전까지",
      },
      {
        id: "evt_004_r2",
        daysBefore: 7,
        feeRate: 0.1,
        label: "공연 7일 전까지",
      },
      {
        id: "evt_004_r3",
        daysBefore: 3,
        feeRate: 0.25,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_004_r4",
        daysBefore: 0,
        feeRate: 1,
        label: "공연 2일 전 및 당일",
      },
    ],
    description: "세계 최대 EDM 페스티벌 울트라코리아 2026.",
  },
  {
    id: "evt_005",
    name: "JARASUM JAZZ FESTIVAL",
    artistName: "Pat Metheny, Snarky Puppy 외",
    date: "2026.10.05 (토) 12:00",
    venue: "자라섬, 가평",
    region: "경기",
    regionCode: "41",
    poster: "/posters/concert5.jpg",
    status: "SOLD_OUT",
    maxPerUser: 8,
    openDate: "2026-10-05",
    grades: [
      { name: "1일권", price: 88000, remaining: 0, color: "#22c55e" },
      { name: "2일권", price: 150000, remaining: 0, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_005_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_005_r2",
        daysBefore: 3,
        feeRate: 0.2,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_005_r3",
        daysBefore: 1,
        feeRate: 0.3,
        label: "공연 1일 전까지",
      },
      { id: "evt_005_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "아시아 최대 재즈 축제 자라섬 국제 재즈 페스티벌.",
  },
  {
    id: "evt_006",
    name: "COLDPLAY MOON MUSIC LIVE",
    artistName: "Coldplay",
    date: "2026.07.11 (토) 19:00",
    venue: "서울월드컵경기장, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert1.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-07-11",
    grades: [
      { name: "VIP", price: 220000, remaining: 16, color: "#f59e0b" },
      { name: "R석", price: 165000, remaining: 42, color: "#ef4444" },
      { name: "S석", price: 121000, remaining: 95, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_006_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_006_r2",
        daysBefore: 3,
        feeRate: 0.1,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_006_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_006_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "스타디움 전체를 채우는 콜드플레이의 대형 라이브 공연.",
  },
  {
    id: "evt_007",
    name: "IU THE GOLDEN HOUR ENCORE",
    artistName: "IU",
    date: "2026.08.02 (일) 18:00",
    venue: "부산아시아드주경기장, 부산",
    region: "부산",
    regionCode: "26",
    poster: "/posters/concert3.jpg",
    status: "ON_SALE",
    maxPerUser: 2,
    openDate: "2026-08-02",
    grades: [
      { name: "VIP", price: 198000, remaining: 24, color: "#f59e0b" },
      { name: "R석", price: 154000, remaining: 66, color: "#ef4444" },
      { name: "S석", price: 121000, remaining: 143, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_007_r1",
        daysBefore: 5,
        feeRate: 0.05,
        label: "공연 5일 전까지",
      },
      {
        id: "evt_007_r2",
        daysBefore: 2,
        feeRate: 0.15,
        label: "공연 2일 전까지",
      },
      {
        id: "evt_007_r3",
        daysBefore: 1,
        feeRate: 0.25,
        label: "공연 1일 전까지",
      },
      { id: "evt_007_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "앵콜 무대로 다시 돌아온 골든 아워 라이브.",
  },
  {
    id: "evt_008",
    name: "SEVENTEEN FOLLOW AGAIN",
    artistName: "SEVENTEEN",
    date: "2026.08.15 (토) ~ 08.16 (일)",
    venue: "인천문학경기장, 인천",
    region: "인천",
    regionCode: "28",
    poster: "/posters/concert4.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-08-15",
    grades: [
      { name: "VIP", price: 210000, remaining: 11, color: "#f59e0b" },
      { name: "R석", price: 165000, remaining: 58, color: "#ef4444" },
      { name: "S석", price: 132000, remaining: 127, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_008_r1",
        daysBefore: 7,
        feeRate: 0.05,
        label: "공연 7일 전까지",
      },
      {
        id: "evt_008_r2",
        daysBefore: 3,
        feeRate: 0.15,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_008_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_008_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "대형 스타디움에서 열리는 세븐틴 앙코르 투어.",
  },
  {
    id: "evt_009",
    name: "BLACKPINK IN YOUR AREA SPECIAL",
    artistName: "BLACKPINK",
    date: "2026.09.06 (일) 18:30",
    venue: "대구스타디움, 대구",
    region: "대구",
    regionCode: "27",
    poster: "/posters/concert2.jpg",
    status: "ON_SALE",
    maxPerUser: 2,
    openDate: "2026-09-06",
    grades: [
      { name: "VIP", price: 230000, remaining: 8, color: "#f59e0b" },
      { name: "R석", price: 176000, remaining: 37, color: "#ef4444" },
      { name: "S석", price: 132000, remaining: 84, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_009_r1",
        daysBefore: 10,
        feeRate: 0,
        label: "공연 10일 전까지",
      },
      {
        id: "evt_009_r2",
        daysBefore: 5,
        feeRate: 0.1,
        label: "공연 5일 전까지",
      },
      {
        id: "evt_009_r3",
        daysBefore: 2,
        feeRate: 0.2,
        label: "공연 2일 전까지",
      },
      {
        id: "evt_009_r4",
        daysBefore: 0,
        feeRate: 1,
        label: "공연 1일 전 및 당일",
      },
    ],
    description: "압도적인 스테이지 연출로 구성된 블랙핑크 스페셜 공연.",
  },
  {
    id: "evt_010",
    name: "ONE OK ROCK ASIA TOUR",
    artistName: "ONE OK ROCK",
    date: "2026.09.20 (일) 19:00",
    venue: "창원실내체육관, 창원",
    region: "경남",
    regionCode: "48",
    poster: "/posters/concert5.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-09-20",
    grades: [
      { name: "스탠딩", price: 143000, remaining: 102, color: "#ef4444" },
      { name: "지정석", price: 121000, remaining: 88, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_010_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_010_r2",
        daysBefore: 3,
        feeRate: 0.1,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_010_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_010_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "폭발적인 밴드 사운드로 채워지는 아시아 투어 한국 공연.",
  },
  {
    id: "evt_011",
    name: "LANY SUMMER SONIC SEOUL",
    artistName: "LANY",
    date: "2026.07.26 (일) 18:00",
    venue: "올림픽공원 88잔디마당, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert3.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-07-26",
    grades: [
      { name: "스탠딩", price: 132000, remaining: 131, color: "#ef4444" },
      { name: "피크닉석", price: 99000, remaining: 74, color: "#22c55e" },
    ],
    refundRules: [
      { id: "evt_011_r1", daysBefore: 5, feeRate: 0, label: "공연 5일 전까지" },
      {
        id: "evt_011_r2",
        daysBefore: 2,
        feeRate: 0.1,
        label: "공연 2일 전까지",
      },
      {
        id: "evt_011_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_011_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "야외 잔디마당에서 열리는 서머 나이트 팝 공연.",
  },
  {
    id: "evt_012",
    name: "ZION.T LIVE IN GWANGJU",
    artistName: "Zion.T",
    date: "2026.11.01 (일) 18:00",
    venue: "광주여대유니버시아드체육관, 광주",
    region: "광주",
    regionCode: "29",
    poster: "/posters/concert1.jpg",
    status: "ON_SALE",
    maxPerUser: 4,
    openDate: "2026-11-01",
    grades: [
      { name: "R석", price: 132000, remaining: 44, color: "#ef4444" },
      { name: "S석", price: 110000, remaining: 77, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_012_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_012_r2",
        daysBefore: 3,
        feeRate: 0.1,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_012_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_012_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "보컬 중심의 섬세한 라이브 구성이 돋보이는 단독 공연.",
  },
  {
    id: "evt_013",
    name: "HYUKOH CLUB TOUR FINAL",
    artistName: "HYUKOH",
    date: "2026.11.15 (일) 19:00",
    venue: "전북대학교 삼성문화회관, 전주",
    region: "전북",
    regionCode: "45",
    poster: "/posters/concert4.jpg",
    status: "ON_SALE",
    maxPerUser: 2,
    openDate: "2026-11-15",
    grades: [
      { name: "스탠딩", price: 121000, remaining: 59, color: "#ef4444" },
      { name: "지정석", price: 99000, remaining: 63, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_013_r1", daysBefore: 5, feeRate: 0, label: "공연 5일 전까지" },
      {
        id: "evt_013_r2",
        daysBefore: 2,
        feeRate: 0.15,
        label: "공연 2일 전까지",
      },
      {
        id: "evt_013_r3",
        daysBefore: 1,
        feeRate: 0.25,
        label: "공연 1일 전까지",
      },
      { id: "evt_013_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "클럽 투어의 마지막을 장식하는 밀도 높은 밴드 셋.",
  },
  {
    id: "evt_014",
    name: "TAEYEON WINTER BALLAD",
    artistName: "TAEYEON",
    date: "2026.12.20 (일) 17:00",
    venue: "KBS아레나, 서울",
    region: "서울",
    regionCode: "11",
    poster: "/posters/concert2.jpg",
    status: "ON_SALE",
    maxPerUser: 2,
    openDate: "2026-12-20",
    grades: [
      { name: "R석", price: 154000, remaining: 39, color: "#ef4444" },
      { name: "S석", price: 121000, remaining: 91, color: "#3b82f6" },
    ],
    refundRules: [
      { id: "evt_014_r1", daysBefore: 7, feeRate: 0, label: "공연 7일 전까지" },
      {
        id: "evt_014_r2",
        daysBefore: 3,
        feeRate: 0.1,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_014_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_014_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "연말 시즌에 맞춘 발라드 중심의 단독 콘서트.",
  },
  {
    id: "evt_015",
    name: "THE 1975 LIVE AT NIGHT",
    artistName: "The 1975",
    date: "2026.12.27 (일) 19:00",
    venue: "벡스코 오디토리움, 부산",
    region: "부산",
    regionCode: "26",
    poster: "/posters/concert5.jpg",
    status: "SOLD_OUT",
    maxPerUser: 4,
    openDate: "2026-12-27",
    grades: [
      { name: "R석", price: 143000, remaining: 0, color: "#ef4444" },
      { name: "S석", price: 110000, remaining: 0, color: "#3b82f6" },
    ],
    refundRules: [
      {
        id: "evt_015_r1",
        daysBefore: 7,
        feeRate: 0.05,
        label: "공연 7일 전까지",
      },
      {
        id: "evt_015_r2",
        daysBefore: 3,
        feeRate: 0.15,
        label: "공연 3일 전까지",
      },
      {
        id: "evt_015_r3",
        daysBefore: 1,
        feeRate: 0.2,
        label: "공연 1일 전까지",
      },
      { id: "evt_015_r4", daysBefore: 0, feeRate: 1, label: "공연 당일 이후" },
    ],
    description: "부산에서 열리는 겨울 시즌 록 콘서트.",
  },
];
// ---------- Home screen extra data ----------

export const BANNER_SLIDES = [
  {
    id: "b1",
    eventId: "evt_004",
    image: "/posters/concert4.jpg",
    title: "2026 울트라코리아",
    subtitle: "ULTRA KOREA IS COMING",
    venue: "잠실종합운동장, 서울",
    dates: "2026.6.7 - 2026.6.8",
  },
  {
    id: "b2",
    eventId: "evt_001",
    image: "/posters/concert1.jpg",
    title: "2026 aespa 월드 투어",
    subtitle: "AESPA WORLD TOUR",
    venue: "올림픽체조경기장, 서울",
    dates: "2026.4.12",
  },
  {
    id: "b3",
    eventId: "evt_002",
    image: "/posters/concert2.jpg",
    title: "메탈리카 내한 공연",
    subtitle: "METALLICA M72 WORLD TOUR",
    venue: "고척스카이돔, 서울",
    dates: "2026.5.24",
  },
];

export const CATEGORY_ICONS = [
  { id: "musical", label: "뮤지컬", icon: "🎭" },
  { id: "concert", label: "콘서트", icon: "🎤" },
  { id: "sports", label: "스포츠", icon: "🏀" },
  { id: "classic", label: "클래식/무용", icon: "🎹" },
  { id: "play", label: "연극", icon: "🎬" },
  { id: "leisure", label: "레저/캠핑", icon: "⛺" },
  { id: "family", label: "아동/가족", icon: "👨‍👩‍👧" },
  { id: "exhibit", label: "전시/행사", icon: "🖼️" },
  { id: "special", label: "특별공연", icon: "✨" },
  { id: "benefit", label: "이달의혜택", icon: "🎁" },
];

export const RANKING_GENRES = [
  "콘서트",
  "뮤지컬",
  "스포츠",
  "전시/행사",
  "클래식",
];

export const RANKING_ITEMS = [
  {
    rank: 1,
    eventId: "evt_001",
    name: "AESPA WORLD TOUR 2026",
    venue: "올림픽체조경기장",
    poster: "/posters/concert1.jpg",
    genre: "콘서트",
  },
  {
    rank: 2,
    eventId: "evt_002",
    name: "METALLICA M72 WORLD TOUR",
    venue: "고척스카이돔",
    poster: "/posters/concert2.jpg",
    genre: "콘서트",
  },
  {
    rank: 3,
    eventId: "evt_004",
    name: "ULTRA KOREA 2026",
    venue: "잠실종합운동장",
    poster: "/posters/concert4.jpg",
    genre: "콘서트",
  },
  {
    rank: 4,
    eventId: "evt_003",
    name: "서울 필하모닉 뉴이어 콘서트",
    venue: "예술의전당",
    poster: "/posters/concert3.jpg",
    genre: "클래식",
  },
  {
    rank: 5,
    eventId: "evt_005",
    name: "JARASUM JAZZ FESTIVAL",
    venue: "자라섬, 가평",
    poster: "/posters/concert5.jpg",
    genre: "콘서트",
  },
];

export const OPEN_SCHEDULE = [
  {
    id: "op_001",
    eventId: "evt_001",
    name: "AESPA WORLD TOUR 2026",
    openLabel: "오늘 18:00",
    openType: "일반예매",
    tags: ["단독판매"],
    poster: "/posters/concert1.jpg",
    isToday: true,
  },
  {
    id: "op_002",
    eventId: "evt_002",
    name: "METALLICA M72 WORLD TOUR",
    openLabel: "내일 20:00",
    openType: "멤버십 선구매",
    tags: ["단독판매"],
    poster: "/posters/concert2.jpg",
    isToday: false,
  },
  {
    id: "op_003",
    eventId: "evt_003",
    name: "서울 필하모닉 뉴이어 콘서트",
    openLabel: "03.03(화) 18:00",
    openType: "일반예매",
    tags: ["HOT", "단독판매"],
    poster: "/posters/concert3.jpg",
    isToday: false,
  },
  {
    id: "op_004",
    eventId: "evt_004",
    name: "ULTRA KOREA 2026",
    openLabel: "03.10(화) 10:00",
    openType: "얼리버드",
    tags: ["단독판매"],
    poster: "/posters/concert4.jpg",
    isToday: false,
  },
];

export const DISCOUNT_ITEMS = [
  {
    id: "dc_001",
    eventId: "evt_005",
    name: "JARASUM JAZZ FESTIVAL",
    venue: "자라섬, 가평",
    dates: "2026.10.5 ~ 10.7",
    discountType: "전석 할인",
    discountPct: 40,
    finalPrice: 88000,
    countdownLabel: "00:00:00",
    isTimeDeal: true,
    poster: "/posters/concert5.jpg",
  },
  {
    id: "dc_002",
    eventId: "evt_003",
    name: "서울 필하모닉 뉴이어 콘서트",
    venue: "예술의전당 콘서트홀",
    dates: "2026.1.1 ~ 1.3",
    discountType: "R석 할인",
    discountPct: 50,
    finalPrice: 45000,
    countdownLabel: "D-10 10:24:33",
    isTimeDeal: true,
    poster: "/posters/concert3.jpg",
  },
  {
    id: "dc_003",
    eventId: "evt_001",
    name: "AESPA WORLD TOUR 2026",
    venue: "올림픽체조경기장",
    dates: "2026.4.12",
    discountType: "S석 할인",
    discountPct: 20,
    finalPrice: 88000,
    countdownLabel: "10:24:33",
    isTimeDeal: false,
    poster: "/posters/concert1.jpg",
  },
];

const NORMALIZED_EVENT_FIELDS: Record<
  string,
  Partial<Pick<Event, "date" | "venue" | "region" | "description">> & {
    dates?: Array<{ label: string; day: string }>
    grades?: string[]
    refundRuleLabels?: string[]
  }
> = {
  evt_001: {
    date: "2026.04.12 (토) ~ 04.13 (일)",
    venue: "올림픽체조경기장, 서울",
    region: "서울",
    regionCode: "11",
    description: "aespa의 월드 투어 서울 공연. 화려한 연출과 강한 퍼포먼스를 한 자리에서 만날 수 있습니다.",
    dates: [
      { label: "2026.04.12 (토) 18:00", day: "DAY 1" },
      { label: "2026.04.12 (토) 21:00", day: "DAY 1 (야간)" },
      { label: "2026.04.13 (일) 18:00", day: "DAY 2" },
    ],
    grades: ["VIP", "R석", "S석", "A석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_002: {
    date: "2026.05.24 (토) ~ 05.25 (일)",
    venue: "고척스카이돔, 서울",
    region: "서울",
    regionCode: "11",
    description: "메탈리카의 M72 월드투어 한국 공연. 대형 스타디움급 무대를 경험할 수 있습니다.",
    dates: [
      { label: "2026.05.24 (토) 18:00", day: "DAY 1" },
      { label: "2026.05.25 (일) 18:00", day: "DAY 2" },
    ],
    refundRuleLabels: ["공연 10일 전까지", "공연 5일 전까지", "공연 2일 전까지", "공연 1일 전 및 당일"],
  },
  evt_003: {
    date: "2026.01.01 (수) 15:00",
    venue: "예술의전당 콘서트홀, 서울",
    region: "서울",
    regionCode: "11",
    description: "새해를 여는 서울 필하모닉의 특별 갈라 콘서트.",
    grades: ["VIP", "R석", "S석"],
    refundRuleLabels: ["공연 14일 전까지", "공연 7일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_004: {
    date: "2026.06.07 (토) ~ 06.08 (일)",
    venue: "잠실종합운동장, 서울",
    region: "서울",
    regionCode: "11",
    description: "세계적인 EDM 아티스트가 함께하는 ULTRA KOREA 2026.",
    dates: [
      { label: "2026.06.07 (토) 14:00", day: "DAY 1" },
      { label: "2026.06.08 (일) 14:00", day: "DAY 2" },
    ],
    refundRuleLabels: ["공연 14일 전까지", "공연 7일 전까지", "공연 3일 전까지", "공연 2일 전 및 당일"],
  },
  evt_005: {
    date: "2026.10.05 (토) 12:00",
    venue: "자라섬, 경기",
    region: "경기",
    regionCode: "41",
    description: "아시아 대표 재즈 페스티벌. 야외에서 즐기는 대형 재즈 무대입니다.",
    grades: ["1일권", "2일권"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_006: {
    date: "2026.07.11 (토) 19:00",
    venue: "서울월드컵경기장, 서울",
    region: "서울",
    regionCode: "11",
    description: "콜드플레이의 대형 스타디움 라이브 공연.",
    grades: ["VIP", "R석", "S석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_007: {
    date: "2026.08.02 (일) 18:00",
    venue: "부산아시아드주경기장, 부산",
    region: "부산",
    regionCode: "26",
    description: "IU 앙코르 공연으로 다시 만나는 골든 아워 라이브.",
    grades: ["VIP", "R석", "S석"],
    refundRuleLabels: ["공연 5일 전까지", "공연 2일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_008: {
    date: "2026.08.15 (토) ~ 08.16 (일)",
    venue: "인천문학경기장, 인천",
    region: "인천",
    regionCode: "28",
    description: "세븐틴의 앙코르 스타디움 투어 공연.",
    grades: ["VIP", "R석", "S석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_009: {
    date: "2026.09.06 (일) 18:30",
    venue: "대구스타디움, 대구",
    region: "대구",
    regionCode: "27",
    description: "블랙핑크의 스페셜 콘서트. 강한 무대 연출과 퍼포먼스가 돋보입니다.",
    grades: ["VIP", "R석", "S석"],
    refundRuleLabels: ["공연 10일 전까지", "공연 5일 전까지", "공연 2일 전까지", "공연 1일 전 및 당일"],
  },
  evt_010: {
    date: "2026.09.20 (일) 19:00",
    venue: "창원실내체육관, 경남",
    region: "경남",
    regionCode: "48",
    description: "ONE OK ROCK의 아시아 투어 한국 공연.",
    grades: ["스탠딩", "지정석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_011: {
    date: "2026.07.26 (일) 18:00",
    venue: "올림픽공원 88잔디마당, 서울",
    region: "서울",
    regionCode: "11",
    description: "야외 잔디마당에서 열리는 서머 나이트 팝 공연.",
    grades: ["스탠딩", "피크닉석"],
    refundRuleLabels: ["공연 5일 전까지", "공연 2일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_012: {
    date: "2026.11.01 (일) 18:00",
    venue: "광주여대유니버시아드체육관, 광주",
    region: "광주",
    regionCode: "29",
    description: "보컬 중심의 섬세한 라이브가 돋보이는 단독 공연.",
    grades: ["R석", "S석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_013: {
    date: "2026.11.15 (일) 19:00",
    venue: "전북대학교 삼성문화회관, 전북",
    region: "전북",
    regionCode: "45",
    description: "클럽 투어의 마지막을 장식하는 밴드 공연.",
    grades: ["스탠딩", "지정석"],
    refundRuleLabels: ["공연 5일 전까지", "공연 2일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_014: {
    date: "2026.12.20 (일) 17:00",
    venue: "KBS아레나, 서울",
    region: "서울",
    regionCode: "11",
    description: "연말 시즌에 어울리는 발라드 중심의 단독 콘서트.",
    grades: ["R석", "S석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
  evt_015: {
    date: "2026.12.27 (일) 19:00",
    venue: "벡스코 오디토리움, 부산",
    region: "부산",
    regionCode: "26",
    description: "부산에서 열리는 겨울 시즌 록 콘서트.",
    grades: ["R석", "S석"],
    refundRuleLabels: ["공연 7일 전까지", "공연 3일 전까지", "공연 1일 전까지", "공연 당일 이후"],
  },
}

MOCK_EVENTS.forEach((event) => {
  const normalized = NORMALIZED_EVENT_FIELDS[event.id]
  if (!normalized) return

  if (normalized.date) event.date = normalized.date
  if (normalized.venue) event.venue = normalized.venue
  if (normalized.region) event.region = normalized.region
  if (normalized.description) event.description = normalized.description

  if (normalized.dates && event.dates) {
    event.dates = event.dates.map((date, index) => ({
      ...date,
      label: normalized.dates?.[index]?.label ?? date.label,
      day: normalized.dates?.[index]?.day ?? date.day,
    }))
  }

  if (normalized.grades) {
    event.grades = event.grades.map((grade, index) => ({
      ...grade,
      name: normalized.grades?.[index] ?? grade.name,
    }))
  }

  if (normalized.refundRuleLabels) {
    event.refundRules = event.refundRules?.map((rule, index) => ({
      ...rule,
      label: normalized.refundRuleLabels?.[index] ?? rule.label,
    }))
  }
})

const NORMALIZED_HOME_TEXT = {
  userName: "김민준",
  bannerSlides: [
    { title: "2026 울트라 코리아", venue: "잠실종합운동장, 서울" },
    { title: "2026 aespa 월드 투어", venue: "올림픽체조경기장, 서울" },
    { title: "메탈리카 내한 공연", venue: "고척스카이돔, 서울" },
  ],
  categories: ["뮤지컬", "콘서트", "스포츠", "클래식/무용", "연극", "레저/캠핑", "아동/가족", "전시/행사", "특별공연", "이달의혜택"],
  rankingGenres: ["콘서트", "뮤지컬", "스포츠", "전시/행사", "클래식"],
  openSchedule: [
    { openLabel: "오늘 18:00", openType: "일반예매", tags: ["단독판매"] },
    { openLabel: "내일 20:00", openType: "멤버십 선구매", tags: ["단독판매"] },
    { openLabel: "03.03(화) 18:00", openType: "일반예매", tags: ["HOT", "단독판매"] },
    { openLabel: "03.10(화) 10:00", openType: "얼리버드", tags: ["단독판매"] },
  ],
  discountTypes: ["전석 할인", "R석 할인", "S석 할인"],
}

MOCK_USER.name = NORMALIZED_HOME_TEXT.userName

BANNER_SLIDES.forEach((slide, index) => {
  const normalized = NORMALIZED_HOME_TEXT.bannerSlides[index]
  if (!normalized) return
  slide.title = normalized.title
  slide.venue = normalized.venue
})

CATEGORY_ICONS.forEach((item, index) => {
  item.label = NORMALIZED_HOME_TEXT.categories[index] ?? item.label
})

RANKING_GENRES.splice(0, RANKING_GENRES.length, ...NORMALIZED_HOME_TEXT.rankingGenres)

RANKING_ITEMS.forEach((item) => {
  const event = MOCK_EVENTS.find((eventItem) => eventItem.id === item.eventId)
  if (!event) return
  item.name = event.name
  item.venue = event.venue.split(",")[0]
})

OPEN_SCHEDULE.forEach((item, index) => {
  const normalized = NORMALIZED_HOME_TEXT.openSchedule[index]
  const event = MOCK_EVENTS.find((eventItem) => eventItem.id === item.eventId)

  if (event) {
    item.name = event.name
  }

  if (!normalized) return
  item.openLabel = normalized.openLabel
  item.openType = normalized.openType
  item.tags = normalized.tags
})

DISCOUNT_ITEMS.forEach((item, index) => {
  item.discountType = NORMALIZED_HOME_TEXT.discountTypes[index] ?? item.discountType

  const event = MOCK_EVENTS.find((eventItem) => eventItem.id === item.eventId)
  if (event) {
    item.name = event.name
    item.venue = event.venue.split(",")[0]
  }
})

export function generateSeats(
  eventId: string,
  gradesOrGradeName?: Array<{ name: string; price: number; remaining: number; color?: string }> | string,
  maybeGradeName?: string
): Seat[] {
  const seats: Seat[] = [];
  const event = MOCK_EVENTS.find((e) => e.id === eventId);
  const gradeName = typeof gradesOrGradeName === "string" ? gradesOrGradeName : maybeGradeName;
  const grades = Array.isArray(gradesOrGradeName) ? gradesOrGradeName : event?.grades ?? [
    { name: "R석", price: 140000, remaining: 50, color: "#ef4444" },
  ];

  // Layout: each grade gets its own section block
  // Columns per section, rows per section
  const COLS = 10;
  const ROWS_PER_GRADE = 4;

  grades.forEach((grade, gradeIdx) => {
    if (gradeName && grade.name !== gradeName) return;

    const rowLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    for (let r = 0; r < ROWS_PER_GRADE; r++) {
      const rowLabel = `${grade.name}-${rowLetters[r]}`;
      for (let num = 1; num <= COLS; num++) {
        const seatIndex = gradeIdx * ROWS_PER_GRADE * COLS + r * COLS + num;
        let status: SeatStatus = "AVAILABLE";
        const rand = seatIndex % 7;
        if (grade.remaining === 0) {
          status = "SOLD";
        } else if (rand === 2 || rand === 5) {
          status = "SOLD";
        } else if (rand === 3) {
          status = "LOCKED";
        }

        seats.push({
          id: `${eventId}_${grade.name}_${rowLetters[r]}${num}`,
          row: rowLabel,
          number: num,
          grade: grade.name,
          price: grade.price,
          status,
        });
      }
    }
  });
  return seats;
}

type SeatLayoutProfile = "arena" | "stadium" | "theater" | "club";

function getSeatLayoutProfile(event?: Pick<Event, "venue" | "name">): SeatLayoutProfile {
  const text = `${event?.venue ?? ""} ${event?.name ?? ""}`;

  if (text.includes("체조경기장") || text.includes("아레나") || text.includes("실내체육관") || text.includes("체육관")) {
    return "arena";
  }

  if (text.includes("주경기장") || text.includes("월드컵경기장") || text.includes("스타디움") || text.includes("고척")) {
    return "stadium";
  }

  if (text.includes("대학로") || text.includes("아트홀") || text.includes("소극장") || text.includes("씨어터") || text.includes("홀")) {
    return "theater";
  }

  return "club";
}

function getRowCount(profile: SeatLayoutProfile, gradeIndex: number) {
  if (profile === "stadium") return [6, 7, 8, 9][Math.min(gradeIndex, 3)];
  if (profile === "arena") return [5, 6, 7, 8][Math.min(gradeIndex, 3)];
  if (profile === "theater") return [4, 5, 6, 6][Math.min(gradeIndex, 3)];
  return [3, 4, 5, 5][Math.min(gradeIndex, 3)];
}

function getSeatsPerRow(profile: SeatLayoutProfile, gradeIndex: number, rowIndex: number) {
  if (profile === "stadium") return Math.max(8, 14 - gradeIndex - Math.floor(rowIndex / 3));
  if (profile === "arena") return Math.max(8, 12 - gradeIndex + (rowIndex % 2 === 0 ? 1 : 0));
  if (profile === "theater") return Math.max(6, 10 - gradeIndex - (rowIndex % 3 === 0 ? 1 : 0));
  return Math.max(5, 8 - gradeIndex - (rowIndex % 2));
}

function getGeneratedSeatStatus(gradeRemaining: number, seed: number): SeatStatus {
  if (gradeRemaining === 0) return "SOLD";

  const mod = seed % 11;
  if (mod === 2 || mod === 7) return "SOLD";
  if (mod === 5) return "LOCKED";
  return "AVAILABLE";
}

export function generateVenueSeats(event: Pick<Event, "id" | "venue" | "name" | "grades">): Seat[] {
  const seats: Seat[] = [];
  const profile = getSeatLayoutProfile(event);
  const rowLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  event.grades.forEach((grade, gradeIndex) => {
    const rowCount = getRowCount(profile, gradeIndex);

    for (let rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      const seatsPerRow = getSeatsPerRow(profile, gradeIndex, rowIndex);
      const rowLabel = `${grade.name}-${rowLetters[rowIndex]}`;

      for (let seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
        const seed = gradeIndex * 1000 + rowIndex * 100 + seatNumber;

        seats.push({
          id: `${event.id}_${grade.name}_${rowLetters[rowIndex]}${seatNumber}`,
          row: rowLabel,
          number: seatNumber,
          grade: grade.name,
          price: grade.price,
          status: getGeneratedSeatStatus(grade.remaining, seed),
        });
      }
    }
  });

  return seats;
}

export const MOCK_TICKETS: Ticket[] = [
  {
    id: "tkt_001",
    eventId: "evt_001",
    eventName: "AESPA WORLD TOUR 2026",
    eventDate: "2026.04.12 (토) 19:00",
    venue: "올림픽체조경기장, 서울",
    poster: "/posters/concert1.jpg",
    seatId: "evt_001_C3",
    seatLabel: "C열 3번",
    grade: "R석",
    originalPrice: 140000,
    status: "SOLD",
  },
  {
    id: "tkt_002",
    eventId: "evt_002",
    eventName: "METALLICA M72 WORLD TOUR",
    eventDate: "2026.05.24 (토) 18:00",
    venue: "고척스카이돔, 서울",
    poster: "/posters/concert2.jpg",
    seatId: "evt_002_B7",
    seatLabel: "B열 7번",
    grade: "FLOOR",
    originalPrice: 150000,
    status: "LISTED",
    resalePrice: 145000,
  },
  {
    id: "tkt_003",
    eventId: "evt_005",
    eventName: "JARASUM JAZZ FESTIVAL",
    eventDate: "2024.10.05 (토) 12:00",
    venue: "자라섬, 가평",
    poster: "/posters/concert5.jpg",
    seatId: "evt_005_A2",
    seatLabel: "A열 2번",
    grade: "2일권",
    originalPrice: 150000,
    status: "USED",
    attendedDate: "2024.10.05",
  },
  {
    id: "tkt_004",
    eventId: "evt_003",
    eventName: "서울 필하모닉 뉴이어 콘서트",
    eventDate: "2025.01.01 (수) 15:00",
    venue: "예술의전당 콘서트홀, 서울",
    poster: "/posters/concert3.jpg",
    seatId: "evt_003_D5",
    seatLabel: "D열 5번",
    grade: "R석",
    originalPrice: 90000,
    status: "EXPIRED",
  },
  {
    id: "tkt_005",
    eventId: "evt_001",
    eventName: "AESPA WORLD TOUR 2026",
    eventDate: "2026.04.13 (일) 19:00",
    venue: "올림픽체조경기장, 서울",
    poster: "/posters/concert1.jpg",
    seatId: "evt_001_A1",
    seatLabel: "A열 1번",
    grade: "VIP",
    originalPrice: 180000,
    status: "USED",
    attendedDate: "2025.04.12",
  },
  {
    id: "tkt_006",
    eventId: "evt_002",
    eventName: "METALLICA M72 WORLD TOUR",
    eventDate: "2026.05.25 (일) 18:00",
    venue: "고척스카이돔, 서울",
    poster: "/posters/concert2.jpg",
    seatId: "evt_002_F11",
    seatLabel: "F열 11번",
    grade: "GA PIT",
    originalPrice: 180000,
    status: "USED",
    attendedDate: "2025.05.24",
  },
];

export const MOCK_RESALE_ITEMS: ResaleItem[] = [
  {
    id: "rs_001",
    ticketId: "tkt_002",
    eventId: "evt_002",
    eventName: "METALLICA M72 WORLD TOUR",
    eventDate: "2026.05.24 (토) 18:00",
    venue: "고척스카이돔, 서울",
    poster: "/posters/concert2.jpg",
    seatLabel: "B열 7번",
    grade: "FLOOR",
    originalPrice: 150000,
    resalePrice: 145000,
    sellerId: "user_001",
  },
  {
    id: "rs_002",
    ticketId: "tkt_ext_001",
    eventId: "evt_001",
    eventName: "AESPA WORLD TOUR 2026",
    eventDate: "2026.04.12 (토) 19:00",
    venue: "올림픽체조경기장, 서울",
    poster: "/posters/concert1.jpg",
    seatLabel: "E열 4번",
    grade: "S석",
    originalPrice: 110000,
    resalePrice: 105000,
    sellerId: "user_042",
  },
  {
    id: "rs_003",
    ticketId: "tkt_ext_002",
    eventId: "evt_004",
    eventName: "ULTRA KOREA 2026",
    eventDate: "2026.06.07 (토) 14:00",
    venue: "잠실종합운동장, 서울",
    poster: "/posters/concert4.jpg",
    seatLabel: "GA 구역 215번",
    grade: "GA",
    originalPrice: 220000,
    resalePrice: 210000,
    sellerId: "user_088",
  },
  {
    id: "rs_004",
    ticketId: "tkt_ext_003",
    eventId: "evt_001",
    eventName: "AESPA WORLD TOUR 2026",
    eventDate: "2026.04.13 (일) 19:00",
    venue: "올림픽체조경기장, 서울",
    poster: "/posters/concert1.jpg",
    seatLabel: "A열 1번",
    grade: "VIP",
    originalPrice: 180000,
    resalePrice: 175000,
    sellerId: "user_019",
  },
  {
    id: "rs_005",
    ticketId: "tkt_ext_004",
    eventId: "evt_006",
    eventName: "COLDPLAY MOON MUSIC LIVE",
    eventDate: "2026.07.11 (토) 19:00",
    venue: "서울월드컵경기장, 서울",
    poster: "/posters/concert1.jpg",
    seatLabel: "R구역 12열 8번",
    grade: "R석",
    originalPrice: 165000,
    resalePrice: 150000,
    sellerId: "user_103",
  },
  {
    id: "rs_006",
    ticketId: "tkt_ext_005",
    eventId: "evt_007",
    eventName: "IU THE GOLDEN HOUR ENCORE",
    eventDate: "2026.08.02 (일) 18:00",
    venue: "부산아시아드주경기장, 부산",
    poster: "/posters/concert3.jpg",
    seatLabel: "VIP 3열 2번",
    grade: "VIP",
    originalPrice: 198000,
    resalePrice: 190000,
    sellerId: "user_204",
  },
  {
    id: "rs_007",
    ticketId: "tkt_ext_006",
    eventId: "evt_008",
    eventName: "SEVENTEEN FOLLOW AGAIN",
    eventDate: "2026.08.15 (토) 18:00",
    venue: "인천문학경기장, 인천",
    poster: "/posters/concert4.jpg",
    seatLabel: "S구역 21열 4번",
    grade: "S석",
    originalPrice: 132000,
    resalePrice: 125000,
    sellerId: "user_305",
  },
  {
    id: "rs_008",
    ticketId: "tkt_ext_007",
    eventId: "evt_009",
    eventName: "BLACKPINK IN YOUR AREA SPECIAL",
    eventDate: "2026.09.06 (일) 18:30",
    venue: "대구스타디움, 대구",
    poster: "/posters/concert2.jpg",
    seatLabel: "VIP 1열 1번",
    grade: "VIP",
    originalPrice: 230000,
    resalePrice: 225000,
    sellerId: "user_407",
  },
  {
    id: "rs_009",
    ticketId: "tkt_ext_008",
    eventId: "evt_011",
    eventName: "LANY SUMMER SONIC SEOUL",
    eventDate: "2026.07.26 (일) 18:00",
    venue: "올림픽공원 88잔디마당, 서울",
    poster: "/posters/concert3.jpg",
    seatLabel: "피크닉 A-14",
    grade: "피크닉석",
    originalPrice: 99000,
    resalePrice: 87000,
    sellerId: "user_511",
  },
];

function normalizeGradeName(value: string) {
  return value
    .replaceAll("Rì„", "R석")
    .replaceAll("Sì„", "S석")
    .replaceAll("Aì„", "A석")
    .replaceAll("1ì¼ê¶Œ", "1일권")
    .replaceAll("2ì¼ê¶Œ", "2일권")
    .replaceAll("ìŠ¤íƒ ë”©", "스탠딩")
    .replaceAll("ì§€ì •ì„", "지정석")
    .replaceAll("í”¼í¬ë‹‰ì„", "피크닉석")
}

function normalizeSeatLabel(value: string) {
  return value
    .replaceAll("ì—´", "열 ")
    .replaceAll("ë²ˆ", "번")
    .replaceAll("êµ¬ì—­", "구역 ")
    .replaceAll("Rêµ¬ì—­", "R구역 ")
    .replaceAll("Sêµ¬ì—­", "S구역 ")
    .replaceAll("ì—´ ", "열 ")
    .replaceAll("  ", " ")
    .trim()
}

MOCK_TICKETS.forEach((ticket) => {
  const event = MOCK_EVENTS.find((eventItem) => eventItem.id === ticket.eventId)
  if (event) {
    ticket.eventName = event.name
    ticket.eventDate = ticket.eventDate || event.date
    ticket.venue = event.venue
  }

  ticket.grade = normalizeGradeName(ticket.grade)
  ticket.seatLabel = normalizeSeatLabel(ticket.seatLabel)
})

MOCK_RESALE_ITEMS.forEach((item) => {
  const event = MOCK_EVENTS.find((eventItem) => eventItem.id === item.eventId)
  if (event) {
    item.eventName = event.name
    item.eventDate = item.eventDate || event.date
    item.venue = event.venue
  }

  item.grade = normalizeGradeName(item.grade)
  item.seatLabel = normalizeSeatLabel(item.seatLabel)
})
