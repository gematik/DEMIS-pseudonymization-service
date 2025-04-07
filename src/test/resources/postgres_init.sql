-- Create PostgreSQL Schema and insert some data

CREATE TABLE IF NOT EXISTS public.secrets
(
    id              serial primary key unique,
    name_fcn_first  varchar(50) not null,
    name_fcn_second varchar(50) not null,
    date_fcn_first  varchar(50) not null,
    date_fcn_second varchar(50) not null,
    created_at      timestamp without time zone not null default CURRENT_TIMESTAMP
)
WITH (OIDS = FALSE) TABLESPACE pg_default;

ALTER TABLE public.secrets OWNER to postgres;

COMMENT ON COLUMN public.secrets.id
    IS 'Id of Pseudonym';

COMMENT ON COLUMN public.secrets.name_fcn_first
    IS 'Pseudonym Secret of Name first function';

COMMENT ON COLUMN public.secrets.name_fcn_second
    IS 'Pseudonym Secret of Name first function';

COMMENT ON COLUMN public.secrets.date_fcn_first
    IS 'Pseudonym Secret of Birthdate first function';

COMMENT ON COLUMN public.secrets.date_fcn_second
    IS 'Pseudonym Secret of Birthdate second function';

COMMENT ON COLUMN public.secrets.created_at
    IS 'Creation timestamp of the pseudonym';

INSERT INTO secrets(name_fcn_first,
                    name_fcn_second,
                    date_fcn_first,
                    date_fcn_second, created_at)
VALUES ('z6aRCrB;k?chQhZy-V%40/O)a]!5IgZUJ§G§)4-zVfSXm%mFEb', '%tJ[;y7u.SU.76at1lbT6W5UFIMkcS[87[%_kLnemi50VXq0lM',
        'TAA?atDRU[8vt9XnL(wps0W5ET6[y(b.m75Ckm!.pCf;?AVJOz', 'yKji8bVmjG5&t-qEq/xC!]zlARM§F8tj2%0F3wYT!$jp5_]N%b',
        '2023-07-14 14:42:46.8771');

INSERT INTO secrets(name_fcn_first,
                    name_fcn_second,
                    date_fcn_first,
                    date_fcn_second, created_at)
VALUES ('Jomj]K.69$e7s.v?qQUu9Gv;lGaZH)vwkvdp/Im&v?;cL6Aj_S', 'oJiGslxcWOP8lfyimq?.G§Zrwp5[pme4T_C!kCSHK§IupMHnhn',
        'D-SeR[kp0G;3v§z$Q§Lrjs-axobAykeU.gTVU1.kKAGtgodbAw', 'yd_3CbY.SH;vShxlcM)§g%U1bjXoyCaHjOM35FJK8AeB9dFjXh',
        '2023-09-14 14:42:46.8771')

----- Create Extended Secrets

CREATE TABLE IF NOT EXISTS public.secrets_two
(
    id              serial primary key unique,
    name_fcn_first  varchar(70) not null,
    name_fcn_second varchar(70) not null,
    date_fcn_first  varchar(70) not null,
    date_fcn_second varchar(70) not null,
    created_at      timestamp without time zone not null default CURRENT_TIMESTAMP
)
WITH (OIDS = FALSE) TABLESPACE pg_default;

ALTER TABLE public.secrets_two OWNER to postgres;;

COMMENT ON COLUMN public.secrets_two.id
    IS 'Id of Pseudonym';

COMMENT ON COLUMN public.secrets_two.name_fcn_first
    IS 'Pseudonym Secret of Name first function';

COMMENT ON COLUMN public.secrets_two.name_fcn_second
    IS 'Pseudonym Secret of Name first function';

COMMENT ON COLUMN public.secrets_two.date_fcn_first
    IS 'Pseudonym Secret of Birthdate first function';

COMMENT ON COLUMN public.secrets_two.date_fcn_second
    IS 'Pseudonym Secret of Birthdate second function';

COMMENT ON COLUMN public.secrets_two.created_at
    IS 'Creation timestamp of the pseudonym';

INSERT INTO secrets_two(name_fcn_first,
                    name_fcn_second,
                    date_fcn_first,
                    date_fcn_second, created_at)
VALUES ('z6aRCrB;k?chQhZy-V%40/O)a]!5IgZUJ§G§)4-zVfSXm%mFEb', '%tJ[;y7u.SU.76at1lbT6W5UFIMkcS[87[%_kLnemi50VXq0lM',
        'TAA?atDRU[8vt9XnL(wps0W5ET6[y(b.m75Ckm!.pCf;?AVJOz', 'yKji8bVmjG5&t-qEq/xC!]zlARM§F8tj2%0F3wYT!$jp5_]N%b',
        '2024-10-22 14:42:46.8771');

INSERT INTO secrets_two(name_fcn_first,
                    name_fcn_second,
                    date_fcn_first,
                    date_fcn_second, created_at)
VALUES ('Jomj]K.69$e7s.v?qQUu9Gv;lGaZH)vwkvdp/Im&v?;cL6Aj_S', 'oJiGslxcWOP8lfyimq?.G§Zrwp5[pme4T_C!kCSHK§IupMHnhn',
        'D-SeR[kp0G;3v§z$Q§Lrjs-axobAykeU.gTVU1.kKAGtgodbAw', 'yd_3CbY.SH;vShxlcM)§g%U1bjXoyCaHjOM35FJK8AeB9dFjXh',
        '2024-10-22 14:42:46.8771')
