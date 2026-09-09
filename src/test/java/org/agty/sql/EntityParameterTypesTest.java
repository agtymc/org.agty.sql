package org.agty.sql;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import org.agty.sql.data.Arguments;
import org.agty.sql.model.ModelAttributes;
import org.agty.sql.model.SaveModelMode;
import org.agty.sql.model.builders.ModelArgumentsBuilder;
import org.agty.sql.model.entity.ColumnEntity;
import org.agty.sql.support.PreparedStatementSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityParameterTypesTest {
    @Test void builderPreservesRuntimeTypesAndNull() {
        var entity = new EntityTypedSaveIntegrationTest.Sample();
        entity.updated_at = LocalDateTime.of(2026, 9, 9, 1, 2);
        entity.active = true;
        entity.amount = new BigDecimal("1.234567");
        entity.optional = null;
        Arguments args = ModelArgumentsBuilder.builder().model(new ModelAttributes<>(entity).build())
                .saveModelMode(SaveModelMode.WITH_CHECK).idColumn(new ColumnEntity()).build();
        assertSame(entity.updated_at, args.getData("updated_at"));
        assertSame(entity.active, args.getData("active"));
        assertSame(entity.amount, args.getData("amount"));
        assertTrue(args.getDataValues().contains(null));
    }

    @Test void stringsAreNeverInferredAsDates() throws Exception {
        var calls = new ArrayList<String>();
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, (proxy, method, args) -> {
                    calls.add(method.getName());
                    return null;
                });
        PreparedStatementSupport.bind(statement, Arrays.asList("2026-09-09 12:34:56", "not a date", null));
        assertEquals(Arrays.asList("setString", "setString", "setObject"), calls);
    }
}
