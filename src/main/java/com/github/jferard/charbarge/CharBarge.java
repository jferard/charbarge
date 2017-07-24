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

/**
 * A CharBarge is a container that carries chars between a producer and a consumer.
 * It uses the old "double buffer" pattern.
 */
public class CharBarge implements Appendable {

    /**
     * Create a CharBarge of a given size
     * @param size the size
     * @return
     */
    public static CharBarge create(int size) {
        Buffer frontBuffer = new Buffer(CharBuffer.wrap(new char[size]));
        Buffer backBuffer = new Buffer(CharBuffer.wrap(new char[size]));
        return new CharBarge(frontBuffer, backBuffer);
    }

    private Buffer frontBuffer;
    private Buffer backBuffer;

    CharBarge(Buffer frontBuffer, Buffer backBuffer) {
        this.frontBuffer = frontBuffer;
        this.backBuffer = backBuffer;
    }

    /**
     * Flush the back buffer to the writer, and swap buffers.
     * @param w
     * @throws IOException
     */
    public synchronized void flushTo(Writer w) throws IOException {
        while (!this.backBuffer.flushTo(w)) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }

        this.swapBuffers();
        this.notifyAll();
    }

    /**
     * Append a CharSequence to the front buffer. If the front buffer is full, swap buffers and retry.
     * @param cs
     * @return
     * @throws IOException
     */
    public synchronized Appendable append(CharSequence cs) throws IOException {
        while (!this.frontBuffer.accept(cs)) {
            this.swapBuffers();
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        }
        this.frontBuffer.append(cs);
        this.notifyAll();
        return this;
    }

    private void swapBuffers() {
        Buffer buf = this.backBuffer;
        this.backBuffer = this.frontBuffer;
        this.frontBuffer = buf;
        this.notifyAll();
    }

    /**
     * Append a char to the front buffer. If the front buffer is full, swap buffers and retry.
     * @param c
     * @return
     * @throws IOException
     */
    public Appendable append(char c) throws IOException {
        this.append(String.valueOf(c));
        return this;
    }

    /**
     * Append a CharSequence subsequence to the front buffer. If the front buffer is full, swap buffers and retry.
     * @param cs
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public Appendable append(CharSequence cs, int start, int end) throws IOException {
        this.append(cs.subSequence(start, end));
        return this;
    }

    /**
     * Close the barge.
     */
    public synchronized void close() {
        this.frontBuffer.closeAfterNextFlush();
        this.backBuffer.closeAfterNextFlush();
        this.notifyAll();
    }

    /**
     * @return true if one of the buffers is open
     */
    public boolean isOpen() {
        return this.frontBuffer.isOpen() || this.backBuffer.isOpen();
    }
}
