
drop trigger if exists rhn_servnet_ipaddr_mon_trig on rhnServerNetwork;

create or replace function rhn_servernetwork_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
 	return new;
end;
$$ language plpgsql;
