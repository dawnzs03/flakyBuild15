--
-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE IF NOT EXISTS rhnPackageExtraTagKey
(
    id          NUMERIC NOT NULL
                    CONSTRAINT rhn_pkg_extra_tags_keys_id_pk PRIMARY KEY,
    name        VARCHAR(256) NOT NULL,
    created     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)
;

CREATE SEQUENCE IF NOT EXISTS rhn_package_extra_tags_keys_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS rhn_pkg_extra_tag_key_idx
    ON rhnPackageExtraTagKey (name);

CREATE TABLE IF NOT EXISTS rhnPackageExtraTag
(
    package_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_pkg_extratags_pid_fk
                       REFERENCES rhnPackage (id)
                       ON DELETE CASCADE,
    key_id      NUMERIC NOT NULL
                       REFERENCES rhnPackageExtraTagKey (id)
                       ON DELETE CASCADE,
    value       VARCHAR(2048) NOT NULL,
    created     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    primary key (package_id, key_id)
)
;



