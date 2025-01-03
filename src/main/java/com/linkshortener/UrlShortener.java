package main.java.com.linkshortener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//Основной класс сервиса сокращения ссылок. Реализует логику создания, хранения и управления короткими ссылками.
public class UrlShortener {

    // Алфавит для кодирования коротких ссылок
    private final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Основание для кодирования (длина алфавита)
    private final int BASE = ALPHABET.length();

    // Уникальный идентификатор для каждой новой ссылки
    private Long dbPrimaryKey = 0L;

    // Хранилище данных о ссылках (ключ - уникальный идентификатор, значение - данные о ссылке)
    private final Map<Long, UrlData> db = new HashMap<>();

    // Хранилище пользователей (ключ - UUID пользователя, значение - объект User)
    private final Map<UUID, User> users = new HashMap<>();

    // Объект конфигурации
    private final Config config;

    // Конструктор класса UrlShortener
    public UrlShortener(Config config) {
        this.config = config;
    }

    // Создает нового пользователя
    public UUID createUser() {
        UUID userId = UUID.randomUUID();
        users.put(userId, new User(userId));
        return userId;
    }

    // Возвращает список всех пользователей
    public Map<UUID, User> getAllUsers() {
        return users;
    }

    // Создает короткую ссылку для указанного пользователя
    public String buildShortUrl(UUID userId, String longUrl, long customClickLimit, int lifetimeSeconds) {
        // Очищаем просроченные и недоступные ссылки перед созданием новой
        cleanupExpiredLinks();

        // Проверяем, существует ли пользователь
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }

        // Генерируем уникальный идентификатор для новой ссылки
        dbPrimaryKey++;

        // Ограничиваем лимит переходов и время жизни ссылки значениями из конфигурации
        long clickLimit = Math.min(customClickLimit, config.getMaxClickLimit());
        int actualLifetimeSeconds = Math.min(lifetimeSeconds, config.getMaxLinkLifetimeSeconds());

        // Создаем объект UrlData для хранения информации о ссылке
        UrlData urlData = new UrlData(longUrl, userId, clickLimit, actualLifetimeSeconds);

        // Добавляем ссылку в хранилище
        db.put(dbPrimaryKey, urlData);

        // Генерируем короткую ссылку
        String shortString = "clck.ru/" + encodePrimaryKeyToShortString(dbPrimaryKey);

        // Добавляем ссылку в коллекцию пользователя
        user.addLink(shortString, urlData);

        // Возвращаем короткую ссылку
        return shortString;
    }

    // Восстанавливает оригинальный URL по короткой ссылке
    public String restoreLongUrl(UUID userId, String shortString) {
        String shortCode = shortString.replace("clck.ru/", "");
        Long primaryKey = decodeShortStringToPrimaryKey(shortCode);
        UrlData urlData = db.get(primaryKey);

        if (urlData == null) {
            notifyUser(userId, "Ссылка не найдена.");
            return null;
        }

        if (!urlData.getUserId().equals(userId)) {
            notifyUser(userId, "Ссылка не принадлежит вам.");
            return null;
        }

        if (urlData.isExpired()) {
            notifyUser(userId, "Ссылка истекла.");
            return null;
        }

        if (!urlData.isClickable()) {
            notifyUser(userId, "Лимит переходов исчерпан.");
            return null;
        }

        urlData.incrementClicks();
        return urlData.getLongUrl();
    }

    // Возвращает все ссылки, созданные указанным пользователем
    public Map<String, UrlData> getUserLinks(UUID userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }
        return user.getLinks();
    }

    // Удаляет короткую ссылку, если она принадлежит указанному пользователю
    public boolean deleteLink(UUID userId, String shortUrl) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }

        // Проверяем, принадлежит ли ссылка пользователю
        if (user.getLinks().containsKey(shortUrl)) {
            // Удаляем ссылку из хранилища
            db.values().removeIf(urlData -> urlData.getUserId().equals(userId) && urlData.getLongUrl().equals(user.getLinks().get(shortUrl).getLongUrl()));

            // Удаляем ссылку у пользователя
            user.getLinks().remove(shortUrl);
            return true;
        }

        return false;
    }

    // Изменяет лимит переходов для указанной короткой ссылки
    public boolean updateClickLimit(UUID userId, String shortUrl, int newClickLimit) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }

        // Проверяем, принадлежит ли ссылка пользователю
        if (user.getLinks().containsKey(shortUrl)) {
            UrlData urlData = user.getLinks().get(shortUrl);
            urlData.setClickLimit(newClickLimit);
            urlData.resetClicks(); // Сбрасываем счетчик переходов
            return true;
        }

        return false;
    }

    // Кодирует уникальный идентификатор (primary key) в короткую строку
    private String encodePrimaryKeyToShortString(Long dbPrimaryKey) {
        StringBuilder sb = new StringBuilder();
        while (dbPrimaryKey > 0) {
            Long remainder = dbPrimaryKey % BASE;
            sb.append(ALPHABET.charAt(remainder.intValue()));
            dbPrimaryKey = dbPrimaryKey / BASE;
        }

        // Дополняем строку до шести символов, если она короче
        while (sb.length() < 6) {
            sb.append(ALPHABET.charAt(0)); // Добавляем первый символ алфавита (например, 'a')
        }

        // Обрезаем строку до шести символов, если она длиннее
        if (sb.length() > 6) {
            sb.setLength(6);
        }

        return sb.reverse().toString();
    }

    // Декодирует короткую строку в уникальный идентификатор (primary key)
    private Long decodeShortStringToPrimaryKey(String shortString) {
        Long result = 0L;
        for (char character : shortString.toCharArray()) {
            int charIndex = ALPHABET.indexOf(character);
            result = result * BASE + charIndex;
        }
        return result;
    }

    // Уведомляет пользователя о событии (например, недоступности ссылки)
    private void notifyUser(UUID userId, String message) {
        System.out.println("Пользователь " + userId + ": " + message);
    }

    // Удаляет просроченные и недоступные ссылки из хранилища
    public void cleanupExpiredLinks() {
        db.entrySet().removeIf(entry -> {
            UrlData urlData = entry.getValue();
            if (urlData.isExpired() || !urlData.isClickable()) {
                // Удаляем ссылку из коллекции пользователя
                User user = users.get(urlData.getUserId());
                if (user != null) {
                    user.getLinks().values().removeIf(data -> data.equals(urlData));
                }
                return true; // Удаляем ссылку из общего хранилища
            }
            return false; // Не удаляем ссылку
        });
    }
}