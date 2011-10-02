// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.expression.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sk89q.worldedit.expression.lexer.tokens.*;

public class Lexer {
    private final String expression;
    private int position = 0;

    private Lexer(String expression) {
        this.expression = expression;
    }

    public static final List<Token> tokenize(String expression) throws LexerException {
        return new Lexer(expression).tokenize();
    }

    private static final Pattern numberPattern = Pattern.compile("^([0-9]*(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)");
    private static final Pattern identifierPattern = Pattern.compile("^([A-Za-z][0-9A-Za-z_]*)");
    private final List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<Token>();

        do {
            skipWhitespace();
            if (position >= expression.length())
                break;

            final char ch = peek();
            switch (ch) {
            case '-':
            case '+':
            case '*':
            case '/':
            case '%':
            case '^':
                tokens.add(new OperatorToken(position++, ch));
                break;

            case ',':
            case '(':
            case ')':
                tokens.add(new CharacterToken(position++, ch));
                break;

            default:
                final Matcher numberMatcher = numberPattern.matcher(expression.substring(position));
                if (numberMatcher.lookingAt()) {
                    String numberPart = numberMatcher.group(1);
                    if (!numberPart.isEmpty()) {
                        try {
                            tokens.add(new NumberToken(position, Double.parseDouble(numberPart)));
                        } catch (NumberFormatException e) {
                            throw new LexerException(position, "Number parsing failed", e);
                        }

                        position += numberPart.length();
                        break;
                    }
                }

                final Matcher identifierMatcher = identifierPattern.matcher(expression.substring(position));
                if (identifierMatcher.lookingAt()) {
                    String identifierPart = identifierMatcher.group(1);
                    if (!identifierPart.isEmpty()) {
                        tokens.add(new IdentifierToken(position, identifierPart));

                        position += identifierPart.length();
                        break;
                    }
                }

                throw new LexerException(position, "Unknown character '"+ch+"'");
            }
        }
        while (position < expression.length());

        return tokens;
    }

    private char peek() {
        return expression.charAt(position);
    }

    private final void skipWhitespace() {
        while (position < expression.length() && Character.isWhitespace(peek())) {
            ++position;
        }
    }
}
