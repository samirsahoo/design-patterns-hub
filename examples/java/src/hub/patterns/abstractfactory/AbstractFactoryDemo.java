package hub.patterns.abstractfactory;

import java.util.List;
import java.util.stream.Collectors;

// region products
/** Abstract products: each SQL dialect must provide all three, and they must agree with each other. */
interface IdentifierQuoter {
    String quote(String identifier);
}

interface Paginator {
    String paginate(String select, int limit, int offset);
}

interface UpsertBuilder {
    String upsert(String table, List<String> columns, String keyColumn);
}
// endregion

// region factory
/** Abstract factory: one method per product in the family. */
interface SqlDialect {
    String name();
    IdentifierQuoter quoter();
    Paginator paginator();
    UpsertBuilder upsertBuilder();
}
// endregion

// region postgres
final class PostgresDialect implements SqlDialect {
    private final IdentifierQuoter quoter = id -> "\"" + id.replace("\"", "\"\"") + "\"";

    @Override public String name() { return "PostgreSQL"; }
    @Override public IdentifierQuoter quoter() { return quoter; }

    @Override public Paginator paginator() {
        return (select, limit, offset) -> select + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override public UpsertBuilder upsertBuilder() {
        return (table, columns, key) -> {
            String cols = columns.stream().map(quoter::quote).collect(Collectors.joining(", "));
            String params = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
            String updates = columns.stream().filter(c -> !c.equals(key))
                    .map(c -> quoter.quote(c) + " = EXCLUDED." + quoter.quote(c))
                    .collect(Collectors.joining(", "));
            return "INSERT INTO " + quoter.quote(table) + " (" + cols + ") VALUES (" + params + ")"
                    + " ON CONFLICT (" + quoter.quote(key) + ") DO UPDATE SET " + updates;
        };
    }
}
// endregion

// region mysql
final class MySqlDialect implements SqlDialect {
    private final IdentifierQuoter quoter = id -> "`" + id.replace("`", "``") + "`";

    @Override public String name() { return "MySQL"; }
    @Override public IdentifierQuoter quoter() { return quoter; }

    @Override public Paginator paginator() {
        return (select, limit, offset) -> select + " LIMIT " + offset + ", " + limit;
    }

    @Override public UpsertBuilder upsertBuilder() {
        return (table, columns, key) -> {
            String cols = columns.stream().map(quoter::quote).collect(Collectors.joining(", "));
            String params = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
            String updates = columns.stream().filter(c -> !c.equals(key))
                    .map(c -> quoter.quote(c) + " = new." + quoter.quote(c))
                    .collect(Collectors.joining(", "));
            return "INSERT INTO " + quoter.quote(table) + " (" + cols + ") VALUES (" + params + ")"
                    + " AS new ON DUPLICATE KEY UPDATE " + updates;
        };
    }
}
// endregion

// region client
/** Client: written once against the abstract factory, never names a concrete dialect. */
final class ProductSql {
    private final SqlDialect dialect;

    ProductSql(SqlDialect dialect) {
        this.dialect = dialect;
    }

    String pageOfProducts(int page, int size) {
        IdentifierQuoter q = dialect.quoter();
        String select = "SELECT " + q.quote("sku") + ", " + q.quote("name") + " FROM " + q.quote("product")
                + " ORDER BY " + q.quote("sku");
        return dialect.paginator().paginate(select, size, page * size);
    }

    String saveProduct() {
        return dialect.upsertBuilder().upsert("product", List.of("sku", "name", "price"), "sku");
    }
}
// endregion

public final class AbstractFactoryDemo {

    public static void main(String[] args) {
        for (SqlDialect dialect : List.of(new PostgresDialect(), new MySqlDialect())) {
            ProductSql sql = new ProductSql(dialect);
            System.out.println("== " + dialect.name());
            System.out.println(sql.pageOfProducts(2, 20));
            System.out.println(sql.saveProduct());
        }
    }
}
