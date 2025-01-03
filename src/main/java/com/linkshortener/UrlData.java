package main.java.com.linkshortener;

import java.time.Instant;
import java.util.UUID;

//Класс для хранения данных о ссылке.Содержит информацию о длинном URL, пользователе, лимите переходов, времени жизни и т.д.
public class UrlData {
    private String longUrl; // Оригинальный URL
    private UUID userId; // Идентификатор пользователя
    private long clickLimit; // Лимит переходов
    private long clicks; // Количество совершенных переходов
    private Instant creationTime; // Время создания ссылки
    private int lifetimeSeconds; // Время жизни ссылки в секундах

    // Конструктор класса UrlData
    public UrlData(String longUrl, UUID userId, long clickLimit, int lifetimeSeconds) {
        this.longUrl = longUrl;
        this.userId = userId;
        this.clickLimit = clickLimit;
        this.clicks = 0;
        this.creationTime = Instant.now();
        this.lifetimeSeconds = lifetimeSeconds;
    }

    // Возвращает оригинальный URL
    public String getLongUrl() {
        return longUrl;
    }

    // Возвращает идентификатор пользователя
    public UUID getUserId() {
        return userId;
    }

    // Возвращает лимит переходов
    public long getClickLimit() {
        return clickLimit;
    }

    // Устанавливает новый лимит переходов
    public void setClickLimit(long clickLimit) {
        this.clickLimit = clickLimit;
    }

    // Проверяет, доступна ли ссылка для переходов
    public boolean isClickable() {
        return clicks < clickLimit;
    }

    // Проверяет, истекло ли время жизни ссылки
    public boolean isExpired() {
        return Instant.now().isAfter(creationTime.plusSeconds(lifetimeSeconds));
    }

    // Увеличивает счетчик переходов по ссылке
    public void incrementClicks() {
        clicks++;
    }

    // Возвращает оставшееся количество переходов
    public long getRemainingClicks() {
        return clickLimit - clicks;
    }

    // Возвращает оставшееся время жизни ссылки в секундах
    public long getRemainingLifetimeSeconds() {
        long elapsedSeconds = Instant.now().getEpochSecond() - creationTime.getEpochSecond();
        return Math.max(0, lifetimeSeconds - elapsedSeconds);
    }

    // Сбрасывает счетчик переходов
    public void resetClicks() {
        this.clicks = 0;
    }
}