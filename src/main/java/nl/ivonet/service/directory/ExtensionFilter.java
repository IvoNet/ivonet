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

import nl.ivonet.service.config.Property;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * @author Ivo Woltring
 */
public class ExtensionFilter implements DirectoryStream.Filter<Path> {

    private static final String DELIMETER = ":";

    @Property
    @Inject
    private String filterExtensions;

    @Override
    public boolean accept(final Path entry) throws IOException {
        return Files.isRegularFile(entry) && isAcceptable(entry);
    }

    private boolean isAcceptable(final Path entry) {
        final String[] split = this.filterExtensions.toLowerCase(Locale.US)
                                                    .split(DELIMETER);
        return Stream.of(split)
                     .anyMatch(extension -> entry.getFileName()
                                                 .toString()
                                                 .toLowerCase(Locale.US)
                                                 .endsWith(extension));
    }
}
