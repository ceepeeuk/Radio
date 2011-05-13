create table android_metadata (
	locale text
);

create table stations (
	_id integer primary key,
	name text,
	url text
);

create table recording_type (
	_id integer primary key,
	type text
);

create table recording_schedule (
	_id integer primary key,
	start_time integer,
	end_time integer,
	station integer,
	type integer,
	foreign key (station) references stations (_id),
	foreign key (type) references recording_type (_id)
);

insert into recording_type values (null, 'one-off' );
insert into recording_type values (null, 'daily' );
insert into recording_type values (null, 'weekly' );