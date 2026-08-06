# 06. Metadata And Helper Operations

In addition to CRUD, the library supports a number of service operations.

### Check table existence

```java
boolean exists = sql.tableIsExists(
        Arguments.builder().setTable("{users}")
);
```

### Truncate

```java
sql.truncate(
        Arguments.builder().setTable("{users}")
);
```

### Drop table / drop column

```java
sql.dropTable(
        Arguments.builder().setTable("{old_users}")
);
```

```java
sql.dropColumn(
        Arguments.builder()
                .setTable("{users}")
                .setActionField("obsolete_column")
);
```

### Min / max / first / last

```java
Long minId = sql.min(
        Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
);
```

```java
Long maxId = sql.max(
        Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
);
```

```java
SqlRow first = sql.getFirstRow(
        Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
);
```

```java
SqlRow last = sql.getLastRow(
        Arguments.builder()
                .setTable("{users}")
                .setActionField("id")
);
```
