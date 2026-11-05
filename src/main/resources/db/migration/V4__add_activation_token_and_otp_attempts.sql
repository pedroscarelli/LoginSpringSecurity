ALTER TABLE tb_users
  ALTER COLUMN st_otp_code TYPE VARCHAR(6);

ALTER TABLE tb_users
  ADD COLUMN it_otp_attempts SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE tb_users
  ADD COLUMN st_activation_token VARCHAR(64);

CREATE UNIQUE INDEX ux_users_activation_token
  ON tb_users(st_activation_token);
