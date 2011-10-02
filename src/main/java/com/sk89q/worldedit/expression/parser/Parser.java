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

package com.sk89q.worldedit.expression.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.expression.Identifiable;
import com.sk89q.worldedit.expression.lexer.IdentifierToken;
import com.sk89q.worldedit.expression.lexer.tokens.NumberToken;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.runtime.Constant;
import com.sk89q.worldedit.expression.runtime.Functions;
import com.sk89q.worldedit.expression.runtime.Invokable;
import com.sk89q.worldedit.expression.runtime.Operators;
import com.sk89q.worldedit.expression.runtime.Variable;

public class Parser {
    private final class NullToken extends Token {
        private NullToken(int position) {
            super(position);
        }
        public char id() {
            return '\0';
        }
        public String toString() {
            return "NullToken";
        }
    }

    private final List<Token> tokens;
    private int position = 0;
    private Map<String, Variable> variables;

    private Parser(List<Token> tokens, Map<String, Variable> variables) {
        this.tokens = tokens;
        this.variables = variables;
    }

    public static final Invokable parse(List<Token> tokens, Map<String, Variable> variables) throws ParserException {
        return new Parser(tokens, variables).parse();
    }

    private Invokable parse() throws ParserException {
        final Invokable ret = parseInternal();
        if (position < tokens.size()) {
            throw new ParserException(peek(), "Extra tokens at the end of the input");
        }
        return ret;
    }

    private final Invokable parseInternal() throws ParserException {
        LinkedList<Identifiable> halfProcessed = new LinkedList<Identifiable>();

        loop: while (position < tokens.size()) {
            final Token current = peek();
            switch (current.id()) {
            case '0':
                halfProcessed.add(new Constant(((NumberToken)current).value));
                ++position;
                break;

            case 'i':
                final IdentifierToken identifierToken = (IdentifierToken) current;
                ++position;
                final Token next = peek();
                if (next.id() == '(') {
                    halfProcessed.add(parseFunction((IdentifierToken) current));
                }
                else {
                    Variable variable = variables.get(identifierToken.value);
                    if (variable == null) {
                        throw new ParserException(current, "Variable not found");
                    }
                    halfProcessed.add(variable);
                }
                break;

            case '(':
                halfProcessed.add(parseBracket());
                break;

            case ',':
            case ')':
                break loop; // not quite clean. parseBracket should probably pass a limiting character
                //throw new ParserException(peek(), "Unmatched closing bracket");

            default:
                halfProcessed.add(current);
                ++position;
            }
        }

        return processLevel4(halfProcessed);
    }

    private static final Map<Character, String> level4Ops  = new HashMap<Character, String>();
    private static final Map<Character, String> level3Ops  = new HashMap<Character, String>();
    private static final Map<Character, String> expOp = Collections.singletonMap('^', "exp");
    static {
        level4Ops.put('+', "add");
        level4Ops.put('-', "sub");
        level3Ops.put('*', "mul");
        level3Ops.put('/', "div");
        level3Ops.put('%', "mod");
    }

    private Invokable processLevel4(LinkedList<Identifiable> input) throws ParserException {
        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
        String operator = process(input, lhs, rhs, level4Ops);

        Invokable rhsInvokable = processLevel3(rhs);
        if (operator == null) return rhsInvokable;

        Invokable lhsInvokable = processLevel4(lhs);

        try {
            return Operators.getOperator(operator, lhsInvokable, rhsInvokable);
        } catch (NoSuchMethodException e) {
            throw new ParserException((Token) input.get(lhs.size()), "Couldn't find operator '"+operator+"'");
        }
    }

    private Invokable processLevel3(LinkedList<Identifiable> input) throws ParserException {
        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
        String operator = process(input, lhs, rhs, level3Ops);

        Invokable rhsInvokable = processExp(rhs);
        if (operator == null) return rhsInvokable;

        Invokable lhsInvokable = processLevel3(lhs);

        try {
            return Operators.getOperator(operator, lhsInvokable, rhsInvokable);
        } catch (NoSuchMethodException e) {
            throw new ParserException((Token) input.get(lhs.size()), "Couldn't find operator '"+operator+"'");
        }
    }

    private Invokable processExp(LinkedList<Identifiable> input) throws ParserException {
        LinkedList<Identifiable> lhs = new LinkedList<Identifiable>();
        LinkedList<Identifiable> rhs = new LinkedList<Identifiable>();
        String operator = process(input, lhs, rhs, expOp);

        Invokable rhsInvokable = processLevel2(rhs);
        if (operator == null) return rhsInvokable;

        Invokable lhsInvokable = processExp(lhs);

        try {
            return Operators.getOperator(operator, lhsInvokable, rhsInvokable);
        } catch (NoSuchMethodException e) {
            throw new ParserException((Token) input.get(lhs.size()), "Couldn't find operator '"+operator+"'");
        }
    }

    private Invokable processLevel2(LinkedList<Identifiable> input) throws ParserException {
        if (input.isEmpty()) {
            throw new ParserException(new NullToken(-1), "Expression missing.");
        }

        Invokable ret = (Invokable) input.removeLast();
        while (!input.isEmpty()) {
            final Identifiable last = input.removeLast();
            switch (last.id()) {
            case '~':
                final Invokable arg = ret;
                ret = new Invokable() {
                    @Override
                    public char id() {
                        return 'n';
                    }

                    @Override
                    public double invoke() throws Exception {
                        return -arg.invoke();
                    }

                    @Override
                    public String toString() {
                        return "-"+arg;
                    }
                };
                break;

            default:
                if (last instanceof Token) {
                    throw new ParserException((Token)last, "Extra token found in expression: "+last);
                }
                else {
                    throw new ParserException(new NullToken(-1), "Extra expression found: "+last);
                }

            }
        }
        return ret;
    }

    private String process(LinkedList<Identifiable> input, LinkedList<Identifiable> lhs, LinkedList<Identifiable> rhs, Map<Character, String> ops) {
        String operator = null;

        for (Iterator<Identifiable> it = input.descendingIterator(); it.hasNext(); ) {
            Identifiable identifiable = it.next();
            if (operator == null) {
                if (rhs.isEmpty()) {
                    rhs.add(identifiable);
                    continue;
                }

                operator = ops.get(identifiable.id());

                if (operator == null) {
                    rhs.addFirst(identifiable);
                }
            }
            else {
                lhs.addFirst(identifiable);
            }
        }
        return operator;
    }

    private Token peek() {
        if (position >= tokens.size()) {
            return new NullToken(position);
        }

        return tokens.get(position);
    }

    private Identifiable parseFunction(IdentifierToken identifierToken) throws ParserException {
        if (peek().id() != '(')
            throw new ParserException(peek(), "Unexpected character in parseBracket");
        ++position;

        try {
            if (peek().id() == ')') {
                return Functions.getFunction(identifierToken.value);
            }

            List<Invokable> args = new ArrayList<Invokable>();

            loop: while (true) {
                args.add(parseInternal());

                final Token current = peek();
                ++position;

                switch (current.id()) {
                case ',':
                    continue;

                case ')':
                    break loop;

                default:
                    throw new ParserException(current, "Unmatched opening bracket");
                }
            }

            return Functions.getFunction(identifierToken.value, args.toArray(new Invokable[args.size()]));
        } catch (NoSuchMethodException e) {
            throw new ParserException(identifierToken, "Function not found", e);
        }
    }

    private final Invokable parseBracket() throws ParserException {
        if (peek().id() != '(')
            throw new ParserException(peek(), "Unexpected character in parseBracket");
        ++position;

        final Invokable ret = parseInternal();

        if (peek().id() != ')')
            throw new ParserException(peek(), "Unmatched opening bracket");
        ++position;

        return ret;
    }

    /*
	private Invokable processLevel1(LinkedList<Identifiable> input) throws ParserException {
		if (input.size() < 1){
			throw new ParserException(new NullToken(-1), "Expression missing.");
		}

		if (input.size() > 1) {
			final Identifiable extraElement = input.get(1);
			if (extraElement instanceof Token) {
				throw new ParserException((Token)extraElement, "Extra token found in expression: "+extraElement.toString());
			}
			else {
				throw new ParserException(new NullToken(-1), "Extra expression found: "+extraElement);
			}
		}
		return (Invokable) input.get(0);
	}

	private static final ListIterator<Identifiable> iteratorOf(List<Identifiable> list, String valid) {
		ListIterator<Identifiable> ret = null;
		for (ListIterator<Identifiable> it = list.listIterator(); it.hasNext(); ) {
			if (valid.indexOf(it.next().id()) != -1) {
				ret = it;
				break;
			}
		}
		return ret;
	}
     */
}
