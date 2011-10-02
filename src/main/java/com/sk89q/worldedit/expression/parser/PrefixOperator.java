package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.Identifiable;
import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;

public class PrefixOperator implements Identifiable {
    char character;

    public PrefixOperator(OperatorToken operatorToken) {
        character = operatorToken.id();
    }

    @Override
    public char id() {
        return character;
    }
    
    @Override
    public String toString() {
        return "PrefixOperator("+character+")";
    }
}
