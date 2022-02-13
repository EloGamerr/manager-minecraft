package fr.elogamerr.manager.files;

public abstract class FileEntity
{
    private transient SeveralFilesObject severalFilesObject;

    public FileEntity(SeveralFilesObject severalFilesObject) {
        this.severalFilesObject = severalFilesObject;
    }

    void setSeveralFilesObject(SeveralFilesObject severalFilesObject) {
        this.severalFilesObject = severalFilesObject;
    }

    public void save() {
        severalFilesObject.addOrUpdate(this);
    }

    public abstract String getId();
}
