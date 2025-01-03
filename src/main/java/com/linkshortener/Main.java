package main.java.com.linkshortener;

import java.io.IOException;

public class Main {
    // Точка входа в программу
    public static void main(String[] args) {
        try {
            // Создаем консольный интерфейс и запускаем его
            ConsoleInterface consoleInterface = new ConsoleInterface("main/resources/config.properties");
            consoleInterface.start();
        } catch (IOException e) {
            // Обрабатываем ошибку загрузки конфигурации
            System.out.println("Ошибка при загрузке конфигурации: " + e.getMessage());
        }
    }
}