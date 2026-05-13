-- Track 1 — messages schema (MVP).
-- Units: media_timestamp is seconds from media start (double precision); align domain DTOs to seconds, not milliseconds.
-- RLS tradeoff: global read for authenticated users — see supabase/README.md.

create extension if not exists "pgcrypto";

create table if not exists public.messages (
    id uuid primary key default gen_random_uuid(),
    content_key text not null,
    media_timestamp double precision not null,
    body text not null,
    author_id uuid not null references auth.users (id) on delete cascade,
    client_created_at timestamptz not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint messages_media_timestamp_non_negative check (media_timestamp >= 0)
);

comment on column public.messages.content_key is 'Opaque thread key (e.g. yt:<id>, manual:<uuid>); client-defined for MVP.';
comment on column public.messages.media_timestamp is 'Playback time in seconds when the message applies.';
comment on column public.messages.author_id is 'Supabase Auth user id; must equal auth.uid() on insert.';
comment on column public.messages.client_created_at is 'Client clock when the message was composed (timestamptz).';

-- Range fetch + pagination by thread key and media time.
create index if not exists messages_content_key_media_timestamp_idx
    on public.messages (content_key, media_timestamp);

create index if not exists messages_author_id_idx on public.messages (author_id);

alter table public.messages enable row level security;

-- PostgREST: authenticated JWT role needs table privileges; RLS still applies.
grant select, insert on table public.messages to authenticated;
grant all on table public.messages to service_role;

-- MVP [§13.4]: any authenticated user may read all messages (no per-key membership yet).
create policy "messages_select_authenticated"
    on public.messages
    for select
    to authenticated
    using (true);

-- Inserts must identify as the caller (cannot spoof author_id).
create policy "messages_insert_own_author"
    on public.messages
    for insert
    to authenticated
    with check (author_id = auth.uid());
