# Development Databases

Scripts in this directory install or start local Docker containers for development and tests.

- `install-mysql.sh` creates or starts `agty-sql-mysql`
- `install-mariadb.sh` creates or starts `agty-sql-mariadb`
- `install-pgsql.sh` creates or starts `agty-sql-pgsql`
- `install-mssql.sh` creates or starts `agty-sql-mssql`
- `install-clickhouse.sh` creates or starts `agty-sql-clickhouse`
- `install-mongodb.sh` is an unrelated local helper; MongoDB is not a supported
  `org.agty.sql` driver and is not part of the test matrix.

The scripts use named Docker volumes for server databases.
The `databases/` directory is reserved for file-based databases such as SQLite and H2.

Set passwords in the environment before running a SQL installer. For example:

```bash
export AGTY_SQL_MYSQL_PASSWORD="$(openssl rand -base64 32)"
export AGTY_SQL_MYSQL_ROOT_PASSWORD="$(openssl rand -base64 32)"
./install/install-mysql.sh
```

Use the matching `AGTY_SQL_MARIADB_PASSWORD`,
`AGTY_SQL_MARIADB_ROOT_PASSWORD`, `AGTY_SQL_PGSQL_PASSWORD`, or
`AGTY_SQL_MSSQL_PASSWORD` variable for the other scripts. ClickHouse uses
`agty_sql` as the local test password by default; override it with
`AGTY_SQL_CLICKHOUSE_PASSWORD` when needed. Keep generated values in a local
secret store; do not commit them.

Behavior notes:

- database data is stored in named Docker volumes, so it survives container recreation;
- published ports bind to `127.0.0.1` by default; set `HOST_ADDRESS` explicitly
  only when remote access is required and protected;
- ClickHouse publishes HTTP/JDBC port `8123` as host port `28123` and native
  TCP port `9000` as host port `29000`;
- the ClickHouse installer uses `docker` directly when available and falls back
  to `sudo docker` otherwise;
- restart policies are not installed automatically for test containers;
- SQL image tags are pinned to explicit patch versions. Override the matching
  `*_IMAGE` variable only after reviewing the image update.
