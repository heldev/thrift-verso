package io.github.heldev.verso.app.man;

import io.github.heldev.verso.annotation.IngressField;
import io.github.heldev.verso.annotation.VersoIngress;

import java.util.List;
import java.util.Optional;

@VersoIngress(thriftFile = "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources/book.thrift", structName = "BookDto")
public class BookIngressDto {
	private final Optional<String> title;
	private final List<String> authors;

//    publc BookIngressDto(@VersoField(id = 1) String title) {
//        this(title, List.of());
//    }

	public BookIngressDto(@IngressField(id = 2) List<String> authors, @IngressField(id = 1) Optional<String> title) {
		this.title = title;
		this.authors = authors;
	}

	public Optional<String> getTitle() {
		return title;
	}

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
