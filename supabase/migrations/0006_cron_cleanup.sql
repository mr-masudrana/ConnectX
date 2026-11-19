-- Step 2 / 0006: automatic cleanup (privacy: nothing stays on the server longer than 30 days)

create extension if not exists pg_cron;

select cron.schedule(
  'purge-pending-messages',
  '17 * * * *',
  $$delete from public.pending_messages where created_at < now() - interval '30 days'$$
);

select cron.schedule(
  'purge-expired-username-reservations',
  '23 3 * * *',
  $$delete from public.reserved_usernames where reserved_until is not null and reserved_until < now()$$
);
