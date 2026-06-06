import java.util.HashMap;
import java.util.Map;

public class SemanticValidator {
    private final Map<String, Map<String, LiteralType>> schema = new HashMap<String, Map<String, LiteralType>>();

    public SemanticValidator() {
        addTable("usuarios", new String[][]{{"nombre", "STRING"}, {"edad", "NUMBER"}, {"activo", "BOOLEAN"}});
        addTable("productos", new String[][]{{"nombre", "STRING"}, {"precio", "NUMBER"}});
    }

    private void addTable(String table, String[][] columns) {
        Map<String, LiteralType> cols = new HashMap<String, LiteralType>();
        for (int i = 0; i < columns.length; i++) cols.put(columns[i][0], LiteralType.valueOf(columns[i][1]));
        schema.put(table, cols);
    }

    public void validate(SelectStatement ast, ValidationResult result) {
        if (ast == null || ast.table == null) return;
        Map<String, LiteralType> table = schema.get(ast.table.toLowerCase());
        if (table == null) {
            result.diagnostics.add(new Diagnostic("SEMANTIC_UNKNOWN_TABLE", "Tabla no existe: " + ast.table, new SourceSpan(1, 1)));
            return;
        }
        for (int i = 0; i < ast.columns.size(); i++) {
            String col = ast.columns.get(i);
            if (!col.equals("*") && !table.containsKey(col.toLowerCase())) {
                result.diagnostics.add(new Diagnostic("SEMANTIC_UNKNOWN_COLUMN", "Columna no existe: " + col, new SourceSpan(1, 1)));
            }
        }
        if (ast.where != null) {
            validateWhere(ast.where, table, result);
        }
    }

    private void validateWhere(ConditionChain where, Map<String, LiteralType> table, ValidationResult result) {
        for (int i = 0; i < where.conditions.size(); i++) {
            WhereCondition condition = where.conditions.get(i);
            LiteralType columnType = table.get(condition.column.toLowerCase());
            if (columnType == null) {
                result.diagnostics.add(new Diagnostic(
                    "SEMANTIC_UNKNOWN_WHERE_COLUMN",
                    "Columna WHERE no existe: " + condition.column,
                    condition.columnSpan));
                continue;
            }

            result.traces.add("TRACE|WHERE_TYPE_CHECK|"
                + condition.columnSpan.format()
                + "|" + condition.column
                + "|" + condition.operator
                + "|" + condition.literalType);

            if (columnType != condition.literalType) {
                result.diagnostics.add(new Diagnostic(
                    "SEMANTIC_TYPE_MISMATCH",
                    "Tipo incompatible en WHERE: " + condition.column,
                    condition.literalSpan));
            }
        }
    }
}
