package simplf;

public class AssocList {
        final String name;
        Object value;
        final AssocList next;

        AssocList(String nameIn, Object valueIn, AssocList nextIn) {
            name = nameIn;
            value = valueIn;
            next = nextIn;
        }
    public Object get(String name) {
        if (name.equals(this.name)) return value;
        if (next == null) return null;
        return next.get(name);
    }

    public void put(String name, Object value) {
        if (name.equals(this.name)) {
            this.value = value;
            return;
        }
        if (next == null) {
            throw new RuntimeException("Variable not found: " + name);
        }
        next.put(name, value);
    }

    public boolean contains(String name) {
        if (name.equals(this.name)) return true;
        if (next == null) return false;
        return next.contains(name);
    }
}

