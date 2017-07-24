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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;

import static org.junit.Assert.*;

/**
 * Created by jferard on 24/07/17.
 */
public class CharBargeTest {
    private CharBarge barge;
    private Buffer b1;
    private Buffer b2;

    @Before
    public void setUp() {
        b1 = PowerMock.createMock(Buffer.class);
        b2 = PowerMock.createMock(Buffer.class);
        barge = new CharBarge(b1, b2);
    }

    @Test
    public void createTest() {
        CharBarge barge = CharBarge.create(10);
    }

    @Test
    public void flushToTest() throws IOException {
        Writer w = new StringWriter();
        EasyMock.expect(b2.flushTo(w)).andReturn(true);

        PowerMock.replayAll();
        barge.flushTo(w);
        PowerMock.verifyAll();
    }

    @Test
    public void flushToWithInterruptTest() throws IOException {
        final Writer w = new StringWriter();
        EasyMock.expect(b2.flushTo(w)).andReturn(false).anyTimes();

        PowerMock.replayAll();
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    barge.flushTo(w);
                } catch (IOException e) {
                    Assert.assertTrue(e.toString().contains("InterruptedException"));
                }
            }
        };
        t.start();
        t.interrupt();
        Assert.assertTrue(t.isInterrupted());
        PowerMock.verifyAll();
    }

    @Test
    public void appendOkTest() throws IOException {
        EasyMock.expect(this.b1.accept("a string")).andReturn(true);
        this.b1.append("a string");

        PowerMock.replayAll();

        this.barge.append("a string");

        PowerMock.verifyAll();
    }

    @Test
    public void appendCharTest() throws IOException {
        EasyMock.expect(this.b1.accept("a")).andReturn(true);
        this.b1.append("a");

        PowerMock.replayAll();

        this.barge.append('a');

        PowerMock.verifyAll();
    }

    @Test
    public void appendSubsequenceTest() throws IOException {
        EasyMock.expect(this.b1.accept("a")).andReturn(true);
        this.b1.append("a");

        PowerMock.replayAll();

        this.barge.append("a string", 0, 1);

        PowerMock.verifyAll();
    }

    @Test
    public void appendInterrupterTest() throws IOException {
        EasyMock.expect(b1.accept("a string")).andReturn(false).anyTimes();

        PowerMock.replayAll();

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    barge.append("a string");
                } catch (IOException e) {
                    Assert.assertTrue(e.toString().contains("InterruptedException"));
                }
            }
        };
        t.start();
        t.interrupt();
        Assert.assertTrue(t.isInterrupted());

        PowerMock.verifyAll();
    }

    @Test
    public void appendNotOkTest() throws IOException {
        EasyMock.expect(b1.accept("a string")).andReturn(false).anyTimes();
        EasyMock.expect(b2.accept("a string")).andReturn(true).anyTimes();
        this.b2.append("a string");
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    barge.append("a string");
                } catch (IOException e) {
                    Assert.fail();
                }
            }
        };
        t.start();
        synchronized (barge) {
            barge.notifyAll();
        }

        PowerMock.verifyAll();
    }

    @Test
    public void closeTest() throws IOException {
        b1.closeAfterNextFlush();
        b2.closeAfterNextFlush();
        EasyMock.expect(b1.isOpen()).andReturn(true);

        PowerMock.replayAll();

        this.barge.close();
        Assert.assertTrue(this.barge.isOpen());

        PowerMock.verifyAll();
    }

    @Test
    public void close2Test() throws IOException {
        b1.closeAfterNextFlush();
        b2.closeAfterNextFlush();
        EasyMock.expect(b1.isOpen()).andReturn(false);
        EasyMock.expect(b2.isOpen()).andReturn(true);

        PowerMock.replayAll();

        this.barge.close();
        Assert.assertTrue(this.barge.isOpen());

        PowerMock.verifyAll();
    }

    @Test
    public void close3Test() throws IOException {
        b1.closeAfterNextFlush();
        b2.closeAfterNextFlush();
        EasyMock.expect(b1.isOpen()).andReturn(false);
        EasyMock.expect(b2.isOpen()).andReturn(false);

        PowerMock.replayAll();

        this.barge.close();
        Assert.assertFalse(this.barge.isOpen());

        PowerMock.verifyAll();
    }

}