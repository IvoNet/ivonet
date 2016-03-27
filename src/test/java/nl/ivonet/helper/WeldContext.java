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

package nl.ivonet.helper;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

class WeldContext {
    private final WeldContainer weldContainer;

    private WeldContext() {
        final Weld weld = new Weld();
        this.weldContainer = weld.initialize();
    }


    static WeldContext getInstance() {
        return WeldContext.Instance.SINGLETON;
    }

    @SuppressWarnings("UtilityClass")
    private static final class Instance {
        static final WeldContext SINGLETON = new WeldContext();
    }

    <T> T getBean(final Class<T> type) {
        return this.weldContainer.instance()
                                 .select(type)
                                 .get();
    }
}
