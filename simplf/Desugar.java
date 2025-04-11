package simplf;

import java.util.List;

public class Desugar {
    public Stmt visitForStmt(Stmt.For stmt) {
        Stmt init = stmt.initializer != null ? stmt.initializer : new Stmt.Expression(null);
        Expr cond = stmt.condition != null ? stmt.condition : new Expr.Literal(true);
        Stmt update = stmt.update != null ? new Stmt.Expression(stmt.update) : new Stmt.Expression(null);
        Stmt whileBody = new Stmt.Block(
                stmt.body instanceof Stmt.Block
                        ? ((Stmt.Block) stmt.body).statements
                        : List.of(stmt.body)
        );
        if (update instanceof Stmt.Expression && ((Stmt.Expression) update).expression != null) {
            whileBody = new Stmt.Block(
                    List.of(stmt.body, update)
            );
        }
        Stmt whileStmt = new Stmt.While(cond, whileBody);
        return new Stmt.Block(List.of(init, whileStmt));
    }
}