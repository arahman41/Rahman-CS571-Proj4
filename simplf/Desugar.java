package simplf;

import java.util.List;

public class Desugar {
    public List<Stmt> desugar(List<Stmt> statements) {
        // Implement desugaring for all statements (e.g., For → While)
        return statements.stream()
                .map(this::desugarStmt)
                .toList();
    }

    private Stmt desugarStmt(Stmt stmt) {
        if (stmt instanceof Stmt.For) {
            return visitForStmt((Stmt.For) stmt);
        }
        return stmt; // No desugaring needed
    }

    public Stmt visitForStmt(Stmt.For stmt) {
        // Convert Expr to Stmt.Expression for init/incr
        Stmt init = stmt.init != null ? new Stmt.Expression(stmt.init) : new Stmt.Expression(null);
        Expr cond = stmt.cond != null ? stmt.cond : new Expr.Literal(true);
        Stmt incr = stmt.incr != null ? new Stmt.Expression(stmt.incr) : null;

        // Build while loop
        Stmt whileBody = stmt.body;
        if (incr != null) {
            whileBody = new Stmt.Block(List.of(stmt.body, incr));
        }
        Stmt whileStmt = new Stmt.While(cond, whileBody);
        return new Stmt.Block(List.of(init, whileStmt));
    }
}