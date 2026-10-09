/*
 * Copyright (C) 2026 Leonardo Ricci Mingani (NeekoKun)
 *
 * This file is part of IdS-proj.
 *
 * IdS-proj is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IdS-proj is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IdS-proj. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.insert_game_name.utils;
import java.lang.reflect.RecordComponent;
import java.util.Objects;
import java.lang.reflect.Constructor;

public final class RecordUtil {

    /** Returns a copy of `rec` with `field` replaced by `newValue`. All other components are preserved. */
    @SuppressWarnings("unchecked")
    public static <R extends Record> R with(R rec, String field, Object newValue) {
        Class<?> cls = rec.getClass();
        RecordComponent[] comps = cls.getRecordComponents();
        Object[] values = new Object[comps.length];
        Class<?>[] types = new Class<?>[comps.length];
        boolean found = false;

        try {
            for (int i = 0; i < comps.length; i++) {
                RecordComponent rc = comps[i];
                types[i] = rc.getType();
                if (rc.getName().equals(field)) {
                    values[i] = newValue;
                    found = true;
                } else {
                    var accessor = rc.getAccessor();
                    accessor.setAccessible(true);
                    values[i] = accessor.invoke(rec);
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No component named '" + field + "' in " + cls.getName());
            }

            Constructor<?> ctor = cls.getDeclaredConstructor(types); // canonical constructor
            ctor.setAccessible(true);
            return (R) ctor.newInstance(values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** True if a and b are equal on every component except `ignored`. */
    public static <R extends Record> boolean equalsExcept(R a, R b, String ignored) {
        if (a == b) return true;
        if (a == null || b == null || a.getClass() != b.getClass()) return false;
    
        try {
            for (RecordComponent rc : a.getClass().getRecordComponents()) {
                if (rc.getName().equals(ignored)) continue;
                Object va = rc.getAccessor().invoke(a);
                Object vb = rc.getAccessor().invoke(b);
                if (!Objects.deepEquals(va, vb)) return false;
            }
            return true;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    /** True if they differ ONLY in `field` (i.e. they differ there, and match everywhere else). */
    public static <R extends Record> boolean differOnlyIn(R a, R b, String field) {
        return !a.equals(b) && equalsExcept(a, b, field);
    }
}