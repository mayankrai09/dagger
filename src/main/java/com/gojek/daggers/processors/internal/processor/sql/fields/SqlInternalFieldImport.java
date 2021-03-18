package com.gojek.daggers.processors.internal.processor.sql.fields;

import com.gojek.daggers.processors.ColumnNameManager;
import com.gojek.daggers.processors.common.RowManager;
import com.gojek.daggers.processors.internal.InternalSourceConfig;
import com.gojek.daggers.processors.internal.processor.sql.SqlConfigTypePathParser;
import com.gojek.daggers.processors.internal.processor.sql.SqlInternalFieldConfig;

public class SqlInternalFieldImport implements SqlInternalFieldConfig {

    private ColumnNameManager columnNameManager;
    private SqlConfigTypePathParser sqlPathParser;
    private InternalSourceConfig internalSourceConfig;

    public SqlInternalFieldImport(ColumnNameManager columnNameManager, SqlConfigTypePathParser sqlPathParser, InternalSourceConfig internalSourceConfig) {
        this.columnNameManager = columnNameManager;
        this.sqlPathParser = sqlPathParser;
        this.internalSourceConfig = internalSourceConfig;
    }

    @Override
    public void processInputColumns(RowManager rowManager) {
        int outputFieldIndex = columnNameManager.getOutputIndex(internalSourceConfig.getOutputField());
        if (outputFieldIndex != -1) {
            Object inputData = sqlPathParser.getData(rowManager);
            rowManager.setInOutput(outputFieldIndex, inputData);
        }
    }
}