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

package nl.ivonet.service.service;

import nl.ivonet.service.config.Property;
import nl.ivonet.service.directory.Directory;
import nl.ivonet.service.directory.EpubDirectory;
import nl.ivonet.service.model.Data;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.nio.file.Paths;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Ivo Woltring
 */
@Stateless
@Path(EpubService.PATH)
public class EpubService {
    static final String PATH = "/epub";
    static final String DOWNLOAD = "/download";
    private static final String APPLICATION_EPUB = "application/epub+zip";

    @Context
    UriInfo uriInfo;

    @Inject
    @EpubDirectory
    Directory directory;

    @Inject
    @Property("epub.folder")
    String epubFolder;

    @GET
    @Produces(APPLICATION_JSON)
    public Data root() {
        return retrievedata("");
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{folder: .+}")
    public Response folder(@PathParam("folder") final String folder) {
        return Response.ok(retrievedata(folder))
                       .build();
    }

    // TODO: 05-04-2016 create @GET for Epub Metadata

    @GET
    @Path("/download/{file: .+epub}")
    @Produces(APPLICATION_EPUB)
    public Response download(@PathParam("file") final String filename) {
        final File file = Paths.get(this.epubFolder, filename)
                               .toFile();
        if (file.exists()) {
            return Response.ok()
                           .type(APPLICATION_EPUB)
                           .entity(file)
                           .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                       .build();

    }

    private Data retrievedata(final String folder) {
        final Data data = new Data(this.directory.folder(folder));
        addMetadata(data);
        return data;
    }

    private void addMetadata(final Data data) {
        final String baseUri = this.uriInfo.getBaseUriBuilder()
                                           .path(getClass())
                                           .build()
                                           .toString();
        final String browseUri = this.uriInfo.getBaseUriBuilder()
                                             .path(getClass())
                                             .path("/")
                                             .build()
                                             .toString();
        final String fileUri = this.uriInfo.getBaseUriBuilder()
                                           .path(DOWNLOAD)
                                           .build()
                                           .toString();
        final String downloadUri = this.uriInfo.getBaseUriBuilder()
                                               .path(DOWNLOAD)
                                               .build()
                                               .toString();
        data.setMetadata(baseUri, browseUri, fileUri, downloadUri);
    }

}
