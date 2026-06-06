CREATE TABLE IF NOT EXISTS weekly_tasking__categories
(
    id int AUTO_INCREMENT PRIMARY KEY,
    title varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS weekly_tasking__tasks
(
    id int AUTO_INCREMENT PRIMARY KEY,
    title varchar(100) NOT NULL,
    category_id int NOT NULL,
    CONSTRAINT fk_weekly_tasking__tasks_category_id__id FOREIGN KEY (category_id) REFERENCES weekly_tasking__categories (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS weekly_tasking__weeks
(
    id int PRIMARY KEY,
    start_date date NOT NULL,
    end_date date NOT NULL
);
ALTER TABLE weekly_tasking__weeks
    ADD CONSTRAINT weekly_tasking__weeks_start_date_unique UNIQUE (start_date);
ALTER TABLE weekly_tasking__weeks
    ADD CONSTRAINT weekly_tasking__weeks_end_date_unique UNIQUE (end_date);

CREATE TABLE IF NOT EXISTS weekly_tasking__responsibles
(
    id int AUTO_INCREMENT PRIMARY KEY,
    display_name varchar(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS weekly_tasking__task_todos
(
    id int AUTO_INCREMENT PRIMARY KEY,
    task_id int NOT NULL,
    week_id int NOT NULL,
    week_day int NOT NULL,
    responsible_id int NOT NULL,
    CONSTRAINT fk_weekly_tasking__task_todos_task_id__id FOREIGN KEY (task_id) REFERENCES weekly_tasking__tasks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_weekly_tasking__task_todos_week_id__id FOREIGN KEY (week_id) REFERENCES weekly_tasking__weeks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_weekly_tasking__task_todos_responsible_id__id FOREIGN KEY (responsible_id) REFERENCES weekly_tasking__responsibles (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
ALTER TABLE weekly_tasking__task_todos
    ADD CONSTRAINT tasks_todos_week_day_range CHECK (week_day BETWEEN 0 AND 6)