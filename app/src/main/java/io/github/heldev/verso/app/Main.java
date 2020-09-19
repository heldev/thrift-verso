package io.github.heldev.verso.app;

import com.twitter.scrooge.frontend.Importer$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.app.thrift.BookDto;
import io.github.heldev.verso.app.thrift.Library;
import io.github.heldev.verso.preprocessor.Something;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import scala.collection.concurrent.TrieMap;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws TException {
        System.out.println(new Something().get());

        new Main().run();
    }

    private void run() throws TException {

//        standardServe();
//        standardClient();
//
//        readWriteDto();
        parseThrift();
    }

    private void standardClient() throws TException {
        TSocket trans = new TSocket("localhost", 8080);
        trans.open();
        var client = new Library.Client(new TBinaryProtocol(trans));
        System.out.println(client.finBooksByAuthor("some author"));
    }

    private void standardServe() throws TTransportException {
        var processor = new Library.Processor<>(new Library.Iface() {
            @Override
            public Set<BookDto> finBooksByAuthor(String author) throws TException {
                var book = new BookDto("finBooksByAuthor call", List.of(author));

                return Set.of(book);
            }

            @Override
            public BookDto findBooksByTitle(String title) throws TException {
                return new BookDto("findBooksByTitle " + title, List.of());
            }
        });

        new Thread(() -> {
            try {
                new TSimpleServer(new TServer.Args(new TServerSocket(8080)).processor(processor)).serve();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void readWriteDto() throws TException {
        var protocol = new TCompactProtocol(new TMemoryBuffer(1024));
        var bookDto = new BookDto("Code Complete 2", List.of("McConnell"));
        bookDto.write(protocol);
    }

    private void parseThrift() {
        ThriftParser parser = new ThriftParser(Importer$.MODULE$.apply(
                "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources"),
                true,
                false,
                false,
                new TrieMap<>(),
                Logger.getGlobal());

        System.out.println(parser.parseFile("/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources/book.thrift"));
    }

}
