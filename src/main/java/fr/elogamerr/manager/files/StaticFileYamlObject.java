package fr.elogamerr.manager.files;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class StaticFileYamlObject extends StaticFileObject
{
    private FileConfiguration yamlConfiguration;

    @Override
    String getFileName() {
        return super.getFileName() + ".yml";
    }

    @Override
    void loadStaticFile() {
        yamlConfiguration = YamlConfiguration.loadConfiguration(file);

        final InputStream defConfigStream = fileManager.getJavaPlugin().getResource("config.yml");
        if (defConfigStream == null) {
            return;
        }

        yamlConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    @Override
    protected void save() {
        fileManager.getJavaPlugin().saveResource(getFileName(), false);
    }

    protected FileConfiguration getYamlConfiguration() {
        if (yamlConfiguration == null) {
            loadStaticFile();
        }
        return yamlConfiguration;
    }
}
