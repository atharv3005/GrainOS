<div align="center">

# 🌾 GrainOS — Enterprise Grain Trading, ERP & WMS Ecosystem

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.7.2-brightgreen.svg?style=flat&logo=android)](https://developer.android.com/studio/releases/gradle-plugin)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09.00-blue.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room Database](https://img.shields.io/badge/Room%20DB-v10%20(SQLite)-orange.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg?style=flat)]()

**A modern, offline-first Enterprise Digital Operating System / ERP + WMS + Procurement + Inventory + Finance + Cash-Flow ecosystem designed specifically for agricultural commodity and grain traders.**

> **Core Architectural Principle:**  
> *"ENTER ONCE → CALCULATE AUTOMATICALLY → STORE ONCE → REUSE EVERYWHERE"*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features & Modules](#-key-features--modules)
  - [1. Dual-Weighbridge Intake & Procurement](#1-dual-weighbridge-intake--procurement)
  - [2. APMC & Statutory Tax Compliance](#2-apmc--statutory-tax-compliance)
  - [3. Outbound Dispatch & Government Compliance](#3-outbound-dispatch--government-compliance)
  - [4. Double-Entry General Ledger & Finance](#4-double-entry-general-ledger--finance)
  - [5. Warehouse Management (WMS) & Telemetry](#5-warehouse-management-wms--telemetry)
  - [6. Post-Dated Cheque (PDC) Lifecycle Machine](#6-post-dated-cheque-pdc-lifecycle-machine)
  - [7. Enterprise RBAC & Security](#7-enterprise-rbac--security)
  - [8. Physical Cash Drawer Reconciliation](#8-physical-cash-drawer-reconciliation)
  - [9. FIFO Costing & P&L Engine](#9-fifo-costing--pl-engine)
  - [10. Hardware Integrations & Localization](#10-hardware-integrations--localization)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Database Schema (Room v10)](#-database-schema-room-v10)
- [Directory Structure](#-directory-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running Unit Tests](#running-unit-tests)
  - [Installing on a Physical Device](#installing-on-a-physical-device)
- [Demo Credentials](#-demo-credentials)
- [Screenshots & UI Showcase](#-screenshots--ui-showcase)
- [License](#-license)

---

## 🌟 Overview

GrainOS completely eliminates paper registers, manual ledger entries, manual tax calculators, duplicate data entry, and disconnected spreadsheets from grain trading operations.

The system handles real-world mandi operations:
- **Dual-weighbridge gross and tare intake** with automatic dockage and moisture cuts.
- **Strict statutory compliance** with Maharashtra APMC Cess (1.0% Mandi Shulk + 0.5% Supervisory Fee) and Section 194Q TDS / Section 206C(1H) TCS tax regimes.
- **Sequential statutory document numbering** per Financial Year (`GIN/26-27/00142`, `DSP/26-27/00088`).
- **Guarded inventory dispatches** preventing negative stock allocations with atomic rollback.
- **Double-Entry General Ledger journal posting** enforcing strict mathematical debit/credit parity.
- **Multi-tenant organization partitioning** allowing seamless multi-branch switching without data cross-contamination.

---

## 🚀 Key Features & Modules

### 1. Dual-Weighbridge Intake & Procurement
- **Full Intake Pipeline**: Gate Entry Registration → Gross Weighment → Moisture/Quality Grading → Tare Weighment → Acceptance & Slip Generation.
- **Partial Transaction Manager**: Split inhomogeneous truck loads into Grade A accepted grain, salvage lots (reduced rate), and rejected quantities.
- **Sequential Token Numbering**: Gapless statutory numbering per facility and financial year.

### 2. APMC & Statutory Tax Compliance
- **APMC Mandi Cess**: Automatic calculation of 1.0% Market Fee and 0.5% Supervisory Charge (1.5% Total Cess).
- **Income Tax Section 194Q TDS**: Tracks cumulative turnover per PAN in the financial year. Applies 0.1% TDS above ₹50 Lakhs threshold (5.0% for missing PAN) with automatic TCS Section 206C(1H) exemption flags.

### 3. Outbound Dispatch & Government Compliance
- **Guarded Stock Reduction**: Atomically verifies available physical stock before dispatch; strictly blocks negative balance dispatches with meaningful error dialogs.
- **NIC E-Way Bill JSON Generation**: Conforms to standard GST portal JSON schemas (Part A & Part B) with statutory 8-digit grain HSN codes (e.g., `10059000` for Maize, `12019000` for Soybean, `10019910` for Wheat).
- **B2B E-Invoicing**: Automatic generation of signed QR code payloads and cryptographic IRN hashes.

### 4. Double-Entry General Ledger & Finance
- **Automated Journal Vouchers**: Every accepted procurement and outbound sales dispatch generates balanced double-entry vouchers:
  - **Procurement JV**: `Dr. Grain Inventory` = `Cr. APMC Mandi Cess Payable` + `Cr. TDS 194Q Payable` + `Cr. Farmer Accounts Payable`.
  - **Sales JV**: `Dr. Accounts Receivable` = `Cr. Grain Sales Revenue`.
- **Financial Statements**: Real-time Trial Balance, Profit & Loss (P&L), and Balance Sheet generation.

### 5. Warehouse Management (WMS) & Telemetry
- **Multi-Facility Tracking**: Real-time capacity, stock level, and weighted average cost tracking across Godowns, Silos, and Drying Yards.
- **IoT Environmental Telemetry**: Real-time temperature and moisture sensor simulation with ventilation status alerts.
- **Moisture Shrinkage Capitalization**: Logs moisture evaporation tonnage loss and capitalizes its acquisition cost across remaining stock to recalculate true COGS.
- **Truck Rejections & Salvage**: Captures demurrage, freight loss, return shifting labor (50% rule), and net salvage realization.

### 6. Post-Dated Cheque (PDC) Lifecycle Machine
- **Full State Machine**: `ISSUED` → `DEPOSITED` → `PRESENTED` → `CLEARED` or `BOUNCED`.
- **Bounced Cheque Handling**: Automatically reverses bank credit, re-opens payable liability ledger, and logs statutory audit trails.

### 7. Enterprise RBAC & Security
- **Role-Based Permissions**: 4 pre-configured roles:
  - `OWNER (मालक)`: Unrestricted administrative access, rate overrides, EOD locks, user management.
  - `OPERATOR (ऑपरेटर)`: Weighbridge intake, tare/gross entry, moisture sampling, storage placement.
  - `ACCOUNTANT (मुनीम / सीए)`: Payment disbursements, PDC clearing, CA reports, P&L statements.
  - `VIEWER (तपासणीस)`: Read-only access to audit logs and telemetry.
- **Salted PBKDF2 PIN Security**: Passwords and PINs hashed with `PBKDF2WithHmacSHA256` (10,000 iterations + 16-byte unique cryptographic salt).
- **Multi-Tenant Scoping**: All 15 database entities partitioned by `org_code` (`OrganizationContext`).
- **Supervisor Approval Engine**: Automated challenges for moisture overrides (>15.5%) or high rate cuts (>₹50/Qtl).

### 8. Physical Cash Drawer Reconciliation
- **Denomination Counter**: Interactive cash drawer calculator for ₹500, ₹200, ₹100, ₹50, ₹20, ₹10 notes and coins.
- **Dynamic Cash Ledger**: Compares physical cash counted against dynamic opening balance + cash inflows - cash outflows - cash procurements - cash expenses.

### 9. FIFO Costing & P&L Engine
- **FIFO Inventory Matching**: Chronologically matches outward sales shipments against inward procurement lots to determine exact Cost of Goods Sold (COGS).
- **Shortage Lot Handling**: Automatically falls back to weighted-average procurement cost per quintal when shipments exceed available intake batches.

### 10. Hardware Integrations & Localization
- **80mm & 58mm ESC/POS Thermal Printing**: Dynamic page height layout calculation to ensure zero clipping on lengthy slips.
- **Multi-Language Support**: Complete bilingual localization in English, Marathi (मराठी), and Hindi (हिंदी).
- **Excel & PDF Exports**: Detailed reports exported using CSV/POI formatting and shareable directly via WhatsApp or Email.

---

## 🛠 Architecture & Tech Stack

```
GrainOS Android App
├── Presentation Layer (Jetpack Compose + Material 3 + Vico Charts)
│   ├── UI Screens & Navigation Hosts
│   └── ViewModels (StateFlow + UDF)
├── Domain Layer (Clean Architecture)
│   ├── Use Cases (Atomic Operations, Financial Math, Tax Engines)
│   ├── Managers (RBAC, Approval, Reconciliation, FIFO Costing, Multi-Tenant)
│   └── Models & Enums
├── Data Layer
│   ├── Room Database v10 (SQLite + Atomic Transaction Wrapping)
│   ├── 15 DAOs & Repositories
│   └── Export Engines (E-Way Bill, E-Invoice, Excel, Thermal ESC/POS)
└── Security & Platform
    ├── PBKDF2 Salted Cryptography
    └── Android Print Framework
```

| Component | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (BOM 2024.09.00) + Material 3 |
| **Database** | Android Room 2.6.1 + KSP |
| **Concurrency** | Kotlin Coroutines & StateFlow |
| **Charts & Graphs** | Vico Compose Charting Engine (v2.0.0-alpha.28) |
| **Cryptography** | `PBKDF2WithHmacSHA256` + AndroidX Security Crypto |
| **Testing** | JUnit4, Robolectric, Roborazzi, Coroutines Test |

---

## 🗄 Database Schema (Room v10)

The system is powered by 15 Room entities with multi-tenant compound indexes:

1. `procurements`: Inbound intake tickets, gross/tare weights, APMC cess, TDS, payout amounts.
2. `outbound_dispatches`: Outward sales delivery orders, transport fees, gate net vs mill unloaded weight.
3. `godowns`: Physical warehouse facilities, stock balance, moisture, base/adjusted COGS.
4. `inventory_movements`: Immutable append-only inventory ledger.
5. `parties`: Unified master database for Farmers, Corporate Buyers, Brokers, Transporters, and Labor Gangs.
6. `vendor_ledgers`: Financial transactions, credit/debit records, running ledger balances.
7. `payment_allocations`: Explicit matching of payments to procurement bills and expense vouchers.
8. `document_sequences`: Gapless sequential document numbering state per FY & facility.
9. `general_ledger`: Double-entry journal accounting entries.
10. `cash_drawer_counts`: Physical cash counts, system cash balance, reconciliation variances.
11. `manual_expenses`: Real-time fluctuating operational expenses (Hamali, Bags, Freight, Tolls).
12. `trade_bookings`: Forward broker contract agreements and margin estimations.
13. `truck_rejections`: Corporate mill rejection tickets, demurrage, and net salvage realization.
14. `inventory_reconciliations`: Moisture shrinkage capitalization and periodic physical adjustments.
15. `users` & `approval_requests`: Salted RBAC user accounts and supervisor override requests.

---

## 📁 Directory Structure

```
app/src/main/java/com/example/
├── data/
│   ├── export/               # E-Way Bill, E-Invoice, Excel exporters
│   ├── local/                # AppDatabase, 15 Room DAOs, DataMigrationManager
│   ├── model/                # Room Entities, Enums, ChartOfAccounts
│   └── repository/           # Repository implementations & GrainRepository
├── domain/
│   ├── managers/             # OrganizationContext, FIFO Engine, Cash Drawer, Stress Runner
│   └── usecase/              # 16 Domain Use Cases & Approval Workflow Engine
├── security/                 # RbacManager, UserEntity, UserRole, Salted Hashing
├── ui/
│   ├── components/           # Reusable UI widgets, Dialogs, Tutorial Dialog
│   ├── navigation/           # Navigation Hosts, Bottom Bars, Top Bars
│   ├── screens/              # 20+ Jetpack Compose screens
│   └── theme/                # Material 3 Color Schemes, Typography, Shapes
└── util/                     # ThermalPrinterHelper, LocaleHelper, SoundEffects
```

---

## 🏁 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK / JBR**: Version 17 or higher (Java 21 supported)
- **Android SDK**: API Level 34 (Android 14) / Min API Level 26 (Android 8.0)

### Building the Project
Clone the repository and build via Gradle:
```bash
git clone https://github.com/atharv3005/GrainOS.git
cd GrainOS

# Build debug APK
./gradlew assembleDebug
```

### Running Unit Tests
Execute the comprehensive domain test suite:
```bash
./gradlew testDebugUnitTest
```

### Installing on a Physical Device
Ensure USB Debugging is enabled on your Android phone and run:
```bash
adb install -r -d -g app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.aistudio.grainwms.procure -c android.intent.category.LAUNCHER 1
```

---

## 🔑 Demo Credentials

| Role | Username | Default PIN | Access Level |
| :--- | :--- | :--- | :--- |
| **Owner / Proprietor** | `owner` | `1234` | Full Administrative & Approval Access |
| **Weighbridge Operator** | `operator` | `0000` | Gate Registration, Weighment, Intake |
| **Accountant / Munim** | `accountant` | `1111` | Payment Vouchers, PDC Clearing, P&L |

---

## 📄 License

Copyright © 2026 GrainOS. All rights reserved.
Developed for agricultural commodity traders, mandi commission agents, and grain warehouse operators.
