create table credentials (
  id serial primary key,
  email text not null,
  password_salt text,
  password_value text,
  email_verified boolean not null default false,
  email_verification_code text
);

create table consultant (
  id serial primary key,
  credentials_id not null references credentials(id),
  first_name text,
  last_name text
);

create table client (
  id serial primary key,
  credentials_id not null references credentials(id),
  company_name text not null,
  owner_name text not null,
  phone text not null default '',
  is_prospect boolean not null default true
);

create table survey (
  id serial primary key,
  title text not null
);

create table survey_question (
  id serial primary key,
  survey_id integer not null references survey(id),
  ordinal smallint not null,
  question text not null,
  question_type text not null, -- EDN keyword
  question_params text not null, -- EDN (pr-str) based on question type
);

create table survey_answer (
  id serial primary key,
  survey_client_id integer not null references client(id),
  survey_question_id integer not null references survey_question(id),
  answer text not null  -- as an EDN pr-str
);
create unique index survey_unique_answer_idx on survey_answer(survey_client_id, survey_question_id);

create table rfp (
  id serial primary key,
  client_id int not null references client(id),
  title text not null,
  short_description text not null,
  time_submitted timestamp not null default CURRENT_TIMESTAMP,
  nda_signed boolean not null default false,
  nda_signed_on timestamp,
  consultation_appointment_time timestamp,
  consultant_id not null references consultant(id)
);

create table rfp_note (
  id bigserial primary key,
  rfp_id int not null references rfp(id),
  consultant_id not null references consultant(id),
  note_time timestamp not null default current_timestamp,
  note text
);

create table tempid_remaps ( -- stores tempid remaps so inserts can be idempotent
  temp_id uuid not null,
  real_id bigint not null,
  resolved timestamp not null default current_timestamp -- so we can GC the table
);
create index tempid_remaps_idx on tempid_remaps(temp_id);
