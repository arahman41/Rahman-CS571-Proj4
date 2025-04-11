package simplf;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    private Environment globalEnv = new Environment();

    // Statement visitors
    public void visitVarStmt(Stmt.Var stmt, Environment env) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer, env);
        }
        env.define(stmt.name, value);
    }

    public void visitBlockStmt(Stmt.Block stmt, Environment env) {
        Environment blockEnv = new Environment(env);
        for (Stmt statement : stmt.statements) {
            visitStmt(statement, blockEnv);
        }
    }

    public void visitIfStmt(Stmt.If stmt, Environment env) {
        Object condition = evaluate(stmt.condition, env);
        if (isTruthy(condition)) {
            visitStmt(stmt.thenBranch, env);
        } else if (stmt.elseBranch != null) {
            visitStmt(stmt.elseBranch, env);
        }
    }

    public void visitWhileStmt(Stmt.While stmt, Environment env) {
        while (isTruthy(evaluate(stmt.condition, env))) {
            visitStmt(stmt.body, env);
        }
    }

    public void visitFunctionStmt(Stmt.Function stmt, Environment env) {
        SimplfFunction function = new SimplfFunction(stmt, env);
        env.define(stmt.name, function);
    }

    // Expression visitors
    public Object visitVarExpr(Expr.Variable expr, Environment env) {
        return env.get(expr.name);
    }

    public Object visitAssignExpr(Expr.Assign expr, Environment env) {
        Object value = evaluate(expr.value, env);
        env.assign(expr.name, value);
        return value;
    }

    public Object visitCallExpr(Expr.Call expr, Environment env) {
        Object callee = evaluate(expr.callee, env);
        if (!(callee instanceof SimplfFunction)) {
            throw new RuntimeException("Can only call functions.");
        }
        SimplfFunction function = (SimplfFunction) callee;
        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            arguments.add(evaluate(arg, env));
        }
        if (arguments.size() != function.arity()) {
            throw new RuntimeException("Expected " + function.arity() + " arguments but got " + arguments.size());
        }
        return function.call(this, arguments);
    }

    // Helper methods
    private void visitStmt(Stmt stmt, Environment env) {
        if (stmt instanceof Stmt.Var) {
            visitVarStmt((Stmt.Var) stmt, env);
        } else if (stmt instanceof Stmt.Block) {
            visitBlockStmt((Stmt.Block) stmt, env);
        } else if (stmt instanceof Stmt.If) {
            visitIfStmt((Stmt.If) stmt, env);
        } else if (stmt instanceof Stmt.While) {
            visitWhileStmt((Stmt.While) stmt, env);
        } else if (stmt instanceof Stmt.Function) {
            visitFunctionStmt((Stmt.Function) stmt, env);
        }
    }

    public Object evaluate(Expr expr, Environment env) {
        if (expr instanceof Expr.Variable) {
            return visitVarExpr((Expr.Variable) expr, env);
        } else if (expr instanceof Expr.Assign) {
            return visitAssignExpr((Expr.Assign) expr, env);
        } else if (expr instanceof Expr.Call) {
            return visitCallExpr((Expr.Call) expr, env);
        }
        return null; // Placeholder for other expressions
    }

    public void execute(Stmt stmt, Environment env) {
        visitStmt(stmt, env);
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return true;
    }
}