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
        env.define(stmt.name.lexeme, value);
    }

    public void visitBlockStmt(Stmt.Block stmt, Environment env) {
        Environment blockEnv = new Environment(env);
        executeBlock(stmt.statements, blockEnv);
    }

    public void visitIfStmt(Stmt.If stmt, Environment env) {
        Object condition = evaluate(stmt.cond, env); // Fixed: stmt.cond
        if (isTruthy(condition)) {
            visitStmt(stmt.thenBranch, env);
        } else if (stmt.elseBranch != null) {
            visitStmt(stmt.elseBranch, env);
        }
    }

    public void visitWhileStmt(Stmt.While stmt, Environment env) {
        while (isTruthy(evaluate(stmt.cond, env))) { // Fixed: stmt.cond
            visitStmt(stmt.body, env);
        }
    }

    public void visitFunctionStmt(Stmt.Function stmt, Environment env) {
        SimplfFunction function = new SimplfFunction(stmt, env);
        env.define(stmt.name.lexeme, function);
    }

    // Expression visitors
    public Object visitVarExpr(Expr.Variable expr, Environment env) {
        return env.get(expr.name.lexeme);
    }

    public Object visitAssignExpr(Expr.Assign expr, Environment env) {
        Object value = evaluate(expr.value, env);
        env.assign(expr.name.lexeme, value);
        return value;
    }

    public Object visitCallExpr(Expr.Call expr, Environment env) {
        Object callee = evaluate(expr.callee, env);
        if (!(callee instanceof SimplfCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions.");
        }
        SimplfCallable function = (SimplfCallable) callee;
        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.args) {
            arguments.add(evaluate(arg, env));
        }
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments but got " + arguments.size());
        }
        return function.call(this, arguments);
    }

    // Expression statement visitor
    public Void visitExprStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr, globalEnv);
        return null;
    }

    // Execute methods
    public void execute(Stmt stmt, Environment env) {
        visitStmt(stmt, env);
    }

    public void executeBlock(List<Stmt> statements, Environment env) {
        for (Stmt statement : statements) {
            execute(statement, env);
        }
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
        } else if (stmt instanceof Stmt.Expression) {
            visitExprStmt((Stmt.Expression) stmt);
        } else if (stmt instanceof Stmt.Print) {
            Object value = evaluate(((Stmt.Print) stmt).expr, env);
            System.out.println(value);
        }
    }

    public Object evaluate(Expr expr, Environment env) {
        if (expr instanceof Expr.Variable) {
            return visitVarExpr((Expr.Variable) expr, env);
        } else if (expr instanceof Expr.Assign) {
            return visitAssignExpr((Expr.Assign) expr, env);
        } else if (expr instanceof Expr.Call) {
            return visitCallExpr((Expr.Call) expr, env);
        } else if (expr instanceof Expr.Literal) {
            return ((Expr.Literal) expr).val;
        } else if (expr instanceof Expr.Grouping) {
            return evaluate(((Expr.Grouping) expr).expression, env);
        } else if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;
            Object right = evaluate(unary.right, env);
            switch (unary.op.type) {
                case MINUS: return -(double) right;
                case BANG: return !isTruthy(right);
            }
        } else if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            Object left = evaluate(binary.left, env);
            Object right = evaluate(binary.right, env);
            switch (binary.op.type) {
                case PLUS:
                    if (left instanceof Double && right instanceof Double) {
                        return (double) left + (double) right;
                    }
                    if (left instanceof String || right instanceof String) {
                        return left.toString() + right.toString();
                    }
                    throw new RuntimeError(binary.op, "Operands must be numbers or strings.");
                case MINUS: return (double) left - (double) right;
                case STAR: return (double) left * (double) right;
                case SLASH:
                    if ((double) right == 0) {
                        throw new RuntimeError(binary.op, "Division by zero.");
                    }
                    return (double) left / (double) right;
                case GREATER: return (double) left > (double) right;
                case GREATER_EQUAL: return (double) left >= (double) right;
                case LESS: return (double) left < (double) right;
                case LESS_EQUAL: return (double) left <= (double) right;
                case EQUAL_EQUAL: return isEqual(left, right);
                case BANG_EQUAL: return !isEqual(left, right);
            }
        } else if (expr instanceof Expr.Logical) {
            Expr.Logical logical = (Expr.Logical) expr;
            Object left = evaluate(logical.left, env);
            if (logical.op.type == TokenType.OR) {
                if (isTruthy(left)) return left;
            } else {
                if (!isTruthy(left)) return left;
            }
            return evaluate(logical.right, env);
        }
        return null;
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    public void interpret(List<Stmt> statements) {
        try {
            executeBlock(statements, globalEnv);
        } catch (RuntimeError error) {
            Simplf.runtimeError(error);
        }
    }
}