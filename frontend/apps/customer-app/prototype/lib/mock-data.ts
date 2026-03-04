export type BannerSlide = {
  id: string;
  title: string;
  subtitle: string;
  venue: string;
  dates: string;
  image: string;
};

export type RankingItem = {
  rank: number;
  name: string;
  venue: string;
  poster: string;
};

export type OpenScheduleItem = {
  id: string;
  openLabel: string;
  name: string;
  openType: string;
  tags: string[];
  poster: string;
};

export type DiscountItem = {
  id: string;
  name: string;
  venue: string;
  dates: string;
  discountType: string;
  discountPct: number;
  finalPrice: number;
  poster: string;
};

export const bannerSlides: BannerSlide[] = [
  {
    id: "bnr-1",
    title: "SEOUL JAZZ FESTIVAL 2026",
    subtitle: "CHEKET PICK",
    venue: "잠실아레나",
    dates: "2026.03.12 - 03.14",
    image: "/posters/concert4.jpg",
  },
  {
    id: "bnr-2",
    title: "AESPA WORLD TOUR 2026",
    subtitle: "HOT ISSUE",
    venue: "올림픽체조경기장",
    dates: "2026.03.20 - 03.21",
    image: "/posters/concert1.jpg",
  },
  {
    id: "bnr-3",
    title: "ULTRA KOREA 2026",
    subtitle: "LIMITED OPEN",
    venue: "잠실종합운동장",
    dates: "2026.04.05 - 04.06",
    image: "/posters/concert2.jpg",
  },
];

export const rankingItems: RankingItem[] = [
  { rank: 1, name: "AESPA WORLD TOUR 2026", venue: "올림픽체조경기장", poster: "/posters/concert1.jpg" },
  { rank: 2, name: "METALLICA M72 WORLD TOUR", venue: "고척스카이돔", poster: "/posters/concert2.jpg" },
  { rank: 3, name: "ULTRA KOREA 2026", venue: "잠실종합운동장", poster: "/posters/concert4.jpg" },
  { rank: 4, name: "서울 필하모닉 신년 콘서트", venue: "예술의전당", poster: "/posters/concert3.jpg" },
  { rank: 5, name: "JARASUM JAZZ FESTIVAL", venue: "가평 자라섬", poster: "/posters/concert5.jpg" },
];

export const openSchedule: OpenScheduleItem[] = [
  {
    id: "open-1",
    openLabel: "오늘 20:00 오픈",
    name: "K-ARENA SPRING TOUR",
    openType: "일반예매",
    tags: ["HOT", "추천"],
    poster: "/posters/concert4.jpg",
  },
  {
    id: "open-2",
    openLabel: "D-1 14:00 오픈",
    name: "BLUEWAVE LIVE",
    openType: "선예매",
    tags: ["추천"],
    poster: "/posters/concert1.jpg",
  },
  {
    id: "open-3",
    openLabel: "D-2 12:00 오픈",
    name: "CITY POP SYMPHONY",
    openType: "일반예매",
    tags: ["신규"],
    poster: "/posters/concert5.jpg",
  },
];

export const discountItems: DiscountItem[] = [
  {
    id: "dc-1",
    name: "CHEKET AFTER PARTY",
    venue: "예스24 라이브홀",
    dates: "2026.03.28",
    discountType: "한정 특가",
    discountPct: 20,
    finalPrice: 88000,
    poster: "/posters/concert3.jpg",
  },
  {
    id: "dc-2",
    name: "URBAN MUSIC NIGHT",
    venue: "홍대 클럽거리",
    dates: "2026.04.02",
    discountType: "타임 세일",
    discountPct: 15,
    finalPrice: 68000,
    poster: "/posters/concert2.jpg",
  },
];
