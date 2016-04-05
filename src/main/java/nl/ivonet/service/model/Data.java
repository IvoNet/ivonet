package nl.ivonet.service.model;

import nl.ivonet.service.directory.Folder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ivo Woltring
 */
@XmlRootElement
public class Data {
    @XmlElement
    private final String path;
    @XmlElement
    private final String pathUri;
    @XmlElement
    private final Map<String, String> folders;
    @XmlElement
    private final Map<String, String> browseFiles;
    @XmlElement
    private final Map<String, String> downloadFiles;


    public Data(final Folder folder, final String baseUri, final String browseUri, final String fileUri,
                final String downloadUri) {
        this.path = folder.getPath();
        this.pathUri = endslash(baseUri) + urlEncode(folder.getPath());
        this.folders = folder.getFolders()
                             .stream()
                             .collect(Collectors.toMap(directory -> directory,
                                                       directory -> endslash(baseUri) + urlEncode(directory)));
        this.browseFiles = folder.getFiles()
                                 .stream()
                                 .collect(Collectors.toMap(filename -> filename,
                                                           filename -> endslash(fileUri) + urlEncode(
                                                                   endslash(folder.getPath()) + filename)));
        this.downloadFiles = folder.getFiles()
                                   .stream()
                                   .collect(Collectors.toMap(filename -> filename,
                                                             filename -> endslash(downloadUri) + urlEncode(
                                                                     endslash(folder.getPath()) + filename)));
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

    public Map<String, String> getFolders() {
        return this.folders;
    }

    public Map<String, String> getBrowseFiles() {
        return this.browseFiles;
    }

    public Map<String, String> getDownloadFiles() {
        return this.downloadFiles;
    }
}
