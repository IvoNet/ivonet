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
import nl.ivonet.service.directory.BlikiDirectory;
import nl.ivonet.service.directory.Directory;
import nl.ivonet.service.model.Content;
import nl.ivonet.service.model.Data;
import nl.ivonet.service.model.Metadata;
import nl.ivonet.service.model.Resource;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Service for bliki endpoints.
 *
 * Without params it will return the root directory and file structure.
 *
 * @author Ivo Woltring
 */
@Stateless
@Path(BlikiService.PATH)
public class BlikiService {
    static final String PATH = "/bliki";
    static final String DOWNLOAD = "/md";

    private String baseUri;
    private String browseUri;
    private String fileUri;
    private String downloadUri;

    @Context
    UriInfo uriInfo;

    @Inject
    @BlikiDirectory
    private Directory directory;


    @Inject
    @Property("bliki.folder")
    private String blikiFolder;

    @GET
    @Produces(APPLICATION_JSON)
    public Data root() {
        return retrieveData("");
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{folder: .+}")
    public Response folder(@PathParam("folder") final String folder) {
        return Response.ok(retrieveData(folder))
                       .build();
    }

    // TODO: 30-03-2016 This method is probably not a good idea in the REST scheme of things. It should be a get...
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response folderByJson(final Resource resource) {
        return Response.ok(retrieveData(resource.resource()))
                       .build();
    }

    // TODO: 30-03-2016 Download should probably be a GET in the REST way of working
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Path(DOWNLOAD)
    public Response download(final Resource resource) {
        try {
            final Content content = new Content(Files.readAllBytes(Paths.get(this.blikiFolder, resource.resource())));
            addMetadata(content);
            return Response.ok()
                           .type(APPLICATION_JSON)
                           .entity(content)
                           .build();
        } catch (final IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }

//    private String endSlash(final String input) {
//        return input.endsWith("/") ? input : input + File.separator;
//    }

    // TODO: 30-03-2016 Hacking attempts like /../.. paths should be refused
    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{mmd: .+\\.md}")
    public Response downloadget(@PathParam("mmd") final String mmd) {
//        if (!endSlash(path.getCanonicalPath()).startsWith(documentRoot) &&
//            (file.contains("/..") || file.contains("%2f..") || file.contains(("%2F..")))) {
////            Build in for security reasons. Relative paths would allow for browsing outside the documentRoot
//            throw new UnsupportedOperationException("Relative paths are not supported.");
//        }

        System.out.println("mmd = " + mmd);
        try {
            final Content content = new Content(Files.readAllBytes(Paths.get(this.blikiFolder, mmd)));
            addMetadata(content);
            return Response.ok()
                           .type(APPLICATION_JSON)
                           .entity(content)
                           .build();
        } catch (final IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }

    private Data retrieveData(final String folder) {
        final Data data = new Data(this.directory.folder(folder));
        addMetadata(data);
        return data;
    }

    private void addMetadata(final Metadata metadata) {
        metadata.setMetadata(this.baseUri, this.browseUri, this.fileUri, this.downloadUri);
    }

    @PostConstruct
    public void init() {
        this.baseUri = this.uriInfo.getBaseUriBuilder()
                                   .path(this.getClass())
                                   .build()
                                   .toString();
        this.browseUri = this.uriInfo.getBaseUriBuilder()
                                     .path(this.getClass())
                                     .path("/")
                                     .build()
                                     .toString();
        this.fileUri = this.uriInfo.getBaseUriBuilder()
                                   .path(DOWNLOAD)
                                   .build()
                                   .toString();
        this.downloadUri = this.uriInfo.getBaseUriBuilder()
                                       .path(DOWNLOAD)
                                       .build()
                                       .toString();
    }

}
