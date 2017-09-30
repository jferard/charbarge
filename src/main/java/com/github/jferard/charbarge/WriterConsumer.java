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

public class WriterConsumer extends Thread {
    private CharBarge barge;
    private Writer w;

    public WriterConsumer(CharBarge barge, Writer w) {
        this.barge = barge;
        this.w = w;
    }

    public void run() {
        try {
            while (this.barge.isOpen()) {
               this.barge.flushTo(this.w);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
