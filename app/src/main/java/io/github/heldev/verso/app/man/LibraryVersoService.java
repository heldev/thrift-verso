package io.github.heldev.verso.app.man;

import io.github.heldev.verso.preprocessor.IngressField;
import io.github.heldev.verso.preprocessor.VersoMethod;
import io.github.heldev.verso.preprocessor.VersoServer;

import java.util.List;
import java.util.Map;
import java.util.Set;

@VersoServer(thriftFile = "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources/book.thrift", serviceName = "Library")
public class LibraryVersoService {

    @VersoMethod("finBooksByAuthor")
    public Set<BookEgressDto> find(@IngressField(id=2) String author) {
        return Set.of();
    }

    @VersoMethod("findBooksByTitle")
    public BookEgressDto getBook(@IngressField(id=3) String title) {
        return new BookEgressDto(List.of(Set.of(List.of("egress_author"))), "egress_" + title, Map.of());
    }
}
