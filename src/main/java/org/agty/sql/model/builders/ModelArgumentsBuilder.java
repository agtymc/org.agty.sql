package org.agty.sql.model.builders;

import org.agty.sql.data.Arguments;
import org.agty.sql.data.SqlExpression;
import org.agty.sql.model.ModelAttributes;
import org.agty.sql.model.SaveModelMode;
import org.agty.sql.model.entity.ColumnEntity;
import org.agty.sql.support.SqlIdentifierValidator;

/**
 * Provides model arguments builder behavior.
 */
public class ModelArgumentsBuilder {
    /** Creates a new ModelArgumentsBuilder instance. */
    public ModelArgumentsBuilder() {
    }

    private ModelAttributes<?> model;
    private final Arguments arguments = Arguments.builder().useStatementPrepare(true);
    private SaveModelMode saveModelMode;
    private ColumnEntity idColumn;

    /**
     * Performs the builder operation.
     * @return operation result
     */
    public static ModelArgumentsBuilder builder() {
        return new ModelArgumentsBuilder();
    }

    /**
     * Performs the model operation.
     * @param model parameter value
     * @return operation result
     */
    public ModelArgumentsBuilder model(ModelAttributes<?> model) {
        this.model = model;
        return this;
    }

    /**
     * Performs the save model mode operation.
     * @param saveModelMode parameter value
     * @return operation result
     */
    public ModelArgumentsBuilder saveModelMode(SaveModelMode saveModelMode) {
        this.saveModelMode = saveModelMode;
        return this;
    }

    /**
     * Performs the id column operation.
     * @param idColumn parameter value
     * @return operation result
     */
    public ModelArgumentsBuilder idColumn(ColumnEntity idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    /**
     * Performs the build operation.
     * @return operation result
     */
    public Arguments build() {
        arguments.setTable(model.getModel().getTableName());

        if (model.getModel().getAttributesBuilder().hasWhereCondition()) {
            arguments.setWhere(SqlExpression.trusted(
                    model.getModel().getAttributesBuilder().getWhereCondition()
            ));
        }

        for (ColumnEntity column : model.getModel().getColumnsBuilder().getColumns()) {
            if (column.columnIsId() && column.getValue() == null) continue;

            if (column.columnIsId() && column.getValue() != null) {
                //In the mode SaveModelMode.WITH_CHECK If an ID field is not null, then on update it maybe troubles.
                //ID field saved in the idColumn object.
                if (saveModelMode == SaveModelMode.WITH_CHECK) {
                    idColumn.setColumn(column.getColumn());
                    idColumn.setValue(column.getValue());
                    idColumn.setColumnIsId(true);
                    idColumn.setForceId(column.isForceId());
                }

                if (column.isForceId()) {
                    setDataToArguments(arguments, column);
                }

                if (!arguments.hasWhere()) {
                    String columnName = SqlIdentifierValidator.requireColumn(
                            column.getColumn(),
                            "model ID field"
                    );
                    arguments.setWhere(
                            "[%s] = ?".formatted(columnName),
                            column.getValue()
                    );
                }

            } else {
                setDataToArguments(arguments, column);
            }
        }

        return arguments;
    }

    private void setDataToArguments(Arguments arguments, ColumnEntity column) {
        arguments.addData(column.getColumn(), column.getValue());
    }
}
