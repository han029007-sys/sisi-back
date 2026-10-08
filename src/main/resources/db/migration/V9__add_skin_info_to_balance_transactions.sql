alter table balance_transactions
    add column if not exists skin_name varchar(255),
    add column if not exists skin_image_url text;