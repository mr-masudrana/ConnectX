-- Step 2 / 0001: users, username rules, reserved names
-- Identity is users.id (UUID = auth.users.id). Username is only a changeable alias.

create extension if not exists citext with schema public;

create table public.users (
  id uuid primary key references auth.users (id) on delete cascade,
  username citext unique,
  display_name text check (char_length(display_name) <= 64),
  avatar_url text,
  username_searchable boolean not null default true,
  phone_searchable boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint users_username_format check (
    username is null
    or (username::text ~ '^[a-z][a-z0-9_]{4,31}$' and username::text !~ '__')
  )
);

-- Permanent system names (reserved_until is null) and old usernames (30 days)
create table public.reserved_usernames (
  name citext primary key,
  reason text not null default 'system',
  previous_owner uuid references public.users (id) on delete cascade,
  reserved_until timestamptz
);

insert into public.reserved_usernames (name) values
  ('admin'), ('administrator'), ('support'), ('help'), ('official'), ('connectx'),
  ('root'), ('system'), ('moderator'), ('staff'), ('security'), ('abuse'),
  ('postmaster'), ('team'), ('info'), ('contact'), ('null'), ('undefined');

create function public.set_updated_at() returns trigger
language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end $$;

create trigger users_set_updated_at
  before update on public.users
  for each row execute function public.set_updated_at();

create function public.enforce_username_rules() returns trigger
language plpgsql security definer set search_path = public, pg_temp as $$
begin
  if new.username is null then
    return new;
  end if;

  new.username := lower(new.username::text)::citext;

  if tg_op = 'UPDATE' and new.username is not distinct from old.username then
    return new;
  end if;

  if exists (
    select 1 from public.reserved_usernames r
    where r.name = new.username
      and (r.reserved_until is null or r.reserved_until > now())
      and r.previous_owner is distinct from new.id
  ) then
    raise exception 'username_reserved';
  end if;

  -- Keep the old name reserved for 30 days so nobody can impersonate the previous owner
  if tg_op = 'UPDATE' and old.username is not null then
    insert into public.reserved_usernames (name, reason, previous_owner, reserved_until)
    values (old.username, 'old_username', old.id, now() + interval '30 days')
    on conflict (name) do update
      set reason = excluded.reason,
          previous_owner = excluded.previous_owner,
          reserved_until = excluded.reserved_until;
  end if;

  return new;
end $$;

create trigger users_enforce_username
  before insert or update of username on public.users
  for each row execute function public.enforce_username_rules();

-- Create the profile row automatically when an auth user signs up
create function public.handle_new_user() returns trigger
language plpgsql security definer set search_path = public, pg_temp as $$
begin
  insert into public.users (id) values (new.id) on conflict do nothing;
  return new;
end $$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- RLS: a user sees and edits only their own row. Others are reached via functions (0005).
alter table public.users enable row level security;
alter table public.reserved_usernames enable row level security;

create policy users_select_own on public.users
  for select to authenticated
  using (id = (select auth.uid()));

create policy users_update_own on public.users
  for update to authenticated
  using (id = (select auth.uid()))
  with check (id = (select auth.uid()));
