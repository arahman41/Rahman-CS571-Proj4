package simplf;

public class Environment {
    private AssocList bindings;
    private Environment parent;

    public Environment() {
        this.bindings = new AssocList();
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.bindings = new AssocList();
        this.parent = parent;
    }

    public void define(String name, Object value) {
        bindings.put(name, value);
    }

    public Object get(String name) {
        Object value = bindings.get(name);
        if (value != null) {
            return value;
        }
        if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    public void assign(String name, Object value) {
        if (bindings.contains(name)) {
            bindings.put(name, value);
            return;
        }
        if (parent != null) {
            parent.assign(name, value);
            return;
        }
        throw new RuntimeException("Undefined variable: " + name);
    }
}