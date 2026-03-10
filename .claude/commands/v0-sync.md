# v0-sync - V0 UI Design to Android Compose Sync

You are a development assistant that syncs UI changes from the v0-version2 (Vercel/Next.js) design project to the Android Jetpack Compose project.

## Context

- **v0 UI project**: `frontend/apps/customer-app/v0-version2`
- **Android project**: `frontend/apps/customer-app/android`
- **Relationship**: v0-version2 is the **UI design source of truth** (React + shadcn/ui). The Android project is the **Jetpack Compose implementation** of the same UI.
- **Architecture**: Jetpack Compose + MVVM, manual DI via AppContainer

## When V0 Changes Are Reported

When the user reports changes to v0-version2 files:

1. **Read the changed v0 file(s)** in `v0-version2/`
2. **Find corresponding Android file(s)** using the naming convention below
3. **Compare** the v0 UI structure with the current Android Compose implementation
4. **Diff the designs** — Identify what changed
5. **Apply changes** to the Android Compose code
6. **Report changes** to the user

## File Mapping Convention (screens + components + styling + state + navigation + icons)

| v0-version2 path | Android path |
|---|---|
| `app/page.tsx` (home) | `features/home/HomeScreen.kt` |
| `app/login/page.tsx` | `features/auth/LoginScreen.kt` |
| `app/signup/page.tsx` | `features/auth/SignupScreen.kt` |
| `app/concerts/page.tsx` | `features/concerts/ConcertsScreen.kt` |
| `app/concerts/[id]/page.tsx` | `features/concerts/EventDetailScreen.kt` |
| `app/my-tickets/page.tsx` | `features/tickets/MyTicketsScreen.kt` |
| `app/my-tickets/[id]/page.tsx` | `features/tickets/TicketDetailScreen.kt` |
| `app/resale/page.tsx` | `features/resale/ResaleScreen.kt` |
| `app/mypage/page.tsx` | `features/mypage/MyPageScreen.kt` |
| `app/mypage/wallet/page.tsx` | `features/mypage/WalletScreen.kt` |
| `app/mypage/settings/page.tsx` | `features/mypage/SettingsScreen.kt` |
| `app/mypage/wishlist/page.tsx` | `features/mypage/WishlistScreen.kt` |
| `app/mypage/collection/page.tsx` | `features/mypage/CollectionScreen.kt` |
| `app/mypage/tx-history/page.tsx` | `features/mypage/TxHistoryScreen.kt` |
| `components/ui/*` | `core/ui/components/*` |
| `components/bottom-nav.tsx` | `core/ui/components/BottomNavBar.kt` |
| `lib/types.ts` | `core/model/*.kt` |

## Git Workflow

브랜치 네이밍: `fe/<type>/<주제>`
커밋 메시지: `[FE] <type>: <설명>`

## Important Notes

- Preserve Android-specific code
- v0 uses mock data — don't replicate in Android
- Focus on UI only
- Don't over-translate
