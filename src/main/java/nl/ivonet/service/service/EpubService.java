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
import nl.ivonet.service.model.Data;
import nl.ivonet.service.model.ResourceName;

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
import java.io.File;
import java.nio.file.Paths;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Ivo Woltring
 */
@Stateless
@Path(EpubService.EPUB)
public class EpubService {
    static final String EPUB = "/epub";
    static final String DOWNLOAD = "/download";
    private static final String APPLICATION_X_CBR = "application/x-cbr";


    @Context
    UriInfo uriInfo;

    @Inject
    private Directory directory;

    @Inject
    @Property
    private String rootFolder;

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
//        data.setFileUri(this.uriInfo.getBaseUriBuilder()
//                                    .path(DownloadService.class)
//                                    .build()
//                                    .toString());
//        data.setDownloadUri(this.uriInfo.getBaseUriBuilder()
//                                        .path(ComicService.class)
//                                        .build()
//                                        .toString());
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


    //{"name":"test Folder met spaties/Name met spatie"}
    //curl -H "Content-Type: application/json" -X POST -L -d '{"name":"Stoker, Bram"}' http://192.168.99
    // .100:8081/books/api/folders
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response folderByJson(final ResourceName resourceName) {
        return Response.ok(retrieveData(resourceName.getName()))
                       .build();
    }

    @POST
    @Produces(APPLICATION_X_CBR)
    @Consumes(APPLICATION_JSON)
    @Path(DOWNLOAD)
    public Response download(final ResourceName resource) {
        final File file = Paths.get(this.rootFolder, resource.getName())
                               .toFile();
        if (file.exists()) {
            System.out.println("File found");
            return Response.ok()
                           .type(APPLICATION_X_CBR)
                           .entity(file)
                           .build();
        } else {
            System.out.println("File not found");
            return Response.status(Response.Status.NOT_FOUND)
                           .build();
        }
    }
}
