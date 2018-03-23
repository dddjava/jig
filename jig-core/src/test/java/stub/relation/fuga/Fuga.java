package stub.relation.fuga;

public class Fuga {
    Foo foo;

    Buz method() {
        return foo.toBar().toBuz();
    }
}
