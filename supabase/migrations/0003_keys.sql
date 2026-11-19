-- Step 2 / 0003: Signal Protocol public keys (private keys never leave the device)
-- MVP: one device per user.

create table public.identity_keys (
  user_id uuid primary key references public.users (id) on delete cascade,
  registration_id integer not null,
  identity_key bytea not null,
  signed_prekey_id integer not null,
  signed_prekey bytea not null,
  signed_prekey_signature bytea not null,
  updated_at timestamptz not null default now()
);

create table public.one_time_prekeys (
  id bigint generated always as identity primary key,
  user_id uuid not null references public.users (id) on delete cascade,
  key_id integer not null,
  public_key bytea not null,
  unique (user_id, key_id)
);

create trigger identity_keys_set_updated_at
  before update on public.identity_keys
  for each row execute function public.set_updated_at();

alter table public.identity_keys enable row level security;
alter table public.one_time_prekeys enable row level security;

create policy identity_keys_select_own on public.identity_keys
  for select to authenticated using (user_id = (select auth.uid()));
create policy identity_keys_insert_own on public.identity_keys
  for insert to authenticated with check (user_id = (select auth.uid()));
create policy identity_keys_update_own on public.identity_keys
  for update to authenticated
  using (user_id = (select auth.uid())) with check (user_id = (select auth.uid()));
create policy identity_keys_delete_own on public.identity_keys
  for delete to authenticated using (user_id = (select auth.uid()));

create policy otp_select_own on public.one_time_prekeys
  for select to authenticated using (user_id = (select auth.uid()));
create policy otp_insert_own on public.one_time_prekeys
  for insert to authenticated with check (user_id = (select auth.uid()));
create policy otp_delete_own on public.one_time_prekeys
  for delete to authenticated using (user_id = (select auth.uid()));

-- Other users get someone's keys only through this function, which consumes
-- one one-time prekey atomically. If none are left, one_time_prekey is null
-- (the client falls back to the signed prekey). Blocked pairs get nothing.
-- TODO (Worker): rate-limit this call to stop prekey exhaustion attacks.
create function public.fetch_prekey_bundle(p_user uuid)
returns table (
  registration_id integer,
  identity_key bytea,
  signed_prekey_id integer,
  signed_prekey bytea,
  signed_prekey_signature bytea,
  one_time_prekey_id integer,
  one_time_prekey bytea
)
language plpgsql security definer set search_path = public, pg_temp as $$
declare
  v_caller uuid := auth.uid();
  v_key_id integer;
  v_pub bytea;
begin
  if v_caller is null then
    raise exception 'not_authenticated';
  end if;
  if v_caller = p_user or public.is_blocked(v_caller, p_user) then
    return;
  end if;

  delete from public.one_time_prekeys o
  where o.id = (
    select p.id from public.one_time_prekeys p
    where p.user_id = p_user
    order by p.id
    limit 1
    for update skip locked
  )
  returning o.key_id, o.public_key into v_key_id, v_pub;

  return query
    select ik.registration_id, ik.identity_key, ik.signed_prekey_id,
           ik.signed_prekey, ik.signed_prekey_signature, v_key_id, v_pub
    from public.identity_keys ik
    where ik.user_id = p_user;
end $$;
