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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;

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
public class BlikiServiceIT {

    @ArquillianResource
    private URL base;


    @Test
    public void testDownload() throws Exception {
        final Response response = ClientBuilder.newClient()
                                               .target(UriBuilder.fromPath(
                                                       this.base + "api" + BlikiService.PATH + "/Java/home.md")
                                                                                                        .build())
                                               .request(MediaType.APPLICATION_JSON)
                                               .get(Response.class);
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertTrue(response.hasEntity());
        final File file = response.readEntity(File.class);
        assertNotNull(file);
        final String responseStr = new String(Files.readAllBytes(file.toPath()));
        System.out.println("responseStr = " + responseStr);
        final JsonObject rootData = Json.createReader(new StringReader(responseStr))
                                        .readObject();
        assertTrue(rootData.getString("content")
                           .startsWith("# Java Home"));

    }


    @Test
    public void testRoot() throws Exception {

        final String root = ClientBuilder.newClient()
                                         .target(UriBuilder.fromPath(this.base + "api" + BlikiService.PATH)
                                                           .build())
                                         .request(MediaType.APPLICATION_JSON)
                                         .get(String.class);

        System.out.println("data = " + root);
        assertThat(root, notNullValue());

        final JsonObject rootData = Json.createReader(new StringReader(root))
                                        .readObject();

        final String baseUri = rootData.getString("baseUri");
        assertThat(baseUri, endsWith("/api/bliki"));
        final String browseUri = rootData.getString("browseUri");
        assertThat(browseUri, endsWith("/api/bliki/"));


        final JsonObject folder = rootData.getJsonObject("folder");
        final JsonArray folders = folder.getJsonArray("folders");
        assertThat(folders.size(), is(2));
        assertThat(folder.getString("path"), is(""));
        assertThat(folder.getJsonArray("files")
                         .size(), is(1));

        final String newFolder = folders.getString(0);
        final String java = ClientBuilder.newClient()
                                         .target(rootData.getString("browseUri") + newFolder)
                                         .request(MediaType.APPLICATION_JSON)
                                         .get(String.class);
        final JsonObject javaData = Json.createReader(new StringReader(java))
                                        .readObject();

        System.out.println("data = " + javaData);
        assertThat(root, notNullValue());

        final JsonObject javaFolder = javaData.getJsonObject("folder");
        assertThat(javaFolder.getString("path"), is(newFolder));
        assertThat(javaFolder.getJsonArray("files")
                             .size(), is(2));

    }

    @Deployment
    public static Archive<?> createDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                                         .addPackage(BootStrap.class.getPackage())
                                         .addPackage(Directory.class.getPackage())
                                         .addPackage(Metadata.class.getPackage())
                                         .addPackage(BlikiService.class.getPackage())
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

}
