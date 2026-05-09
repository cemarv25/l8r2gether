# LaterTogether — Companion App + Playback-Aware Sync

**Version:** 0.1  
**Status:** Draft specification  
**Scope:** **Android tablet only.** The companion runs **alongside** third-party players (e.g. streaming apps); playback is **not** assumed to occur inside LaterTogether unless explicitly added later. The client is a **native Kotlin** application to access **Android platform APIs** (notably **MediaSession** and, when needed, **AccessibilityService**). Message persistence uses **Supabase** (PostgreSQL and related Supabase features—see §13).

---

## 1. Product summary

**LaterTogether** provides **asynchronous, timestamp-anchored chat** for a shared piece of media: users send messages tied to **media time** (seconds from the start of a specific edition of content). When others watch the same content later, messages **surface at the corresponding moment**, analogous to chat replay on live streams.

**Playback observation:** The app **reads current playback state and position** from the device using a **tiered strategy**:

1. **Primary:** **`MediaSession` APIs** (and related platform surfaces that expose active session metadata: playback state, position when exposed, supported actions). This is the preferred path when the active media session reports usable information.
2. **Fallback:** **`AccessibilityService`**, used when **`MediaSession` does not yield reliable position/state** for the app the user is watching. In that mode, the service may use **accessibility node tree content** (e.g. visible text for timecodes or player controls)—**not** **screen capture** or frame sampling of protected video (see §11.3). Subject to user consent, Play policy, and technical limits (DRM, overlays, OEM variance).

When **neither** path gives a trustworthy signal, the product falls back to **explicit user checkpoints** (manual time entry / “Sync now”) and the **session estimation model** in §6—same recovery philosophy as before, but invoked only when automation cannot anchor.

---

## 2. Goals

| ID | Goal |
|----|------|
| G1 | Anchor every chat message to **canonical media time** for a **canonical content identity**. |
| G2 | Estimate **current media time** during a watch session with **acceptable error**, prioritizing **observed** signals from **MediaSession**, then **AccessibilityService**, then **manual** checkpoints. |
| G3 | Recover from **seek**, **pause**, **variable playback speed**, and **drift** without silent failure; surface uncertainty and prompt for **resync** when confidence drops. |
| G4 | Ship a **polished Android tablet** experience (split-screen, landscape, large layouts)—**no** requirement to share UI or logic with other platforms. |
| G5 | Implement native integrations **in Kotlin** (foreground/coroutine boundaries, session callbacks, accessibility events) with **testable** domain logic separated from Android framework glue where practical. |

---

## 3. Non-goals (MVP)

- **Multiplatform clients** (iOS, desktop, Flutter, web companion)—out of scope for this specification; only **Android tablet** is targeted.
- Guaranteed **frame-accurate** sync without user checkpoints when OEM/app behavior hides position entirely.
- Parity for **TV / console** or **offline-download** playback models unless explicitly added later.
- Replacing **legal / ToS** review for accessibility capture or third-party apps—out of band.
- **Supabase Realtime** (or other push-style delivery) for **new** messages—**not** required for **MVP**; clients **fetch** thread data on demand (see §10.2, §13). **Post-MVP:** keep schema and client layering compatible with subscribing later without a rewrite.

---

## 4. Definitions

| Term | Definition |
|------|------------|
| **Media time** | Scalar offset in seconds (or milliseconds in implementation) from the **start** of the watched **edition** (one deterministic timeline). |
| **Content key** | Stable identifier in LaterTogether’s catalog for threading (see §5). |
| **Watch session** | A single continuous attempt to map wall-clock to media time for one user + one content key + optional breaks modeled explicitly. |
| **Checkpoint** | User-provided or system-prompted observation linking **wall time** to **media time**, used to re-anchor or correct drift. |
| **Edition** | A specific cut (region, director’s cut, remaster). Different runtimes may imply different editions; MVP may merge naively with user override. |
| **Observed playhead** | Position/state derived from **MediaSession** or **AccessibilityService** (when implemented), as opposed to **extrapolated** `t_est` from anchors alone. |

---

## 5. Content identity

### 5.1 Requirements

- Every message MUST be stored with `(contentKey, mediaTimestamp, …)`.
- Clients MUST resolve “what we’re watching” to `contentKey` before posting or joining a thread.

### 5.2 Sources (priority order for MVP)

1. **Explicit URL / ID** where stable (e.g. YouTube `videoId` from share).
2. **User selection** from search / catalog (title, season, episode).
3. **Metadata-assisted match** (external IDs, runtime, locale)—**best-effort**, user confirms.

### 5.3 Known limitations

- Many providers do not expose a stable public ID via share; **human-in-the-loop** selection remains acceptable.
- Same title may map to **multiple editions**; later versions may add `editionId` or runtime fingerprint.

---

## 6. Session model (client state)

### 6.1 Required fields

Implementations SHOULD persist:

| Field | Type | Description |
|-------|------|-------------|
| `contentKey` | string | Active thread identity. |
| `sessionId` | UUID | Server-issued or client-provisional until joined. |
| `clockMode` | enum | `monotonic` preferred for elapsed math; document fallback. |
| `sessionAnchorWall` | instant | Wall/monotonic instant when `baseMediaTime` was last set. |
| `baseMediaTime` | number | Media time (seconds) at `sessionAnchorWall`. |
| `playbackState` | enum | `playing` \| `paused` — ideally from **observation** when available. |
| `pausedAtMediaTime` | number \| null | Frozen media time when paused. |
| `playbackRate` | number | Default `1.0`; from user settings and/or observed hints when available. |
| `driftMs` | number | Small correction from checkpoints; optional. |
| `confidence` | enum | `low` \| `medium` \| `high` (drives prompt aggressiveness). |
| `observationSource` | enum | `media_session` \| `accessibility` \| `manual` \| `mixed` — last authoritative source for anchoring (detailed policy implementation-defined). |

### 6.2 Estimated media time

When **no fresh observed position** is integrated at this instant, extrapolate:

While **`playbackState === playing`**:

```
t_est = baseMediaTime
      + playbackRate * elapsedWallSecondsSince(sessionAnchorWall)
      + driftMs / 1000
```

While **`playbackState === paused`**:

```
t_est = pausedAtMediaTime   // plus drift if modeled as additive
```

**Re-anchor rule:** On every authoritative checkpoint \((wall, media)\), set:

- `sessionAnchorWall = wall`
- `baseMediaTime = media`
- Reset or recompute `driftMs` per implementation policy.

### 6.3 Observed vs extrapolated time

When **MediaSession** or **AccessibilityService** provides a usable **position update**:

- Apply policy to **blend or replace** extrapolation (e.g. periodic snap to observed position when within tolerance; larger deltas trigger **seek suspected** behavior and confidence downgrade until user confirms or a new checkpoint resolves ambiguity).

### 6.4 Seek / jump

- **Requirement:** User MUST be able to **re-anchor** via **Sync now** + media time input when automation disagrees or fails.
- Optional: detect large discontinuities from **observed** position jumps—implementation-defined.

### 6.5 Playback speed

- **Requirement:** UI exposes **playback speed** presets matching common player settings when extrapolation is used.
- Elapsed integration MUST multiply wall elapsed by `playbackRate` when extrapolating.

---

## 7. Pause / play semantics

### 7.1 Preferred truth sources (when available)

1. **MediaSession** playback state / position updates.
2. **Accessibility-derived** indicators when consistent with session policy.
3. **Explicit** **Playing / Paused** control inside LaterTogether (always available).
4. **Checkpoint** after the fact.

### 7.2 Explicit pause (baseline UX)

- User taps **Paused** in LaterTogether: set `playbackState = paused`, `pausedAtMediaTime = t_est` at transition instant.
- User taps **Playing**: set `playbackState = playing`, new `sessionAnchorWall = now`, `baseMediaTime = pausedAtMediaTime`.

### 7.3 Forgotten pause / stale observation

If extrapolation assumes **playing** while the native app is paused (or observation stalls):

- Estimated `t_est` may run ahead.
- **Mitigation:** **“Still in sync?”** prompts; lowered `confidence`; prominent **Resync**.

---

## 8. Manual sync flows

### 8.1 Start session

1. User selects `contentKey` (or creates thread).
2. User opens the third-party app as usual.
3. User starts **Watch session** in LaterTogether.
4. **Calibration:** preferably from **observed** position; otherwise user taps **Sync now** and enters **current media time** (mm:ss or seconds).

### 8.2 Resync (any time)

Captures **(wall, media)** and re-anchors per §6.

### 8.3 Input UX requirements

- Accept **numeric time** and **mm:ss** entry.
- Validate plausible range when runtime is known.
- Optional: **±30s / ±2m** nudge controls.

---

## 9. Checkpoint & confidence

### 9.1 Periodic prompts

- Trigger when `confidence !== high` or after **N minutes** without a solid observation/checkpoint.
- Actions: **[Still synced]** | **[Fix time]** (calibration).

### 9.2 Confidence rules (example policy)

| Condition | Confidence |
|-----------|------------|
| Fresh calibration or strong **MediaSession** alignment | `high` |
| Long interval with weak or missing observation | toward `medium` / `low` |
| User dismissed multiple prompts | `low` |

---

## 10. Chat delivery rules

### 10.1 Message schema (minimum)

```text
{
  "contentKey": "<string>",
  "mediaTimestamp": <number>,   // seconds, authoritative
  "body": "<string>",
  "authorId": "<string>",
  "clientCreatedAt": "<ISO8601>"
}
```

Server MUST treat **`mediaTimestamp`** as the timeline authority for replay.

### 10.2 Display scheduling

- **MVP:** Load messages via **Supabase-facing fetch** (REST/PostgREST or SDK) when the user opens or refreshes a thread; **no** Realtime subscription required. Compute `t_est` (and merge **observed** updates per policy) on a tick interval (e.g. 250–500 ms) to decide which loaded messages to reveal.
- Show messages where `mediaTimestamp <= t_est` (subject to session display ledger / dedupe rules).
- **Seek backward:** spec default remains **re-show allowed** when crossing forward again—implement idempotent display ledger.

### 10.3 Burst control

- Optional minimum spacing when catching up after pause.

---

## 11. Platform scope — Android tablet (Kotlin)

### 11.1 MVP assumptions

- User watches in **native** streaming apps; LaterTogether runs **alongside** (split-screen; PiP-friendly layout encouraged).
- **Single platform:** **Android tablet**; layouts and QA target **large**, **landscape-first** tablet use cases.

### 11.2 Playback observation — MediaSession (primary)

- Use Android **`MediaSession`** / **`MediaController`** / related APIs to discover the **active** session and read **metadata** and **playback state** where the OS and the playing app expose them.
- **Limitations:** Not all apps expose **precise position** or update frequency sufficient for chat sync; some sessions may be **incomplete** or **stale**. The implementation MUST degrade gracefully to §11.3 or §8.

### 11.3 Playback observation — AccessibilityService (fallback)

- When §11.2 is insufficient, an **`AccessibilityService`** MAY:
  - Inspect the **accessibility node tree** (text labels, content descriptions, hierarchy) for time displays or player state cues.
  - **MUST NOT** rely on **screen capture**, **MediaProjection**, or **pixel/OCR pipelines** on the video surface to infer position—those approaches increase **DRM / content-protection** risk and are **out of scope** for this product’s playback inference.

**Disclosure:** Users MUST explicitly enable and understand assistive modes before operation.

### 11.4 UI — Jetpack Compose

- **Agreed stack:** **Jetpack Compose** with **Material 3** for new UI in this project (tablet layouts, navigation, theming).
- **Architecture:** Prefer **ViewModel** + **Kotlin coroutines** / **StateFlow** (or equivalent unidirectional state) for screen logic; keep composables largely **stateless** where practical.
- **Escape hatches:** Use **`AndroidView`** in Compose only when a **View**-based SDK or control has no Compose equivalent; avoid introducing parallel XML-first screens unless necessary.

**Alternatives (not default):** Traditional **XML + Views** remains valid Android-wide but is **not** the LaterTogether baseline—see revision history when this decision was recorded.

### 11.5 Kotlin implementation expectations

- **Domain logic:** Session estimation, checkpoints, confidence rules, and “which messages to show at `t_est`” SHOULD be **testable** units decoupled from raw framework callbacks where feasible (pure Kotlin modules + instrumented tests for Android-specific bindings).

---

## 12. Optional lanes (non-MVP unless revived)

- **WebView “watch inside LaterTogether”** with `HTMLMediaElement` access where permitted.
- **Desktop browser extension** for web players.

---

## 13. Backend & data — Supabase (sole backend)

### 13.1 Responsibilities

- **Supabase is the only backend** for MVP: **PostgreSQL** persistence, **PostgREST**-style access via Supabase clients, **Auth**, and **Row Level Security**. No separate long-lived application server (e.g. FastAPI) is required when query patterns stay simple.
- **Edge Functions** MAY host **thin** logic that does not belong in the client (validation glue, aggregations, or hiding implementation details)—keep most reads/writes **contentKey-centric** and filterable in Postgres or via narrow RPCs.
- **Supabase Realtime** is **not** part of **MVP** message delivery; **post-MVP**, the same tables and RLS can back Realtime subscriptions without changing the core message model.

### 13.2 Client responsibilities

- Maintain **local** `t_est` and observation fusion; **Supabase** does not need millisecond-accurate global playhead state for MVP messaging.

### 13.3 Illustrative surface (non-binding)

Exact table and RPC names are **implementation details**, but the MVP needs at least:

- Insert message with `(contentKey, mediaTimestamp, body, authorId, …)`.
- **Fetch messages by `contentKey`** with optional **range filters** on `mediaTimestamp` and pagination (primary access pattern).

**Post-MVP:** Optional **Realtime** subscription per thread when near-live delivery is prioritized.

### 13.4 Auth & RLS

- **Supabase Auth** is the **expected** way to identify **`authorId`** and enforce **Row Level Security** policies. Exact policies are **implementation-defined** and MUST be documented in the repository when added.

---

## 14. Privacy, security, compliance

- **Accessibility** modes require **clear consent**; prefer **structured** reads from the **accessibility tree** only—**no** mass retention of screen captures or frame buffers for inference (see §11.3).
- Minimize collection of viewing habits beyond threads users join.
- Honor **Google Play** policies for **AccessibilityService** (declaration, disclosure, non-deceptive use).
- Honor content-provider **terms**; assistive modes may be restricted for some apps—communicate limitations honestly.

---

## 15. Success metrics (product)

- Calibration / manual fix events per hour (lower is better, bounded by perceived accuracy).
- Time spent with **`confidence: high`** vs lower bands.
- User-reported “out of sync” rate.
- Message delivery perceived **on time** vs `t_est`.

---

## 16. Open questions (product / engineering)

1. **Edition collision:** Same `contentKey` for different cuts—how to split threads post-MVP?
2. **Offline:** Queue outgoing messages until connectivity returns?
3. **Moderation:** Report/block at thread level; spam at high media density.
4. **Multi-language UI** for time entry (locale formats).

---

## 17. Implementation decisions (agreed)

**Purpose:** Record build-time choices so the specification and the codebase stay aligned.

### 17.1 Client application

| Decision | Choice |
|----------|--------|
| Platform | **Android tablet only** — no multiplatform companion requirement. |
| Language | **Kotlin** — native Android for **MediaSession**, **AccessibilityService**, and other platform APIs. |
| UI | **Jetpack Compose** + **Material 3** (see §11.4). |
| Playback signals | **MediaSession first**; **AccessibilityService** when MediaSession is insufficient; **manual** checkpoints always available. |

### 17.2 Backend and data (MVP)

| Decision | Choice |
|----------|--------|
| Backend | **Supabase only** — Postgres, Auth, RLS; **Edge Functions** for thin server logic when needed. |
| Data access pattern | **contentKey-centric** fetch/filter for threads and messages; minimal custom backend beyond Supabase. |
| Realtime | **Out of scope for MVP**; schema and client layering SHOULD remain compatible with **Supabase Realtime** later. |

### 17.3 Rationale (short)

Native Kotlin and **Compose** maximize control over **session**, **accessibility**, and **tablet UI** on Android. **Supabase** keeps persistence and auth managed without a dedicated app server for simple query shapes; **Edge Functions** cover the occasional server-side branch. Scope is intentionally **single-platform** to avoid splitting logic across non-Android targets.

---

## 18. Revision history

| Version | Date | Notes |
|---------|------|-------|
| 0.1.1 | 2026-05-07 | Initial companion + manual/checkpoint sync spec |
| 0.1.2 | 2026-05-08 | Stack alignment: Flutter client; Android tablet MVP; multiplatform planned; REST + Postgres MVP |
| 0.1.3 | 2026-05-09 | **Major revision:** Android tablet **only**; **Kotlin** client; **MediaSession** + **AccessibilityService** playback observation; **Supabase** backend; multiplatform / Flutter / Postgres-FastAPI MVP choices superseded—see §17 |
| 0.1.4 | 2026-05-09 | **Supabase-only** backend + **Edge Functions**; **Realtime** explicitly post-MVP with upgrade path; **no screen capture** for playback inference (§11.3); **UI toolkit options** (§11.4); open questions trimmed |
| 0.1.5 | 2026-05-09 | **Jetpack Compose** + **Material 3** locked as UI stack (§11.4, §17.1); UI toolkit open question closed |
