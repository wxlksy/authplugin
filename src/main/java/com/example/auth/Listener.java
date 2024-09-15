package com.example.auth;

import com.example.auth.database.Auth;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class Listener implements org.bukkit.event.Listener {

    private Auth plugin;

    public Listener(com.example.auth.Auth auth) {
        this.plugin = plugin;
    }

    public Listener(Auth auth) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Welcome to " + plugin.getConfig().getString("server-name"));
        player.sendMessage("Please /login or /register to continue.");
    }
}