package fr.elogamerr.manager.files;

import org.bukkit.entity.Player;

public abstract class SeveralFilesObjectPlayer<T extends PlayerFileEntity> extends SeveralFilesObject<T> {
    public T get(Player player)
    {
        if(player == null) return null;

        return this.get(player.getName());
    }

    public T getOrCreate(Player player)
    {
        if(player == null) return null;

        return this.getOrCreate(player.getName());
    }
}
