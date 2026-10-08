create table app_config (
                            id bigint primary key,

                            usd_rub_rate numeric(10, 4) not null,
                            skin_markup numeric(5, 4) not null,

                            updated_at timestamp not null default now()
);

insert into app_config (
    id,
    usd_rub_rate,
    skin_markup,
    updated_at
)
values (
           1,
           88.0000,
           1.2000,
           now()
       );