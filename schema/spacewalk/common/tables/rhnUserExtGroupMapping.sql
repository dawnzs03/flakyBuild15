--
-- Copyright (c) 2014 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnUserExtGroupMapping
(
    id        NUMERIC NOT NULL
                  CONSTRAINT rhn_userExtGroupMap_id_pk PRIMARY KEY
                  ,
    ext_group_id        NUMERIC NOT NULL
                  constraint rhn_userextgroupmap_e_fk
                  references rhnUserExtGroup(id)
                  on delete cascade,
    int_group_type_id        NUMERIC NOT NULL
                  constraint rhn_userextgroupmap_i_fk
                  references rhnUserGroupType(id)
                  on delete cascade,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_userextgroupmap_ei_uq
    ON rhnUserExtGroupMapping (ext_group_id, int_group_type_id)
    ;

CREATE SEQUENCE rhn_userextgroupmap_seq;
