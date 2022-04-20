package fr.elogamerr.manager.files;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class UUIDFileEntity extends FileEntity {

    private final String uuid;

    public UUIDFileEntity(SeveralFilesObject severalFilesObject, String uuid) {
        super(severalFilesObject);
        this.uuid = uuid;
    }

    @Override
    public String getId() {
        return this.uuid.toLowerCase();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(UUID.fromString(this.uuid));
    }

    public UUID getUuid() {
        return UUID.fromString(this.uuid);
    }
}
