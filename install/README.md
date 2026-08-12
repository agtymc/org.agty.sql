# Development Databases

Scripts in this directory install or start local Docker containers for development and tests.

- `install-mysql.sh` creates or starts `agty-sql-mysql`
- `install-mariadb.sh` creates or starts `agty-sql-mariadb`
- `install-pgsql.sh` creates or starts `agty-sql-pgsql`
- `install-mssql.sh` creates or starts `agty-sql-mssql`
- `install-mongodb.sh` creates or starts `agty-sql-mongodb`

The scripts use named Docker volumes for server databases.
The `databases/` directory is reserved for file-based databases such as SQLite and H2.

Behavior notes:

- database data is stored in named Docker volumes, so it survives container recreation;
- containers are expected to be long-lived development services rather than
  one-shot temporary runs;
- install scripts create containers with `--restart unless-stopped`, so after
  creation they should come back automatically after Docker daemon restarts or
  host reboots unless they were explicitly stopped or removed.
- if a container already exists, rerunning the install script also reapplies
  `--restart unless-stopped` via `docker update`, so older containers get the
  persistent restart policy too.
