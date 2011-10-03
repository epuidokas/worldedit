package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.runtime.Constant;
import com.sk89q.worldedit.expression.runtime.EvaluationException;
import com.sk89q.worldedit.expression.runtime.Invokable;

final class NegateOperator extends Invokable {
    private final Invokable arg;

    NegateOperator(Invokable arg) {
        this.arg = arg;
    }

    @Override
    public char id() {
        return 'n';
    }

    @Override
    public double invoke() throws EvaluationException {
        return -arg.invoke();
    }

    @Override
    public String toString() {
        return "-("+arg+")";
    }

    @Override
    public Invokable optimize() throws EvaluationException {
        final Invokable optimized = arg.optimize();

        if (optimized instanceof Constant) {
            return new Constant(invoke());
        } else {
            return new NegateOperator(optimized);
        }
    }
}