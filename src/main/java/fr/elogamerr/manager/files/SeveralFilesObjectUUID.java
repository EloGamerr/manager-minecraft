package fr.elogamerr.manager.files;

import org.bukkit.entity.Player;

public abstract class SeveralFilesObjectUUID<T extends UUIDFileEntity> extends SeveralFilesObject<T> {
    public T get(Player player)
    {
        if(player == null) return null;

        return this.get(player.getUniqueId().toString());
    }

    public T getOrCreate(Player player)
    {
        if(player == null) return null;

        return this.getOrCreate(player.getUniqueId().toString());
    }
}
