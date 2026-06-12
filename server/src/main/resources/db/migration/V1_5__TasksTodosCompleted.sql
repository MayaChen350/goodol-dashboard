ALTER TABLE weekly_tasking__task_todos
    ADD is_completed boolean DEFAULT FALSE NOT NULL;
ALTER TABLE weekly_tasking__task_todos
    DROP FOREIGN KEY fk_weekly_tasking__task_todos_task_id__id;
ALTER TABLE weekly_tasking__task_todos
    ADD CONSTRAINT fk_weekly_tasking__task_todos_task_id__id FOREIGN KEY (task_id) REFERENCES weekly_tasking__tasks (id) ON DELETE CASCADE ON UPDATE RESTRICT;
