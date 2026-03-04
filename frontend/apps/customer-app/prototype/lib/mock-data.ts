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
    venue: "\uC7A0\uC2E4 \uC544\uB808\uB098, \uC11C\uC6B8",
    dates: "2026.03.12 - 03.14",
    image: "/posters/concert4.jpg",
  },
  {
    id: "bnr-2",
    title: "AESPA WORLD TOUR 2026",
    subtitle: "HOT ISSUE",
    venue: "\uC7A0\uC2E4 \uCCB4\uC870\uACBD\uAE30\uC7A5, \uC11C\uC6B8",
    dates: "2026.03.20 - 03.21",
    image: "/posters/concert1.jpg",
  },
  {
    id: "bnr-3",
    title: "ULTRA KOREA 2026",
    subtitle: "LIMITED OPEN",
    venue: "\uC11C\uC6B8 \uC6D4\uB4DC\uCEF5\uACBD\uAE30\uC7A5, \uC11C\uC6B8",
    dates: "2026.04.05 - 04.06",
    image: "/posters/concert2.jpg",
  },
];

export const rankingItems: RankingItem[] = [
  {
    rank: 1,
    name: "AESPA WORLD TOUR 2026",
    venue: "\uC7A0\uC2E4 \uCCB4\uC870\uACBD\uAE30\uC7A5",
    poster: "/posters/concert1.jpg",
  },
  {
    rank: 2,
    name: "METALLICA M72 WORLD TOUR",
    venue: "\uACE0\uCC99\uC2A4\uCE74\uC774\uB3D4",
    poster: "/posters/concert2.jpg",
  },
  {
    rank: 3,
    name: "ULTRA KOREA 2026",
    venue: "\uC7A0\uC2E4 \uC885\uD569\uC6B4\uB3D9\uC7A5",
    poster: "/posters/concert4.jpg",
  },
  {
    rank: 4,
    name: "\uC11C\uC6B8 \uD544\uD558\uBAA8\uB2C9 \uC2E0\uB144 \uCF58\uC11C\uD2B8",
    venue: "\uC608\uC220\uC758\uC804\uB2F9",
    poster: "/posters/concert3.jpg",
  },
  {
    rank: 5,
    name: "JARASUM JAZZ FESTIVAL",
    venue: "\uC790\uB77C\uC12C \uAC00\uD3C9\uAD70",
    poster: "/posters/concert5.jpg",
  },
];

export const openSchedule: OpenScheduleItem[] = [
  {
    id: "open-1",
    openLabel: "\uC624\uB298 20:00 \uC624\uD508",
    name: "K-ARENA SPRING TOUR",
    openType: "\uC77C\uBC18\uC608\uB9E4",
    tags: ["HOT", "\uB2E8\uB3C5"],
    poster: "/posters/concert4.jpg",
  },
  {
    id: "open-2",
    openLabel: "D-1 14:00 \uC624\uD508",
    name: "BLUEWAVE LIVE",
    openType: "\uC120\uC608\uB9E4",
    tags: ["\uB2E8\uB3C5"],
    poster: "/posters/concert1.jpg",
  },
  {
    id: "open-3",
    openLabel: "D-2 12:00 \uC624\uD508",
    name: "CITY POP SYMPHONY",
    openType: "\uC77C\uBC18\uC608\uB9E4",
    tags: ["\uC2E0\uADDC"],
    poster: "/posters/concert5.jpg",
  },
];

export const discountItems: DiscountItem[] = [
  {
    id: "dc-1",
    name: "CHEKET AFTER PARTY",
    venue: "\uD64D\uB300 MUV HALL",
    dates: "2026.03.28",
    discountType: "\uD55C\uC815 \uD560\uC778",
    discountPct: 20,
    finalPrice: 88000,
    poster: "/posters/concert3.jpg",
  },
  {
    id: "dc-2",
    name: "URBAN MUSIC NIGHT",
    venue: "\uC131\uC218 \uC544\uD2B8\uD640",
    dates: "2026.04.02",
    discountType: "\uC870\uAE30\uC608\uB9E4",
    discountPct: 15,
    finalPrice: 68000,
    poster: "/posters/concert2.jpg",
  },
];
