package fr.elogamerr.manager.files;

import org.bukkit.Bukkit;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public abstract class StaticFileObject extends FileObject
{
    transient FileManager fileManager;
    private transient long lastModified;
    transient File file;

    protected static StaticFileObject init(Class<?> commandClass, FileManager fileManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object object = commandClass.getConstructor().newInstance();
        if(object instanceof StaticFileObject)
        {
            StaticFileObject staticFileObject = (StaticFileObject) object;
            staticFileObject.fileManager = fileManager;
            staticFileObject.file = new File(staticFileObject.fileManager.getJavaPlugin().getDataFolder(), staticFileObject.getFileName());
            staticFileObject.preLoad();
            staticFileObject.tryLoad();
            staticFileObject.postLoad();
            return staticFileObject;
        }

        return null;
    }

    String getFileName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    protected boolean tryLoad() {
        if(this.lastModified == file.lastModified() && file.exists()) return false;

        this.load();

        return true;
    }

    @Override
    protected void load()
    {
        if(!file.exists())
        {
            this.save();
            this.lastModified = file.lastModified();
            return;
        }

        Bukkit.getLogger().warning("[" + this.fileManager.getJavaPlugin().getName()+"] Changements dans le fichier " + getFileName() + " détectés. Chargement des données.");
        this.lastModified = file.lastModified();

        this.loadStaticFile();
    }

    abstract void loadStaticFile();
}
