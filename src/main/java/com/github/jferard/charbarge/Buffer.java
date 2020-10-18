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

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * A Buffer is a wrap over a CharBuffer.
 * The Buffer may be : 1. flushed into a writer ; 2. closed
 */
class Buffer {
    private CharBuffer buf;
    private boolean accept;
    private boolean closed;

    /**
     * Create a new Buffer
     * @param buf the wrapped CharBuffer
     */
    public Buffer(CharBuffer buf) {
        this.buf = buf;
        this.accept = false;
    }

    /**
     * Flush the wrapped buffer to a Appendable. The data will be flushed iff there is no room left in the buffer.
     * More formally, the last attempt to "accept" a CharSequence failed.
     * @param appendable
     * @return true if the buffer was flushed
     * @throws IOException
     */
    public synchronized boolean flushTo(Appendable appendable) throws IOException {
        if(this.accept) {
            return false;
        } else {
            this.forceFlushTo(appendable);
            return true;
        }
    }

    /**
     * Flush the wrapped buffer to a Appendable.
     * @param appendable
     * @throws IOException
     */
    public synchronized void forceFlushTo(Appendable appendable) throws IOException {
        this.buf.flip();
        appendable.append(this.buf);
        this.buf.flip();
        this.buf.clear();
        this.accept = true;
        this.notifyAll();
    }


    /**
     * @param cs
     * @return true if there is enough room left for the CharSequence
     * @throws IllegalArgumentException if the CharSequence is larger than the buffer
     */
    public boolean accept(CharSequence cs) {
        int neededRoom = cs.length();
        if(neededRoom > this.buf.array().length)
            throw new IllegalArgumentException();

        this.accept = neededRoom <= this.buf.remaining();
        return this.accept;
    }

    /**
     * Append a CharSequence to the buffer. One must call "accept" before append.
     * @param cs
     * @throws IllegalStateException if accept was not called
     */
    public void append(CharSequence cs) {
        if (!this.accept)
            throw new IllegalStateException("Use accept before append!");

        this.buf.append(cs);
    }

    /**
     * Close the buffer after the next flush. That means: 1. the buffer won't accept more chars.
     * 2. the buffer will be closed on next flush
     */
    public void closeAfterNextFlush() {
        this.accept = false;
        this.closed = true;
    }

    @Override
    public String toString() {
        return "Buffer[" + new String(this.buf.array()) + ", accept=" + this.accept + ", closed=" + this.closed + "]";
    }

    /**
     * @return true if the buffer was closed and flushed.
     */
    public boolean isOpen() {
        return !this.closed || !this.accept;
    }
}