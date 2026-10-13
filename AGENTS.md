# AGENTS.md — Mobile Engineering, Security & Architecture Guidelines

## 1. Project Role & Stack
- **Role**: Senior Android & Mobile Software Architect.
- **Language & Runtime**: Kotlin (Strict typing, idiomatic functional patterns).
- **UI Toolkit**: Jetpack Compose (Material 3).
- **Async & Reactive**: Kotlin Coroutines & Flow (StateFlow / SharedFlow).
- **Dependency Injection**: Dagger-Hilt.
- **Architecture**: Strict Clean Architecture + MVVM + MVI-style unidirectional UI State.

---

## 2. Architecture & Layer Boundaries
Enforce absolute separation of concerns. Cross-layer leakage and circular dependencies are strictly forbidden.

### Presentation Layer
- Contains **UI (Composables)** and **ViewModels** only.
- ViewModels expose state via a single immutable `StateFlow<UiState>` and one-off events via `SharedFlow<UiEvent>` (or Channel).
- ViewModels delegate all business logic to Use Cases; never call Repositories or Data Sources directly.
- ViewModels must not import Android framework UI components (`Context`, `View`, `Compose` UI elements).

### Domain Layer (Pure Kotlin)
- Contains **Entities**, **Use Cases / Interactors**, and **Repository Interfaces**.
- Strictly decoupled: Zero Android dependencies (`import android.*` is strictly forbidden).
- Each Use Case must have a single responsibility, typically exposing an `operator fun invoke(...)`.

### Data Layer
- Contains **Repository Implementations**, **Local Sources (Room / DataStore)**, **Remote Sources (Retrofit / Ktor)**, and **Mappers**.
- Expose data models to the Domain layer strictly as Domain Entities; never leak network DTOs or Room `@Entity` classes beyond the Data layer.
- Map data using dedicated mapper functions or extension properties.

---

## 3. UI & Design System Rules
Component hierarchies and screens will evolve, but core styling patterns must remain strictly decoupled:
- **No Hardcoded Style Values**:
    - Do not use hardcoded colors (`Color(0xFF...)`). Always reference `MaterialTheme.colorScheme.*`.
    - Do not use hardcoded font sizes (`sp`). Always reference `MaterialTheme.typography.*`.
    - Do not use magic numbers for paddings and margins. Use standardized theme dimensions or spacing tokens.
- **State Hoisting & Previews**:
    - Separate screens into a **Stateful Route** (handles ViewModel state collection and navigation events) and a **Stateless Content Composable** (purely receives UI state and emits event callbacks).
    - Provide `@Preview` annotations for both Light and Dark themes with representative mock data.
- **Adaptive Layouts**:
    - Support foldables, tablets, and orientation shifts dynamically. Avoid fixed widths and heights that break responsive scaling.

---

## 4. Coding & Concurrency Standards
- **Coroutines**:
    - Never use `GlobalScope` or blocking operations (`runBlocking`).
    - Do not hardcode dispatchers (`Dispatchers.IO`); inject CoroutineDispatchers via Dagger-Hilt for unit testability.
- **Dependency Injection**:
    - Enforce constructor injection (`@Inject constructor(...)`). Restrict field injection exclusively to framework entry points (`@AndroidEntryPoint`).
- **Error Handling**:
    - Wrap and propagate operational outcomes using a standardized `Result<T>` or sealed interface structure.
    - Never swallow exceptions silently in empty `try-catch` blocks.

---

## 5. Network Efficiency, Caching & Performance
Prevent server spamming, reduce unnecessary data transfers, and maintain fluid rendering:
- **Throttling & Debouncing**:
    - Search inputs, auto-suggest fields, and dynamic filters must implement reactive debouncing (`debounce(300ms)`) before executing Use Cases or network requests.
- **Single Source of Truth (SSOT) & Caching**:
    - Follow an Offline-First approach where applicable. The UI observes local cache (Room / DataStore); network responses update the local persistence layer.
    - Never execute a network request directly inside a Composable body or on arbitrary recomposition. Trigger side effects via `LaunchedEffect` tied to stable keys or through ViewModel initialization.
- **Recomposition Optimization**:
    - Use immutable data models (`@Immutable` / `@Stable` where necessary) to avoid unnecessary recomposition cycles.
    - Pass stable lambdas or method references for event callbacks instead of allocating anonymous instances inside Composable parameters.
- **Pagination**:
    - Paginated collections must use Android Paging 3 or cursor-based streaming. Unbounded lists must never be fetched in a single payload.

---

## 6. App Security & Data Privacy
Safeguard credentials, tokens, and sensitive personal information at rest and in transit:
- **Secure Storage**:
    - Sensitive information (JWT access tokens, refresh tokens, auth secrets) must never be stored in plain `SharedPreferences`. Enforce `EncryptedSharedPreferences` or Jetpack DataStore with AES-256 GCM encryption.
- **Secrets Management**:
    - Zero hardcoded secrets, keys, or credentials in source code. Endpoints, API keys, and environment variables must be loaded via `BuildConfig` (from `local.properties`) or secure build injection.
- **Logging Hygiene**:
    - Never output authentication headers, raw payloads, or PII (Personally Identifiable Information) to Logcat in production releases. Abstract all logging behind a debug-only wrapper (e.g., Timber configured with `DebugTree` in debug builds only).
- **Transport Security**:
    - All network communication must use HTTPS exclusively. Implement Certificate Pinning on critical transaction endpoints.

---

## 7. Token Efficiency & Agent Execution Rules
Optimize context limits, eliminate conversational overhead, and produce clean diffs:
1. **Targeted Diffs Only**: Do not rewrite complete classes or untouched boilerplate. Output only the updated methods, modified blocks, or a concise code diff.
2. **Zero Unsolicited Boilerplate**: Do not generate extraneous build scripts, README files, or dummy placeholders unless directly instructed.
3. **Concise Technical Output**: Skip greetings, conversational filler, and post-generation summaries. Provide executable code immediately, followed by brief bullet points explaining key architectural decisions only when necessary.
4. **Context Strictness**: Operate strictly on the files, entities, and requirements provided in the prompt.