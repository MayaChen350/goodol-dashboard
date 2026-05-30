ALTER TABLE weekly_tasking__responsibles
    ADD chosen_color int UNSIGNED NOT NULL;
ALTER TABLE weekly_tasking__responsibles
    ADD CONSTRAINT weekly_tasking__responsibles_display_name_unique UNIQUE (display_name);
ALTER TABLE weekly_tasking__task_todos
    ADD CONSTRAINT check_weekly_tasking__task_todos_0 CHECK (week_day BETWEEN 0 AND 6);
ALTER TABLE weekly_tasking__task_todos DROP CONSTRAINT tasks_todos_week_day_range;
