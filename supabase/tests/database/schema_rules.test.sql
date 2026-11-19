begin;
select * from no_plan();

insert into auth.users (id, email) values
  ('00000000-0000-0000-0000-0000000000a1', 'alice@test.local'),
  ('00000000-0000-0000-0000-0000000000b2', 'bob@test.local'),
  ('00000000-0000-0000-0000-0000000000c3', 'carol@test.local');

-- ---------- usernames ----------
select is(
  (select count(*) from public.users
   where id in ('00000000-0000-0000-0000-0000000000a1',
                '00000000-0000-0000-0000-0000000000b2',
                '00000000-0000-0000-0000-0000000000c3')),
  3::bigint, 'profile rows are created for new auth users');

update public.users set username = 'Alice_One'
  where id = '00000000-0000-0000-0000-0000000000a1';
select is(
  (select username::text from public.users where id = '00000000-0000-0000-0000-0000000000a1'),
  'alice_one', 'username is stored lowercase');

select throws_ok(
  $$update public.users set username = 'abcd' where id = '00000000-0000-0000-0000-0000000000b2'$$,
  '23514', null, 'too short username is rejected');

select throws_ok(
  $$update public.users set username = 'ab__cde' where id = '00000000-0000-0000-0000-0000000000b2'$$,
  '23514', null, 'double underscore is rejected');

select throws_ok(
  $$update public.users set username = 'admin' where id = '00000000-0000-0000-0000-0000000000b2'$$,
  'P0001', 'username_reserved', 'system names are reserved');

update public.users set username = 'alice_two'
  where id = '00000000-0000-0000-0000-0000000000a1';
select throws_ok(
  $$update public.users set username = 'alice_one' where id = '00000000-0000-0000-0000-0000000000b2'$$,
  'P0001', 'username_reserved', 'old username is reserved for other users');

select lives_ok(
  $$update public.users set username = 'alice_one' where id = '00000000-0000-0000-0000-0000000000a1'$$,
  'previous owner can take the old name back');

select lives_ok(
  $$update public.users set username = 'bob_user' where id = '00000000-0000-0000-0000-0000000000b2'$$,
  'bob can set a free username');

-- ---------- messages: alice -> bob ----------
select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000a1","role":"authenticated"}', true);
set local role authenticated;

select lives_ok(
  $$insert into public.pending_messages (sender_id, recipient_id, client_message_id, message_type, ciphertext)
    values ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-0000000000b2',
            gen_random_uuid(), 2, decode('0102', 'hex'))$$,
  'alice can send to bob');

select is((select count(*) from public.pending_messages), 0::bigint,
  'sender cannot read the relay queue');
reset role;

select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000c3","role":"authenticated"}', true);
set local role authenticated;
select is((select count(*) from public.pending_messages), 0::bigint,
  'a stranger cannot read bob''s messages');
reset role;

select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000b2","role":"authenticated"}', true);
set local role authenticated;
select is((select count(*) from public.pending_messages), 1::bigint,
  'bob can read his message');
select lives_ok($$delete from public.pending_messages$$, 'bob can ACK (delete) it');
reset role;
select is((select count(*) from public.pending_messages), 0::bigint,
  'message is gone from the server after ACK');

-- ---------- blocking ----------
select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000b2","role":"authenticated"}', true);
set local role authenticated;
select lives_ok(
  $$insert into public.blocks (blocker_id, blocked_id)
    values ('00000000-0000-0000-0000-0000000000b2', '00000000-0000-0000-0000-0000000000a1')$$,
  'bob blocks alice');
reset role;

select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000a1","role":"authenticated"}', true);
set local role authenticated;
select lives_ok(
  $$insert into public.pending_messages (sender_id, recipient_id, client_message_id, message_type, ciphertext)
    values ('00000000-0000-0000-0000-0000000000a1', '00000000-0000-0000-0000-0000000000b2',
            gen_random_uuid(), 2, decode('0102', 'hex'))$$,
  'blocked sender gets no error');
select is((select count(*) from public.find_user_by_username('bob_user')), 0::bigint,
  'blocked user cannot find the blocker');
reset role;
select is((select count(*) from public.pending_messages), 0::bigint,
  'message from blocked sender was silently dropped');

-- ---------- search ----------
select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000c3","role":"authenticated"}', true);
set local role authenticated;
select is((select count(*) from public.find_user_by_username('ALICE_ONE')), 1::bigint,
  'exact-match search is case-insensitive');
select is((select count(*) from public.find_user_by_username('alice')), 0::bigint,
  'partial search returns nothing');
reset role;

-- ---------- prekeys ----------
select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000a1","role":"authenticated"}', true);
set local role authenticated;
insert into public.identity_keys
  (user_id, registration_id, identity_key, signed_prekey_id, signed_prekey, signed_prekey_signature)
values ('00000000-0000-0000-0000-0000000000a1', 1234, decode('01', 'hex'), 1,
        decode('02', 'hex'), decode('03', 'hex'));
insert into public.one_time_prekeys (user_id, key_id, public_key) values
  ('00000000-0000-0000-0000-0000000000a1', 1, decode('0a', 'hex')),
  ('00000000-0000-0000-0000-0000000000a1', 2, decode('0b', 'hex'));
reset role;

select set_config('request.jwt.claims',
  '{"sub":"00000000-0000-0000-0000-0000000000c3","role":"authenticated"}', true);
set local role authenticated;
select is((select count(*) from public.identity_keys), 0::bigint,
  'other users cannot read identity keys directly');
select is((select one_time_prekey_id from public.fetch_prekey_bundle('00000000-0000-0000-0000-0000000000a1')),
  1, 'first bundle uses one-time prekey 1');
select is((select one_time_prekey_id from public.fetch_prekey_bundle('00000000-0000-0000-0000-0000000000a1')),
  2, 'second bundle uses one-time prekey 2');
select is((select one_time_prekey_id from public.fetch_prekey_bundle('00000000-0000-0000-0000-0000000000a1')),
  null::integer, 'third bundle has no one-time prekey left');
reset role;

select * from finish();
rollback;
