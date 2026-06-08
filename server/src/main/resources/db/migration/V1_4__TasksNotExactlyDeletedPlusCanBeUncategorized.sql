ALTER TABLE weekly_tasking__tasks
    ADD is_deleted boolean DEFAULT FALSE NOT NULL;
ALTER TABLE weekly_tasking__tasks
    MODIFY COLUMN category_id int DEFAULT NULL NULL;
