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
import java.io.Writer;
import java.nio.CharBuffer;

class Buffer {
    private CharBuffer buf;
    private boolean accept;
    private boolean closed;

    public Buffer(CharBuffer buf) {
        this.buf = buf;
        this.accept = false;
    }

    public synchronized boolean flushTo(Writer w) throws IOException {
        if(this.accept) {
            return false;
        } else {
            this.buf.flip();
            w.write(this.buf.toString());
            this.buf.flip();
            this.buf.clear();
            this.accept = true;
            this.notifyAll();
            return true;
        }
    }

    public boolean accept(CharSequence cs) {
        if(cs.length() > this.buf.array().length) {
            throw new IllegalArgumentException();
        } else {
            this.accept = cs.length() <= this.buf.remaining();
            return this.accept;
        }
    }

    public void append(CharSequence cs) {
        if (!this.accept)
            throw new IllegalStateException("Use accept before append!");

        this.buf.append(cs);
    }

    public void closeAfterNextFlush() {
        this.accept = false;
        this.closed = true;
    }

    public String toString() {
        return "Buffer[" + new String(this.buf.array()) + ", accept=" + this.accept + ", closed=" + this.closed + "]";
    }

    public boolean isOpen() {
        return !this.closed || !this.accept;
    }
}