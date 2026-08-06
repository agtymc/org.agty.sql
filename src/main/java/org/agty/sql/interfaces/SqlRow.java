package org.agty.sql.interfaces;

import org.agty.sql.data.Arguments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * An interface for data rows
 */
public interface SqlRow {
    /**
     * Конвертирует и возвращает SqlRow из Arguments
     * @param arguments Arguments
     * @return SqlRow
     */
    SqlRow convertFromArguments(Arguments arguments);

    /**
     * Все данные являются строкой
     * @return SqlRo
     */
    SqlRow setValuesAsString(Boolean isString);
    boolean isDataStringified();

    /**
     * @deprecated use {@link #setValuesAsString(Boolean)}
     */
    @Deprecated
    SqlRow setDataIsString(Boolean isString);

    /**
     * @deprecated use {@link #isDataStringified()}
     */
    @Deprecated
    boolean dataIsString();

    /**
     * Добавить данные.
     *
     * @param key имя столбца/данных.
     * @param value значение типа Object.
     */
    SqlRow setData(String key, Object value);

    /**
     * Возвращает данные в виде объекта
     * @param key имя столбца/данных.
     * @return значение типа Object.
     */
    Object getObject(String key);

    /**
     * Возвращает строковое значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    String getString(String key);

    /**
     * Строковое значение с перекодировкой
     * @param key имя ключа.
     * @return данные.
     */
    String getEstring(String key);

    /**
     * Строковое значение с декодировкой
     * @param key имя ключа.
     * @return данные.
     */
    String getDstring(String key);

    /**
     * Возвращает цифровое значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Integer getInt(String key);

    /**
     * Возвращает цифровое значение Long.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Long getLong(String key);

    /**
     * Возвращает цифровое значение Long.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Double getDouble(String key);

    /**
     * Возвращает цифровое значение Float.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Float getFloat(String key);

    /**
     * Возвращает цифровое значение Short.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Short getShort(String key);

    /**
     * Возвращает символьное значение.
     *
     * @param key имя ключа.
     * @return данные.
     */
    Character getChar(String key);

    /**
     * Возвращает true если значение == true, иначе false;
     *
     * @param key имя ключа.
     * @return Boolean|null, true если утверждение истинно. Если значения нет, вернет null.
     */
    Boolean getBoolean(String key);

    /**
     * Возвращает дату в объекте Date
     *
     * @param key имя ключа.
     * @return дата
     */
    Date getDate(String key);
    /**
     * Возвращает дату в объекте LocalDate
     *
     * @param key имя ключа.
     * @return дата
     */
    LocalDate getLocalDate(String key);

    /**
     * Возвращает дату в объекте LocalDateTime
     *
     * @param key имя ключа.
     * @return дата
     */
    LocalDateTime getLocalDateTime(String key);

    /**
     * Возвращает время в объекте LocalTime
     *
     * @param key имя ключа.
     * @return дата
     */
    LocalTime getLocalTime(String key);

    /**
     * Возвращает год в объекте Date
     *
     * @param key имя ключа.
     * @return дата
     */
    Integer getYear(String key);

    /**
     * Возвращает форматированную дату
     *
     * @param key имя ключа.
     * @return дата
     */
    String getDateFormat(String key, String format);

    /**
     * Наличие объекта.
     *
     * @param key имя ключа.
     * @return true если данные есть.
     */
    boolean isSet(String key);

    /**
     * Проверяет пустой ли массив данных
     * @return bool
     */
    boolean isEmpty();

    /**
     * Проверяет не пустой ли массив данных
     * @return bool
     */
    boolean isNotEmpty();

    /**
     * @deprecated use {@link #isNotEmpty()}
     */
    @Deprecated
    boolean noEmpty();
}
