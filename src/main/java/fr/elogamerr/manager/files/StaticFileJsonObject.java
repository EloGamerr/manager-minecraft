package fr.elogamerr.manager.files;

import java.io.*;

public abstract class StaticFileJsonObject extends StaticFileObject {
    @Override
    String getFileName() {
        return super.getFileName() + ".json";
    }

    @Override
    protected void preLoad() {

    }

    @Override
    void loadStaticFile() {
        try {
            FileReader reader = new FileReader(file);
            this.fileManager.getGson().fromJson(reader, this.getClass());
        }
        catch(FileNotFoundException ignored)
        {}
    }

    @Override
    protected void save() {
        this.fileManager.checkFileIsCreated(file);
        try {
            FileWriter writer = new FileWriter(file);
            this.fileManager.getGson().toJson(this, writer);
            writer.close();
        }
        catch(IOException ignored)
        {}
    }
}
