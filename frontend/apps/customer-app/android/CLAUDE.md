# Cheket Android Customer App

티켓팅 플랫폼 "체켓(Cheket)"의 Android 고객용 앱.

## Terminology

**v0-version2 프로토타입에서 "event", "concert" 등으로 되어 있는 용어는 모두 "show"로 통일한다.**
백엔드 API 기준 용어는 `show`이며, 프론트엔드 코드에서도 모델, 변수명, 라우트, 화면 이름 등 모든 곳에서 `Show`를 사용한다.

| 금지 (사용하지 않음) | 올바른 용어 |
|---|---|
| Event, event, eventId | Show, show, showId |
| Concert, concerts | Show, shows |
| EventRepository | ShowRepository |
| EventCardItem | ShowCardItem |
| onEventClick | onShowClick |

## Architecture

**MVVM with Repository + Feature-based (화면별 분리)**

- **UI**: Jetpack Compose (Material3)
- **State**: StateFlow + collectAsStateWithLifecycle
- **DI**: Manual DI via `AppContainer`
- **Navigation**: Jetpack Navigation Compose (`AppNavGraph.kt`)
- **Image Loading**: Coil (AsyncImage)
- **Network**: Retrofit + OkHttp (`core/network/`)

## Project Structure

```
com/ssafy/cheket/
├── AppContainer.kt          # Manual DI container
├── AppNavGraph.kt           # Navigation graph + Routes object
├── CheketApplication.kt     # Application class
├── MainActivity.kt          # Single Activity
│
├── core/
│   ├── datasource/mock/MockDataSource.kt   # Mock data for features not yet on API
│   ├── model/Models.kt                     # All data classes in one file
│   ├── navigation/NavParams.kt             # Navigation parameter helpers
│   ├── network/                            # Retrofit API service + interceptors
│   ├── repository/                         # Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── ShowRepository.kt
│   │   ├── ResaleRepository.kt
│   │   ├── TicketRepository.kt
│   │   └── UserRepository.kt
│   ├── repository/impl/                    # Repository implementations
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── ShowRepositoryImpl.kt
│   │   ├── ResaleRepositoryImpl.kt
│   │   ├── TicketRepositoryImpl.kt
│   │   └── UserRepositoryImpl.kt
│   └── ui/component/                       # Shared UI components
│       ├── AppHeader.kt                    # Top app bar with back button
│       ├── BottomBar.kt                    # CheketBottomBar (5 tabs)
│       ├── EmptyState.kt                   # Empty state placeholder
│       ├── ShowCardItem.kt                # Reusable show card
│       ├── StatusBadge.kt                 # Status indicator badge
│       └── TicketCardItem.kt              # Reusable ticket card
│
├── ui/theme/
│   ├── Color.kt    # Design tokens (Primary, Background, Muted, etc.)
│   ├── Theme.kt    # MaterialTheme setup
│   └── Type.kt     # Typography
│
└── features/                  # Feature-based screen modules
    ├── auth/                  # Login, Signup, FindAccount, PasswordReset
    ├── collection/            # NFT ticket collection, Archive
    ├── home/                  # Home (banners, categories, rankings, schedules)
    ├── mypage/                # User profile
    ├── mytickets/             # My tickets, ticket detail, QR checkin
    ├── purchase/              # Seat selection, payment, waiting queue
    ├── resale/                # Resale market (list, detail, create, purchase complete)
    ├── settings/              # Settings, password change
    ├── show/                  # Show detail, date selection
    ├── shows/                 # Show listing (탐색 탭)
    ├── transfer/              # Ticket transfer
    ├── wallet/                # Wallet, history, withdraw
    └── wishlist/              # Wishlist
```

## Navigation (Bottom Tabs)

| Tab | Route | Screen |
|-----|-------|--------|
| Home | `home` | HomeScreen |
| Shows | `shows` | ShowsScreen |
| Resale | `resale` | ResaleScreen |
| My Tickets | `my_tickets` | MyTicketsScreen |
| Collection | `collection` | CollectionScreen |

## Key Patterns

### ViewModel 생성
```kotlin
class SomeViewModel(private val repo: SomeRepository) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                SomeViewModel(app.appContainer.someRepository)
            }
        }
    }
}
```

### Screen 구조 (with ViewModel)
```kotlin
@Composable
fun SomeScreen(
    appContainer: AppContainer,
    onNavigate: (String) -> Unit = {},
    viewModel: SomeViewModel = viewModel(factory = SomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### Screen 구조 (without ViewModel - direct mock data)
```kotlin
@Composable
fun SomeScreen(
    someId: String,
    onAction: () -> Unit,
    onBack: () -> Unit,
) {
    val data = remember { MockDataSource.someData.find { it.id == someId } }
    // ...
}
```

## Design Tokens (Color.kt)

- Primary: `#00C598` (green)
- Background: `#FAFAFA`
- Surface/CardBg: `#FFFFFF`
- OnBackground: `#0D1F1A`
- MutedForeground: `#6B7280`
- BorderColor: `#E5E7EB`
- Danger: `#E53E3E`
- PrimaryLight: `#EEF9F7` (badge backgrounds)

## v0 Reference

v0 프로토타입 (RN 버전) 위치:
- `../rn/cheket/src/features/` - Feature screens
- `../rn/cheket/src/shared/components/` - Shared components
- `../rn/cheket/src/core/theme/` - Theme (colors, typography)

Android UI는 v0/RN 디자인을 기준으로 구현. 차이 발견 시 v0를 정본으로 사용.
**단, v0에서 event/concert로 되어 있는 용어는 반드시 show로 변환하여 구현할 것.**

## Resale Flow

```
ResaleScreen (bottom tab, grouped by show)
  → ResaleTicketsScreen (individual tickets per show, with sort)
    → ResaleDetailScreen (ticket detail + purchase)
      → ResalePurchaseCompleteScreen (success)

TicketDetailScreen
  → ResaleCreateScreen (list ticket for resale)
```
