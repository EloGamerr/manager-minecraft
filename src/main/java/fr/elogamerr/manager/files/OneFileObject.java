package fr.elogamerr.manager.files;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class OneFileObject<T> extends FileObject
{
    private FileManager fileManager;
    private T object;

    protected static OneFileObject<?> init(Class<?> commandClass, FileManager fileManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object object = commandClass.getConstructor().newInstance();
        if(object instanceof OneFileObject)
        {
            OneFileObject<?> oneFileObject = (OneFileObject<?>) object;
            oneFileObject.fileManager = fileManager;
            oneFileObject.preLoad();
            oneFileObject.load();
            oneFileObject.postLoad();
            return oneFileObject;
        }

        return null;
    }

    private String getFileName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public T get() {
        return this.object;
    }

    public void set(T object) {
        this.object = object;
    }

    protected abstract T getDefault();

    @Override
    protected void load()
    {
        String fileName = this.getFileName().endsWith(".json") ? this.getFileName() : this.getFileName()+".json";
        File file = new File(fileManager.getJavaPlugin().getDataFolder(), fileName);
        fileManager.checkFileIsCreated(file);
        T object = null;
        try {
            FileReader reader = new FileReader(file);
            object = fileManager.getGson().fromJson(reader, fileManager.getGenericType(this.getClass()));
            reader.close();
        } catch(IOException ignored)
        {}

        if(object == null)
        {
            object = this.getDefault();
        }

        this.set(object);
    }

    @Override
    protected void save()
    {
        String fileName = this.getFileName().endsWith(".json") ? this.getFileName() : this.getFileName()+".json";
        File file = new File(fileManager.getJavaPlugin().getDataFolder(), fileName);
        fileManager.checkFileIsCreated(file);

        try {
            FileWriter writer = new FileWriter(file);
            fileManager.getGson().toJson(this.get(), writer);
            writer.close();
        }
        catch(IOException ignored)
        {}
    }
}
