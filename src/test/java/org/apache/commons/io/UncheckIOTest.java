/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.commons.io.function.IOBiFunction;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IORunnable;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.io.function.IOTriFunction;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Uncheck}.
 */
public class UncheckIOTest {

    private static final String CAUSE_MESSAGE = "CauseMessage";
    private static final String CUSTOM_MESSAGE = "Custom message";
    private static final byte[] BYTES = { 'a', 'b' };

    private void assertUncheckedIOException(final IOException expected, final UncheckedIOException e) {
        assertEquals(CUSTOM_MESSAGE, e.getMessage());
        final IOException cause = e.getCause();
        assertEquals(expected.getClass(), cause.getClass());
        assertEquals(CAUSE_MESSAGE, cause.getMessage());
    }

    private ByteArrayInputStream newInputStream() {
        return new ByteArrayInputStream(BYTES);
    }

    /**
     * Tests {@link Uncheck#accept(IOConsumer, Object)}.
     */
    @Test
    public void testAccept() {
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.accept(n -> stream.skip(n), 1);
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }

    /**
     * Tests {@link Uncheck#apply(IOFunction, Object)}.
     */
    @Test
    public void testApply1() {
        final ByteArrayInputStream stream = newInputStream();
        assertEquals(1, Uncheck.apply(n -> stream.skip(n), 1).intValue());
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }

    /**
     * Tests {@link Uncheck#apply(IOBiFunction, Object, Object)}.
     */
    @Test
    public void testApply2() {
        final ByteArrayInputStream stream = newInputStream();
        final byte[] buf = new byte[BYTES.length];
        assertEquals(1, Uncheck.apply((o, l) -> stream.read(buf, o, l), 0, 1).intValue());
        assertEquals('a', buf[0]);
    }

    /**
     * Tests {@link Uncheck#apply(IOTriFunction, Object, Object, Object)}.
     */
    @Test
    public void testApply3() {
        final ByteArrayInputStream stream = newInputStream();
        final byte[] buf = new byte[BYTES.length];
        assertEquals(1, Uncheck.apply((b, o, l) -> stream.read(b, o, l), buf, 0, 1).intValue());
        assertEquals('a', buf[0]);
    }

    /**
     * Tests {@link Uncheck#get(IOSupplier)}.
     */
    @Test
    public void testGet() {
        assertEquals('a', Uncheck.get(() -> newInputStream().read()).intValue());
    }

    /**
     * Tests {@link Uncheck#get(IOSupplier, Supplier)}.
     */
    @Test
    public void testGetMessage() {
        // No exception
        assertEquals('a', Uncheck.get(() -> newInputStream().read()).intValue(), () -> CUSTOM_MESSAGE);
        // Exception
        final IOException expected = new IOException(CAUSE_MESSAGE);
        try {
            Uncheck.get(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }

    /**
     * Tests {@link Uncheck#run(IORunnable)}.
     */
    @Test
    public void testRun() {
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.run(() -> stream.skip(1));
        assertEquals('b', Uncheck.get(stream::read).intValue());
    }

    /**
     * Tests {@link Uncheck#run(IORunnable, Supplier))}.
     *
     * @throws IOException
     */
    @Test
    public void testRunMessage() throws IOException {
        // No exception
        final ByteArrayInputStream stream = newInputStream();
        Uncheck.run(() -> stream.skip(1), () -> CUSTOM_MESSAGE);
        assertEquals('b', Uncheck.get(stream::read).intValue());
        final IOException expected = new IOException(CAUSE_MESSAGE);
        // Exception
        try {
            Uncheck.run(() -> new BrokenInputStream(expected).read(), () -> CUSTOM_MESSAGE);
            fail();
        } catch (final UncheckedIOException e) {
            assertUncheckedIOException(expected, e);
        }
    }
}