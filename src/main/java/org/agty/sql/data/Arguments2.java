package org.agty.sql.data;


import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Arguments for the query
 */
public class Arguments2 {

    /** Table */
    public String table;
    private String fields;
    private String actionField;
    private String primaryKey;
    private String where = "";
    private String having = "";
    private String groupBy = "";
    private String orderBy = "";
    private final Limit2 limit = new Limit2();
    private LinkedHashMap<String, Object> data = new LinkedHashMap<>();
    private final List<String> columns = new LinkedList<>();
    /**
     * Таблица.
     *
     * @param table имя таблицы.
     */
    public Arguments2 setTable(String table) {
        this.table = table;
        return this;
    }

    /**
     * Текущая таблица.
     * Пример: my_table, {my_table}.
     *
     * @return имя таблицы.
     */
    public String getTable() {
        return table;
    }

    /*@Override
    public String toString() {

        StringBuilder toString = new StringBuilder("Arguments {\n");

        //Основные параметры (только те, что заполнены)
        toString.append("\tParams: {\n");

        toString.append("\t\ttable = ");
        toString.append(table);
        toString.append("\n");

        toString.append("\t\tfields = ");
        toString.append(fields);
        toString.append("\n");

        toString.append("\t\tactionField = ");
        toString.append(actionField);
        toString.append("\n");

        toString.append("\t\tprimaryKey = ");
        toString.append(primaryKey);
        toString.append("\n");

        toString.append("\t\twhere = ");
        toString.append(where);
        toString.append("\n");

        toString.append("\t\thaving = ");
        toString.append(having);
        toString.append("\n");

        toString.append("\t\tgroupBy = ");
        toString.append(groupBy);
        toString.append("\n");

        toString.append("\t\t");
        toString.append("orderBy");
        toString.append(" = ");
        toString.append(orderBy);
        toString.append("\n");

        toString.append("\t\t");
        toString.append("limit");
        toString.append(" = ");
        toString.append(limit.getLimit());
        toString.append("\n");

        toString.append("\t\t");
        toString.append("offset");
        toString.append(" = ");
        toString.append(limit.getOffset());
        toString.append("\n");

        toString.append("\t}\n");

        //Данные
        toString.append("\tData: {\n");

        for (Map.Entry<String, Object> entry: data.entrySet()) {
            toString.append("\t\t");
            toString.append(entry.getKey());
            toString.append(" = ");
            toString.append(entry.getValue());
            toString.append("\n");
        }

        toString.append("\t}\n");
        toString.append("}\n");

        //Поля
        toString.append("\tColumns: {\n\t");
        toString.append(columns);
        toString.append("\t}\n");
        toString.append("}\n");

        return toString.toString();
    }*/
}
