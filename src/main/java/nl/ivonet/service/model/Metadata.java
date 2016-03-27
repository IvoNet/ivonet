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

package nl.ivonet.service.model;

/**
 * @author Ivo Woltring
 */
public class Metadata {
    private String baseUri;
    private String browseUri;
    private String fileUri;
    private String downloadUri;

    public String getBaseUri() {
        return this.baseUri;
    }

    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }

    public String getBrowseUri() {
        return this.browseUri;
    }

    public void setBrowseUri(final String browseUri) {
        this.browseUri = browseUri;
    }

    public String getFileUri() {
        return this.fileUri;
    }

    public void setFileUri(final String fileUri) {
        this.fileUri = fileUri;
    }

    public String getDownloadUri() {
        return this.downloadUri;
    }

    public void setDownloadUri(final String downloadUri) {
        this.downloadUri = downloadUri;
    }
}
