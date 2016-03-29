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

package nl.ivonet.service.model;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Ivo Woltring
 */
public class ResourceTest {

    private Resource resource;

    @Before
    public void setUp() throws Exception {
        this.resource = new Resource();
    }

    @Test
    public void retrieveResource() throws Exception {
        this.resource.setPath("path");
        this.resource.setName("resource");
        assertThat(this.resource.resource(), is("path/resource"));

        this.resource.setPath("path with a lot of spaces");
        assertThat(this.resource.resource(), is("path with a lot of spaces/resource"));
    }

    @Test
    public void pathWithEndslash() throws Exception {
        this.resource.setPath("path/");
        this.resource.setName("resource");
        assertThat(this.resource.resource(), is("path/resource"));

    }

    @Test
    public void pathNoName() throws Exception {
        this.resource.setPath("path");
        assertThat(this.resource.resource(), is("path"));
    }

    @Test
    public void pathNoPath() throws Exception {
        this.resource.setName("name");
        assertThat(this.resource.resource(), is("/name"));
    }
}