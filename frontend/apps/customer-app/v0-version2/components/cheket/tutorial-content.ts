'use client'

export type TutorialId =
  | 'resale-list'
  | 'resale-detail'
  | 'resale-create'
  | 'wallet'
  | 'wallet-history'
  | 'tx-history'
  | 'transfer'
  | 'collection'
  | 'collectible-ticket-detail'
  | 'qr-checkin'

export interface TutorialContent {
  id: TutorialId
  category: string
  title: string
  summary: string
  points: string[]
  caution?: string
}

export const TUTORIAL_CONTENT: Record<TutorialId, TutorialContent> = {
  'resale-list': {
    id: 'resale-list',
    category: 'Resale',
    title: '재판매 마켓 안내',
    summary: '이 화면은 일반 예매가 아니라 다른 사용자가 다시 판매한 티켓을 모아보는 재판매 마켓이에요.',
    points: [
      '공연을 누르면 실제로 거래 가능한 재판매 티켓 목록으로 이동해요.',
      '표시된 가격은 현재 등록된 티켓 중 가장 낮은 재판매가 기준이에요.',
      '같은 공연이라도 좌석, 가격, 수량이 각각 다를 수 있어요.',
    ],
    caution: '할인율만 보지 말고 좌석 등급과 일정도 함께 확인하세요.',
  },
  'resale-detail': {
    id: 'resale-detail',
    category: 'Resale',
    title: '재판매 티켓 고르기',
    summary: '같은 공연이라도 판매자마다 좌석과 가격이 다르기 때문에 조건을 비교해서 선택하는 화면이에요.',
    points: [
      '좌석 위치와 등급, 재판매가를 함께 비교할 수 있어요.',
      '구매가 완료되면 티켓은 내 티켓함으로 이동해요.',
      '판매 중인 수량만큼만 구매할 수 있어요.',
    ],
    caution: '잔액이 부족하면 구매가 진행되지 않으니 CTK 잔액을 먼저 확인하세요.',
  },
  'resale-create': {
    id: 'resale-create',
    category: 'Resale',
    title: '내 티켓 재판매 등록',
    summary: '사용하지 못하는 티켓을 다시 판매할 수 있도록 가격을 정해 등록하는 화면이에요.',
    points: [
      '보유 중인 티켓만 재판매로 등록할 수 있어요.',
      '등록한 티켓은 재판매 목록에서 다른 사용자에게 노출돼요.',
      '판매가 완료되면 티켓 상태와 거래 기록이 함께 변경돼요.',
    ],
    caution: '재판매 등록 전에는 가격과 판매 의사를 다시 확인하세요.',
  },
  wallet: {
    id: 'wallet',
    category: 'Wallet',
    title: 'CTK 지갑 안내',
    summary: '이 화면에서는 서비스 안에서 사용하는 CTK 잔액과 지갑 주소를 확인할 수 있어요.',
    points: [
      '현재 보유 중인 CTK 잔액을 바로 확인할 수 있어요.',
      '충전한 금액은 지갑 잔액에 즉시 반영돼요.',
      '더 자세한 사용 기록은 지갑 내역 화면에서 볼 수 있어요.',
    ],
    caution: '지갑 주소는 자산 식별 정보이므로 복사 전후를 확인하는 편이 좋아요.',
  },
  'wallet-history': {
    id: 'wallet-history',
    category: 'Wallet',
    title: '지갑 내역 보는 법',
    summary: 'CTK가 언제 충전되고 어디에 사용됐는지 시간순으로 확인하는 기록 화면이에요.',
    points: [
      '충전, 구매, 환불 기록을 한곳에서 볼 수 있어요.',
      '각 기록에는 당시 잔액이 함께 표시돼요.',
      '최근 기록부터 순서대로 확인할 수 있어요.',
    ],
  },
  'tx-history': {
    id: 'tx-history',
    category: 'Ledger',
    title: '거래 기록 안내',
    summary: '이 화면은 일반 지갑 사용 내역보다 더 자세한 거래 상태와 처리 기록을 확인하는 공간이에요.',
    points: [
      '티켓 구매, 재판매, 환불, 양도 같은 주요 거래 기록이 남아요.',
      '거래 상태와 해시 정보를 함께 확인할 수 있어요.',
      '문제가 생겼을 때 실제로 처리가 됐는지 확인하는 데 유용해요.',
    ],
  },
  transfer: {
    id: 'transfer',
    category: 'Transfer',
    title: '티켓 양도 안내',
    summary: '내 티켓을 다른 CHEKET 사용자에게 전달할 수 있는 기능이에요.',
    points: [
      '받는 사람의 전화번호를 입력한 뒤 계정을 확인하고 양도할 수 있어요.',
      '양도가 완료되면 티켓 소유 상태가 변경돼요.',
      '실패 시에는 사유를 확인하고 다시 시도할 수 있어요.',
    ],
    caution: '양도 전에는 받는 사람 이름과 전화번호를 꼭 다시 확인하세요.',
  },
  collection: {
    id: 'collection',
    category: 'Collection',
    title: '컬렉션 안내',
    summary: '컬렉션은 사용할 티켓을 모아두는 곳이 아니라 소장 가치가 있는 디지털 티켓을 감상하는 공간이에요.',
    points: [
      '관람이 끝난 티켓을 기록처럼 남겨둘 수 있어요.',
      '일반 티켓함과 다르게 소장용 비주얼과 효과를 중심으로 보여줘요.',
      '티켓을 열어 앞면과 뒷면을 확인할 수 있어요.',
    ],
  },
  'collectible-ticket-detail': {
    id: 'collectible-ticket-detail',
    category: 'Collection',
    title: '컬렉션 티켓 상세',
    summary: '공연 정보뿐 아니라 소장용 티켓으로서의 메타 정보까지 함께 보는 상세 화면이에요.',
    points: [
      '좌석과 관람 정보, 소장 상태를 함께 확인할 수 있어요.',
      '토큰 ID 같은 컬렉션용 정보가 강조돼요.',
      '일반 티켓 상세와 다른 서비스 차별 포인트를 보여주는 화면이에요.',
    ],
  },
  'qr-checkin': {
    id: 'qr-checkin',
    category: 'Check-in',
    title: 'QR 체크인 안내',
    summary: '공연장 입장 시 사용하는 QR 티켓을 보여주는 화면이에요.',
    points: [
      '현장에서 이 화면의 QR 코드를 보여주면 입장을 진행할 수 있어요.',
      'QR 코드는 일정 시간마다 새로 갱신돼요.',
      '사용이 완료되면 티켓 상태가 변경될 수 있어요.',
    ],
    caution: '입장 직전에는 QR 화면을 미리 열어두는 것이 좋아요.',
  },
}

export const TUTORIAL_ORDER: TutorialId[] = [
  'resale-list',
  'resale-detail',
  'resale-create',
  'wallet',
  'wallet-history',
  'tx-history',
  'transfer',
  'collection',
  'collectible-ticket-detail',
  'qr-checkin',
]
