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

import java.io.File;
import java.lang.reflect.Field;

/**
 * @author Ivo Woltring
 */
@SuppressWarnings("UtilityClass")
public final class Utils {

    private Utils() {
    }

    public static void injectField(final Object injectable, final String fieldname, final Object value) {
        try {
            final Field field = injectable.getClass()
                                          .getDeclaredField(fieldname);
            final boolean origionalValue = field.isAccessible();
            field.setAccessible(true);
            field.set(injectable, value);
            field.setAccessible(origionalValue);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get a filename from the recourse folder.
     *
     * @param fileName the filename to get in src/test/resources
     * @return the absolute path to the filename
     */
    public static String getFileResource(final String fileName) {
        String abspath = new File(".").getAbsolutePath();
        abspath = abspath.substring(0, abspath.length() - 1);
        return new File(abspath + "src/test/resources/" + fileName).getAbsolutePath();
    }

    /**
     * Get the target location.
     *
     * @return string representation of the target location
     */
    public static String getTargetLocation() {
        String abspath = new File(".").getAbsolutePath();
        abspath = abspath.substring(0, abspath.length() - 1);
        return new File(abspath + "target/").getAbsolutePath();
    }
}
