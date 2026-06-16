# Finatra — Product Requirements Document
**by Jinatra** · Software & Digital Solutions
**Version:** 1.1
**Date:** June 2026
**Author:** Liad, Founder — Jinatra
**Platform:** Android (Native)

---

## 1. Product Overview

**Finatra** is a personal finance manager Android application built under the Jinatra brand. It provides users with a full financial overview — tracking income, expenses, budgets, accounts, and net worth — entirely offline with all data stored locally on the device. No cloud services, no bank integrations, no external data sharing. Privacy is a core principle.

The app features optional AI-powered insights via user-supplied API keys or fully on-device Gemma models, and is designed with Material You expressive design rooted in the Jinatra brand identity.

---

## 2. Goals & Principles

- **Privacy First** — all data stays on-device, no cloud sync, no telemetry
- **Full Ownership** — users manage everything manually, no third-party connections
- **Brand Consistency** — Finatra is a Jinatra product; design, typography, and color follow the Jinatra brand guide
- **Offline AI** — AI features work without internet using on-device Gemma models
- **Material You Native** — follows Android 12+ dynamic theming, feels native and polished

---

## 3. Target Users

- Individuals who want full control over their finances without trusting apps with bank credentials
- Privacy-conscious users who prefer local-only data
- Users in Bangladesh and similar markets using multiple account types (bKash, bank, cash, credit)
- People who want AI financial insights without sacrificing privacy

---

## 4. Platform & Tech Stack (Recommended)

| Layer | Technology |
|---|---|
| Platform | Android (API 26+ / Android 8.0 minimum, API 31+ for Material You) |
| Language | Kotlin |
| UI Framework | Jetpack Compose + Material 3 Expressive |
| Local Database | Room (SQLite) |
| Widget | Glance API (Material You dynamic color) |
| AI — Cloud | Gemini, Claude, OpenRouter APIs (user-supplied key) |
| AI — On-device | MediaPipe LLM Inference + Gemma Lite / AI Edge Gallery integration |
| Security | AndroidX Biometric API + EncryptedSharedPreferences |
| Notifications | WorkManager + NotificationManager |
| Export | CSV via FileProvider |

---

## 5. Design System

### 5.1 Brand Identity
Finatra is a Jinatra product. All design decisions reference the Jinatra Brand Guidelines v1.0. The app should feel like a natural sibling of the Jinatra brand — warm, minimal, confident.

### 5.2 App Name & Wordmark
- **App name:** Finatra
- **Wordmark font:** Negan (same as Jinatra logo) — used exclusively for the logo mark and the "Finatra" app name display. Not used in UI typography.
- **Package name:** `com.jinatra.finatra`
- **Tagline:** *Your finances, your way.*

### 5.3 App Icon
Two adaptive icon variants for Android:

| Variant | Background | Mark Color |
|---|---|---|
| Light | Sweet Cream `#FFEACF` | Deep Teal `#0A756C` |
| Dark | Deep Teal `#0A756C` | Sweet Cream `#FFEACF` |

Icon construction: rounded "F" path (same stroke weight and corner radius as the Jinatra "J") + single solid filled node at top right — consistent with the Jinatra logo node motif representing connection and intelligence. Android adaptive icon shape: rounded square (108dp corner radius at 512×512).

### 5.4 Color Palette

#### Light Theme
| Role | Name | Hex |
|---|---|---|
| Background | Warm White | `#FFF8F2` |
| Surface / Cards | Mist Teal | `#E0F0EE` |
| Primary Accent | Deep Teal | `#0A756C` |
| Text Primary | Ink | `#1A1A1A` |
| Text Secondary | Deep Teal 70% | `#0A756CB2` |
| Error | System Red | `#B00020` |

#### Dark Theme
| Role | Name | Hex |
|---|---|---|
| Background | Deep Ink | `#0F1F1E` |
| Surface / Cards | Dark Teal | `#1A3330` |
| Primary Accent | Mist Teal | `#E0F0EE` |
| Text Primary | Sweet Cream | `#FFEACF` |
| Text Secondary | Mist Teal 70% | `#B2D8D4` |
| Error | Light Red | `#CF6679` |

#### Home Screen Widget
| Role | Hex |
|---|---|
| Widget Background | `#FFEACF` (Sweet Cream) |
| Widget Accent | `#0A756C` (Deep Teal) |
| Widget Text | `#1A1A1A` (Ink) |

> The home screen widget uses Material You dynamic color API (Android 12+) to harmonize with the user's wallpaper-derived color scheme, while preserving the Jinatra brand warmth as the base seed color.

### 5.5 Typography

| Style | Font | Weight | Size |
|---|---|---|---|
| Logo / App Name | Negan | — | Display only |
| Heading 1 | Poppins | Bold | 38–56sp |
| Heading 2 | Poppins | SemiBold | 24–32sp |
| Heading 3 | Inter | SemiBold | 16–20sp |
| Body | Inter | Regular | 14–16sp |
| Caption / Meta | Inter | Regular | 11–12sp |

> Negan is used exclusively for the Finatra logo/wordmark and is not applied to any UI typography elements.

### 5.6 Design Language
- Material 3 Expressive — rounded corners, elevation surfaces, expressive motion
- Minimal and uncluttered — no unnecessary chrome
- Warm and grounded — never pure white or pure black backgrounds
- Dark/Light mode toggle in-app, also follows system default
- Colored account cards — user picks a color per account for visual distinction
- Custom app icon: light and dark adaptive variants

---

## 6. Features

### 6.1 Onboarding
- Welcome screen with Finatra / Jinatra branding
- Set base currency on first launch
- Create initial accounts with opening balances
- Optional: set up PIN / biometric lock
- Optional: configure AI provider

### 6.2 Account Management
Users can create and manage multiple accounts:
- **Types:** Cash, Bank Account, Credit Card, Mobile Wallet (e.g. bKash)
- Each account has: name, type, currency, opening balance, color tag, icon
- No bank integration — all entries are manual, 100% private
- Transfer between accounts supported
- Colored account cards — user picks color per account

### 6.3 Transactions
- **Types:** Income, Expense, Transfer
- **Fields:** amount, currency, date & time, category, subcategory, account, notes/memo, tags, receipt photo (optional)
- Quick Add — floating action button, log a transaction in under 5 seconds
- Natural language entry via AI — type "spent 500 on lunch today" → AI parses into fields
- Swipe to edit or delete in list view
- Search & filter — by keyword, tag, date range, category, account
- Audit log — history of edits and deletions per transaction

#### Recurring / Scheduled Transactions
- Set frequency: daily, weekly, monthly, custom interval
- Auto-log or remind user to confirm
- Manage all recurring entries from a dedicated screen

### 6.4 Categories
- Predefined categories with icons (Food, Transport, Housing, Health, Entertainment, Shopping, Income, etc.)
- User can create fully custom categories
- Subcategories supported under each category
- Categories are color-coded

### 6.5 Budgets
- **Monthly budgets** — set per category, resets each month
- **Custom period budgets** — user defines start and end date
- Visual progress bar per budget (safe / warning / over)
- Budget overspend alerts (push notification + in-app)
- Budget summary on dashboard

### 6.6 Dashboard (Home Screen)
Customizable — users pin and reorder cards:
- Total balance across all accounts
- Net worth snapshot
- Income vs Expense this month
- Budget progress overview
- Recent transactions list
- Upcoming recurring transactions
- AI insight card (if AI configured, dismissible)

### 6.7 Analytics & Reports

#### Charts & Visuals
- Spending trends over time (line / bar chart)
- Category breakdown (donut / pie chart)
- Income vs Expense comparison (bar chart)
- Budget progress per category
- Net worth over time graph

#### Reports
- Monthly report — full summary of income, expenses, savings, top categories
- Yearly report — annual overview with month-by-month breakdown
- All reports viewable in-app

### 6.8 Multi-Currency
- Each account can have its own currency
- Manual exchange rate entry per currency pair
- Base currency set during onboarding (changeable in settings)
- All totals converted to base currency on dashboard

### 6.9 AI Integration

#### Cloud AI (User API Key)
- API key input field in settings
- Dropdown of supported free AI providers (Gemini, Claude, OpenRouter, etc.)
- Button next to each provider → opens provider's API key page in browser
- API key stored encrypted on-device via EncryptedSharedPreferences, never transmitted elsewhere

#### On-Device AI (Gemma)
- Option to download Gemma Lite models directly inside the app
- Auto-detect if Gemma is already installed via **Google AI Edge Gallery** — use it automatically without re-downloading
- All on-device inference runs locally, zero network calls

#### AI Features
- **Spending insights** — "You spent 40% more on food this month compared to last"
- **Smart categorization** — AI suggests a category when user types a transaction description
- **Natural language entry** — type "spent 500 on lunch today" → AI parses into transaction fields
- **Budget recommendations** — AI suggests budget limits based on spending history
- AI insight card on dashboard (dismissible)

### 6.10 Notifications
- Budget overspend alerts — triggered when a category budget is exceeded
- Recurring transaction reminders — remind user before scheduled transactions
- Low balance warnings — configurable threshold per account
- Weekly summary — every Monday, overview of last week's spending vs budget with warnings
- Monthly summary — first day of each month, full previous month recap with budget usage analysis
- All notifications individually configurable (on/off per type) in settings

### 6.11 Security
- **PIN lock** — 4 or 6 digit PIN, set during onboarding or settings
- **Biometric lock** — fingerprint / face unlock via AndroidX Biometric API
- **Auto-lock** — lock app after X minutes of inactivity (options: 1, 5, 15, 30 min, never)
- **Screenshot prevention** — FLAG_SECURE enabled by default (toggleable in settings)
- All sensitive data encrypted at rest

### 6.12 Data Portability
- **Export to CSV** — transactions, categories, accounts exported as CSV saved locally
- **Import from CSV** — import transactions from a formatted CSV template
- **Local Backup** — full app data backup to a local file (JSON format)
- **Restore** — restore from a previously saved backup file
- No cloud upload — all backup/export files stay on device

### 6.13 Home Screen Widget
- Shows: total balance, today's spending, budget status
- Follows **Material You dynamic color** — adapts to wallpaper-derived color scheme on Android 12+
- Jinatra brand warmth maintained (Sweet Cream / Deep Teal as base seed)
- Widget sizes: small (balance only), medium (balance + budget progress bar)
- Tappable — opens Finatra directly
- Gracefully degrades to static brand colors on Android below API 31

### 6.14 Additional Features
- **Customizable home dashboard** — users pin and reorder cards
- **Quick add** — FAB to log a transaction in under 5 seconds
- **Swipe to edit/delete** — transactions list gesture support
- **Colored account cards** — user picks color per account
- **Custom app icon** — light and dark adaptive icon variants
- **Onboarding flow** — set up base currency, accounts, opening balances on first launch
- **Audit log** — history of edits and deletions per transaction

### 6.15 Settings
- Theme: Light / Dark / System default (toggle in-app)
- Base currency
- Account management
- Category management
- Budget management
- AI provider & API key configuration
- Gemma model download / management
- Notification preferences (per type toggle)
- Security: PIN setup, biometric toggle, auto-lock timer, screenshot prevention
- Data: export CSV, import CSV, backup, restore
- App info: version, Jinatra branding, open source licenses

---

## 7. Screens & Navigation

### Bottom Navigation (5 tabs)
1. **Home** — customizable dashboard
2. **Transactions** — full transaction list with search & filter
3. **Budgets** — budget overview and management
4. **Analytics** — charts and reports
5. **Settings** — all configuration

### Additional Screens
- Account detail screen
- Add / Edit transaction screen (with AI quick-parse)
- Add / Edit account screen
- Add / Edit category / subcategory screen
- Add / Edit budget screen
- Recurring transactions management screen
- AI settings screen (provider, API key, Gemma model manager)
- Security setup screen (PIN, biometric, auto-lock)
- Onboarding flow (first launch only)
- Backup & restore screen
- Audit log screen

---

## 8. Non-Functional Requirements

| Requirement | Detail |
|---|---|
| Offline-first | App works 100% without internet (except cloud AI if configured) |
| No telemetry | Zero analytics, crash reporting, or usage data collection |
| No permissions abuse | Only: biometric, notifications, storage (backup/export), camera (receipt photos) |
| Performance | App launch < 2 seconds; transaction list scrolls at 60fps |
| Accessibility | WCAG AA contrast ratios, scalable text, content descriptions on all icons |
| Min Android version | API 26 (Android 8.0); Material You features gracefully degrade below API 31 |
| Data encryption | All sensitive data encrypted at rest via EncryptedSharedPreferences + Room encryption |

---

## 9. Out of Scope (v1.0)

- Bank / card integration of any kind
- Cloud sync or backup
- Multi-user / family sharing
- Web or iOS version
- In-app purchases or subscriptions
- Receipt OCR (consider for v2 with on-device AI)
- Automated exchange rate fetching (manual only in v1.0)

---

## 10. Branding & App Store Presence

- **App Name:** Finatra
- **Developer:** Jinatra
- **Tagline:** *Your finances, your way.*
- **Wordmark Font:** Negan (logo & app name display only)
- **App Icon:** Rounded "F" path + solid node — Deep Teal on Sweet Cream (light), Sweet Cream on Deep Teal (dark). Adaptive icon, 512×512, corner radius 108dp.
- **Package Name:** `com.jinatra.finatra`

---

*Finatra is a Jinatra product. Built with care. © 2026 Jinatra.*
