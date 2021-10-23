namespace java io.github.heldev.verso.app

struct BookDto {
    1: required string title
    2: list<set<list<string>>> authors = []
    3: optional Type type
    4: list<list<list<Publisher>>> publishers = [[], [[]]]
    40: required list<list<list<Publisher>>> publishers2 = [[[{"name": "Jo"}]]]
    5: map<set<i32>, list<string>> reviews
    6: optional MyUnion myUnion
}

struct RecursivePerson {
    1: string name
    2: RecursivePerson parent = {"name": "pname"}
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
    3: HList hlist
}

struct HList {
    1: optional HList tail
    2: string val
}

struct NeLi {
    1: list<list<list<set<string>>>> woow
    2: map<set<list<i32>>, set<list<BookDto>>> complexity
}
