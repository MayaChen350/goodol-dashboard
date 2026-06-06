ALTER TABLE weekly_tasking__task_todos
    MODIFY COLUMN responsible_id int NULL;
ALTER TABLE weekly_tasking__categories
    ADD CONSTRAINT weekly_tasking__categories_title_unique UNIQUE (title);
ALTER TABLE weekly_tasking__task_todos
    ADD CONSTRAINT weekly_tasking__task_todos_task_id_week_id_week_day_unique UNIQUE (task_id, week_id, week_day);
ALTER TABLE weekly_tasking__tasks
    ADD CONSTRAINT weekly_tasking__tasks_title_unique UNIQUE (title);
ALTER TABLE weekly_tasking__responsibles
    ADD CONSTRAINT check_weekly_tasking__responsibles_0 CHECK (chosen_color BETWEEN 0 AND 16777215);
