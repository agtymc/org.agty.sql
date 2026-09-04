package org.agty.sql.model;

import org.agty.sql.AgtySQL;
import org.agty.sql.data.Arguments;
import org.agty.sql.interfaces.SqlRow;
import org.agty.sql.model.builders.ModelArgumentsBuilder;
import org.agty.sql.model.builders.ObjectBuilder;
import org.agty.sql.model.entity.ColumnEntity;
import org.agty.sql.support.RowFactory;

import java.lang.reflect.InvocationTargetException;

public class ModelControl {
    private final ColumnEntity idColumn = new ColumnEntity();

    public static ModelControl newModelControl() {
        return new ModelControl();
    }

    /**
     * The main method for save an entity
     *
     * @param object A model of entity
     * @param agtySQL AgtySQL
     * @param saveModelMode SaveModelMode (WITH_CHECK, WITHOUT_CHECK)
     * @param returnFields Return fields
     * @return New instance of an Entity
     * @param <T> Type of Entity
     */
    @SuppressWarnings("unchecked")
    public <T> T save(T object, AgtySQL agtySQL, SaveModelMode saveModelMode, String returnFields) {

        SqlRow sqlRow = saveModel(object, agtySQL, saveModelMode, returnFields);

        if (sqlRow.isEmpty()) return null;

        return (T) ObjectBuilder.builder()
                .object(object)
                .sqlRow(sqlRow)
                .build();
    }

    /**
     * The main method for save an entity and get one field.
     *
     * @param object A model of entity
     * @param agtySQL AgtySQL
     * @param saveModelMode SaveModelMode (WITH_CHECK, WITHOUT_CHECK)
     * @param returnField Return field
     * @return Object
     * @param <T> Type of Entity
     * @throws IllegalAccessException when reflected access is denied
     * @throws InvocationTargetException when a reflected method fails
     */
    public <T> Object saveAndGetField(T object, AgtySQL agtySQL, SaveModelMode saveModelMode, String returnField) throws IllegalAccessException, InvocationTargetException {
        SqlRow sqlRow = saveModel(object, agtySQL, saveModelMode, returnField);
        if (sqlRow.isEmpty()) return null;
        return sqlRow.getObject(returnField);
    }

    /**
     * Save a model (entity)
     *
     * @param object A model of entity
     * @param agtySQL AgtySQL
     * @param saveModelMode SaveModelMode
     * @param returnFields Return fields
     * @return A SqlRow object (maybe empty)
     * @param <T> An @Entity class
     */
    public <T> SqlRow saveModel(T object, AgtySQL agtySQL, SaveModelMode saveModelMode, String returnFields) {
        ModelAttributes<?> model = new ModelAttributes<>(object).build();

        Arguments arguments = ModelArgumentsBuilder.builder()
                .model(model)
                .saveModelMode(saveModelMode)
                .idColumn(idColumn)
                .build();

        if (saveModelMode == SaveModelMode.INSERT_ONLY) {
            return agtySQL.insertAndGet(arguments, returnFields);
        }
        else if (saveModelMode == SaveModelMode.INSERT_ONLY_WITH_CHECK) {
            if (arguments.hasWhere() && agtySQL.rowIsExists(arguments)) {
                return RowFactory.emptyRow();
            }
            if (agtySQL.hasErrors()) return RowFactory.emptyRow();
            return agtySQL.insertAndGet(arguments, returnFields);
        } else if (saveModelMode == SaveModelMode.UPDATE_ONLY) {
            if (!arguments.hasWhere()) {
                return RowFactory.emptyRow();
            }
            return agtySQL.updateAndGet(arguments, returnFields);
        } else if (saveModelMode == SaveModelMode.SAVE_OR_SKIP) {
            if (arguments.hasWhere() && agtySQL.rowIsExists(arguments)) {
                return RowFactory.emptyRow();
            }
            return agtySQL.insertAndGet(arguments, returnFields);
        }
        /*
        //По сути обновить с проверкой это лишнее действие. Так как обновление и есть проверка.
        //Не понятно, что потом возвращать
        else if (saveModelMode == SaveModelMode.UPDATE_ONLY_WITH_CHECK) {
            if (!arguments.hasWhere()) {
                return RowFactory.emptyRow();
            }

            if (agtySQL.rowIsExists(arguments)) {
                return RowFactory.newSqlRow().convertFromArguments(arguments);
            }

            return agtySQL.updateAndGet(arguments, returnFields);
        }*/
        else if (saveModelMode == SaveModelMode.WITH_CHECK) {
            return saveWithCheck(agtySQL, arguments, returnFields);
        } else {
            return saveWithoutCheck(agtySQL, arguments, returnFields);
        }
    }

    /**
     * Insert or update a row of data.
     * Before update row will check into a database.
     *
     * @param agtySQL AgtySQL
     * @param arguments Arguments
     * @param returnFields Return fields
     * @return SqlRow data (maybe empty)
     */
    private SqlRow saveWithCheck(AgtySQL agtySQL, Arguments arguments, String returnFields) {

        //If an ID field is not null, then on update it maybe troubles. ID field saved in the idColumn object.
        if (arguments.hasWhere() && agtySQL.rowIsExists(arguments)) {
            return agtySQL.updateAndGet(arguments, returnFields);
        }

        if (!agtySQL.hasErrors()) {
            if (idColumn.columnIsExist()) {
                if (idColumn.valueIsDigit()) {
                    arguments.addData(
                            idColumn.getColumn(),
                            idColumn.getDigitValue()
                    );
                } else {
                    arguments.addData(
                            idColumn.getColumn(),
                            idColumn.getStringValue()
                    );
                }
            }
            return agtySQL.insertAndGet(arguments, returnFields);
        }

        return RowFactory.emptyRow();
    }

    /**
     * Insert or update a row of data.
     * Before update row will check into a database.
     *
     * @param agtySQL AgtySQL
     * @param arguments Arguments
     * @param returnFields Return fields
     * @return SqlRow data (maybe empty)
     */
    private SqlRow saveWithoutCheck(AgtySQL agtySQL, Arguments arguments, String returnFields) {
        if (arguments.hasWhere()) {
            return agtySQL.updateAndGet(arguments, returnFields);
        } else {
            return agtySQL.insertAndGet(arguments, returnFields);
        }
    }

    /**
     * Fetch data from DB and convert into an entity
     * @param agtySQL AgtySQL
     * @param arguments Arguments
     * @param object Entity
     * @return Entity
     * @param <T> Entity
     */
    @SuppressWarnings("unchecked")
    public <T> T fetchEntity(AgtySQL agtySQL, Arguments arguments, T object) {
        SqlRow sqlRow = agtySQL.fetch(arguments);

        if (sqlRow.isEmpty()) return null;

        return (T) ObjectBuilder.builder()
                .object(object)
                .sqlRow(sqlRow)
                .build();
    }

    /**
     * Fetch data from DB and convert into an entity
     * @param agtySQL AgtySQL
     * @param arguments Arguments
     * @param clazz Entity.class
     * @return Entity
     * @param <T> Entity
     */
    @SuppressWarnings("unchecked")
    public <T> T fetchEntity(AgtySQL agtySQL, Arguments arguments, Class<?> clazz) {
        SqlRow sqlRow = agtySQL.fetch(arguments);

        if (sqlRow.isEmpty()) return null;

        return (T) ObjectBuilder.builder()
                .clazz(clazz)
                .sqlRow(sqlRow)
                .build();
    }
}
