-- Step 2 / 0007: lock down privileges. RLS still applies on top of these.

revoke all on all tables in schema public from anon;

-- Internal / trigger functions: not callable by clients
revoke execute on function public.is_blocked(uuid, uuid) from public, anon, authenticated;
revoke execute on function public.enforce_username_rules() from public, anon, authenticated;
revoke execute on function public.handle_new_user() from public, anon, authenticated;
revoke execute on function public.set_updated_at() from public, anon, authenticated;
revoke execute on function public.pending_messages_before_insert() from public, anon, authenticated;

-- Client-callable functions: signed-in users only
revoke execute on function public.fetch_prekey_bundle(uuid) from public, anon;
revoke execute on function public.find_user_by_username(text) from public, anon;
revoke execute on function public.username_available(text) from public, anon;

grant execute on function public.fetch_prekey_bundle(uuid) to authenticated;
grant execute on function public.find_user_by_username(text) to authenticated;
grant execute on function public.username_available(text) to authenticated;
