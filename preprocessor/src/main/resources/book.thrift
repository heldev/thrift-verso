namespace java io.github.heldev.verso.app

struct BookDto {
    1: required string title
    2: list<string> authors = []
    3: optional Type type
    4: list<Publisher> publishers
    5: map<i32, string> reviews = {}
}

enum Type {
    Book,
    Journal
}

struct Publisher {
    1: required i32 id
    2: string name = "noname"
}

service Library {
    set<BookDto> finBooksByAuthor(2: string author)
    BookDto findBooksByTitle(3: string title)
}

union MyUnion {
    1: MyUnion myUnion
    2: string myEnd
}
