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

import com.github.javafaker.Faker;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

class Producer extends Thread {
    private CharBarge barge;
    private StringProvider provider;
    private Writer s;

    public Producer(CharBarge barge, StringProvider provider) {
        this.barge = barge;
        this.provider = provider;
    }

    public void run() {
        try {
            this.s = new StringWriter();

            for(int i = 0; i < 1000; ++i) {
                String w = this.provider.next();
                this.barge.append(w);
                this.s.append(w);
            }

            this.barge.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String get() {
        return this.s.toString();
    }
}