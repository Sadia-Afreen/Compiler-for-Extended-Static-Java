package esjc.util;

import java.util.Collection;
import java.util.Map;

public class Util {

    /**
     * Returns the first line of an object's {@link String} representation
     *
     * @param o The object.
     * @return The first line of the given object's {@link String} representation
     */
    public static
    String getFirstLine( final Object o) {
        assert o != null;
        final String nText = o.toString();
        final int index = nText.indexOf('\n');
        return index >= 0 ? nText.substring(0, index) : nText;
    }

    /**
     * Checks whether a {@link Collection} is non-null and all its elements are
     * also non-null.
     *
     * @param c The {@link Collection}.
     * @return True, if the {@link Collection} is non-null and all its elements
     * are also non-null.
     */
    public static boolean checkNonNullElements(
            @SuppressWarnings("rawtypes") final Collection c) {
        if (c == null) {
            return false;
        }
        for (final Object o : c) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether a {@link Map} is non-null and all its keys and values are
     * also non-null.
     *
     * @param m The {@link Map}.
     * @return True, if the {@link Map} is non-null and all its keys and values
     * are also non-null.
     */
    public static boolean checkNonNullElements(
            @SuppressWarnings("rawtypes")  final Map m) {
        if (m == null) {
            return false;
        }
        if (!Util.checkNonNullElements(m.keySet())) {
            return false;
        }
        return Util.checkNonNullElements(m.values());
    }
}
