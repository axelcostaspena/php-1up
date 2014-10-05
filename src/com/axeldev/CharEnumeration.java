package com.axeldev;

import java.util.NoSuchElementException;

/**
 * Wraps a native char array providing sequential access to its elements, one at a time. Successive calls to the
 * <code>nextElement</code> method return successive elements of the series.
 * <p>
 * For example, to print all character codes on a <tt>char</tt> <tt>array</tt> <i>charArray</i>:
 * <pre>
 *   while (charArray.hasMoreElements()) {
 *       System.out.println((int) charArray.nextElement());
 *   }</pre>
 * </p>
 * <p>Mirrors <tt>Enumeration</tt> implementation.</p>
 *
 * @author Áxel Costas Pena &lt;axl.coding@gmail.com&gt;
 * @version 1
 * @see java.util.Enumeration
 */
public class CharEnumeration {
    private final char[] charArray;
    private int pointer;

    public CharEnumeration(char[] charArray) {
        this.charArray = charArray;
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return <code>true</code> if and only if this enumeration object contains at least one more element to provide;
     * <code>false</code> otherwise.
     */
    boolean hasMoreElements() {
        return pointer < charArray.length;
    }

    /**
     * Returns the next element of this enumeration if this enumeration object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @throws java.util.NoSuchElementException if no more elements exist.
     */
    char nextElement() {
        if (!hasMoreElements()) throw new NoSuchElementException();
        return charArray[pointer++];
    }
}
