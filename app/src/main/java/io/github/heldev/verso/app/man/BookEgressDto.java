package io.github.heldev.verso.app.man;

import io.github.heldev.verso.preprocessor.EgressField;
import io.github.heldev.verso.preprocessor.VersoEgress;

import java.util.List;

@VersoEgress(thriftFile = "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources/book.thrift", structName = "BookDto")
public class BookEgressDto {
    private final String title;
    private final List<String> authors;


    public BookEgressDto(List<String> authors, String title) {
        this.title = title;
        this.authors = authors;
    }

    @EgressField(id = 1)
    public String getTitle() {
        return title;
    }

    @EgressField(id = 2)
    public List<String> getAuthors() {
        return authors;
    }

    @Override
    public String toString() {
        return "BookIngressDto{" +
                "title='" + title + '\'' +
                ", authors=" + authors +
                '}';
    }
}
