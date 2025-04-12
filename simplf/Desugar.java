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
        Object init = stmt.init != null ? stmt.init : new Stmt.Expression(null);
        Expr cond = stmt.cond != null ? stmt.cond : new Expr.Literal(true);
        Stmt update = stmt.incr != null ? new Stmt.Expression(stmt.incr) : new Stmt.Expression(null);

        List<Stmt> bodyStatements = new ArrayList<>();
        bodyStatements.add(stmt.body);
        bodyStatements.add(update);

        Stmt whileBody = new Stmt.Block(bodyStatements);
        Stmt whileStmt = new Stmt.While(cond, whileBody);

        List<Stmt> blockStatements = new ArrayList<>();
        blockStatements.add((Stmt) init);
        blockStatements.add(whileStmt);

        return new Stmt.Block(blockStatements);
    }
}