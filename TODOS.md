# Dev TODOs

This is here I have the current todo list for this project.

## Weekly Tasking

Goal is to informatize a "system" that was literally placing dots on plasticized paper.

### Backend

- [X] Tasks
    - [X] Management (service)
        - [X] Add new tasks
        - [X] Edit tasks
        - [X] Remove tasks
        - [X] Assign tasks
        - [X] Mark a task as completed
    - [X] Endpoints
        - [X] Add a new task
        - [X] Edit a task
        - [X] Remove a task
        - [X] Get all tasks
        - [X] Assign yourself task (check: no tasks assigned before today)
        - [X] Get all tasks of a day (with optional person) (default today)
        - [X] Get all tasks of a week (with optional person) (default today)
        - [X] Mark a task as completed
- [X] People
    - [X] Edit fields
    - [X] Get them
- [X] Weeks
    - [X] Create a new week
    - [X] Get/Create current week id based on date sent
    - [X] Garbage collect weeks with no tasks
- [X] Categories
    - [X] Get all categories
    - [X] Add categories
    - [X] Edit a category
    - [X] Delete a category
    - [X] Change a task's category

### Frontend

- [ ] iPad design
    - [ ] Main menu
    - [ ] Board
      - [ ] Be able to add new tasks
      - [ ] Be able to edit tasks, rename them and edit their category
      - [ ] Be able to remove tasks, or put them as HIDDEN in the week
      - [ ] Show people colors
      - [ ] Show tasks from current week, grouped by categories
      - [ ] Show next and early week
      - [ ] Be able to add new week
      - [ ] Show no task here by default? with option to show default table to then be able to add a task
      - [ ] The button "begin assigning" sends the current week data (weekId + startOfWeekDate +
      endOfWeekDate?)
      - [ ] Choose who is the current user
      - [ ] Be able to assign yourself tasks
      - [ ] Be able to mark a task as COMPLETED