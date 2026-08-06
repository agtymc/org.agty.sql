package org.agty.sql.model.utils;

public class ModelUtils {
    /**
     * Проверяет на простой тип данных.
     * Такой тип данных используется при сохранении в базу данных.
     *
     * @param type тип данных в строчном виде.
     * @return true если тип данных является простым.
     */
    public static boolean columnIsSimple(Class<?> type) {
        switch (type.getSimpleName()) {
            case "String", "Integer", "boolean",
                 "Character", "Double",
                 "Float", "Long",
                 "Date", "LocalTime",
                 "LocalDate", "LocalDateTime",
                 "Short", "Byte" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
