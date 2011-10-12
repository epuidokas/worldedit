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

package com.sk89q.worldedit.expression.runtime;

public final class Operators {
    public static final Function getOperator(int position, String name, Invokable lhs, Invokable rhs) throws NoSuchMethodException {
        return new Function(position, Operators.class.getMethod(name, Invokable.class, Invokable.class), lhs, rhs);
    }
    public static final Function getOperator(int position, String name, Invokable argument) throws NoSuchMethodException {
        return new Function(position, Operators.class.getMethod(name, Invokable.class), argument);
    }

    public static final double add(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke()+rhs.invoke();
    }
    public static final double sub(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke()-rhs.invoke();
    }
    public static final double mul(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke()*rhs.invoke();
    }
    public static final double div(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke()/rhs.invoke();
    }
    public static final double mod(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke()%rhs.invoke();
    }
    public static final double pow(Invokable lhs, Invokable rhs) throws EvaluationException {
        return Math.pow(lhs.invoke(), rhs.invoke());
    }

    public static final double neg(Invokable x) throws EvaluationException {
        return -x.invoke();
    }
    public static final double not(Invokable x) throws EvaluationException {
        return x.invoke() > 0.0 ? 0.0 : 1.0;
    }
    public static final double inv(Invokable x) throws EvaluationException {
        return ~(long)x.invoke();
    }

    public static final double lth(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() < rhs.invoke() ? 1.0 : 0.0;
    }
    public static final double gth(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() > rhs.invoke() ? 1.0 : 0.0;
    }
    public static final double leq(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() <= rhs.invoke() ? 1.0 : 0.0;
    }
    public static final double geq(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() >= rhs.invoke() ? 1.0 : 0.0;
    }

    public static final double equ(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() == rhs.invoke() ? 1.0 : 0.0;
    }
    public static final double neq(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() != rhs.invoke() ? 1.0 : 0.0;
    }
    public static final double near(Invokable lhs, Invokable rhs) throws EvaluationException {
        return Math.abs(lhs.invoke() - rhs.invoke()) < 1e-7 ? 1.0 : 0.0;
    }

    public static final double or(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() > 0.0 || rhs.invoke() > 0.0 ? 1.0 : 0.0;
    }
    public static final double and(Invokable lhs, Invokable rhs) throws EvaluationException {
        return lhs.invoke() > 0.0 && rhs.invoke() > 0.0 ? 1.0 : 0.0;
    }

    public static final double shl(Invokable lhs, Invokable rhs) throws EvaluationException {
        return (long)lhs.invoke() << (long)rhs.invoke();
    }
    public static final double shr(Invokable lhs, Invokable rhs) throws EvaluationException {
        return (long)lhs.invoke() >> (long)rhs.invoke();
    }
}
