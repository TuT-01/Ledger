# Bking Android Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first Android project foundation for a personal asset and liability accounting app.

**Architecture:** The app uses a local-first Android architecture with Jetpack Compose for UI, ViewModel-ready screen state, and a domain layer that models double-entry accounting. Room, DataStore, and Hilt are wired into the Gradle project so the next development phase can add persistence, settings, and dependency injection without changing the project shape.

**Tech Stack:** Kotlin, Jetpack Compose, Android Gradle Plugin, Room, DataStore, Hilt, Coroutines, JUnit.

---

### Task 1: Project Shell

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `.gitignore`
- Create: `app/build.gradle.kts`

- [ ] **Step 1: Create Gradle settings**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Bking"
include(":app")
```

- [ ] **Step 2: Create root plugin declarations**

```kotlin
plugins {
    id("com.android.application") version "9.1.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("com.google.devtools.ksp") version "2.3.7" apply false
    id("com.google.dagger.hilt.android") version "2.59.1" apply false
}
```

- [ ] **Step 3: Create app module configuration**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.bking"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bking"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
```

### Task 2: Domain Model With Tests

**Files:**
- Create: `app/src/main/java/com/bking/domain/model/Money.kt`
- Create: `app/src/main/java/com/bking/domain/model/AccountingTypes.kt`
- Create: `app/src/main/java/com/bking/domain/model/LedgerDraft.kt`
- Create: `app/src/main/java/com/bking/domain/service/DoubleEntryFactory.kt`
- Create: `app/src/test/java/com/bking/domain/service/DoubleEntryFactoryTest.kt`

- [ ] **Step 1: Write tests for generated double-entry drafts**

```kotlin
@Test
fun `expense debits expense account and credits asset account`() {
    val draft = DoubleEntryFactory.expense(
        amount = Money.cnyCents(1800),
        paidFromAccountId = "bank",
        expenseAccountId = "food",
        occurredAt = Instant.parse("2026-05-12T10:00:00Z"),
        note = "Lunch"
    )

    assertEquals(TransactionType.EXPENSE, draft.type)
    assertEquals(2, draft.entries.size)
    assertEquals(LedgerEntryDraft("food", EntryDirection.DEBIT, Money.cnyCents(1800)), draft.entries[0])
    assertEquals(LedgerEntryDraft("bank", EntryDirection.CREDIT, Money.cnyCents(1800)), draft.entries[1])
    assertTrue(draft.isBalanced())
}
```

- [ ] **Step 2: Run the test and verify it fails before implementation**

Run: `./gradlew testDebugUnitTest --tests com.bking.domain.service.DoubleEntryFactoryTest`

Expected: compile failure because `DoubleEntryFactory` and model classes do not exist yet.

- [ ] **Step 3: Implement minimal domain model and factory**

Implement immutable money, accounting enums, transaction draft, ledger entry draft, and helper methods for expense, income, transfer, credit card spending, repayment, lending, borrowing, investment buy, investment sell, and balance adjustment.

- [ ] **Step 4: Run unit tests and verify the domain behavior**

Run: `./gradlew testDebugUnitTest --tests com.bking.domain.service.DoubleEntryFactoryTest`

Expected: all tests in `DoubleEntryFactoryTest` pass.

### Task 3: Android Entry Point

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/bking/BkingApplication.kt`
- Create: `app/src/main/java/com/bking/MainActivity.kt`
- Create: `app/src/main/java/com/bking/ui/BkingApp.kt`
- Create: `app/src/main/java/com/bking/ui/theme/BkingTheme.kt`

- [ ] **Step 1: Add application and activity**

Create `BkingApplication` for Hilt and `MainActivity` for Compose.

- [ ] **Step 2: Add first Compose screen**

Render total assets, liabilities, net worth, monthly income, monthly expense, and quick accounting actions as the first usable screen.

- [ ] **Step 3: Build debug APK**

Run: `./gradlew assembleDebug`

Expected: debug build succeeds and produces `app/build/outputs/apk/debug/app-debug.apk`.

### Task 4: Next Phase Readiness

**Files:**
- Create: `README.md`

- [ ] **Step 1: Document local setup**

Explain Android Studio sync, required SDK level, command-line verification, and next implementation milestones.

- [ ] **Step 2: Commit scaffold**

```bash
git add .
git commit -m "chore: scaffold android accounting app"
```
