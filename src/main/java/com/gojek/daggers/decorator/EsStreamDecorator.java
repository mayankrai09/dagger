package com.gojek.daggers.decorator;

import com.gojek.daggers.async.connector.ESAsyncConnector;
import com.gojek.de.stencil.StencilClient;
import com.timgroup.statsd.StatsDClient;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.types.Row;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EsStreamDecorator implements StreamDecorator {
    private Map<String, String> configuration;
//    private StatsDClient statsDClient;
    private StencilClient stencilClient;
    private Integer asyncIOCapacity;
    private Integer fieldIndex;

    public EsStreamDecorator(Map<String, String> configuration, StencilClient stencilClient, Integer asyncIOCapacity, Integer fieldIndex) {
        this.configuration = configuration;
//        this.statsDClient = statsDClient;
        this.stencilClient = stencilClient;
        this.asyncIOCapacity = asyncIOCapacity;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public Boolean canDecorate() {
        String source = configuration.get("source");
        return source.equals("es");
    }

    @Override
    public DataStream<Row> decorate(DataStream<Row> inputStream) {
        if (!canDecorate())
            return inputStream;
        Integer streamTimeout = getIntegerConfig(configuration, "stream_timeout");
        ESAsyncConnector esConnector = new ESAsyncConnector(fieldIndex, configuration, stencilClient);
        return AsyncDataStream.orderedWait(inputStream, esConnector, streamTimeout, TimeUnit.MILLISECONDS, asyncIOCapacity);
    }

    private Integer getIntegerConfig(Map<String, String> fieldConfiguration, String key) {
        return Integer.valueOf(fieldConfiguration.get(key));
    }
}