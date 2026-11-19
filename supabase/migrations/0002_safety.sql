-- Step 2 / 0002: blocks and reports

create table public.blocks (
  blocker_id uuid not null references public.users (id) on delete cascade,
  blocked_id uuid not null references public.users (id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (blocker_id, blocked_id),
  check (blocker_id <> blocked_id)
);
create index blocks_blocked_idx on public.blocks (blocked_id);

create table public.reports (
  id uuid primary key default gen_random_uuid(),
  reporter_id uuid not null references public.users (id) on delete cascade,
  reported_id uuid not null references public.users (id) on delete cascade,
  reason text not null check (reason in ('spam', 'harassment', 'impersonation', 'illegal', 'other')),
  details text check (char_length(details) <= 2000),
  created_at timestamptz not null default now(),
  check (reporter_id <> reported_id)
);

alter table public.blocks enable row level security;
alter table public.reports enable row level security;

create policy blocks_select_own on public.blocks
  for select to authenticated using (blocker_id = (select auth.uid()));
create policy blocks_insert_own on public.blocks
  for insert to authenticated with check (blocker_id = (select auth.uid()));
create policy blocks_delete_own on public.blocks
  for delete to authenticated using (blocker_id = (select auth.uid()));

-- Reports are write-only for users; moderators read them with the service role
create policy reports_insert_own on public.reports
  for insert to authenticated with check (reporter_id = (select auth.uid()));

-- True if either user blocked the other. Internal only (execute revoked in 0007),
-- so nobody can probe who blocked whom.
create function public.is_blocked(a uuid, b uuid) returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select exists (
    select 1 from public.blocks
    where (blocker_id = a and blocked_id = b)
       or (blocker_id = b and blocked_id = a)
  );
$$;
