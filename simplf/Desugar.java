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
        // Convert all parts to proper statements
        Object init = forStmt.init != null ? forStmt.init : createEmptyStatement();
        Expr cond = forStmt.cond != null ? forStmt.cond : new Expr.Literal(true);
        Object incr = forStmt.incr != null ? forStmt.incr : createEmptyStatement();

        // Build the while loop body
        List<Stmt> whileBodyStmts = new ArrayList<>();
        whileBodyStmts.add(forStmt.body);

        if (!isEmptyStatement((Stmt) incr)) {
            whileBodyStmts.add((Stmt) incr);
        }

        Stmt whileStmt = new Stmt.While(cond, new Stmt.Block(whileBodyStmts));

        // Build the final block
        List<Stmt> blockStmts = new ArrayList<>();
        if (!isEmptyStatement((Stmt) init)) {
            blockStmts.add((Stmt) init);
        }
        blockStmts.add(whileStmt);

        return new Stmt.Block(blockStmts);
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