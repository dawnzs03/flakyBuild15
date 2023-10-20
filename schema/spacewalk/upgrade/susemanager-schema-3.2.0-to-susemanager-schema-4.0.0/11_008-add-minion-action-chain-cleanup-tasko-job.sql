INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
    SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'minion-action-chain-cleanup-bunch', 'Cleanup actions chains for Minions', null
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoBunch WHERE
        name='minion-action-chain-cleanup-bunch'
    );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-action-chain-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch'),
        current_timestamp, '0 0 * * * ?'
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoSchedule WHERE
        job_label='minion-action-chain-cleanup-default'
    );

INSERT INTO rhnTaskoTask (id, name, class)
    SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'minion-action-chain-cleanup', 'com.redhat.rhn.taskomatic.task.MinionActionChainCleanup'
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask WHERE
        name='minion-action-chain-cleanup'
    );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    SELECT sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='minion-action-chain-cleanup'),
                        0,
                        null
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTemplate WHERE
        bunch_id=(SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch') AND
        task_id=(SELECT id FROM rhnTaskoTask WHERE name='minion-action-chain-cleanup')
    );
