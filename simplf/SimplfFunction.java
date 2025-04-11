package simplf;

import java.util.List;

public class SimplfFunction {
    private final Stmt.Function declaration;
    private final Environment closure;

    public SimplfFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            env.define(declaration.parameters.get(i), arguments.get(i));
        }
        try {
            interpreter.execute(declaration.body, env);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    public int arity() {
        return declaration.parameters.size();
    }
}

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}