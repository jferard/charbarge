/*
 * CharBarge - Transferring char sequences in a producer-consumer pattern
 *     Copyright (C) 2017 J. Férard <https://github.com/jferard>
 *
 * This file is part of CharBarge.
 *
 * CharBarge is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * CharBarge is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses />.
 */

package com.github.jferard.charbarge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;

import static org.junit.Assert.*;

/**
 * Created by jferard on 24/07/17.
 */
public class BufferTest {
    private Buffer buf;

    @Before
    public void setUp() {
        CharBuffer b = CharBuffer.allocate(10);
        buf = new Buffer(b);
    }


    @Test
    public void StringWriterTest() throws Exception {
        Writer w = new StringWriter();
        w.write('c');
        Assert.assertEquals("c", w.toString());
    }

    @Test
    public void flushToWithoutAcceptTest() throws Exception {
        Writer w = new StringWriter();
        Assert.assertTrue(buf.accept("a string"));
        buf.append("a string");
        Assert.assertFalse(buf.flushTo(w));
        Assert.assertEquals("", w.toString());
        Assert.assertFalse(buf.accept("to long"));
        Assert.assertTrue(buf.flushTo(w));
        Assert.assertEquals("a string", w.toString());
    }

    @Test
    public void flushToTestWithAccept() throws Exception {
        Writer w = new StringWriter();
        Assert.assertTrue(buf.accept("a string"));
        buf.append("a string");
        Assert.assertFalse(buf.flushTo(w));
        Assert.assertEquals("", w.toString());
    }

    @Test(expected=IllegalStateException.class)
    public void appendWithoutAcceptTest() throws Exception {
        buf.append("a string");
    }

    @Test
    public void appendWithAcceptTest() throws Exception {
        Assert.assertTrue(buf.accept("a string"));
        buf.append("a string");
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendWithOverflowTest() throws Exception {
        buf.accept("to looooongest");
    }

    @Test
    public void toStringTest() throws Exception {
        String blankString = new String(new char[]{'\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0'});
        Assert.assertEquals("Buffer["+ blankString +", accept=false, closed=false]", buf.toString());
        Assert.assertTrue(buf.accept("a string"));
        buf.append("a string");
        final String aString = new String(new char[]{'a', ' ', 's', 't', 'r', 'i', 'n', 'g', '\0', '\0'});
        Assert.assertEquals("Buffer["+ aString +", accept=true, closed=false]", buf.toString());
        Assert.assertFalse(buf.accept("to long"));
        Assert.assertEquals("Buffer["+ aString +", accept=false, closed=false]", buf.toString());
        buf.closeAfterNextFlush();
        Assert.assertEquals("Buffer["+ aString +", accept=false, closed=true]", buf.toString());
    }

    @Test
    public void isOpenTest() throws Exception {
        Assert.assertTrue(buf.isOpen());
        buf.closeAfterNextFlush();
        Assert.assertTrue(buf.isOpen());
        Assert.assertTrue(buf.accept("a string"));
        buf.append("a string");
        buf.flushTo(new StringWriter());
        Assert.assertFalse(buf.isOpen());
    }

}