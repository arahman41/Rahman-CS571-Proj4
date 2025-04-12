package simplf;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {
    private Environment globalEnv = new Environment();

    // Core evaluation method
    public Object evaluate(Expr expr, Environment env) {
        if (expr instanceof Expr.Variable) {
            return visitVarExpr((Expr.Variable) expr, env);
        } else if (expr instanceof Expr.Assign) {
            return visitAssignExpr((Expr.Assign) expr, env);
        } else if (expr instanceof Expr.Literal) {
            return ((Expr.Literal) expr).val;
        } else if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            Object left = evaluate(binary.left, env);
            Object right = evaluate(binary.right, env);

            switch (binary.op.type) {
                case PLUS:
                    if (left instanceof Double && right instanceof Double) {
                        return (double)left + (double)right;
                    }
                    if (left instanceof String || right instanceof String) {
                        return left.toString() + right.toString();
                    }
                    throw new RuntimeError(binary.op, "Operands must be numbers or strings.");
                case MINUS:
                    return (double)left - (double)right;
                case STAR:
                    return (double)left * (double)right;
                case SLASH:
                    if ((double)right == 0) {
                        throw new RuntimeError(binary.op, "Division by zero.");
                    }
                    return (double)left / (double)right;
                default:
                    throw new RuntimeError(binary.op, "Unknown operator");
            }
        } else if (expr instanceof Expr.Unary) {
            Expr.Unary unary = (Expr.Unary) expr;
            Object right = evaluate(unary.right, env);
            switch (unary.op.type) {
                case MINUS:
                    return -(double)right;
                case BANG:
                    return !isTruthy(right);
                default:
                    throw new RuntimeError(unary.op, "Unknown operator");
            }
        } else if (expr instanceof Expr.Grouping) {
            return evaluate(((Expr.Grouping) expr).expression, env);
        }
        throw new RuntimeError(null, "Unknown expression type");
    }

    // Variable handling methods
    public Object visitVarExpr(Expr.Variable expr, Environment env) {
        return env.get(expr.name.lexeme);
    }

    public Void visitVarStmt(Stmt.Var stmt, Environment env) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer, env);
        }
        env.define(stmt.name.lexeme, value);
        return null;
    }

    public Object visitAssignExpr(Expr.Assign expr, Environment env) {
        Object value = evaluate(expr.value, env);
        env.assign(expr.name.lexeme, value);
        return value;
    }

    public Void visitPrintStmt(Stmt.Print stmt, Environment env) {
        Object value = evaluate(stmt.expr, env);
        System.out.println(stringify(value));
        return null;
    }

    // Helper methods
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    // Statement execution
    public void execute(Stmt stmt, Environment env) {
        if (stmt instanceof Stmt.Print) {
            visitPrintStmt((Stmt.Print) stmt, env);
        } else if (stmt instanceof Stmt.Var) {
            visitVarStmt((Stmt.Var) stmt, env);
        } else if (stmt instanceof Stmt.Expression) {
            evaluate(((Stmt.Expression) stmt).expr, env);
        }
        // Add other statement types as needed
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement, globalEnv);
            }
        } catch (RuntimeError error) {
            Simplf.runtimeError(error);
        }
    }
}