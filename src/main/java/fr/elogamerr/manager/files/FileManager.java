package fr.elogamerr.manager.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileManager
{
    private final Gson gson;
    private final JavaPlugin javaPlugin;
    private final HashMap<Class<? extends OneFileObject>, OneFileObject<?>> oneFileObjectList;
    private final HashMap<Class<? extends StaticFileObject>, StaticFileObject> staticFileObjectList;

    public FileManager(JavaPlugin javaPlugin, String filesPackage)
    {
        this(javaPlugin, filesPackage, new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE));
    }

    public FileManager(JavaPlugin javaPlugin, String filesPackage, GsonBuilder gsonBuilder)
    {
        this.javaPlugin = javaPlugin;
        this.gson = gsonBuilder.create();

        this.oneFileObjectList = new HashMap<>();
        this.staticFileObjectList = new HashMap<>();

        try
        {
            ArrayList<String> packageFiles = getPackageContentJar(javaPlugin, filesPackage);
            for(String packageFile : packageFiles)
            {
                if(packageFile != null && packageFile.endsWith(".class"))
                {
                    Class<?> commandClass = Class.forName(packageFile.replaceAll("\\.class", "").replaceAll("/", "."), true, javaPlugin.getClass().getClassLoader());
                    if(StaticFileObject.class.equals(commandClass.getSuperclass()))
                    {
                        StaticFileObject staticFileObject = StaticFileObject.init(commandClass, this);
                        if(staticFileObject != null) {
                            staticFileObjectList.put(staticFileObject.getClass(), staticFileObject);
                            this.staticFileObjectList.put(staticFileObject.getClass(), staticFileObject);
                        }
                    }
                    else if(SeveralFilesObject.class.equals(commandClass.getSuperclass()) || SeveralFilesObjectPlayer.class.equals(commandClass.getSuperclass()))
                    {
                        SeveralFilesObject.init(commandClass, this);
                    }
                    else if(OneFileObject.class.equals(commandClass.getSuperclass()))
                    {
                        OneFileObject<?> oneFileObject = OneFileObject.init(commandClass, this);
                        if(oneFileObject != null)
                            this.oneFileObjectList.put(oneFileObject.getClass(), oneFileObject);
                    }
                }
            }

            javaPlugin.getServer().getPluginManager().registerEvents(new FileListeners(this), javaPlugin);

            new BukkitRunnable()
            {
                public void run()
                {
                    save();
                }
            }.runTaskTimer(javaPlugin, 30*60*20, 30*60*20);

            new BukkitRunnable()
            {
                public void run()
                {
                    reloadStaticFiles();
                }
            }.runTaskTimer(javaPlugin, 10*20, 10*20);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public void checkFileIsCreated(File file)
    {
        if(!file.getParentFile().exists())
        {
            file.getParentFile().mkdir();
        }

        if(!file.exists())
        {
            try {
                file.createNewFile();
            }
            catch(IOException e)
            {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
    }

    public void checkFolderIsCreated(File folder)
    {
        if(!folder.getParentFile().exists())
        {
            folder.getParentFile().mkdir();
        }

        if(!folder.exists())
        {
            folder.mkdir();
        }
    }

    public Gson getGson() {
        return gson;
    }

    public static ArrayList<String> getPackageContentJar(JavaPlugin plugin, String commandsPackage) throws IOException
    {
        ArrayList<String> content = new ArrayList<String>();
        String commandPackagePath = commandsPackage;
        commandPackagePath = commandPackagePath.replaceAll("\\.", "/").replaceAll("\\\\", "/");
        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while(true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null)
                    break;
                String name = e.getName();
                if (name.startsWith(commandPackagePath)) {
                    content.add(name);
                }
            }
        }
        return content;
    }

    public void save()
    {
        for(OneFileObject<?> oneFileObject : this.oneFileObjectList.values())
        {
            oneFileObject.save();
        }
    }

    public void reloadStaticFiles()
    {
        for(StaticFileObject staticFileObject : this.staticFileObjectList.values())
        {
            staticFileObject.tryLoad();
        }
    }

    protected Type getGenericType(Class<?> clazz) {
        Type superclass = clazz.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }
}
