package fr.elogamerr.manager.files;

public abstract class FileEntity
{
    private transient final SeveralFilesObject severalFilesObject;

    public FileEntity(SeveralFilesObject severalFilesObject) {
        this.severalFilesObject = severalFilesObject;
    }

    public void save() {
        severalFilesObject.addOrUpdate(this);
    }

    public abstract String getId();
}
