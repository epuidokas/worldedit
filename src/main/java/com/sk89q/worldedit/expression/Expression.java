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

package com.sk89q.worldedit.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.expression.lexer.Lexer;
import com.sk89q.worldedit.expression.lexer.tokens.Token;
import com.sk89q.worldedit.expression.parser.Parser;
import com.sk89q.worldedit.expression.parser.ParserException;
import com.sk89q.worldedit.expression.runtime.EvaluationException;
import com.sk89q.worldedit.expression.runtime.Invokable;
import com.sk89q.worldedit.expression.runtime.Variable;

public class Expression {
    private final Map<String, Variable> variables = new HashMap<String, Variable>();
    private final String[] variableNames;
    private Invokable invokable;

    public static Expression compile(String expression, String... variableNames) throws ExpressionException {
        return new Expression(expression, variableNames);
    }

    private Expression(String expression, String... variableNames) throws ExpressionException {
        this(Lexer.tokenize(expression), variableNames);
    }

    private Expression(List<Token> tokenize, String... variableNames) throws ParserException {
        this.variableNames = variableNames;
        variables.put("e", new Variable(Math.E));
        variables.put("pi", new Variable(Math.PI));
        for (String variableName : variableNames) {
            variables.put(variableName, new Variable(0));
        }

        invokable = Parser.parse(tokenize, variables);
    }

    public double evaluate(double... values) throws Exception {
        for (int i = 0; i < values.length; ++i) {
            variables.get(variableNames[i]).value = values[i];
        }

        return invokable.invoke();
    }

    public void optimize() throws EvaluationException {
        invokable = invokable.optimize();
    }

    @Override
    public String toString() {
        return invokable.toString();
    }
}
