package com.keycommand.keycommandmod.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import com.keycommand.keycommandmod.KeyCommandMod;

/**
 * 配置工具类
 * 提供统一的配置文件读写功能
 */
public class ConfigUtils {
    // ========== Properties配置文件支持 ==========

    /**
     * 读取Properties配置文件
     * @param fileName 配置文件名（相对于config/Keycommand目录）
     * @param defaultProps 默认配置属性
     * @return Properties对象
     */
    public static Properties readPropertiesConfig(String fileName, Properties defaultProps) {
        Properties props = new Properties(defaultProps);
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("config/Keycommand/" + fileName);
            KeyCommandMod.LOGGER.info("尝试读取Properties配置文件: " + path.toAbsolutePath());
            
            if (!Files.exists(path)) {
                KeyCommandMod.LOGGER.info("Properties配置文件不存在，使用默认值");
                return props;
            }

            try (FileReader reader = new FileReader(path.toFile())) {
                props.load(reader);
                KeyCommandMod.LOGGER.info("Properties配置文件加载成功: " + fileName);
            }
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("读取Properties配置文件失败: " + fileName, e);
        } catch (NumberFormatException e) {
            KeyCommandMod.LOGGER.error("Properties配置文件格式错误，使用默认值: " + fileName, e);
        }
        return props;
    }

    /**
     * 保存Properties配置文件
     * @param fileName 配置文件名（相对于config/Keycommand目录）
     * @param props 配置属性
     * @param comment 配置文件注释
     */
    public static void savePropertiesConfig(String fileName, Properties props, String comment) {
        try {
            java.nio.file.Path configDir = java.nio.file.Paths.get("config/Keycommand");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            java.nio.file.Path path = configDir.resolve(fileName);
            try (FileWriter writer = new FileWriter(path.toFile())) {
                props.store(writer, comment);
                KeyCommandMod.LOGGER.info("Properties配置文件保存成功: " + fileName);
            }
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("保存Properties配置文件失败: " + fileName, e);
        }
    }

    /**
     * 检查Properties配置文件是否存在
     * @param fileName 配置文件名（相对于config/Keycommand目录）
     * @return 是否存在
     */
    public static boolean propertiesConfigExists(String fileName) {
        java.nio.file.Path path = java.nio.file.Paths.get("config/Keycommand/" + fileName);
        return Files.exists(path);
    }
}