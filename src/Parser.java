import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;
    private ValidationResult result;

    public SelectStatement parse(List<Token> tokens, ValidationResult result) {
        this.tokens = tokens;
        this.pos = 0;
        this.result = result;
        SelectStatement statement = new SelectStatement();
        expect(TokenType.SELECT, "SYNTACTIC_EXPECTED_SELECT");
        parseColumns(statement);
        expect(TokenType.FROM, "SYNTACTIC_EXPECTED_FROM");
        Token table = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_TABLE");
        if (table != null) statement.table = table.lexeme;

        if (match(TokenType.WHERE)) {
            parseWhere(statement);
        }

        if (check(TokenType.SEMICOLON)) advance();
        if (!check(TokenType.EOF)) {
            result.diagnostics.add(new Diagnostic("SYNTACTIC_UNEXPECTED_TOKEN", "Token inesperado: " + current().lexeme, current().span));
        }
        return statement;
    }

    private void parseColumns(SelectStatement statement) {
        if (match(TokenType.STAR)) {
            statement.columns.add("*");
            return;
        }
        Token first = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
        if (first != null) statement.columns.add(first.lexeme);
        while (match(TokenType.COMMA)) {
            Token next = expect(TokenType.IDENTIFIER, "SYNTACTIC_EXPECTED_COLUMN");
            if (next != null) statement.columns.add(next.lexeme);
        }
    }

    private void parseWhere(SelectStatement statement) {
        ConditionChain chain = new ConditionChain();
        WhereCondition first = parseWhereCondition();
        if (first == null) {
            skipWhereRemainder();
            return;
        }
        chain.conditions.add(first);

        while (check(TokenType.AND) || check(TokenType.OR)) {
            Token connector = advance();
            WhereCondition next = parseWhereCondition();
            if (next == null) {
                skipWhereRemainder();
                return;
            }
            chain.connectors.add(connector.type == TokenType.AND ? "AND" : "OR");
            chain.conditions.add(next);
        }

        statement.where = chain;
    }

    private WhereCondition parseWhereCondition() {
        Token column = expectWhereToken(TokenType.IDENTIFIER);
        if (column == null) return null;

        Token operator = null;
        if (isComparisonOperator(current().type)) {
            operator = advance();
        } else {
            addWhereOperandDiagnostic(current());
            return null;
        }

        Token literal = current();
        LiteralType literalType = literalType(literal.type);
        if (literalType == LiteralType.UNKNOWN) {
            addWhereOperandDiagnostic(literal);
            return null;
        }
        advance();

        return new WhereCondition(column.lexeme, operator.lexeme, literal.lexeme, literalType,
                                  column.span, operator.span, literal.span);
    }

    private Token expectWhereToken(TokenType type) {
        if (check(type)) return advance();
        addWhereOperandDiagnostic(current());
        return null;
    }

    private boolean isComparisonOperator(TokenType type) {
        return type == TokenType.EQUAL
            || type == TokenType.GREATER
            || type == TokenType.LESS
            || type == TokenType.GREATER_EQUAL
            || type == TokenType.LESS_EQUAL
            || type == TokenType.NOT_EQUAL;
    }

    private LiteralType literalType(TokenType type) {
        if (type == TokenType.NUMBER) return LiteralType.NUMBER;
        if (type == TokenType.STRING) return LiteralType.STRING;
        if (type == TokenType.TRUE || type == TokenType.FALSE) return LiteralType.BOOLEAN;
        return LiteralType.UNKNOWN;
    }

    private void addWhereOperandDiagnostic(Token token) {
        result.diagnostics.add(new Diagnostic(
            "SYNTACTIC_EXPECTED_WHERE_OPERAND",
            "Se esperaba operando de WHERE y se encontró " + token.type,
            token.span));
    }

    private void skipWhereRemainder() {
        while (!check(TokenType.EOF) && !check(TokenType.SEMICOLON)) advance();
    }

    private Token expect(TokenType type, String code) {
        if (check(type)) return advance();
        result.diagnostics.add(new Diagnostic(code, "Se esperaba " + type + " y se encontró " + current().type, current().span));
        return null;
    }

    private boolean match(TokenType type) { if (check(type)) { advance(); return true; } return false; }
    private boolean check(TokenType type) { return current().type == type; }
    private Token current() { return tokens.get(pos); }
    private Token advance() { return tokens.get(pos++); }
}
