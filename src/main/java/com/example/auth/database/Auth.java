package com.example.auth.database;

import com.example.auth.Listener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Auth extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String url = "jdbc:mysql://localhost:3306/minecraft";
        String username = "root";
        String password = "password";

        try {
            // Подключаемся к базе данных MySQL
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("Подключение к базе данных успешно.");
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().severe("Не удалось подключиться к базе данных. Отключаем плагин.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Регистрируем обработчики событий
        Bukkit.getPluginManager().registerEvents(new Listener(this), this);
    }

    @Override
    public void onDisable() {
        // Закрываем подключение к базе данных при отключении плагина
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length != 1) {
                player.sendMessage("Использование: /login <пароль>");
                return true;
            }

            String password = args[0];

            if (loginPlayer(player.getUniqueId().toString(), password)) {
                player.sendMessage("Успешный вход в систему!");
            } else {
                player.sendMessage("Неверный пароль или вы не зарегистрированы.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length != 1) {
                player.sendMessage("Использование: /register <пароль>");
                return true;
            }

            String password = args[0];
            String uuid = player.getUniqueId().toString();
            String username = player.getName();

            if (!isPlayerRegistered(uuid)) {
                if (registerPlayer(uuid, username, password)) {
                    player.sendMessage("Вы успешно зарегистрированы!");
                } else {
                    player.sendMessage("Произошла ошибка при регистрации.");
                }
            } else {
                player.sendMessage("Вы уже зарегистрированы.");
            }
            return true;
        }

        return false;
    }

    // Метод для проверки, зарегистрирован ли игрок
    private boolean isPlayerRegistered(String uuid) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Метод для регистрации игрока
    private boolean registerPlayer(String uuid, String username, String password) {
try {
PreparedStatement statement = connection.prepareStatement("INSERT INTO players (uuid, username, password) VALUES (?, ?, ?)");
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.setString(3, password); // Здесь желательно хешировать пароль
            statement.executeUpdate();
            return true;
                    } catch (SQLException e) {
        e.printStackTrace();
        }
                return false;
                }

// Метод для входа игрока
private boolean loginPlayer(String uuid, String password) {
    try {
        PreparedStatement statement = connection.prepareStatement("SELECT password FROM players WHERE uuid = ?");
        statement.setString(1, uuid);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");
            return storedPassword.equals(password); // Сравнение пароля, хеширование нужно добавить для безопасности
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
}