# Dev TODOs

This is here I have the current todo list for this project.

## Weekly Tasking

Goal is to informatize a "system" that was literally placing dots on plasticized paper.

### Backend

- [ ] Tasks
    - [ ] Management (service)
        - [X] Add new tasks
        - [X] Edit tasks
        - [X] Remove tasks
        - [ ] Assign tasks
    - [ ] Endpoints
        - [X] Add a new task
        - [X] Edit a task
        - [X] Remove a task
        - [ ] Assign yourself task (check: no tasks assigned before today)
        - [ ] Get all tasks of a day (with optional person) (default today)
        - [ ] Get all tasks of a week (with optional person) (default today)
- [ ] People
    - [ ] Edit fields
    - [X] Get them
- [ ] Weeks
    - [X] Create a new week
    - [X] Get/Create current week id based on date sent
    - [ ] Garbage collect weeks with no tasks
- [ ] Categories
    - [ ] Add categories
    - [ ] Edit a category
    - [ ] Delete a category
    - [ ] Change a task's category

### Frontend

- [ ] iPad design
    - [ ] Board
        - [ ] Show people colors
        - [ ] Show tasks from current week, grouped by categories
        - [ ] Show next and early week
        - [ ] Be able to add new week
            - [ ] Show no task here by default? with option to show default table to then be able to add a task
            - [ ] The button "begin assigning" sends the current week data (weekId + startOfWeekDate + endOfWeekDate?)
        - [ ] Choose who is the current user
        - [ ] Be able to assign yourself tasks