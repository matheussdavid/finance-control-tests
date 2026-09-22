package br.com.financecontrol.utils;

import br.com.financecontrol.config.ConfigManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/** Captura de evidências de falha: screenshot + URL + título da página. */
public final class ScreenshotUtil {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);

    private ScreenshotUtil() {
    }

    public static Path capture(WebDriver driver, String className, String methodName) {
        Path dir = Paths.get(ConfigManager.SCREENSHOT_DIR).toAbsolutePath().normalize();
        String base = "%s_%s_%d".formatted(className, methodName, Instant.now().toEpochMilli());
        Path shot = dir.resolve(base + ".png");
        Path meta = dir.resolve(base + ".txt");

        try {
            Files.createDirectories(dir);
            File raw = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(raw.toPath(), shot, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(meta, """
                    Browser URL : %s
                    Título      : %s
                    """.formatted(driver.getCurrentUrl(), driver.getTitle()));
            log.error("Falha registrada — screenshot: {} | url: {}", shot, driver.getCurrentUrl());
            return shot;
        } catch (IOException | ClassCastException e) {
            log.error("Não foi possível capturar evidência de falha: {}", e.getMessage());
            return null;
        }
    }
}