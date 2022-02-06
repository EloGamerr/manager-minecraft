package fr.elogamerr.manager.files;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class PlayerFileEntity extends FileEntity {

    private final String playerName;

    public PlayerFileEntity(SeveralFilesObject severalFilesObject, String playerName) {
        super(severalFilesObject);
        this.playerName = playerName;
    }

    @Override
    public String getId() {
        return this.playerName.toLowerCase();
    }

    public Player getPlayer() {
        return Bukkit.getPlayerExact(this.playerName);
    }

    public String getPlayerName() {
        return playerName;
    }
}
