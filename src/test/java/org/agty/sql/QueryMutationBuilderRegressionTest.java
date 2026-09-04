package org.agty.sql;

import org.agty.sql.sqlbuilder.QueryDeleteBuilder;
import org.agty.sql.sqlbuilder.QueryUpdateBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QueryMutationBuilderRegressionTest {

    @Test
    void updateUsesOffsetValueInsteadOfLimit() {
        String query = new QueryUpdateBuilder()
                .setTable("users")
                .setUpdateData("active=1")
                .setWhere("role='member'")
                .setOrderBy("id")
                .setLimit(10)
                .setOffset(25)
                .build();

        Assertions.assertEquals(
                "UPDATE users SET active=1 WHERE role='member' ORDER BY id LIMIT 10 OFFSET 25",
                query
        );
    }

    @Test
    void deleteUsesOffsetValueInsteadOfLimit() {
        String query = new QueryDeleteBuilder()
                .setTable("users")
                .setWhere("active=0")
                .setOrderBy("id")
                .setLimit(10)
                .setOffset(25)
                .build();

        Assertions.assertEquals(
                "DELETE FROM users WHERE active=0 ORDER BY id LIMIT 10 OFFSET 25",
                query
        );
    }
}
