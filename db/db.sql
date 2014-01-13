create table users (
	id bigserial not null,
	name varchar(128) not null,
	password varchar(128) not null,
	regdate timestamp without time zone not null default now(),
	constraint pk_users primary key (id)
);

create table genres (
  id bigserial not null,
  name varchar(128) not null,
  constraint pk_genres primary key (id)
);

insert into genres
(name)
values
('Death Metal'), ('Black Metal'), ('Doom Metal');

create table artists (
  id bigserial not null,
  name varchar(128) not null,
  genre_id bigint not null references genres (id),
  constraint pk_artists primary key (id)
);

insert into artists (name, genre_id)
values
('Hanging Garden', 3), ('Doom:VS', 3);

create table albums (
  id bigserial not null,
  name varchar(255) not null,
  issue_year bigint not null,
  format varchar(10) not null default 'LP',
  artist_id bigint not null references artists (id),
  constraint pk_albums primary key (id)
);

insert into albums
(name, issue_year, format, artist_id)
values
('Empire Of The Fallen', 2004, 'LP', 2),
('Aeternum Vale', 2006, 'LP', 2),
('Dead Words Speak', 2008, 'LP', 2);

create table tracks (
  id bigserial not null,
  name varchar(512) not null,
  duration varchar(16) not null,
  location varchar(1024) not null,
  album_id bigint not null references albums (id),
  constraint pk_tracks primary key (id)
);

insert into tracks
(name, duration, location, album_id)
values
('Half Light', '7:48', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/01-Half Light.mp3', 3),
('Dead Words Speak', '8:01', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/02-Dead Words Speak.mp3', 3),
('The Lachymal Sleep', '8:00', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/03-The Lachymal Sleep.mp3', 3),
('Upon The Cataract', '8:00', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/04-Upon The Cataract.mp3', 3),
('Leaden Winged Burden', '6:43', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/05-Leaden Winged Burden.mp3', 3),
('Threnode', '8:00', 'c:/Dropbox/music/Doom VS/2008 - Dead Words Speak/06-Threnode.mp3', 3);

create table settings (
  id bigserial not null,
  name varchar(128) not null,
  description varchar(255) not null,
  value varchar(255) not null,
  constraint pk_settings primary key (id)
);

insert into settings
(name, description, value)
values
('repo_location', 'music repository location on local drive', 'c:/music/');

create table logs (
  id bigserial not null,
  log_text text not null,
  log_date timestamp without time zone not null default now(),
  log_level varchar(2) not null default 'D',
  constraint pk_logs primary key (id)
);

create table supported_formats (
  id bigserial not null,
  name varchar(16) not null,
  constraint pk_supported_formats primary key (id)
);

insert into supported_formats
(name)
values
('mp3');

insert into settings
(name, description, value)
values
('temp_location', 'temporary location on local drive', 'temp/');

insert into settings
(name, description, value)
values 
('lastfm_api_key', 'LastFM API key', '3df5180904a9a83cd62ab535f0567e67'),
('lastfm_secret', 'LastFM Secret key', 'f35a156fccfe2963b6051e23c1c2aa99');

create table last_fm_tokens (
  id bigserial not null,
  token varchar(64) not null,
  create_date timestamp without time zone not null default now(),
  constraint pk_last_fm_tokens primary key (id)
);

alter table users
add lastfm_account varchar(128);

alter table users
add lastfm_session_key varchar(255);

insert into settings
(name, description, value)
values
('lastfm_api_url', 'URL to LastFM API', 'http://ws.audioscrobbler.com/2.0/');

create table countries (
  id bigserial not null,
  name varchar(128),
  constraint pk_countries primary key (id)
);

insert into countries
(name)
values
('Afghanistan'),
('Albania'),
('Algeria'),
('America'),
('Angola'),
('Anguilla'),
('Antigua And Barbuda'),
('Argentina'),
('Armenia'),
('Aruba'),
('Australia'),
('Austria'),
('Azerbaijan'),
('Bahamas'),
('Bahrain'),
('Bangladesh'),
('Barbados'),
('Belarus'),
('Belgium'),
('Belize'),
('Benin'),
('Bermuda'),
('Bhutan'),
('Bolivia'),
('Bosnia Herzegovina'),
('Botswana'),
('Brazil'),
('British Virgin Islands'),
('Brunei'),
('Bulgaria'),
('Burkina Faso'),
('Burundi'),
('Cambodia'),
('Cameroon, United Republic Of'),
('Canada'),
('Cape Verde, Republic Of'),
('Central African Republic'),
('Chad'),
('Chile'),
('China'),
('Cocos'),
('Colombia'),
('Comoros'),
('Congo'),
('Congo Democratic Republic Of'),
('Cook Islands'),
('Costa Rica'),
('Croatia'),
('Cuba'),
('Cyprus'),
('Czechia'),
('Denmark'),
('Djibouti'),
('Dominica'),
('Dominican Republic'),
('East Timor'),
('Ecuador'),
('Egypt'),
('El Salvador'),
('Eritrea'),
('Estonia'),
('Ethiopia'),
('Falkland Islands'),
('Fiji Islands'),
('Finland'),
('France'),
('French Guiana'),
('French Polynesia'),
('Gabon'),
('Gambia'),
('Georgia'),
('Germany'),
('Ghana'),
('Greece'),
('Greenland'),
('Guadeloupe'),
('Guam'),
('Guatemala'),
('Guinea'),
('Guinea Bissau'),
('Guyana'),
('Haiti'),
('Holland'),
('Honduras'),
('Hong Kong'),
('Hungary'),
('Iceland'),
('India'),
('Indonesia'),
('Iran'),
('Iraq'),
('Ireland, Republic Of'),
('Israel'),
('Italy'),
('Ivory Coast'),
('Jamaica'),
('Japan'),
('Jordan'),
('Kazakstan'),
('Kenya'),
('Kiribati'),
('Kiritimati'),
('Korea, Democratic Peoples Republic'),
('Korea, Republic Of'),
('Kuwait'),
('Kyrgyzstan'),
('Latvia'),
('Lebanon'),
('Lesotho'),
('Liberia'),
('Libya'),
('Lithuania'),
('Luxembourg'),
('Macao'),
('Macedonia'),
('Madagascar (Malagasy)'),
('Malawi'),
('Malaysia'),
('Maldives'),
('Mali'),
('Malta'),
('Marianna Islands'),
('Marshall Islands'),
('Martinique'),
('Mauritania'),
('Mauritius'),
('Mayotte'),
('Mexico'),
('Micronesia'),
('Moldova'),
('Monaco'),
('Mongolia'),
('Montenegro'),
('Morocco'),
('Mozambique'),
('Myanmar'),
('Namibia'),
('Nauru'),
('Nepal'),
('Netherland Antilles'),
('New Caledonia'),
('New Zealand'),
('Nicaragua'),
('Niger'),
('Nigeria'),
('Niue'),
('Norfolk Island'),
('Norway'),
('Occupied Palestinian Territory'),
('Oman Sultanate Of'),
('Pakistan'),
('Palau'),
('Panama'),
('Papua New Guinea (Niugini)'),
('Paraguay'),
('Peru'),
('Philippines'),
('Poland'),
('Portugal'),
('Qatar'),
('Reunion'),
('Romania'),
('Russia'),
('Rwanda'),
('Saint Vincent And The Grenadines'),
('Samoa, American'),
('Samoa, Independent State Of'),
('Sao Tome & Principe'),
('Saudi Arabia'),
('Senegal'),
('Serbia'),
('Seychelles Islands'),
('Sierra Leone'),
('Singapore'),
('Slovakia'),
('Slovenia'),
('Solomon Islands'),
('Somalia'),
('South Africa'),
('Spain'),
('Spanish Guinea'),
('Sri Lanka'),
('St Lucia'),
('St. Kitts - Nevis'),
('Sudan'),
('Suriname'),
('Swaziland'),
('Sweden'),
('Switzerland'),
('Syrian Arab Rep.'),
('Taiwan, Republic of China'),
('Tajikistan'),
('Tanzania'),
('Thailand'),
('The Caymans'),
('The Rock'),
('Togo'),
('Tonga'),
('Trinidad & Tobago'),
('Tunisia'),
('Turkey'),
('Turkmenistan'),
('Turks And Caicos Islands'),
('Tuvalu'),
('Uganda'),
('UK'),
('Ukraine'),
('United Arab Emirates'),
('Uruguay'),
('Uzbekistan Sum'),
('Vanuatu'),
('Venezuela'),
('Vietnam'),
('Yemen, Republic Of'),
('Zambia'),
('Zimbabwe');

alter table artists
add country_id bigint references countries (id);

create table invites (
  id bigserial not null,
  invite varchar(256) not null,
  used char(1) not null default '0',
  constraint pk_invites primary key (id)
);

alter table users
add email varchar(256);

update users
set email = 'serejja@gmail.com';

alter table users
alter column email set not null;

alter table tracks
add column track_no int;