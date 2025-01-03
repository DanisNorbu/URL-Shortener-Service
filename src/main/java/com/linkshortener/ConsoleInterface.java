package main.java.com.linkshortener;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.awt.Desktop;
import java.net.URI;

//Класс для взаимодействия с пользователем через консоль. Предоставляет интерфейс для выполнения операций с сервисом сокращения ссылок.
public class ConsoleInterface {
    private UrlShortener urlShortener; // Сервис сокращения ссылок
    private Scanner scanner; // Сканер для ввода данных от пользователя
    private Config config; // Конфигурация сервиса

    // Конструктор класса ConsoleInterface
    public ConsoleInterface(String configFilePath) throws IOException {
        this.config = new Config(configFilePath); // Загружаем конфигурацию
        this.urlShortener = new UrlShortener(this.config); // Инициализируем сервис
        this.scanner = new Scanner(System.in); // Инициализируем сканер
    }

    // Запускает консольный интерфейс
    public void start() {
        System.out.println("Добро пожаловать в сервис сокращения ссылок!");

        UUID currentUserId = null; // Идентификатор текущего пользователя

        while (true) {
            displayCurrentUser(currentUserId); // Отображаем текущего пользователя

            if (currentUserId == null) {
                // Действия для неавторизованного пользователя
                System.out.println("\nВыберите действие:");
                System.out.println("1. Создать нового пользователя");
                System.out.println("2. Выбрать существующего пользователя");
                System.out.println("3. Выйти");
                System.out.print("Ваш выбор: ");

                String input = scanner.nextLine();
                try {
                    int choice = Integer.parseInt(input);
                    switch (choice) {
                        case 1:
                            currentUserId = urlShortener.createUser(); // Создаем нового пользователя
                            System.out.println("Создан новый пользователь с ID: " + currentUserId);
                            break;
                        case 2:
                            currentUserId = selectUser(); // Выбираем существующего пользователя
                            break;
                        case 3:
                            System.out.println("Выход из программы.");
                            return; // Завершаем программу
                        default:
                            System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите число от 1 до 3.");
                }
            } else {
                // Действия для авторизованного пользователя
                System.out.println("\nВыберите действие:");
                System.out.println("1. Создать короткую ссылку");
                System.out.println("2. Перейти по короткой ссылке");
                System.out.println("3. Показать мои ссылки");
                System.out.println("4. Удалить ссылку");
                System.out.println("5. Изменить лимит переходов");
                System.out.println("6. Сменить пользователя");
                System.out.println("7. Выйти");
                System.out.print("Ваш выбор: ");

                String input = scanner.nextLine();
                try {
                    int choice = Integer.parseInt(input);
                    switch (choice) {
                        case 1:
                            createShortUrl(currentUserId); // Создаем короткую ссылку
                            break;
                        case 2:
                            followShortUrl(currentUserId); // Переходим по короткой ссылке
                            break;
                        case 3:
                            showUserLinks(currentUserId); // Показываем ссылки пользователя
                            break;
                        case 4:
                            deleteShortUrl(currentUserId); // Удаляем ссылку
                            break;
                        case 5:
                            updateClickLimit(currentUserId); // Изменяем лимит переходов
                            break;
                        case 6:
                            currentUserId = null; // Сбрасываем текущего пользователя
                            break;
                        case 7:
                            System.out.println("Выход из программы.");
                            return; // Завершаем программу
                        default:
                            System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: введите число от 1 до 7.");
                }
            }
        }
    }

    // Отображает текущего пользователя
    private void displayCurrentUser(UUID userId) {
        if (userId == null) {
            System.out.println("\nТекущий пользователь: Пользователь не выбран или не создан");
        } else {
            System.out.println("\nТекущий пользователь: " + userId);
        }
    }

    // Создает короткую ссылку для текущего пользователя
    private void createShortUrl(UUID userId) {
        System.out.print("Введите длинный URL: ");
        String longUrl = scanner.nextLine();

        System.out.print("Введите лимит переходов (максимум " + config.getMaxClickLimit() + "): ");
        long clickLimit = 0;
        boolean validInput = false;
        while (!validInput) {
            try {
                String input = scanner.nextLine();
                clickLimit = Long.parseLong(input);
                if (clickLimit <= 0) {
                    System.out.println("Лимит переходов должен быть положительным числом. Попробуйте снова:");
                } else if (clickLimit > config.getMaxClickLimit()) {
                    System.out.println("Лимит переходов не может превышать " + config.getMaxClickLimit() + ". Попробуйте снова:");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число. Попробуйте снова:");
            }
        }

        System.out.print("Введите время жизни ссылки в секундах (максимум " + config.getMaxLinkLifetimeSeconds() + "): ");
        int lifetimeSeconds = 0;
        validInput = false;
        while (!validInput) {
            try {
                lifetimeSeconds = Integer.parseInt(scanner.nextLine());
                if (lifetimeSeconds <= 0) {
                    System.out.println("Время жизни ссылки должно быть положительным числом. Попробуйте снова:");
                } else if (lifetimeSeconds > config.getMaxLinkLifetimeSeconds()) {
                    System.out.println("Время жизни ссылки не может превышать " + config.getMaxLinkLifetimeSeconds() + ". Попробуйте снова:");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число. Попробуйте снова:");
            }
        }

        String shortUrl = urlShortener.buildShortUrl(userId, longUrl, clickLimit, lifetimeSeconds);
        System.out.println("Короткая ссылка создана: " + shortUrl);
    }

    // Переход по короткой ссылке
    private void followShortUrl(UUID userId) {
        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();

        String longUrl = urlShortener.restoreLongUrl(userId, shortUrl);
        if (longUrl != null) {
            System.out.println("Переход по ссылке: " + longUrl);
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    // Открываем URL в браузере
                    Desktop.getDesktop().browse(new URI(longUrl));
                } else {
                    System.out.println("Открытие ссылок в браузере не поддерживается на этой платформе.");
                }
            } catch (Exception e) {
                System.out.println("Не удалось открыть ссылку в браузере: " + e.getMessage());
            }
        }
    }

    // Показывает все ссылки текущего пользователя
    private void showUserLinks(UUID userId) {
        Map<String, UrlData> links = urlShortener.getUserLinks(userId);
        if (links.isEmpty()) {
            System.out.println("У вас нет созданных ссылок.");
        } else {
            System.out.println("Ваши ссылки:");
            for (Map.Entry<String, UrlData> entry : links.entrySet()) {
                UrlData urlData = entry.getValue();
                System.out.println(
                        "Короткая ссылка: " + entry.getKey() +
                                " -> Оригинальный URL: " + urlData.getLongUrl() +
                                " | Оставшиеся переходы: " + urlData.getRemainingClicks() +
                                " | Оставшееся время жизни (сек): " + urlData.getRemainingLifetimeSeconds()
                );
            }
        }
    }

    // Удаляет короткую ссылку
    private void deleteShortUrl(UUID userId) {
        System.out.print("Введите короткую ссылку для удаления: ");
        String shortUrl = scanner.nextLine();

        boolean isDeleted = urlShortener.deleteLink(userId, shortUrl);
        if (isDeleted) {
            System.out.println("Ссылка успешно удалена.");
        } else {
            System.out.println("Ссылка не найдена или не принадлежит вам.");
        }
    }

    // Изменяет лимит переходов для короткой ссылки
    private void updateClickLimit(UUID userId) {
        System.out.print("Введите короткую ссылку для изменения лимита переходов: ");
        String shortUrl = scanner.nextLine();
        System.out.print("Введите новый лимит переходов: ");
        int newClickLimit = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера

        boolean isUpdated = urlShortener.updateClickLimit(userId, shortUrl, newClickLimit);
        if (isUpdated) {
            System.out.println("Лимит переходов успешно изменен.");
        } else {
            System.out.println("Ссылка не найдена или не принадлежит вам.");
        }
    }

    // Позволяет выбрать существующего пользователя
    private UUID selectUser() {
        Map<UUID, User> users = urlShortener.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Нет созданных пользователей.");
            return null;
        }

        System.out.println("Список пользователей:");
        int index = 1;
        for (UUID userId : users.keySet()) {
            System.out.println(index + ". Пользователь с ID: " + userId);
            index++;
        }

        System.out.print("Выберите пользователя (введите номер): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Очистка буфера

        if (choice < 1 || choice > users.size()) {
            System.out.println("Неверный выбор.");
            return null;
        }

        return (UUID) users.keySet().toArray()[choice - 1];
    }
}