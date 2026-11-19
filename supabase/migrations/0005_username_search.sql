-- Step 2 / 0005: exact-match username search and availability check

-- Exact match only (no prefix search, so nobody can list all users).
-- Honors the user's "searchable by username" setting and blocks.
create function public.find_user_by_username(p_username text)
returns table (id uuid, username citext, display_name text, avatar_url text)
language sql stable security definer set search_path = public, pg_temp as $$
  select u.id, u.username, u.display_name, u.avatar_url
  from public.users u
  where u.username = lower(p_username)::citext
    and u.username_searchable
    and u.id <> auth.uid()
    and not public.is_blocked(u.id, auth.uid())
  limit 1;
$$;

-- For the sign-up screen. TODO (Worker): rate-limit.
create function public.username_available(p_username text) returns boolean
language sql stable security definer set search_path = public, pg_temp as $$
  select
    lower(p_username) ~ '^[a-z][a-z0-9_]{4,31}$'
    and lower(p_username) !~ '__'
    and not exists (select 1 from public.users u where u.username = lower(p_username)::citext)
    and not exists (
      select 1 from public.reserved_usernames r
      where r.name = lower(p_username)::citext
        and (r.reserved_until is null or r.reserved_until > now())
        and r.previous_owner is distinct from auth.uid()
    );
$$;
