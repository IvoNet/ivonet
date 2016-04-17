package nl.ivonet.service.model;

import nl.ivonet.service.directory.Folder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ivo Woltring
 */
@XmlRootElement
public class Data {
    private final String path;
    private final String pathUri;
    private final List<KeyValue> folders;
    private final List<KeyValue> browseFiles;
    private final List<KeyValue> downloadFiles;


    public Data(final Folder folder, final String baseUri, final String browseUri, final String fileUri,
                final String downloadUri) {
        this.path = folder.getPath();
        this.pathUri = endslash(baseUri) + urlEncode(folder.getPath());
        this.folders = folder.getFolders()
                             .stream()
                             .map(directory -> new KeyValue(directory, this.pathUri + urlEncode(directory)))
                             .collect(Collectors.toList());
        this.browseFiles = folder.getFiles()
                                 .stream()
                                 .map(filename -> new KeyValue(filename, endslash(browseUri) + urlEncode(
                                         endslash(this.path) + filename)))
                                 .collect(Collectors.toList());
        this.downloadFiles = folder.getFiles()
                                   .stream()
                                   .map(filename -> new KeyValue(filename, endslash(downloadUri) + urlEncode(
                                           endslash(folder.getPath()) + filename)))
                                   .collect(Collectors.toList());
    }

    private String urlEncode(final String path) {
        try {
            return URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    private String endslash(final String entry) {
        return entry.endsWith("/") ? entry : (entry + "/");
    }

    public String getPath() {
        return this.path;
    }

    public String getPathUri() {
        return this.pathUri;
    }

    public List<KeyValue> getFolders() {
        return this.folders;
    }

    public List<KeyValue> getBrowseFiles() {
        return this.browseFiles;
    }

    public List<KeyValue> getDownloadFiles() {
        return this.downloadFiles;
    }
}
