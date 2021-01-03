package io.github.heldev.verso.app.man;

import io.github.heldev.verso.interfaces.EgressField;
import io.github.heldev.verso.interfaces.VersoEgress;

import java.util.List;
import java.util.Map;
import java.util.Set;

@VersoEgress(thriftFile = "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources/book.thrift", structName = "BookDto")
public class BookEgressDto {
	private final String title;
	private final List<Set<List<String>>> authors;
	private final Map<Set<Integer>, List<String>> reviewers;


	public BookEgressDto(List<Set<List<String>>> authors, String title, Map<Set<Integer>, List<String>> reviewers) {
		this.title = title;
		this.authors = authors;
		this.reviewers = reviewers;
	}

	@EgressField(id = 1)
	public String getTitle() {
		return title;
	}

	@EgressField(id = 2)
	public List<Set<List<String>>> getAuthors() {
		return authors;
	}

	@EgressField(id = 5)
	public Map<Set<Integer>, List<String>> getReviewers() {
		return reviewers;
	}

	@Override
	public String toString() {
		return "BookIngressDto{" +
				"title='" + title + '\'' +
				", authors=" + authors +
				'}';
	}

}
