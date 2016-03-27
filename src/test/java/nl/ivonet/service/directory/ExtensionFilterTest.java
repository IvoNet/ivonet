/*
 * Copyright 2016 Ivo Woltring <WebMaster@ivonet.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.ivonet.service.directory;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static nl.ivonet.helper.Utils.injectField;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ExtensionFilterTest {

    private ExtensionFilter filter;

    @Before
    public void setUp() throws Exception {
        this.filter = new ExtensionFilter();
    }

    @Test
    public void testEpub() throws Exception {
        injectField(this.filter, "filterExtensions", ".epub:.kepub");
        assertTrue(this.filter.accept(Paths.get("src/test/resources/books/Twain, Mark/pg74.epub")
                                           .toAbsolutePath()));
    }
    @Test
    public void testEpubWrong() throws Exception {
        injectField(this.filter, "filterExtensions", ".txt");
        assertFalse(this.filter.accept(Paths.get("src/test/resources/books/Twain, Mark/pg74.epub")
                                           .toAbsolutePath()));
    }

}