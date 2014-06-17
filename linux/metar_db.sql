CREATE DATABASE metar;
USE metar;
CREATE TABLE reports
(
   icao varchar(4) NOT NULL PRIMARY KEY,
   time varchar(4),
   report varchar(450),
   colour varchar(3)
);
CREATE USER 'web_user'@'localhost' IDENTIFIED BY '******';
CREATE USER 'local_user'@'localhost' IDENTIFIED BY '******';
GRANT SELECT ON reports TO 'web_user'@'localhost' IDENTIFIED BY '*******';
GRANT ALL PRIVILEGES ON reports TO 'local_user'@'localhost' IDENTIFIED BY '*******';
