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
import nl.ivonet.service.model.Resource;

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
 * @author Ivo Woltring
 */
@Stateless
@Path(BlikiService.BLIKI)
public class BlikiService {
    static final String BLIKI = "/bliki";
    static final String DOWNLOAD = "/md";

    @Context
    UriInfo uriInfo;

    @Inject
    @BlikiDirectory
    private Directory directory;

    @Inject
    @Property("bliki.folder")
    private String blikiFolder;

    private Data retrieveData(final String folder) {
        final Data data = new Data(this.directory.folder(folder));
        data.setBaseUri(this.uriInfo.getBaseUriBuilder()
                                    .path(this.getClass())
                                    .build()
                                    .toString());
        data.setBrowseUri(this.uriInfo.getBaseUriBuilder()
                                      .path(this.getClass())
                                      .path("/")
                                      .build()
                                      .toString());
        data.setFileUri(this.uriInfo.getBaseUriBuilder()
                                    .path(this.getClass())
                                    .path(DOWNLOAD)
                                    .build()
                                    .toString());
        data.setDownloadUri(this.uriInfo.getBaseUriBuilder()
                                        .path(this.getClass())
                                        .path(DOWNLOAD)
                                        .build()
                                        .toString());
        return data;
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Data root() {
        return retrieveData("");
    }


    // FIXME: 26-03-2016 remove me when post completely works
    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{folder: .+}")
    public Response folder(@PathParam("folder") final String folder) {
        return Response.ok(retrieveData(folder))
                       .build();
    }


    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response folderByJson(final Resource resource) {
        return Response.ok(retrieveData(resource.resource()))
                       .build();
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Path(DOWNLOAD)
    public Response download(final Resource resource) {
        try {
            final Content data = new Content(Files.readAllBytes(Paths.get(this.blikiFolder, resource.resource())));
            return Response.ok()
                           .type(APPLICATION_JSON)
                           .entity(data)
                           .build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/md/{mmd: .+md}")
    public Response downloadget(@PathParam("mmd") final String mmd) {
        System.out.println("mmd = " + mmd);
        try {
            final Content data = new Content(Files.readAllBytes(Paths.get(this.blikiFolder, mmd)));
            return Response.ok()
                           .type(APPLICATION_JSON)
                           .entity(data)
                           .build();
        } catch (IOException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }
}
