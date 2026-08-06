package org.agty.sql;

import org.agty.sql.base.RowData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RowDataBooleanCoercionTest {

    @Test
    void returnsNullForMissingValue() {
        RowData row = new RowData();

        Assertions.assertNull(row.getBoolean("flag"));
    }

    @Test
    void supportsBooleanNumberStringAndCharacterValues() {
        RowData row = new RowData();
        row.setData("boolTrue", true);
        row.setData("boolFalse", false);
        row.setData("numberZero", 0);
        row.setData("numberOne", 1);
        row.setData("numberNegative", -7);
        row.setData("stringTrue", " true ");
        row.setData("stringFalse", "OFF");
        row.setData("stringYes", "YeS");
        row.setData("stringNo", "n");
        row.setData("charTrue", 'Y');
        row.setData("charFalse", '0');

        Assertions.assertTrue(row.getBoolean("boolTrue"));
        Assertions.assertFalse(row.getBoolean("boolFalse"));
        Assertions.assertFalse(row.getBoolean("numberZero"));
        Assertions.assertTrue(row.getBoolean("numberOne"));
        Assertions.assertTrue(row.getBoolean("numberNegative"));
        Assertions.assertTrue(row.getBoolean("stringTrue"));
        Assertions.assertFalse(row.getBoolean("stringFalse"));
        Assertions.assertTrue(row.getBoolean("stringYes"));
        Assertions.assertFalse(row.getBoolean("stringNo"));
        Assertions.assertTrue(row.getBoolean("charTrue"));
        Assertions.assertFalse(row.getBoolean("charFalse"));
    }

    @Test
    void resolvesKeysCaseInsensitively() {
        RowData row = new RowData();
        row.setData("IS_EXISTS", "t");

        Assertions.assertTrue(row.getBoolean("is_exists"));
    }

    @Test
    void rejectsUnsupportedBooleanRepresentations() {
        RowData row = new RowData();
        row.setData("invalidString", "maybe");
        row.setData("invalidCharacter", 'x');
        row.setData("invalidObject", new Object());

        Assertions.assertThrows(IllegalArgumentException.class, () -> row.getBoolean("invalidString"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> row.getBoolean("invalidCharacter"));
        Assertions.assertNull(row.getBoolean("invalidObject"));
    }
}
