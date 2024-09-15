package com.example.auth;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Auth extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        // Сохранение конфигурации при первом запуске
        saveDefaultConfig();
        connectDatabase();

        // Регистрируем события и команды
        Bukkit.getPluginManager().registerEvents(new Listener(this), this);
    }

    @Override
    public void onDisable() {
        // Закрываем соединение с базой данных
        closeDatabase();
    }

    private void connectDatabase() {
        String url = "jdbc:mysql://localhost:3306/minecraft";
        String username = "root";
        String password = "password";
        try {
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("Database connected.");
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("Could not connect to database.");
        }
    }

    private void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                getLogger().info("Database connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /login <password>");
                return true;
            }
            String password = args[0];
            loginPlayer(player, password);
        }

        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /register <password>");
                return true;
            }
            String password = args[0];
            registerPlayer(player, password);
        }

        return true;
    }

    private void loginPlayer(Player player, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE uuid = ? AND password = ?");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                player.sendMessage("Successfully logged in!");
            } else {
                player.sendMessage("Incorrect password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("An error occurred while trying to log in.");
        }
    }

    private void registerPlayer(Player player, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (uuid, username, password) VALUES (?, ?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.setString(3, password);
            ps.executeUpdate();
            player.sendMessage("Successfully registered!");
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("An error occurred while trying to register.");
        }
    }
}