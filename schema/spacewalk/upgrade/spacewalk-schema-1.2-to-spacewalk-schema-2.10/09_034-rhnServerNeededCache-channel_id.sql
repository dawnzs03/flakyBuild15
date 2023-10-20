

ALTER TABLE rhnServerNeededCache ADD
        channel_id NUMERIC
                CONSTRAINT rhn_sncp_cid_fk
                REFERENCES rhnChannel (id)
                ON DELETE CASCADE;

CREATE INDEX rhn_snc_cid_idx
    ON rhnServerNeededCache (channel_id)
    ;
