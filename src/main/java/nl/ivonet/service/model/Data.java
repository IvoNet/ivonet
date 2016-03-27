package nl.ivonet.service.model;

import nl.ivonet.service.directory.Folder;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ivo Woltring
 */
@XmlRootElement
public class Data extends Metadata {
    private Folder folder;

    public Data(final Folder folder) {
        this.folder = folder;
    }

    public Folder getFolder() {
        return this.folder;
    }

    public void setFolder(final Folder folder) {
        this.folder = folder;
    }

}
