# Supabase — LaterTogether MVP (Track 1)

SQL lives in `supabase/migrations/` (timestamp-prefixed, ordered).

## `public.messages`

| Column | Type | Notes |
|--------|------|--------|
| `id` | `uuid` | Primary key; server-generated. |
| `content_key` | `text` | Thread key (opaque string; client-defined for MVP). |
| `media_timestamp` | `double precision` | **Seconds** from media start; `>= 0`. |
| `body` | `text` | Message text. |
| `author_id` | `uuid` | **`auth.users(id)`**; insert policy requires `author_id = auth.uid()`. |
| `client_created_at` | `timestamptz` | Client-composed time (ordering/diagnostics). |
| `created_at` | `timestamptz` | Server default `now()`. |
| `updated_at` | `timestamptz` | Server default `now()`; no trigger in MVP (append-only chat expected). |

Indexes: `(content_key, media_timestamp)` for range queries and pagination; `author_id` for optional moderation or account-scoped tooling.

## RLS policies (MVP)

| Operation | Who | Rule |
|-----------|-----|------|
| `SELECT` | `authenticated` | Allowed for **all rows** (`using (true)`). |
| `INSERT` | `authenticated` | Allowed only when **`author_id = auth.uid()`**. |
| `UPDATE` / `DELETE` | — | **No policies** → denied for `authenticated` (append-only via API). |

`service_role` bypasses RLS for admin/maintenance (Supabase default); do not ship the service key in clients.

## Tradeoffs (explicit)

### Global read vs per-key membership

**Chosen for MVP:** any logged-in user can read **every** message in `public.messages`.

**Pros:** no membership table, no join flows, fastest path for demos and internal testing; simple PostgREST queries from the tablet.

**Cons:** in production with arbitrary users, **all authenticated accounts see all threads** — unacceptable for private watch parties without another gate (invite links, room codes, paywalls, etc.).

**Alternative (not implemented here):** add `thread_members (content_key, user_id)` (or a `threads` table plus membership) and replace the `SELECT` policy with `exists (select 1 from thread_members … where user_id = auth.uid() and content_key = messages.content_key)`. You may also move “public” threads behind a boolean or catalog table.

### Identity on insert

**Chosen:** `with check (author_id = auth.uid())`.

**Pros:** clients cannot attribute posts to another user through the anon/authenticated API.

**Cons:** does not stop a user from posting garbage; validation/rate limits belong in app logic or Edge Functions if needed later.

### Append-only

**Chosen:** no `UPDATE`/`DELETE` policies for `authenticated`.

**Pros:** simpler audit and sync story for MVP.

**Cons:** no edit/delete UX without a new migration adding policies (and product rules).

Document policy changes here whenever you tighten read access or add update/delete.
