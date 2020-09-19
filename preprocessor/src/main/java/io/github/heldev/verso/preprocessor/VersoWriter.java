package io.github.heldev.verso.preprocessor;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

public interface VersoWriter<T> {
    void write(T object, TProtocol protocol) throws TException;
}
