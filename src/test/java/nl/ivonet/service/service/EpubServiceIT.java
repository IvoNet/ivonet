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
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.zip.ZipFile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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
    public void testGetDownload() throws Exception {
        final Response response = getResponse(EpubService.DOWNLOAD + "/Stoker, Bram/pg345.epub");

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
    public void testGetDownloadWrongFile() throws Exception {
        final Response response = getResponse(EpubService.DOWNLOAD + "/Stoker, Bram/I do not exist.epub");

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void testGet() throws Exception {

        final JsonObject data = getResponseAsJson("/Stoker, Bram");

        assertThat(data.getString("path"), is("Stoker, Bram"));
        final JsonArray folder = data.getJsonArray("folders");
        assertThat(folder.size(), is(0));

        final JsonArray browseFiles1 = data.getJsonArray("browseFiles");
        final JsonObject jsonObject = browseFiles1.getJsonObject(0);
        final String key = jsonObject.getString("key");
        assertThat(key, endsWith("pg345.epub"));
        final String value = jsonObject.getString("value");
        assertThat(value, endsWith("/api/epub/meta/Stoker%2C+Bram%2Fpg345.epub"));


    }

    @Test
    public void testThroughApi() throws Exception {

        //get the epub root data
        final JsonObject json = getResponseAsJson("/");

        //test a few things
        final String pathUri = json.getString("pathUri");
        assertThat(pathUri, endsWith("/api/epub/"));


        //get the files
        final JsonArray files = json.getJsonArray("browseFiles");
        assertNotNull(files);
        assertThat(files.size(), is(0));

        final JsonArray folders = json.getJsonArray("folders");
        assertThat(folders.size(), not(0));

        final boolean condition = folders.stream()
                                         .filter(jsonValue -> jsonValue.getValueType() == JsonValue.ValueType.OBJECT)
                                         .map(jsonValue -> (JsonObject) jsonValue)
                                         .anyMatch(jsonObject -> "Stoker, Bram".equals(
                                                 jsonObject.getString("key")));
        assertTrue(condition);
//        final String bram = folders.getString("Stoker, Bram");
//        final JsonObject bramStokerFolder = getResponseByUrlAsJson(bram);
//
//        final JsonObject bramFiles = bramStokerFolder.getJsonObject("browseFiles");
//        assertNotNull(bramFiles);
//        assertThat(bramFiles.size(), not(0));
//        assertTrue(bramFiles.containsKey("pg345.epub"));
//        final String home = bramFiles.getString("pg345.epub");
//        assertNotNull(home);


    }

    private JsonObject getResponseByUrlAsJson(final String url) {
        final String response = ClientBuilder.newClient()
                                             .target(url)
                                             .request()
                                             .get(String.class);

        assertThat(response, notNullValue());
        System.out.println("response = " + response);
        return Json.createReader(new StringReader(response))
                   .readObject();

    }

    private Response getResponse(final String relativeApiPath) {
        final Response response = ClientBuilder.newClient()
                                               .target(UriBuilder.fromPath(
                                                       this.base.toString() + "api" + EpubService.PATH
                                                       + relativeApiPath)
                                                                 .build())
                                               .request()
                                               .get(Response.class);

        assertThat(response, notNullValue());
        System.out.println("response = " + response);
        return response;
    }

    private JsonObject getResponseAsJson(final String relativeApiPath) {
        final String response = ClientBuilder.newClient()
                                             .target(UriBuilder.fromPath(
                                                     this.base.toString() + "api" + EpubService.PATH + relativeApiPath)
                                                               .build())
                                             .request()
                                             .get(String.class);

        assertThat(response, notNullValue());
        System.out.println("response = " + response);
        return Json.createReader(new StringReader(response))
                   .readObject();
    }

}
