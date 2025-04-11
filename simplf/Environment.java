package simplf;

class Environment {
    private AssocList assocList;
    private final Environment enclosing;

    Environment() {
        this(null);
    }

    Environment(Environment enclosing) {
        this.assocList = null;
        this.enclosing = enclosing;
    }

    Environment define(Token varToken, String name, Object value) {
        AssocList newAssocList = new AssocList(name, value, this.assocList);
        return new Environment(newAssocList, this.enclosing);
    }

    void assign(Token name, Object value) {
        AssocList current = this.assocList;
        while (current != null) {
            if (current.name.equals(name.lexeme)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object get(Token name) {
        AssocList current = this.assocList;
        while (current != null) {
            if (current.name.equals(name.lexeme)) {
                return current.value;
            }
            current = current.next;
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}