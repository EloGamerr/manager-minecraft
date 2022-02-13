package fr.elogamerr.manager.files;

import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SeveralFilesObject<T extends FileEntity> extends FileObject
{
    private FileManager fileManager;
    private HashMap<String, T> idTofileEntities;
    private List<T> fileEntities;
    private List<String> filesWithRunningSave;
    private ConcurrentHashMap<String, T> waitingSaves;

    protected static SeveralFilesObject<?> init(Class<?> commandClass, FileManager fileManager) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object object = commandClass.getConstructor().newInstance();
        if(object instanceof SeveralFilesObject)
        {
            SeveralFilesObject<?> severalFilesObject = (SeveralFilesObject<?>) object;
            severalFilesObject.fileManager = fileManager;
            severalFilesObject.idTofileEntities = new HashMap<>();
            severalFilesObject.fileEntities = new ArrayList<>();
            severalFilesObject.saveThread();
            severalFilesObject.preLoad();
            severalFilesObject.load();
            severalFilesObject.postLoad();
            return severalFilesObject;
        }
        return null;
    }

    private String getDirectoryName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public HashMap<String, T> getIdToFileEntities()
    {
        return this.idTofileEntities;
    }

    public List<T> getFileEntities()
    {
        return this.fileEntities;
    }

    public HashMap<String, T> getIdToFileEntitiesThreadSafe()
    {
        HashMap<String, T> map = new HashMap<>();

        File folder = new File(this.fileManager.getJavaPlugin().getDataFolder(), this.getDirectoryName());
        this.fileManager.checkFolderIsCreated(folder);

        Type type = this.fileManager.getGenericType(this.getClass());
        for(File file : folder.listFiles())
        {
            try {
                FileReader reader = new FileReader(file);
                T object = this.fileManager.getGson().fromJson(reader, type);
                if(object != null)
                {
                    map.put(object.getId(), object);
                }
                reader.close();
            } catch(IOException ignored)
            {}
        }

        return map;
    }

    public List<T> getFileEntitiesThreadSafe()
    {
        List<T> list = new ArrayList<>();

        File folder = new File(this.fileManager.getJavaPlugin().getDataFolder(), this.getDirectoryName());
        this.fileManager.checkFolderIsCreated(folder);
        Type type = this.fileManager.getGenericType(this.getClass());
        for(File file : folder.listFiles())
        {
            try {
                FileReader reader = new FileReader(file);
                try {
                    T object = this.fileManager.getGson().fromJson(reader, type);
                    if(object != null)
                    {
                        list.add(object);
                    }
                    reader.close();
                } catch(JsonParseException ex) {
                    Bukkit.getLogger().warning("Impossible de charger le fichier " + file.getName() + " dans le répertoire " + this.getDirectoryName());
                    ex.printStackTrace();
                }
            } catch(IOException ignored)
            {}
        }

        return list;
    }

    public T get(String id)
    {
        return this.idTofileEntities.get(id.toLowerCase());
    }

    public T getOrCreate(String id)
    {
        T object = this.get(id);
        return object == null ? this.create(id) : object;
    }

    private T create(String id) {
        T object = this.newFileEntity(id);
        this.addOrUpdate(object);
        return object;
    }

    protected abstract T newFileEntity(String id);

    public boolean contains(String id)
    {
        return this.idTofileEntities.get(id.toLowerCase()) != null;
    }

    public T getOrDefault(String id, T fileEntity)
    {
        T object = this.idTofileEntities.get(id.toLowerCase());
        if(object == null)
        {
            return fileEntity;
        }

        return object;
    }

    public T getOrDefaultAndCreate(String id, T fileEntity)
    {
        T object = this.idTofileEntities.get(id.toLowerCase());
        if(object == null)
        {
            this.addOrUpdate(fileEntity);
            return fileEntity;
        }

        return object;
    }

    public void addOrUpdate(final T fileEntity)
    {
        if(!this.getFileEntities().contains(fileEntity))
        {
            T object = this.getIdToFileEntities().get(fileEntity.getId().toLowerCase());
            if(object != null)
            {
                this.getFileEntities().remove(object);
            }
            this.getIdToFileEntities().put(fileEntity.getId().toLowerCase(), fileEntity);
            this.getFileEntities().add(fileEntity);
        }

        String fileName = fileEntity.getId().endsWith(".json") ? fileEntity.getId() : fileEntity.getId()+".json";
        waitingSaves.put(fileName, fileEntity);
    }

    private void saveThread() {
        waitingSaves = new ConcurrentHashMap<>();
        filesWithRunningSave = new ArrayList<>();

        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Iterator<Map.Entry<String, T>> it = waitingSaves.entrySet().iterator();

                while(it.hasNext()) {
                    Map.Entry<String, T> entry = it.next();
                    String fileName = entry.getKey();
                    if(!filesWithRunningSave.contains(fileName)) {
                        filesWithRunningSave.add(fileName);
                        T fileEntity = entry.getValue();
                        if(fileEntity != null) {
                            File file = new File(fileManager.getJavaPlugin().getDataFolder()+"/"+getDirectoryName(), fileName);
                            fileManager.checkFileIsCreated(file);

                            try {
                                FileWriter writer = new FileWriter(file);
                                fileManager.getGson().toJson(fileEntity, writer);
                                writer.close();
                            }
                            catch(IOException| ConcurrentModificationException ex)
                            {
                                ex.printStackTrace();
                                new Throwable().printStackTrace();
                                System.out.println("DEBUG: " + fileManager.getJavaPlugin().getName());
                            }
                        }
                        filesWithRunningSave.remove(fileName);
                    }

                    it.remove();
                }
            }
        }).start();
    }

    public void remove(final String id)
    {
        final FileEntity fileEntity1 = this.getIdToFileEntities().get(id.toLowerCase());
        this.getFileEntities().remove(fileEntity1);
        this.getIdToFileEntities().remove(id.toLowerCase());

        new Thread(() -> {
            String fileName = fileEntity1.getId().endsWith(".json") ? fileEntity1.getId() : fileEntity1.getId()+".json";
            File file = new File(fileManager.getJavaPlugin().getDataFolder()+"/"+getDirectoryName(), fileName);

            if(file.exists())
            {
                file.delete();
            }
        }).start();
    }

    @Override
    protected void load()
    {
        File folder = new File(this.fileManager.getJavaPlugin().getDataFolder(), this.getDirectoryName());
        this.fileManager.checkFolderIsCreated(folder);

        Type type = this.fileManager.getGenericType(this.getClass());

        for(File file : folder.listFiles())
        {
            try {
                FileReader reader = new FileReader(file);
                try {
                    T object = this.fileManager.getGson().fromJson(reader, type);

                    if(object != null)
                    {
                        object.setSeveralFilesObject(this);
                        this.getIdToFileEntities().put(object.getId().toLowerCase(), object);
                        this.getFileEntities().add(object);
                    }
                    reader.close();
                } catch(JsonParseException ex) {
                    Bukkit.getLogger().warning("Impossible de charger le fichier " + file.getName() + " dans le répertoire " + this.getDirectoryName());
                    ex.printStackTrace();
                }
            } catch(IOException ignored)
            {}
        }
    }

    @Deprecated
    protected void save() {

    }
}
