package com.gojek.daggers.postProcessors.external;

import com.gojek.daggers.core.StencilClientOrchestrator;
import com.gojek.daggers.postProcessors.common.ColumnNameManager;
import com.google.gson.Gson;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import static com.gojek.daggers.utils.Constants.*;

public class SchemaConfig implements Serializable {
    private final Configuration configuration;
    private final StencilClientOrchestrator stencilClientOrchestrator;
    private ColumnNameManager columnNameManager;
    private String[] inputProtoClasses;
    private String outputProtoClassName;

    public SchemaConfig(Configuration configuration, StencilClientOrchestrator stencilClientOrchestrator, ColumnNameManager columnNameManager) {
        this.configuration = configuration;
        this.stencilClientOrchestrator = stencilClientOrchestrator;
        this.columnNameManager = columnNameManager;
        this.inputProtoClasses = getMessageProtoClasses();
        this.outputProtoClassName = configuration.getString(OUTPUT_PROTO_MESSAGE, "");
    }

    public StencilClientOrchestrator getStencilClientOrchestrator() {
        return stencilClientOrchestrator;
    }

    public ColumnNameManager getColumnNameManager() {
        return columnNameManager;
    }

    public String[] getInputProtoClasses() {
        return inputProtoClasses;
    }

    public String getOutputProtoClassName() {
        return outputProtoClassName;
    }

    private String[] getMessageProtoClasses() {
        String jsonArrayString = configuration.getString(INPUT_STREAMS, "");
        Map[] streamsConfig = new Gson().fromJson(jsonArrayString, Map[].class);
        ArrayList<String> protoClasses = new ArrayList<>();
        for (Map individualStreamConfig : streamsConfig) {
            protoClasses.add((String) individualStreamConfig.get(STREAM_PROTO_CLASS_NAME));
        }
        return protoClasses.toArray(new String[0]);
    }
}