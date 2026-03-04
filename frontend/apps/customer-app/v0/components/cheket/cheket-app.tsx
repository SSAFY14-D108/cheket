'use client'

import { useApp } from '@/lib/app-context'
import { LoginScreen } from './screens/login-screen'
import { SignupScreen } from './screens/signup-screen'
import { HomeScreen } from './screens/home-screen'
import { ConcertsScreen } from './screens/concerts-screen'
import { EventDetailScreen } from './screens/event-detail-screen'
import { WaitingQueueScreen } from './screens/waiting-queue-screen'
import { SeatSelectionScreen } from './screens/seat-selection-screen'
import { PaymentScreen } from './screens/payment-screen'
import { PurchaseFailedScreen } from './screens/purchase-failed-screen'
import { ResaleListScreen } from './screens/resale-list-screen'
import { ResaleDetailScreen } from './screens/resale-detail-screen'
import { ResalePurchaseCompleteScreen } from './screens/resale-purchase-complete-screen'
import { MyTicketsScreen } from './screens/my-tickets-screen'
import { TicketDetailScreen } from './screens/ticket-detail-screen'
import { ResaleCreateScreen } from './screens/resale-create-screen'
import { QrCheckinScreen } from './screens/qr-checkin-screen'
import { ArchiveScreen } from './screens/archive-screen'
import { CollectibleTicketDetailScreen } from './screens/collectible-ticket-detail-screen'
import { MyPageScreen } from './screens/my-page-screen'
import { WalletScreen } from './screens/wallet-screen'
import { CollectionScreen } from './screens/collection-screen'
import { TransferScreen } from './screens/transfer-screen'
import { TransferCompleteScreen } from './screens/transfer-complete-screen'
import { TransferFailedScreen } from './screens/transfer-failed-screen'
import { WishlistScreen } from './screens/wishlist-screen'

export function CheketApp() {
  const { screen } = useApp()

  const screenMap: Record<string, React.ReactNode> = {
    login: <LoginScreen />,
    signup: <SignupScreen />,
    home: <HomeScreen />,
    concerts: <ConcertsScreen />,
    'event-detail': <EventDetailScreen />,
    'waiting-queue': <WaitingQueueScreen />,
    'seat-selection': <SeatSelectionScreen />,
    payment: <PaymentScreen />,
    'purchase-failed': <PurchaseFailedScreen />,
    'resale-list': <ResaleListScreen />,
    'resale-detail': <ResaleDetailScreen />,
    'resale-purchase-complete': <ResalePurchaseCompleteScreen />,
    'my-tickets': <MyTicketsScreen />,
    'ticket-detail': <TicketDetailScreen />,
    'resale-create': <ResaleCreateScreen />,
    'qr-checkin': <QrCheckinScreen />,
    archive: <ArchiveScreen />,
    'collectible-ticket-detail': <CollectibleTicketDetailScreen />,
    'my-page': <MyPageScreen />,
    wallet: <WalletScreen />,
    collection: <CollectionScreen />,
    transfer: <TransferScreen />,
    'transfer-complete': <TransferCompleteScreen />,
    'transfer-failed': <TransferFailedScreen />,
    wishlist: <WishlistScreen />,
  }

  return (
    <div className="relative w-full max-w-[390px] mx-auto h-screen bg-background overflow-hidden flex flex-col shadow-2xl">
      {screenMap[screen] ?? <LoginScreen />}
    </div>
  )
}
