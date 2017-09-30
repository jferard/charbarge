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
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Assert;
import org.junit.Test;

public class ProducerConsumerTest {
    @Test
    public void test() throws FileNotFoundException, InterruptedException {
        CharBarge barge = CharBarge.create(1024);
        final Faker f = new Faker();
        StringProvider provider = new StringProvider() {
            @Override
            public String next() {
                return f.chuckNorris().fact();
            }
        };

        Producer p1 = new Producer(barge, provider);
        Writer w = new StringWriter();
        WriterConsumer c1 = new WriterConsumer(barge, w);
        c1.start();
        p1.start();
        c1.join();
        Assert.assertEquals(p1.get(), w.toString());
    }
}
