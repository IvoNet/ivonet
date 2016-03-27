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

import nl.ivonet.service.config.BootStrap;
import nl.ivonet.service.directory.Directory;
import nl.ivonet.service.model.Metadata;
import nl.ivonet.service.model.ResourceName;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.filter.ExcludeRegExpPaths;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.zip.ZipFile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for the {@link EpubService} class.
 *
 * @author Ivo Woltring
 */
@RunWith(Arquillian.class)
public class EpubServiceIT {

    @ArquillianResource
    private URL base;

    @Deployment
    public static Archive<?> createDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                                         .addPackage(BootStrap.class.getPackage())
                                         .addPackage(Directory.class.getPackage())
                                         .addPackage(Metadata.class.getPackage())
                                         .addPackage(EpubService.class.getPackage())
                                         .filter(new ExcludeRegExpPaths(".*Test.class"))
                                         .filter(new ExcludeRegExpPaths(".*IT.class"))
                                         .addAsResource("application.properties")
                                         .addAsLibraries(Maven.resolver()
                                                              .loadPomFromFile("pom.xml")
                                                              .importRuntimeDependencies()
                                                              .resolve()
                                                              .withTransitivity()
                                                              .asFile())
                                         .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;


    }

    @Test
    public void testPostDownload() throws Exception {
        final ResourceName resourceName = new ResourceName();
        resourceName.setName("Stoker, Bram/pg345.epub");
        final Response response = ClientBuilder.newClient()
                                               .target(UriBuilder.fromPath(
                                                       this.base + "api" + EpubService.EPUB + EpubService.DOWNLOAD)
                                                                 .build())
                                               .request()
                                               .post(Entity.entity(resourceName, MediaType.APPLICATION_JSON),
                                                     Response.class);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertTrue(response.hasEntity());
        final File file = response.readEntity(File.class);
        assertNotNull(file);
        assertTrue(new ZipFile(file).stream()
                                    .filter(resource -> resource.getName()
                                                                .contains("toc"))
                                    .findAny()
                                    .isPresent());
    }

    @Test
    public void testPostDownloadWrongFile() throws Exception {
        final ResourceName resourceName = new ResourceName();
        resourceName.setName("Stoker, Bram/I do not exist.epub");
        final Response response = ClientBuilder.newClient()
                                               .target(UriBuilder.fromPath(
                                                       this.base + "api" + EpubService.EPUB + EpubService.DOWNLOAD)
                                                                 .build())
                                               .request()
                                               .post(Entity.entity(resourceName, MediaType.APPLICATION_JSON),
                                                     Response.class);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }


    //curl -H "Content-Type: application/json" -X POST -L -d '{"name":"Stoker, Bram"}' http://192.168.99
    // .100:8081/books/api/folders
    @Test
    public void testPost() throws Exception {
        final ResourceName resourceName = new ResourceName();
        resourceName.setName("Stoker, Bram");
        final String response = ClientBuilder.newClient()
                                             .target(UriBuilder.fromPath(this.base + "api" + EpubService.EPUB)
                                                               .build())
                                             .request()
                                             .post(Entity.entity(resourceName, MediaType.APPLICATION_JSON),
                                                   String.class);
//        System.out.println("response = " + response);
        assertThat(response, notNullValue());

        final JsonObject data = Json.createReader(new StringReader(response))
                                    .readObject();
        final JsonObject folder = data.getJsonObject("folder");
        assertThat(folder.getString("path"), is("Stoker, Bram"));
        assertThat(folder.getJsonArray("files")
                         .size(), is(1));
        assertThat(folder.getJsonArray("files")
                         .getString(0), is("pg345.epub"));


    }


    // {"baseUri":"http://127.0.0.1:8080/1d6a8823-a1eb-44be-91c6-c38c2cfa91e6/api/folders","browseUri":"http://127.0
    // .0.1:8080/1d6a8823-a1eb-44be-91c6-c38c2cfa91e6/api/folders/","fileUri":null,"downloadUri":null,
    // "folder":{"folders":["Stoker, Bram","Twain, Mark"],"files":[],"path":""}}
    @Test
    public void testRoot() throws Exception {

        final String root = ClientBuilder.newClient()
                                         .target(UriBuilder.fromPath(this.base + "api" + EpubService.EPUB)
                                                           .build())
                                         .request(MediaType.APPLICATION_JSON)
                                         .get(String.class);

//        System.out.println("data = " + root);
        assertThat(root, notNullValue());

        final JsonObject rootData = Json.createReader(new StringReader(root))
                                        .readObject();

        final String baseUri = rootData.getString("baseUri");
        assertThat(baseUri, endsWith("/api/epub"));
        final String browseUri = rootData.getString("browseUri");
        assertThat(browseUri, endsWith("/api/epub/"));


        final JsonObject folder = rootData.getJsonObject("folder");
        final JsonArray folders = folder.getJsonArray("folders");
        assertThat(folders.size(), is(2));
        assertThat(folder.getString("path"), is(""));
        assertThat(folder.getJsonArray("files")
                         .size(), is(0));
        final String newFolder = folders.getString(0);


        final String bramStoker = ClientBuilder.newClient()
                                               .target(UriBuilder.fromPath(this.base + "api/epub/" + newFolder)
                                                                 .build())
                                               .request(MediaType.APPLICATION_JSON)
                                               .get(String.class);
        final JsonObject bramStokerData = Json.createReader(new StringReader(bramStoker))
                                              .readObject();

//        System.out.println("data = " + bramStokerData);
        assertThat(root, notNullValue());

        final JsonObject bramStrokerFolder = bramStokerData.getJsonObject("folder");
        assertThat(bramStrokerFolder.getString("path"), is(newFolder));
        assertThat(bramStrokerFolder.getJsonArray("files")
                                    .size(), is(1));

    }

}
