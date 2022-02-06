package fr.elogamerr.manager.files;

import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public abstract class StaticFileObject extends FileObject
{
    private transient FileManager fileManager;
    private transient long lastModified;

    protected static StaticFileObject init(Class<?> commandClass, FileManager fileManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object object = commandClass.getConstructor().newInstance();
        if(object instanceof StaticFileObject)
        {
            StaticFileObject staticFileObject = (StaticFileObject) object;
            staticFileObject.fileManager = fileManager;
            staticFileObject.preLoad();
            staticFileObject.tryLoad();
            staticFileObject.postLoad();
            return staticFileObject;
        }

        return null;
    }

    private String getFileName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    protected boolean tryLoad() {
        String fileName = this.getFileName().endsWith(".json") ? this.getFileName() : this.getFileName()+".json";
        File file = new File(this.fileManager.getJavaPlugin().getDataFolder(), fileName);

        if(this.lastModified == file.lastModified() && file.exists()) return false;

        this.load();

        return true;
    }

    @Override
    protected void load()
    {
        String fileName = this.getFileName().endsWith(".json") ? this.getFileName() : this.getFileName()+".json";
        File file = new File(this.fileManager.getJavaPlugin().getDataFolder(), fileName);

        if(!file.exists())
        {
            this.save();
            this.lastModified = file.lastModified();
            return;
        }

        Bukkit.getLogger().warning("[" + this.fileManager.getJavaPlugin().getName()+"] Changements dans le fichier " + fileName + " détectés. Chargement des données.");
        this.lastModified = file.lastModified();

        try {
            FileReader reader = new FileReader(file);
            this.fileManager.getGson().fromJson(reader, this.getClass());
            //this.save();
        }
        catch(FileNotFoundException ignored)
        {}
    }

    @Override
    public void save()
    {
        String fileName = this.getFileName().endsWith(".json") ? this.getFileName() : this.getFileName()+".json";
        File file = new File(this.fileManager.getJavaPlugin().getDataFolder(), fileName);

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
