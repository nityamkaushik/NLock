# Graph Report - .  (2026-06-21)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 282 nodes · 331 edges · 34 communities (27 shown, 7 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]

## God Nodes (most connected - your core abstractions)
1. `AppLockRepository` - 17 edges
2. `AppLockAccessibilityService` - 15 edges
3. `ServiceLifecycleOwner` - 12 edges
4. `AppPreferences` - 11 edges
5. `LockOverlayManager` - 10 edges
6. `NLockNavGraph()` - 9 edges
7. `NLockApplication` - 7 edges
8. `LockedAppDao` - 7 edges
9. `AppNotificationMasker` - 7 edges
10. `AppListViewModel` - 7 edges

## Surprising Connections (you probably didn't know these)
- `NLockNavGraph()` --calls--> `DecoyCalculatorScreen()`  [INFERRED]
  app/src/main/java/com/nityam/nlock/ui/navigation/NLockNavGraph.kt → app/src/main/java/com/nityam/nlock/ui/decoy/DecoyCalculatorScreen.kt
- `NLockNavGraph()` --calls--> `SetupScreen()`  [INFERRED]
  app/src/main/java/com/nityam/nlock/ui/navigation/NLockNavGraph.kt → app/src/main/java/com/nityam/nlock/ui/setup/SetupScreen.kt
- `NLockNavGraph()` --calls--> `VaultScreen()`  [INFERRED]
  app/src/main/java/com/nityam/nlock/ui/navigation/NLockNavGraph.kt → app/src/main/java/com/nityam/nlock/ui/vault/VaultScreen.kt
- `NLockNavGraph()` --calls--> `AppListScreen()`  [INFERRED]
  app/src/main/java/com/nityam/nlock/ui/navigation/NLockNavGraph.kt → app/src/main/java/com/nityam/nlock/ui/applist/AppListScreen.kt
- `LockScreenContent()` --calls--> `PinKeypad()`  [INFERRED]
  app/src/main/java/com/nityam/nlock/ui/lock/LockScreenContent.kt → app/src/main/java/com/nityam/nlock/ui/components/PinKeypad.kt

## Import Cycles
- None detected.

## Communities (34 total, 7 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.10
Nodes (15): Modifier, String, Int, Modifier, LockScreenState, Modifier, LockScreenState, StateFlow (+7 more)

### Community 1 - "Community 1"
Cohesion: 0.17
Nodes (8): Boolean, Flow, Int, List, LockedAppEntity, Long, String, AppLockRepository

### Community 2 - "Community 2"
Cohesion: 0.14
Nodes (12): Boolean, String, ComposeView, Lifecycle, LifecycleOwner, SavedStateRegistry, SavedStateRegistryOwner, LockOverlayManager (+4 more)

### Community 3 - "Community 3"
Cohesion: 0.14
Nodes (9): AccessibilityEvent, AccessibilityService, Boolean, CoroutineScope, Long, String, LockOverlayManager, ScreenOffReceiver (+1 more)

### Community 4 - "Community 4"
Cohesion: 0.13
Nodes (10): Boolean, StateFlow, StateFlow, String, SettingsViewModel, SetupStep, SetupViewModel, SetupStep (+2 more)

### Community 5 - "Community 5"
Cohesion: 0.12
Nodes (13): Boolean, Boolean, String, AppListItemRow(), AppListScreen(), AppListItem, DecoyCalculatorScreen(), NavHostController (+5 more)

### Community 6 - "Community 6"
Cohesion: 0.30
Nodes (5): Flow, List, LockedAppEntity, String, LockedAppDao

### Community 7 - "Community 7"
Cohesion: 0.27
Nodes (5): Boolean, Flow, Int, String, AppPreferences

### Community 8 - "Community 8"
Cohesion: 0.20
Nodes (7): Bundle, Boolean, ComponentActivity, OemGuideDialog(), MainActivity, NLockColors, NLockTheme

### Community 9 - "Community 9"
Cohesion: 0.18
Nodes (6): Boolean, Context, FragmentActivity, Bundle, BiometricProxyActivity, BiometricAuthManager

### Community 10 - "Community 10"
Cohesion: 0.18
Nodes (7): Context, Intent, Context, Intent, BroadcastReceiver, BootReceiver, ScreenOffReceiver

### Community 11 - "Community 11"
Cohesion: 0.28
Nodes (4): CoroutineScope, AppNotificationMasker, NotificationListenerService, StatusBarNotification

### Community 12 - "Community 12"
Cohesion: 0.36
Nodes (5): Boolean, String, ByteArray, Pair, PinHashManager

### Community 13 - "Community 13"
Cohesion: 0.28
Nodes (6): Boolean, List, StateFlow, String, AppListItem, AppListViewModel

### Community 14 - "Community 14"
Cohesion: 0.32
Nodes (4): Boolean, Cipher, SecretKey, KeystoreManager

### Community 15 - "Community 15"
Cohesion: 0.36
Nodes (6): Application, AppLockRepository, AppPreferences, NLockApplication, NLockDatabase, PinHashManager

### Community 16 - "Community 16"
Cohesion: 0.33
Nodes (4): Boolean, Context, Class, PermissionHelper

### Community 17 - "Community 17"
Cohesion: 0.29
Nodes (5): Context, CoroutineWorker, Result, enqueue(), ServiceHealthCheckWorker

### Community 18 - "Community 18"
Cohesion: 0.33
Nodes (3): Boolean, Context, OemHelper

### Community 19 - "Community 19"
Cohesion: 0.40
Nodes (3): Boolean, Context, PackageUtils

### Community 20 - "Community 20"
Cohesion: 0.40
Nodes (3): NLockDatabase, LockedAppDao, RoomDatabase

### Community 22 - "Community 22"
Cohesion: 0.83
Nodes (3): Locked, LockScreenState, Unlocked

## Knowledge Gaps
- **60 isolated node(s):** `Bundle`, `NLockDatabase`, `LockedAppEntity`, `LockedAppDao`, `Flow` (+55 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LockScreenViewModel` connect `Community 0` to `Community 4`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **What connects `Bundle`, `NLockDatabase`, `LockedAppEntity` to the rest of the system?**
  _60 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.10276679841897234 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14210526315789473 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.14035087719298245 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.13450292397660818 - nodes in this community are weakly interconnected._
- **Should `Community 5` be split into smaller, more focused modules?**
  _Cohesion score 0.12418300653594772 - nodes in this community are weakly interconnected._