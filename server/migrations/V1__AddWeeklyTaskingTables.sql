CREATE TABLE goodol_weekly_tasking.categories
(
    id int AUTO_INCREMENT PRIMARY KEY,
    title varchar(100) NOT NULL
);

CREATE TABLE goodol_weekly_tasking.tasks
(
    id int AUTO_INCREMENT PRIMARY KEY,
    title varchar(100) NOT NULL,
    category_id int NOT NULL,
    CONSTRAINT fk_tasks_category_id__id FOREIGN KEY (category_id) REFERENCES goodol_weekly_tasking.categories (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE goodol_weekly_tasking.weeks
(
    id int PRIMARY KEY,
    start_date date NOT NULL,
    end_date date NOT NULL
);
ALTER TABLE goodol_weekly_tasking.weeks
    ADD CONSTRAINT weeks_start_date_unique UNIQUE (start_date);
ALTER TABLE goodol_weekly_tasking.weeks
    ADD CONSTRAINT weeks_end_date_unique UNIQUE (end_date);

CREATE TABLE goodol_weekly_tasking.responsibles
(
    id int AUTO_INCREMENT PRIMARY KEY,
    display_name varchar(100) NOT NULL
);

CREATE TABLE goodol_weekly_tasking.task_todos
(
    id int AUTO_INCREMENT PRIMARY KEY,
    task_id int NOT NULL,
    week_id int NOT NULL,
    week_day int NOT NULL,
    responsible_id int NOT NULL,
    CONSTRAINT fk_task_todos_task_id__id FOREIGN KEY (task_id) REFERENCES goodol_weekly_tasking.tasks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_task_todos_week_id__id FOREIGN KEY (week_id) REFERENCES goodol_weekly_tasking.weeks (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_task_todos_responsible_id__id FOREIGN KEY (responsible_id) REFERENCES goodol_weekly_tasking.responsibles (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);
ALTER TABLE goodol_weekly_tasking.task_todos
    ADD CONSTRAINT tasks_todos_week_day_range CHECK (week_day BETWEEN 0 AND 6)