# Cheket Frontend Design System Analysis
## v0-version2 Screen Architecture & Visual Patterns

**Analysis Date:** 2026-03-19  
**Scope:** 34 screens across authentication, ticketing, marketplace, and wallet flows  
**Technology Stack:** Next.js 13+ (App Router), Tailwind CSS (oklch), TypeScript, React Context API

---

## 1. COLOR SYSTEM & PALETTE

### Primary Brand Colors
- **Oklch-based Variables:** Leverages perceptually uniform oklch() color space via Tailwind custom tokens
- **Key Colors Observed:**
  - Primary Gradient: Appears in headers, buttons, borders (typical gradient from vibrant to secondary hue)
  - Destructive/Error Red: `#ef4444` (red-500) for alerts, delete actions, insufficient balance
  - Success Green: `#10b981` (emerald-500) for completed actions, checkmarks
  - Warning Orange: `#f97316` (orange-500) for warnings, limited time offers
  - Info Blue: `#3b82f6` (blue-500) for informational states

### Status Color Mapping
| Status | Color | Usage |
|--------|-------|-------|
| UPCOMING | Blue (#3b82f6) | Future shows |
| ON_SALE | Green (#10b981) | Active purchasing |
| SOLD_OUT | Red (#ef4444) | No tickets available |
| COMPLETED | Gray (#6b7280) | Past events |
| PAID/USED | Green | Confirmed tickets |
| EXPIRED | Red | Refund/transfer windows closed |
| LISTED | Orange | Resale available |
| PENDING | Orange | Transaction processing |

### Text Color Hierarchy
- **Primary Text:** Dark gray/black (`#1f2937` or darker)
- **Secondary Text:** Medium gray (`#6b7280`)
- **Tertiary Text:** Light gray (`#9ca3af`)
- **Inverted (on dark backgrounds):** White with opacity adjustments

### Special Color Usage
- **Floating Sparkle Effects:** `#d9ccff` (purple-200), `#c5e2ff` (blue-200), `#c4f7e0` (teal-200), `#dfe7ff` (indigo-200)
- **Wallet/Card Gradient:** Multi-stop gradient with oklch color transitions
- **Alert Boxes:** 10% red tint backgrounds with red-600 borders (`destructive` class)

---

## 2. SPACING & SIZING CONVENTIONS

### Viewport Constraints
- **Target Width:** 390px max-width (mobile-first)
- **Responsive Breakpoints:** Standard Tailwind (sm, md, lg, xl)
- **Padding:** Consistent 16px (p-4) to 20px (p-5) horizontal margins on main containers

### Spacing Scale
| Use Case | Value | Tailwind |
|----------|-------|----------|
| Vertical Section Gap | 24px | gap-6 |
| Component Inner Gap | 16px | gap-4 |
| Tight Grouping | 12px | gap-3 |
| Item Padding | 16px | p-4 |
| Large Card Padding | 20px | p-5 |
| Headline Spacing | 24-32px | mb-6 to mb-8 |

### Common Dimensions
- **Card Heights:** Varies by content (event card ~120px, ticket card ~160px)
- **Avatar Icons:** 48px × 48px (standard), 32px × 32px (small)
- **Poster Images:** 80px × 120px (3:4 ratio for event thumbnails)
- **Full Event Hero:** 100vh with gradient overlay
- **Grid Layouts:** 2-column for resale/collection, single column default
- **Button Heights:** 44-48px (touchable minimum)

---

## 3. TYPOGRAPHY PATTERNS

### Font Scale
- **Hero/Large Titles:** 28px-32px font-bold (h1 equivalent)
- **Section Titles:** 20px-24px font-bold (h2 equivalent)
- **Subsection Titles:** 16px-18px font-semibold (h3 equivalent)
- **Body Text:** 14px-16px font-normal
- **Small/Metadata:** 12px font-normal (text-xs to text-sm)
- **Monospace (Codes/IDs):** `font-mono` for TX hashes, token IDs, order numbers

### Font Weight Distribution
- **Bold:** Titles, CTAs, price values
- **Semibold:** Section headers, status labels
- **Normal:** Body copy, descriptions
- **Light:** Placeholder text, disabled states

### Korean Localization
- All UI text uses Korean equivalents (예: "확인" for confirm, "취소" for cancel)
- Consistent Korean terms for status badges and action labels
- Date formats in Korean style: "YYYY.MM.DD"
- Weekday headers: "일월화수목금토" (Sun-Sat abbreviations)

---

## 4. COMPONENT PATTERNS & REUSABLE ELEMENTS

### Button Styles
**Primary CTA Button:**
- Gradient fill (brand colors)
- 44px min-height
- Full width by default
- Border-radius: rounded-lg (8px)
- Disabled state: opacity-50 with cursor-not-allowed

**Secondary Button:**
- Transparent with colored border
- Same height/sizing as primary
- Hover state: slight background tint

**Gradient Border Button:**
- Custom component (`gradient-border-button`)
- Gradient stroke using CSS `border-image`
- Used for duplicate check, verification steps
- Special effect: gradient animates/glows on hover

**Destructive Button:**
- Red background (#ef4444)
- White text
- Used for delete, withdraw, refund actions
- Confirmation required before action

### Card Patterns
**Event Card:**
- Poster image (top, 3:4 ratio)
- Title (2 lines max, truncated)
- Date + Venue (secondary text)
- Price range with "CTK~" suffix
- Interactive: click → detail page or modal

**Ticket Card:**
- Poster thumbnail (left or top)
- Ticket info: seat, grade, date
- Status badge (top-right corner)
- Action buttons (QR, Transfer, Resale, Refund)
- Conditional rendering based on status

**Resale Card:**
- Event poster (compact)
- Event name + date + venue
- Seat information
- Original price (strikethrough) + resale price
- Discount percentage highlight
- Click → detail modal or full page

**Wallet Card:**
- Gradient border (oklch multi-stop)
- CTK balance (large, bold)
- Wallet address (monospace, truncated with copy button)
- Background: elevated-surface (slightly lighter than page)

### Input Patterns
**Text Inputs:**
- Elevated-surface background
- Rounded corners (rounded-lg)
- 44px height for mobile touch targets
- Border-bottom separator (bottom-2 border-gray-300)
- Icon prefix (email, lock, phone) on left
- Focus state: border color change to primary
- Error state: red border + error message below

**Phone Input:**
- Auto-formatting: spaces after 3rd and 7th digit
- Validation: length check before enable
- Pattern: "(010) 1234-5678" display format

**Password Input:**
- Eye icon toggle for visibility (right side)
- Masked dots when hidden
- Validation feedback (length, special chars, etc.)

**Verification Code Input:**
- 6-digit input (or similar length)
- Monospace font
- Auto-focus to next digit
- Submit button enable after complete

**Date/Dropdown Inputs:**
- Bottom sheet or modal selection UI
- Calendar widget for date picking
- Pill-based selection for categorical options

### Badge & Status Indicators
**Status Badges:**
- Pill-shaped (rounded-full)
- Color-coded by status
- Typically 12px font, padding x-3 y-1
- Placement: top-right corner of cards, headers

**Count Badges:**
- Small circular badges on tab icons/buttons
- Red background with white text
- 20px × 20px typical size

**Category Pills:**
- Background with border
- Used for grade selection, region filtering
- Active state: gradient background, white text

### Modal & Dialog Patterns
**Confirmation Modal:**
- Centered white card on dark overlay
- Icon at top (Check, X, Alert based on type)
- Title + description
- Two buttons (primary action + cancel)
- Slide/fade animation on entrance

**Bottom Sheet:**
- Slides up from bottom
- Header with close button (X icon)
- Scrollable content area
- Touch-dismiss by dragging down
- Used for filtering, selection lists

**Alert Box:**
- Colored border (red for error, orange for warning)
- 10% color tint background
- Icon (AlertCircle, Info) + message
- Optional: action link or button
- Spacing: 16px padding, rounded-lg

### Dividers & Separators
- **Simple Line:** `border-gray-200` or `border-gray-300`
- **Dashed Divider:** Used with centered label (e.g., "NFT 소장")
- **Double Line Separator:** For section breaks (top/bottom border)
- **Spacing:** 16px margin above/below typical section divider

---

## 5. NAVIGATION PATTERNS

### Screen Navigation
**AppShell Wrapper:**
- Consistent container for all screens
- Customizable header (title, icons, back button)
- Safe area insets for notch/status bar
- Bottom navigation bar (sticky)

**Header Components:**
- Back button (left) with ChevronLeft icon
- Screen title (center or left)
- Action icons (right): search, menu, settings, notification
- Gradient background optional (some screens use white header)

**Bottom Navigation Bar:**
- 5 tabs: Home, Concerts, My Tickets, Resale Market, My Page
- Icons only (no text labels)
- Active state: colored icon + underline
- Navigation via React Router or Next.js navigation

**Tab Navigation (In-screen):**
- Horizontal scroll for multiple options
- Border-bottom indicator for active tab
- 12px-16px gap between tabs

### Multi-Step Flows
**Seat Selection (3+ steps):**
1. Date selection (calendar modal)
2. Seat selection (zoomable map)
3. Payment (summary + approval)
4. Confirmation (success or failure screen)

**Sign Up (2-3 steps):**
1. Personal info (email, phone, name)
2. Password setup + terms agreement
3. Confirmation (email verification optional)

**Password Reset (3 steps):**
1. Email input + verification code
2. New password input
3. Success confirmation

### List Navigation
- **Infinite Scroll:** Concerts, resale lists load on scroll
- **Pagination:** Backup for large datasets
- **Empty State:** Heart icon + contextual message + action button (e.g., "홈으로" go home)

---

## 6. STATUS-BASED UI RENDERING

### Ticket Status States
```
PAID → USED (after check-in)
PAID → LISTED (resale)
PAID → TRANSFERRED (transfer complete)
PAID → REFUNDED (refund complete)
UPCOMING → SOLD_OUT → COMPLETED
```

### Visual Indicators by Status
| Status | Badge Color | Icons | Actions Available |
|--------|------------|-------|-------------------|
| PAID (upcoming) | Green | CheckCircle2 | Transfer, Resale, Refund |
| USED | Gray | CheckCircle | View Details, Refund if eligible |
| LISTED | Orange | Tag | Cancel Listing, View Details |
| TRANSFERRED | Blue | Send | View Details, Archive |
| REFUNDED | Green | RefreshCw | View Details, Archive |
| EXPIRED (transfer) | Red | Clock | None - archived |

### Show Status States
- **UPCOMING:** Blue badge, "곧 시작" (Coming Soon)
- **ON_SALE:** Green badge, "예매 중" (On Sale)
- **SOLD_OUT:** Red badge, "완매" (Sold Out)
- **COMPLETED:** Gray badge, "종료" (Completed)

### Transaction Status
- **CONFIRMED:** CheckCircle icon, gray text
- **PENDING/CONFIRMING:** Clock icon, orange text + confirmation count
- **FAILED:** AlertCircle icon, red text + error message

### Queue Status (Waiting)
- **WAITING:** Animated circular progress, queue position number
- **READY_TO_ENTER:** 60-second countdown timer, CheckCircle icon
- **EXPIRED:** Grayed out, re-queue option

---

## 7. ANIMATION & TRANSITION PATTERNS

### Page Transitions
- **Default:** Fade-in (opacity 0→1 over 300ms)
- **Modal Entry:** Scale + fade (scale 0.9→1, opacity 0→1)
- **Bottom Sheet:** Slide up (translateY 100%→0)
- **Back Navigation:** Slide out right (subtle)

### Component Animations
- **Rotating Icons:** Loading spinners use infinite rotation
- **Pulsing Effects:** Ping animation for attention (e.g., "NFT 소장" badge)
- **Stroke Animation:** SVG progress circles animate stroke on state change
- **Card Flip:** ReactCardFlip library for collection cards (90° rotation)
- **Parallax Tilt:** react-parallax-tilt for 3D card effects
- **Shimmer/Skeleton:** Placeholder while loading content

### Micro-interactions
- **Button Hover:** Slight scale increase + opacity change
- **Input Focus:** Border color transition + shadow
- **List Item Tap:** Background color highlight (temporary)
- **Icon Transitions:** Smooth color/opacity changes
- **Accordion:** Slide down/up for expand/collapse

### Special Effects (Collection Cards)
- **Holographic Effects:** 12 variants (rainbow, aurora, prism, cosmos, sunset, neon)
- **Pokemon Card Effects:** 12 styles (poke-holo, poke-galaxy, poke-v, poke-vmax, etc.)
- **Foil Effects:** Gold, silver, rose foil with reflective properties
- **Paper Effects:** Cotton, crumpled textures
- **Aura Types:** Rare, milestone, spark with particle effects
- **Shimmer:** Animated gradient sweep across card surface

---

## 8. FORM & INPUT PATTERNS

### Input Field Structure
```
┌─────────────────────────┐
│ 📧 Email                │  (icon prefix)
├─────────────────────────┤  (bottom border)
│ error message if invalid│  (error state)
└─────────────────────────┘
```

### Validation Patterns
- **Real-time Feedback:** Email duplicate check, phone format validation
- **Submit Button:** Disabled until all required fields valid + terms agreed
- **Error Messages:** Displayed below field in red, specific to validation rule
- **Success Indicators:** Checkmark icon or green border on valid fields

### Complex Input Patterns
**Calendar Date Picker:**
- Grid layout with Korean weekday headers (일월화수목금토)
- Disabled dates (no shows) appear grayed out
- Dot indicators below dates with sessions available
- Selected date highlighted with background color
- Navigation: left/right arrows for month traversal

**Time Selection:**
- Displayed as session/date pills
- "DAY 1", "DAY 2" labels for multi-day events
- Price display per session
- Selection persists to next step

**Grade/Seat Selection:**
- Color-coded pills per grade (different hues)
- Price range display per grade
- Seat map (zoomable, pannable) for precise selection
- Selected seat highlighted/outlined
- Confirmation before proceeding to payment

**SMS Verification:**
- Phone number input (formatted)
- Send button → "전송됨" (Sent) state
- 6-digit code input appears after send
- Timer countdown (if applicable)
- Resend option after timeout

---

## 9. LOADING & EMPTY STATES

### Loading States
- **Skeleton Loaders:** Shimmer animation on placeholder elements
- **Spinner:** Center-aligned, colored primary
- **Progress Indicator:** SVG-based circular progress for queue/uploads
- **Text:** "로딩 중..." (Loading...) with spinner

### Empty States
**Pattern Components:**
- Large icon (48px-64px) in secondary color
- Contextual message (2-3 lines)
- Optional: action button to navigate elsewhere

**Examples:**
- **Empty Wishlist:** Heart icon + "아직 관심 상품이 없어요" + "홈으로" button
- **Empty Wallet History:** LogIn icon + message + navigation
- **Empty Transaction History:** Clock icon + message
- **No Search Results:** Magnifying glass icon + "검색 결과가 없어요"

### Error States
- **Generic Error:** AlertCircle icon, red text, retry button
- **Network Error:** WiFi/globe icon, message, retry option
- **Insufficient Balance:** Red alert box with CTK top-up suggestion
- **Sold Out:** Large "완매" badge with retry/browser button

---

## 10. SHARED COMPONENT LIBRARY

### Reusable UI Components
**From codebase observations:**

- **event-card.tsx:** Event poster, title, date, price range, click handler
- **ticket-card.tsx:** Ticket visualization with status badge, actions
- **status-badge.tsx:** Status indicator pill with color mapping
- **empty-state.tsx:** Icon + message + optional button
- **ticket-sparkles-aura.tsx:** Particle/aura effect wrapper for special cards
- **tutorial-dialog.tsx:** Onboarding or how-to modal
- **gradient-border-button.tsx:** CSS border-image gradient effect
- **elevated-surface:** Class for cards with subtle depth (background color lift)
- **Sheet (from @/components/ui/sheet):** Bottom sheet modal component

### Core State Management
**AppContext (@/lib/app-context.tsx):**
- Global user state (logged in, user profile)
- Wallet balance (CTK)
- Notification settings
- Wishlist state
- Ticket collection
- Theme/preferences (implied)

**Navigation via AppShell (app-shell.tsx):**
- Header customization
- Bottom nav state tracking
- Safe area handling

### Icon Libraries
- **lucide-react:** Primary icon set (ChevronLeft, AlertCircle, CheckCircle2, Heart, Lock, Eye, Search, Menu, Settings, Bell, Clock, Users, ArrowDownLeft, ArrowUpRight, Award, QrCode, Send, Tag, RefreshCw, LogIn, X, Info, Plus, Minus, Download, Share2, ZoomIn, ZoomOut)
- **@heroicons/react:** Secondary/alternate icons (color variants)
- **Custom SVGs:** QR codes, progress circles, holographic patterns

---

## 11. LAYOUT CONVENTIONS

### Screen Layout Template
```
┌─────────────────────────┐
│   Header (Customizable) │
├─────────────────────────┤
│                         │
│   Main Content Area     │
│   (Scrollable)          │
│                         │
├─────────────────────────┤
│   Bottom Nav (Fixed)    │
└─────────────────────────┘
```

### Content Area Patterns
- **Single Column:** Default for most screens
- **Two Column Grid:** Resale marketplace, collection gallery
- **Card List:** Concerts, wishlist, transaction history
- **Form Stack:** Vertical input stack with consistent gaps
- **Carousel:** Horizontal scrolling for hero or related items

### Responsive Strategies
- **Mobile-First:** Base styles for 390px, scale up for larger devices
- **Max-Width Container:** Prevent excessive width on tablets/desktops
- **Flexible Grid:** 2 columns auto-switches to 1 on smaller screens
- **Sheet vs Modal:** Bottom sheet preferred for mobile, modal possible on desktop

### Spacing Around Content
- **Horizontal Padding:** 16-20px sides (maintained throughout)
- **Top Spacing:** 16px below header
- **Bottom Spacing:** 16px above bottom nav (or safe-area-b)
- **Section Gaps:** 24px between major content blocks

---

## 12. SPECIAL FEATURE PATTERNS

### Blockchain/NFT Integration
- **Token ID Display:** Monospace font, typically truncated (first 10 + "..." + last 8)
- **Owner Address:** Truncated with copy-to-clipboard button
- **Contract Info:** Read-only display, may link to explorer
- **Transaction Hash:** Formatted same as Token ID
- **Status Indicators:** Confirmation count (targets 12), CONFIRMED/FAILED state
- **Network Label:** Chain name display (blockchain type)

### Payment & Wallet
- **CTK Balance:** Large, prominent display (bold, 24px+ font)
- **Price Breakdown:** Original → Discounted → Final (strikethrough style)
- **Refund Policy:** Fee rate display with daysBefore mapping
- **Transaction Types:** 7 types (PURCHASE, RESALE_LIST, RESALE_BUY, TRANSFER, REFUND, CHARGE, RESALE_SELL)
- **Arrow Icons:** In for ArrowDownLeft, out for ArrowUpRight

### QR Check-in Flow
- **OTP Display:** SVG-rendered QR code (mock implementation)
- **Refresh Timer:** 30-second countdown with visual indicator
- **Circular Progress:** SVG stroke animation for countdown
- **Status Transitions:** WAITING → CHECKED_IN with animation
- **Auto-refresh:** OTP value updates every 30 seconds

### Resale Marketplace
- **Price Comparison:** Original (strikethrough) vs resale with discount %
- **Insufficient Balance Alert:** Red-bordered alert box
- **Purchase Guidelines:** Info box with bullet points
- **Session Filtering:** Pill or sheet-based selector
- **Sorting Options:** Latest, price (ascending/descending implied)

### Refund/Transfer/Resale Actions
- **Multi-step Confirmations:** Modal with fee/policy info
- **Recipient Details:** Phone/email, avatar icon
- **Seat Information:** "Row A, Seat 10" or similar
- **Blockchain Info:** TX hash, status, confirmations visible
- **Completion Screen:** Animated success with event details, wallet balance update

---

## 13. ANDROID VS V0 SCREEN MAPPING

### V0 Screens (34 identified)
**Auth Flows:**
- login-screen, signup-screen, password-reset-screen, find-account-screen, sms-verification-screen, password-change-screen

**Ticket Management:**
- my-tickets-screen, ticket-detail-screen, qr-checkin-screen, transfer-screen, transfer-complete-screen, transfer-failed-screen

**Event Browsing:**
- home-screen, concerts-screen, event-detail-screen, event-date-selection-screen, seat-selection-screen, waiting-queue-screen

**Purchase & Payment:**
- payment-screen, purchase-failed-screen

**Resale Marketplace:**
- resale-list-screen, resale-tickets-screen, resale-detail-screen, resale-create-screen, resale-purchase-complete-screen

**Wallet & Collectibles:**
- wallet-screen, wallet-history-screen, tx-history-screen, collection-screen, collectible-ticket-detail-screen

**User Profile & Settings:**
- my-page-screen, settings-screen, withdraw-screen, wishlist-screen

**Note:** Android equivalent screens not visible in this codebase review. Cross-platform mapping would require Android source review.

---

## 14. DESIGN SYSTEM EXTRACTION SUMMARY

### Color Palette
- Oklch-based primary gradient
- Systematic status colors (green, red, orange, blue, gray)
- Accent floats: purple-200, blue-200, teal-200, indigo-200
- Dark overlay (black with alpha) for modals

### Spacing Scale
- Base unit: 4px (Tailwind convention)
- Common gaps: 12px (3), 16px (4), 24px (6), 32px (8)
- Consistent 16-20px horizontal padding

### Typography
- 3-tier hierarchy: titles, subsections, body
- Bold/semibold/normal weight distribution
- Monospace for codes/IDs
- 14-16px body default, 28-32px hero titles

### Components
- Reusable button, card, input, badge, modal patterns
- Gradient borders as premium accent
- Elevated-surface for depth
- Icons from lucide-react + custom SVGs

### Interactions
- Fade/scale/slide animations (300ms typical)
- Real-time validation feedback
- Status-driven conditional rendering
- Touch-friendly 44px+ target sizes

### Mobile Patterns
- 390px max-width target
- Bottom navigation persistent
- Bottom sheets for selection
- Safe area insets respected

---

## 15. IMPLEMENTATION GUIDELINES FOR NEW SCREENS

### Checklist for New Screen Addition
- [ ] Wrap with `AppShell` and customize header
- [ ] Apply consistent spacing (p-4 to p-5 horizontal)
- [ ] Use oklch variables for colors (avoid hardcodes)
- [ ] Implement empty state if applicable
- [ ] Add loading state with skeleton/spinner
- [ ] Use status-badge for status indicators
- [ ] Ensure 44px+ touch targets
- [ ] Add Korean localization strings
- [ ] Include error handling with alert boxes
- [ ] Test responsive behavior at 390px width
- [ ] Use Bottom Sheet or Modal for dialogs
- [ ] Implement smooth transitions (300ms)

### Common Pattern Template
```tsx
'use client';
import { useState } from 'react';
import { AppShell } from '@/components/cheket/app-shell';
import { StatusBadge } from '@/components/cheket/shared/status-badge';

export default function NewScreen() {
  const [state, setState] = useState('initial');
  
  return (
    <AppShell title="Screen Title">
      <div className="space-y-6 p-4">
        {/* Content blocks with gap-6 spacing */}
        <div className="rounded-lg bg-elevated-surface p-4">
          {/* Card content */}
        </div>
      </div>
    </AppShell>
  );
}
```

---

## 16. NOTABLE OBSERVATIONS

### Design Excellence
1. **Consistency:** All screens follow AppShell pattern, maintaining visual hierarchy
2. **Accessibility:** Status badges clearly indicate state, icons provide visual support
3. **Performance:** Image lazy-loading on collection cards, efficient state management
4. **Mobile Optimization:** Touch targets, safe area handling, readable text sizes
5. **User Feedback:** Clear success/error states, confirmation modals for irreversible actions

### Technical Patterns
1. **Client-side Rendering:** 'use client' directives enable interactivity on all screens
2. **Context API:** Centralized state for user, wallet, notifications
3. **Type Safety:** TypeScript throughout (implied by .tsx extensions)
4. **Tailwind Extensibility:** Custom oklch tokens allow brand color flexibility
5. **Component Reusability:** Shared components reduce code duplication

### Localization Excellence
1. **Korean-First:** UI completely localized (no English labels)
2. **Date Formatting:** Consistent YYYY.MM.DD format
3. **Korean-Specific Strings:** Status terms, action labels, error messages
4. **Weekday Abbreviations:** 일월화수목금토 used in calendar widget
5. **Number Formatting:** CTK amounts formatted with appropriate spacing

---

## APPENDIX: File Reference Map

**Screens Read (34 total):**
1. home-screen.tsx - Hero carousel, rankings, time sales
2. login-screen.tsx - Email/password auth
3. my-tickets-screen.tsx - Ticket list with filters
4. concerts-screen.tsx - Browse events, infinite scroll
5. payment-screen.tsx - Order summary, payment approval
6. wallet-screen.tsx - CTK balance, address management
7. resale-list-screen.tsx - Search resale marketplace
8. my-page-screen.tsx - User profile, quick links
9. seat-selection-screen.tsx - Multi-step seat selection
10. event-detail-screen.tsx - Event information, wishlist
11. transfer-screen.tsx - Transfer ticket to recipient
12. resale-create-screen.tsx - List ticket for resale
13. signup-screen.tsx - Two-step registration
14. waiting-queue-screen.tsx - Queue position tracking
15. purchase-failed-screen.tsx - Failure reason display
16. resale-detail-screen.tsx - Resale item details
17. ticket-detail-screen.tsx - Ticket info, actions, NFT data
18. qr-checkin-screen.tsx - Check-in with OTP QR code
19. resale-purchase-complete-screen.tsx - Purchase success
20. collectible-ticket-detail-screen.tsx - NFT collection detail
21. collection-screen.tsx - Card flip, holographic effects (40.5KB)
22. transfer-complete-screen.tsx - Transfer success animation
23. transfer-failed-screen.tsx - Transfer failure reasons
24. wishlist-screen.tsx - Saved events list
25. tx-history-screen.tsx - Blockchain transactions
26. wallet-history-screen.tsx - CTK transaction history
27. settings-screen.tsx - Notification, password settings
28. password-change-screen.tsx - Password reset flow
29. sms-verification-screen.tsx - SMS code verification
30. find-account-screen.tsx - Account recovery
31. password-reset-screen.tsx - Multi-step password reset
32. event-date-selection-screen.tsx - Calendar date picker
33. withdraw-screen.tsx - Account deletion flow
34. resale-tickets-screen.tsx - Session filtering, resale list

**Shared Components:**
- event-card.tsx
- ticket-card.tsx
- status-badge.tsx
- empty-state.tsx
- ticket-sparkles-aura.tsx
- tutorial-dialog.tsx
- gradient-border-button.tsx

**Core Files:**
- cheket-app.tsx (main app wrapper)
- app-shell.tsx (screen template)
- bottom-nav.tsx (navigation)
- lib/app-context.tsx (global state)

---

**Document Complete** — All 34 screens analyzed, design system extracted, patterns documented, and guidelines provided for consistent implementation.