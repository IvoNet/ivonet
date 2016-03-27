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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Ivo Woltring
 */
public class Directory {

    @Inject
    @Property
    private String rootFolder;

    @Inject private DirectoryFilter directoryFilter;
    @Inject private ExtensionFilter extensionFilter;

    public Folder folder() {
        return folder("");
    }

    public Folder folder(final String path) {
        final Path dir = Paths.get(this.rootFolder + path);
        final Folder folder = new Folder(path);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, this.directoryFilter)) {
            for (final Path entry : stream) {
                folder.addFolder(entry.getFileName()
                                      .toString());

            }
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, this.extensionFilter)) {
            for (final Path entry : stream) {
                final String filename = entry.getFileName()
                                             .toString();
                folder.addFile(filename);
            }
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
        return folder;
    }


    @PostConstruct
    public void postConstruct() {
        if (!this.rootFolder.endsWith(File.separator)) {
            this.rootFolder += "/";
        }
    }

}
