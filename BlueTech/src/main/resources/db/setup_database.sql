CREATE DATABASE IF NOT EXISTS blue_tech
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE blue_tech;

CREATE USER IF NOT EXISTS 'usuarioBlueTech'@'localhost' IDENTIFIED BY 'BlueTechADM123';

GRANT SELECT, INSERT, UPDATE, DELETE ON bluetech.* TO 'usuarioBlueTech'@'localhost';

FLUSH PRIVILEGES;