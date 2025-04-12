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

    private Stmt desugarForStmt(Stmt.For stmt) {
        Object init = stmt.init != null ? stmt.init : new Stmt.Expression(new Expr.Literal(null));
        Expr cond = stmt.cond != null ? stmt.cond : new Expr.Literal(true);
        Object incr = stmt.incr != null ? stmt.incr : new Stmt.Expression(new Expr.Literal(null));

        List<Stmt> bodyStatements = new ArrayList<>();
        bodyStatements.add(stmt.body);
        if (!isNullStatement((Stmt) incr)) {
            bodyStatements.add((Stmt) incr);
        }

        Stmt whileStmt = new Stmt.While(cond, new Stmt.Block(bodyStatements));

        List<Stmt> blockStatements = new ArrayList<>();
        if (!isNullStatement((Stmt) init)) {
            blockStatements.add((Stmt) init);
        }
        blockStatements.add(whileStmt);

        return new Stmt.Block(blockStatements);
    }

    private boolean isNullStatement(Stmt stmt) {
        return (stmt instanceof Stmt.Expression) &&
                (((Stmt.Expression) stmt).expr instanceof Expr.Literal) &&
                (((Expr.Literal) ((Stmt.Expression) stmt).expr).val == null);
    }
}