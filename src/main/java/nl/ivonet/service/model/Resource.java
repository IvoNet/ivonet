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


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ivo Woltring
 */
@XmlRootElement
public class Resource {

    @XmlElement
    private String path;

    @XmlElement
    private String name;


    public void setName(final String name) {
        this.name = name;
    }

    public void setPath(final String path) {
        this.path = path;
    }


    public String resource() {
        String ret = "";
        if (isNotEmpty(this.path)) {
            ret += this.path;
        }
        if (isNotEmpty(this.name)) {
            if (!ret.endsWith("/")) {
                ret += "/";
            }
            ret += this.name;
        }
        return ret;
    }

    private boolean isNotEmpty(final String value) {
        return (value != null) && !value.isEmpty();
    }
}
