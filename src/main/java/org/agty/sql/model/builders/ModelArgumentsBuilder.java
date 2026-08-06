package org.agty.sql.model.builders;

import org.agty.sql.data.Arguments;
import org.agty.sql.model.ModelAttributes;
import org.agty.sql.model.SaveModelMode;
import org.agty.sql.model.entity.ColumnEntity;

public class ModelArgumentsBuilder {
    private ModelAttributes<?> model;
    private final Arguments arguments = Arguments.builder();
    private SaveModelMode saveModelMode;
    private ColumnEntity idColumn;

    public static ModelArgumentsBuilder builder() {
        return new ModelArgumentsBuilder();
    }

    public ModelArgumentsBuilder model(ModelAttributes<?> model) {
        this.model = model;
        return this;
    }

    public ModelArgumentsBuilder saveModelMode(SaveModelMode saveModelMode) {
        this.saveModelMode = saveModelMode;
        return this;
    }

    public ModelArgumentsBuilder idColumn(ColumnEntity idColumn) {
        this.idColumn = idColumn;
        return this;
    }

    public Arguments build() {
        arguments.setTable(model.getModel().getTableName());

        if (model.getModel().getAttributesBuilder().hasWhereCondition()) {
            arguments.setWhere(model.getModel().getAttributesBuilder().getWhereCondition());
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
                    arguments.setWhere(
                            column.valueIsDigit() && !column.columnIsString() ? "[%s] = %d" : "[%s] = '%s'",
                            column.getColumn(),
                            column.valueIsDigit() && !column.columnIsString() ? column.getValue() : column.getStringValue()
                    );
                }

            } else {
                setDataToArguments(arguments, column);
            }
        }

        return arguments;
    }

    private void setDataToArguments(Arguments arguments, ColumnEntity column) {
        if (column.valueIsDigit() && !column.columnIsString()) {
            arguments.setData(
                    column.getColumn(),
                    column.getDigitValue()
            );
        } else {
            arguments.setData(
                    column.getColumn(),
                    column.getStringValue()
            );
        }
    }
}
