# LaterTogether — Companion App + Manual / Checkpoint Sync

**Version:** 0.2  
**Status:** Draft specification  
**Scope:** Mobile-first (**Android tablet** is the MVP compatibility target); playback assumed to occur in **third-party native apps** (e.g. Netflix), not inside LaterTogether’s player unless explicitly in scope. **Multiplatform** clients are planned; agreed stack choices are summarized in **§17**.

---

## 1. Product summary

**LaterTogether** delivers **asynchronous, timestamp-anchored chat** for a shared piece of media: users send messages tied to **media time** (seconds from the start of a specific edition of content). When others watch the same content later, messages **surface at the corresponding moment**, similar in spirit to chat replay on live streams.

**Core constraint:** Third-party streaming apps do not expose playback state to arbitrary companion apps. This spec defines sync using **user-provided anchors**, **explicit session state** in LaterTogether, and **checkpoint / drift handling**—not reliance on OS-level “what Netflix is doing now” APIs.

---

## 2. Goals

| ID | Goal |
|----|------|
| G1 | Anchor every chat message to **canonical media time** for a **canonical content identity**. |
| G2 | Estimate **current media time** during a watch session with **acceptable error** for readable chat timing. |
| G3 | Recover from **seek**, **pause**, **variable playback speed**, and **user drift** without silent failure. |
| G4 | Optimize for **Android tablet** where browser extensions and cross-app playback introspection are unavailable. |
| G5 | Keep an upgrade path for **optional assistive sync** (Accessibility, screen-assisted inference, WebView lane) without blocking MVP. |

---

## 3. Non-goals (MVP)

- Reading playback position directly from Netflix / Prime / Disney+ native SDKs (no supported API).
- Guaranteed frame-accurate sync without user checkpoints.
- TV / console / offline-download playback parity.
- Replacing platform ToS compliance review (legal remains out of band).
- **Live message feeds** — no WebSocket, SSE, or push-based delivery of newly posted messages as a required MVP feature; the MVP operates on **messages that already exist** with timestamps, loaded over **REST** (see §10.2, §13, §17).

---

## 4. Definitions

| Term | Definition |
|------|------------|
| **Media time** | Scalar offset in seconds (or milliseconds in implementation) from the **start** of the watched **edition** (one deterministic timeline). |
| **Content key** | Stable identifier in LaterTogether’s catalog for threading (see §5). |
| **Watch session** | A single continuous attempt to map wall-clock to media time for one user + one content key + optional breaks modeled explicitly. |
| **Checkpoint** | User-provided or system-prompted observation linking **wall time** to **media time**, used to re-anchor or correct drift. |
| **Edition** | A specific cut (region, director’s cut, remaster). Different runtimes may imply different editions; MVP may merge naively with user override. |

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

- Netflix and similar may not expose a public ID via share; **human-in-the-loop** selection is acceptable for MVP.
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
| `playbackState` | enum | `playing` \| `paused`. |
| `pausedAtMediaTime` | number \| null | Frozen media time when paused. |
| `playbackRate` | number | Default `1.0`; user-settable (e.g. 0.75, 1.25). |
| `driftMs` | number | Small correction from checkpoints; optional. |
| `confidence` | enum | `low` \| `medium` \| `high` (drives prompt aggressiveness). |

### 6.2 Estimated media time

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
- Reset or recompute `driftMs` per implementation policy (simplest: zero drift on full re-anchor).

### 6.3 Seek / jump

User performs seek in the native app:

- **Requirement:** User MUST be able to **re-anchor** via **Sync now** + media time input (or equivalent).
- Optional: detect “large discontinuity” only via **future assistive signals**—not required for MVP.

### 6.4 Playback speed

- **Requirement:** UI exposes **playback speed** presets matching common player settings.
- Elapsed integration MUST multiply wall elapsed by `playbackRate`.

---

## 7. Pause / play semantics

### 7.1 Truth model

LaterTogether **does not** receive pause events from Netflix (or similar). **Ground truth** for pause is:

1. **Explicit** **Playing / Paused** control inside LaterTogether (primary MVP path), or  
2. **Checkpoint** that corrects accumulated error after the fact.

### 7.2 Explicit pause (recommended MVP UX)

- User taps **Paused** in LaterTogether when they pause the native player: set `playbackState = paused`, `pausedAtMediaTime = t_est` at transition instant.
- User taps **Playing** when they resume: set `playbackState = playing`, new `sessionAnchorWall = now`, `baseMediaTime = pausedAtMediaTime`.

### 7.3 Forgotten pause

If user leaves LaterTogether in `playing` while native app is paused:

- Estimated `t_est` runs ahead of actual playback.
- **Mitigation:** time-based **“Still in sync?”** prompts; lowered `confidence`; prominent **Resync**.

---

## 8. Manual sync flows

### 8.1 Start session

1. User selects `contentKey` (or creates thread).
2. User opens Netflix (or other app) as usual.
3. User starts **Watch session** in LaterTogether.
4. **First calibration:** user taps **Sync now** and enters **current on-screen media time** (mm:ss or seconds).

### 8.2 Resync (any time)

Same as calibration: captures **(wall, media)** and re-anchors per §6.2.

### 8.3 Input UX requirements

- Accept **numeric time** and **mm:ss** entry.
- Validate plausible range (e.g. 0 … known runtime if available).
- Optional: quick **±30s / ±2m** nudge when user reports “a bit off.”

---

## 9. Checkpoint & “smart” re-sync

Smart behavior reduces manual entry frequency; it must **never** silently assume native app state.

### 9.1 Periodic prompts

- Trigger when `confidence !== high` or after **N minutes** of playback-estimated time (configurable).
- Actions: **[Still synced]** (no-op or tiny drift bump) | **[Fix time]** (opens calibration).

### 9.2 Confidence rules (example policy)

| Condition | Confidence |
|-----------|------------|
| Fresh calibration &lt; 2 min ago | `high` |
| Long playing interval without checkpoint | degrade toward `medium` |
| User dismissed multiple prompts | `low` |

### 9.3 Optional future signals (non-blocking)

Documented for roadmap only:

- Accessibility-assisted hints (Android).
- Screen capture + OCR / CV (assistive, DRM-sensitive).
- WebView lane with `HTMLMediaElement` access where permitted.

---

## 10. Chat delivery rules

### 10.1 Message schema (minimum)

```text
{
  "contentKey": "<string>",
  "mediaTimestamp": <number>,   // seconds, authoritative
  "body": "<string>",
  "authorId": "<string>",
  "clientCreatedAt": "<ISO8601>"  // optional ordering / moderation
}
```

Server MUST treat **`mediaTimestamp`** as the timeline authority for replay.

### 10.2 Display scheduling

- **MVP:** Load messages via **REST** when the user opens or refreshes a thread (no dedicated realtime channel). Compute `t_est` locally at a tick interval (e.g. 250–500 ms) to decide which loaded messages to reveal.
- **Later:** Optional polling or subscription (WebSocket / SSE / push) when near-real-time delivery of **new** messages is in scope.
- Show messages where `mediaTimestamp <= t_est` and not yet displayed (per-session dedupe).
- **Seek backward:** if `t_est` decreases, hide or mark future messages per product decision (spec default: **re-show allowed** when crossing forward again—implement idempotent display ledger).

### 10.3 Burst control

- Optional minimum spacing or batching to avoid UI overload when catching up after pause.

---

## 11. Platform scope — Android tablet

### 11.1 MVP assumptions

- User watches in **native** streaming apps.
- LaterTogether runs **alongside** (split-screen, PiP-friendly layout encouraged).
- No dependency on cross-app playback introspection.

### 11.2 Explicitly unsupported without future work

- Automatic pause detection from Netflix internals.
- Reliable sync purely from notifications across all providers.

### 11.3 Client implementation (agreed direction)

- **Flutter (Dart)** for the companion app — one codebase for MVP and planned multiplatform expansion without rewriting core flows.
- **MVP focus:** Polish layouts and behavior for **Android tablet** (split-screen, landscape, large breakpoints); other platforms follow the same codebase when prioritized.
- Session estimation, checkpoints, confidence rules, and “which messages to show at `t_est`” SHOULD live in **plain Dart** (or shared packages), not tightly coupled to widgets, so logic stays portable.
- Future **Android-specific** assistive sync (e.g. Accessibility, §9.3) MAY use **platform channels** (or Pigeon); keep those integrations thin.

---

## 12. Optional lanes (documented, not MVP-critical)

### 12.1 WebView “watch inside LaterTogether”

- **Purpose:** Where DOM access allows, read `HTMLMediaElement.currentTime` / `paused`.
- **Limitations:** DRM, WebView blocking, CSP/iframes, policy risk—not universal (see prior discussion).

### 12.2 Desktop browser extension

- **Purpose:** Inject into known players on web for stronger automation.
- **Out of scope** for this document’s MVP mobile deliverable.

---

## 13. Backend & API (sketch)

### 13.1 Responsibilities

- **MVP:** Persist and serve messages keyed by `contentKey` + `mediaTimestamp` over **REST**; no requirement for subscriber fan-out or live protocols.
- **Later:** Optional fan-out of new messages to subscribers (WebSocket / SSE / push), session presence (typing, online counts), when live delivery is in product scope.

### 13.2 Client responsibilities

- Maintain **local** `t_est`; server does not need millisecond-accurate “global playhead.”

### 13.3 Endpoints (illustrative)

**MVP (REST):**

- `POST /threads/{contentKey}/messages`
- `GET /threads/{contentKey}/messages?fromMedia=&toMedia=`

**Post-MVP (when live feeds are in scope):**

- `WS /threads/{contentKey}/live` (or SSE equivalent)

**Suggested persistence:** **PostgreSQL** (or a compatible managed service) for relational queries and indexes on thread + media time.

---

## 14. Privacy, security, compliance

- Minimize collection of precise viewing habits beyond what’s needed for threads users join.
- Clear disclosure that sync accuracy depends on **user-maintained** session state unless optional assistive modes are enabled.
- Honor platform and content-provider terms; assistive capture features require separate legal review.

---

## 15. Success metrics (product)

- Calibration events per hour of viewing (lower is better, bounded by perceived accuracy).
- User-reported “out of sync” rate (survey or explicit flag).
- Message delivery perceived as **on time** (qualitative + latency histogram vs checkpoint).

---

## 16. Open questions

1. **Edition collision:** Same `contentKey` for different cuts—how to split threads post-MVP?
2. **Offline:** Queue outgoing messages with local `mediaTimestamp` until connectivity returns?
3. **Moderation:** Report/block at thread level; spam at high media density.
4. **Multi-language UI** for time entry (locale formats).

---

## 17. Implementation decisions (agreed)

**Purpose:** Record build-time choices so the specification and the codebase stay aligned. Update this section when scope or stack changes.

### 17.1 Client application

| Decision | Choice |
|----------|--------|
| UI framework | **Flutter** (Dart) — single codebase for MVP and planned multiplatform targets. |
| MVP compatibility target | **Android tablet** — primary QA and UX investment (split-screen, landscape, large layouts). |
| Multiplatform | **Planned** (e.g. iOS, desktop, web) using the same Flutter project; avoid duplicating session/sync logic outside Dart where possible. |
| Native integrations | Use **platform channels** (or **Pigeon**) for thin Android-only capabilities when Flutter cannot access an API directly (e.g. future assistive sync per §9.3). |

Session estimation, checkpoint handling, and message display scheduling SHOULD remain **testable Dart** modules separate from presentation widgets.

### 17.2 Backend and data (MVP)

| Decision | Choice |
|----------|--------|
| Message / thread store | **PostgreSQL** (or API-compatible managed Postgres). |
| API surface | **REST only** for creating and reading timestamped messages (including range queries by media time). |
| Live delivery | **Out of scope for MVP** — no required WebSocket, SSE, or push feed; see §3 and §10.2. Add subscription or push when near-real-time fan-out is prioritized. |

### 17.3 Rationale (short)

Flutter limits duplication across future platforms while the release strategy stays **Android-tablet-first**. REST and Postgres keep server and operations minimal until live message feeds are required.

---

## 18. Revision history

| Version | Date | Notes |
|---------|------|-------|
| 0.1 | 2026-05-07 | Initial companion + manual/checkpoint sync spec from product discussion |
| 0.2 | 2026-05-08 | Stack alignment: Flutter client; Android tablet MVP; multiplatform planned; REST + Postgres MVP; no live message feeds. §11.3, §13, §17; §3/§10.2 updates. |
