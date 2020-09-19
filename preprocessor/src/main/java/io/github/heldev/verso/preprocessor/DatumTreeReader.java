package io.github.heldev.verso.preprocessor;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.apache.thrift.protocol.TProtocolException.INVALID_DATA;
import static org.apache.thrift.protocol.TType.*;

public class DatumTreeReader {
    public Map<Short, Object> readStruct(TProtocol protocol) throws TException {
        var map = new HashMap<Short, Object>();
        protocol.readStructBegin();

        while(true) {
            var field = protocol.readFieldBegin();
            if (field.type != STOP) {
                map.put(field.id, readFieldContent(protocol, field.type));
                protocol.readFieldEnd();
            } else {
                break;
            }
        }

        protocol.readStructEnd();
        return map;
    }

    private Object readFieldContent(TProtocol protocol, byte type) throws TException {

        return switch (type) {
            case BYTE -> protocol.readByte();
            case I32 -> protocol.readI32();
            case STRING -> protocol.readString();
            case LIST -> readList(protocol);
            case STRUCT -> readStruct(protocol);
            case SET -> readSet(protocol);
            default -> throw new TProtocolException(INVALID_DATA, "Unrecognized type " + type);
        };
    }

    private Object readList(TProtocol protocol) throws TException {
        var tList = protocol.readListBegin();

        var data = new ArrayList<>(tList.size);
        for(int i = 0; i < tList.size; i++) {
            data.add(readFieldContent(protocol, tList.elemType));
        }

        protocol.readListEnd();
        return data;
    }

    private Object readSet(TProtocol protocol) throws TException {
        var set = protocol.readSetBegin();

        var data = new HashSet<>(set.size);
        for(int i = 0; i < set.size; i++) {
            data.add(readFieldContent(protocol, protocol.readFieldBegin().type));
        }

        protocol.readListEnd();
        return data;
    }

}
