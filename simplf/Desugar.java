package simplf;

import java.util.ArrayList;
import java.util.List;

public class Desugar {
    public List<Stmt> desugar(List<Stmt> statements) {
        List<Stmt> desugared = new ArrayList<>();
        for (Stmt stmt : statements) {
            desugared.add(desugarStmt(stmt));
        }
        return desugared;
    }

    private Stmt desugarStmt(Stmt stmt) {
        if (stmt instanceof Stmt.For) {
            return desugarForStmt((Stmt.For) stmt);
        }
        return stmt;
    }

    private Stmt desugarForStmt(Stmt.For forStmt) {
        // Convert initialization to statement (wrap in Expression if needed)
        Stmt init = forStmt.init != null ?
                asStatement(forStmt.init) :
                createEmptyStatement();

        // Handle condition (default to true if not specified)
        Expr cond = forStmt.cond != null ?
                forStmt.cond :
                new Expr.Literal(true);

        // Convert increment to statement (wrap in Expression if needed)
        Stmt incr = forStmt.incr != null ?
                asStatement(forStmt.incr) :
                createEmptyStatement();

        // Build the while loop body
        List<Stmt> bodyStatements = new ArrayList<>();
        bodyStatements.add(forStmt.body);

        if (!isEmptyStatement(incr)) {
            bodyStatements.add(incr);
        }

        Stmt whileStmt = new Stmt.While(cond, new Stmt.Block(bodyStatements));

        // Combine initialization and while loop
        List<Stmt> blockStatements = new ArrayList<>();
        if (!isEmptyStatement(init)) {
            blockStatements.add(init);
        }
        blockStatements.add(whileStmt);

        return new Stmt.Block(blockStatements);
    }

    // Helper to convert Expr to Stmt.Expression
    private Stmt asStatement(Stmt stmt) {
        // If it's already a statement, return it directly
        return stmt;
    }

    private Stmt asStatement(Expr expr) {
        // Wrap expression in Stmt.Expression
        return new Stmt.Expression(expr);
    }

    private Stmt createEmptyStatement() {
        return new Stmt.Expression(new Expr.Literal(null));
    }

    private boolean isEmptyStatement(Stmt stmt) {
        if (!(stmt instanceof Stmt.Expression)) {
            return false;
        }
        Expr expr = ((Stmt.Expression) stmt).expr;
        return expr instanceof Expr.Literal && ((Expr.Literal) expr).val == null;
    }
}