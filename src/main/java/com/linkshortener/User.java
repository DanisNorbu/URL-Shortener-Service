package main.java.com.linkshortener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Класс для хранения информации о пользователе.Содержит идентификатор пользователя и его ссылки
public class User {
    private UUID userId; // Идентификатор пользователя
    private Map<String, UrlData> links = new HashMap<>(); // Ссылки пользователя

    // Конструктор класса User
    public User(UUID userId) {
        this.userId = userId;
    }

    // Добавляет ссылку пользователю
    public void addLink(String shortUrl, UrlData urlData) {
        links.put(shortUrl, urlData);
    }

    // Возвращает все ссылки пользователя
    public Map<String, UrlData> getLinks() {
        return links;
    }

    // Возвращает идентификатор пользователя
    public UUID getUserId() {
        return userId;
    }
}