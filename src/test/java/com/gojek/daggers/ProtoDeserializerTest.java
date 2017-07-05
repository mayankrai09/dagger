package com.gojek.daggers;

import com.gojek.esb.booking.BookingLogKey;
import com.gojek.esb.participant.DriverLocation;
import com.gojek.esb.participant.ParticipantLogMessage;
import com.google.protobuf.Timestamp;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static com.gojek.esb.participant.ParticipantStatus.Enum.ACCEPTED;
import static com.gojek.esb.types.ServiceTypeProto.ServiceType.Enum.GO_AUTO;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ProtoDeserializerTest {

    @Mock
    ProtoType protoType;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void shouldGetTypeInfomationFromProtoType(){

        String[] expectedFieldNames = {"field1", "field2"};
        TypeInformation<?>[] expectedTypes = {Types.DOUBLE, Types.STRING};
        when(protoType.getFieldNames()).thenReturn(expectedFieldNames);
        when(protoType.getFieldTypes()).thenReturn(expectedTypes);

        TypeInformation<Row> actualType = new ProtoDeserializer(BookingLogKey.class.getTypeName(), protoType).getProducedType();
        assertTrue(actualType instanceof RowTypeInfo);

        RowTypeInfo actualRowType = (RowTypeInfo)actualType;
        assertArrayEquals(actualRowType.getFieldNames(), expectedFieldNames);
        assertArrayEquals(actualRowType.getFieldTypes(), expectedTypes);
    }

    @Test
    public void shouldAlwaysReturnFalseForEndOfStream(){
        assertFalse(new ProtoDeserializer(BookingLogKey.class.getTypeName(), protoType).isEndOfStream(null));
    }

    @Test
    public void shouldDeserializeProtoAsRowWithSimpelFields() throws IOException {

        String expectedOrderNumber = "111";
        int expectedIterationNumber = 10;
        byte[] protoBytes = ParticipantLogMessage.newBuilder().setOrderId(expectedOrderNumber)
                                                .setIterationNumber(expectedIterationNumber).build().toByteArray();
        ProtoDeserializer protoDeserializer = new ProtoDeserializer(ParticipantLogMessage.class.getTypeName(), protoType);

        Row row = protoDeserializer.deserialize(protoBytes);

        assertEquals(expectedOrderNumber, row.getField(participantLogFieldIndex("order_id")));
        assertEquals(expectedIterationNumber, row.getField(participantLogFieldIndex("iteration_number")));
    }

    @Test
    public void shouldDeserializeEnumAsString() throws IOException{

        byte[] protoBytes = ParticipantLogMessage.newBuilder().setServiceType(GO_AUTO).setStatus(ACCEPTED).build().toByteArray();
        ProtoDeserializer protoDeserializer = new ProtoDeserializer(ParticipantLogMessage.class.getTypeName(), protoType);

        Row row = protoDeserializer.deserialize(protoBytes);

        assertEquals(GO_AUTO.toString(), row.getField(participantLogFieldIndex("service_type")));
        assertEquals(ACCEPTED.toString(), row.getField(participantLogFieldIndex("status")));
    }

    @Test
    public void shouldDeserializeNestedMessagesAsSubRows() throws IOException{
        Timestamp expectedTimestamp = Timestamp.newBuilder().setSeconds(10l).setNanos(10).build();
        DriverLocation expectedDriverLocation = DriverLocation.newBuilder().setAccuracy(111l).setLatitude(222l).build();
        byte[] protoBytes = ParticipantLogMessage.newBuilder()
                                .setEventTimestamp(expectedTimestamp)
                                .setLocation(expectedDriverLocation).build().toByteArray();
        ProtoDeserializer protoDeserializer = new ProtoDeserializer(ParticipantLogMessage.class.getTypeName(), protoType);

        Row row = protoDeserializer.deserialize(protoBytes);

        Row eventTimestampRow = (Row) row.getField(participantLogFieldIndex("event_timestamp"));
        assertEquals(expectedTimestamp.getSeconds(), eventTimestampRow.getField(0));
        assertEquals(expectedTimestamp.getNanos(), eventTimestampRow.getField(1));

        Row locationRow = (Row) row.getField(participantLogFieldIndex("location"));
        assertEquals(expectedDriverLocation.getAccuracy(), locationRow.getField(3));
        assertEquals(expectedDriverLocation.getLatitude(), locationRow.getField(0));
    }

    private int participantLogFieldIndex(String order_id) {
        return ParticipantLogMessage.getDescriptor().findFieldByName(order_id).getIndex();
    }
}