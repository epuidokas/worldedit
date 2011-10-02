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

import java.lang.reflect.Method;

public final class Function implements Invokable {
    final Method method;
    final Object[] args;

    public Function(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    @Override
    public final double invoke() throws Exception {
        return (Double) method.invoke(null, args);
    }

    @Override
    public String toString() {
        String ret = method.getName()+"(";
        boolean first = true;
        for (Object obj : args) {
            if (!first)
                ret += ", ";
            first = false;
            ret += obj;
        }
        return ret+")";
    }

    @Override
    public char id() {
        return 'f';
    }
}
