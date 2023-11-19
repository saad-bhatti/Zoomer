DROP TABLE IF EXISTS Users;

CREATE TABLE Users(
	uid SERIAL NOT NULL PRIMARY KEY,
	email varchar NOT NULL UNIQUE,
	prefer_name varchar NOT NULL,
	"password" varchar NOT NULL,
	rides NUMERIC NOT NULL,
	isDriver bool NOT NULL DEFAULT false
);