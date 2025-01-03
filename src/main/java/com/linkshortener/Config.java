package main.java.com.linkshortener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//Класс для работы с конфигурационным файлом.Загружает параметры из файла config.properties и предоставляет методы для их получения.
public class Config {
    private Properties properties; // Хранилище свойств конфигурации

    // Конструктор класса Config
    public Config(String configFilePath) throws IOException {
        properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFilePath)) {
            if (inputStream == null) {
                throw new IOException("Файл " + configFilePath + " не найден в ресурсах.");
            }
            properties.load(inputStream); // Загружаем свойства из файла
        }
    }

    // Возвращает время жизни ссылки по умолчанию (в секундах)
    // Этот метод не используется, но не стал его удалять, так как думал, что можно будет заюзать как параметр, если пользоваетль не задал время жизни ссылки
    public int getDefaultLinkLifetimeSeconds() {
        return Integer.parseInt(properties.getProperty("default.link.lifetime.seconds"));
    }

    // Возвращает максимальное время жизни ссылки (в секундах)
    public int getMaxLinkLifetimeSeconds() {
        return Integer.parseInt(properties.getProperty("max.link.lifetime.seconds"));
    }

    // Возвращает лимит переходов по умолчанию.
    // Этот метод не используется, но не стал его удалять, так как думал, что можно будет заюзать как параметр, если пользоваетль не задал количество переходов
    public int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default.click.limit"));
    }

    // Возвращает максимальный лимит переходов
    public long getMaxClickLimit() {
        return Long.parseLong(properties.getProperty("max.click.limit"));
    }
}