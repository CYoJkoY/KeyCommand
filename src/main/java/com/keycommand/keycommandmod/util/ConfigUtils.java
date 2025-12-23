package com.keycommand.keycommandmod.util;

import com.keycommand.keycommandmod.KeyCommandMod;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class ConfigUtils {
    // 自动循环配置类
    public static class AutoLoopConfig {
        private boolean autoLoop;
        private String loopSequence;
        private int loopCount;

        public AutoLoopConfig(boolean autoLoop, String loopSequence, int loopCount) {
            this.autoLoop = autoLoop;
            this.loopSequence = loopSequence;
            this.loopCount = loopCount;
        }

        public boolean isAutoLoop() {
            return autoLoop;
        }

        public String getLoopSequence() {
            return loopSequence;
        }

        public int getLoopCount() {
            return loopCount;
        }
    }

    // 读取自动循环配置
    public static AutoLoopConfig readAutoLoopConfig() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("config/keycommandmod_autorun.json");
            KeyCommandMod.LOGGER.info("尝试读取配置文件: " + path.toAbsolutePath());
            if (!Files.exists(path)) {
                KeyCommandMod.LOGGER.info("配置文件不存在，跳过自动执行");
                return new AutoLoopConfig(false, "", 1);
            }

            String s = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            boolean autoLoop = s.contains("\"autoLoop\":true");
            String seq = "";
            int loopCount = 1;

            // 解析序列名
            int idx = s.indexOf("\"loopSequence\":\"");
            if (idx != -1) {
                int start = idx + "\"loopSequence\":\"".length();
                int end = s.indexOf("\"", start);
                if (end > start) seq = s.substring(start, end);
            }

            // 解析循环次数
            idx = s.indexOf("\"loopCount\":");
            if (idx != -1) {
                int start = idx + "\"loopCount\":".length();
                int end = s.indexOf("}", start);
                if (end == -1) end = s.length();
                try {
                    loopCount = Integer.parseInt(s.substring(start, end).replaceAll("[^\\d\\-]", ""));
                } catch (Exception ignore) {}
            }

            return new AutoLoopConfig(autoLoop, seq, loopCount);
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("读取自动循环配置失败", e);
            return new AutoLoopConfig(false, "", 1);
        }
    }

    // 保存自动循环配置
    public static void saveAutoLoopConfig(String sequenceName, int loopCount) {
        try {
            String json = String.format("{\"autoLoop\":true,\"loopSequence\":\"%s\",\"loopCount\":%d}", sequenceName.replace("\"", "\\\""), loopCount);
            java.nio.file.Path configDir = java.nio.file.Paths.get("config");
            if (!Files.exists(configDir)) Files.createDirectories(configDir);
            Files.write(Paths.get("config/keycommandmod_autorun.json"), json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("保存循环配置失败", e);
        }
    }

    // 清除自动循环配置
    public static void clearAutoLoopConfig() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("config/keycommandmod_autorun.json");
            if (Files.exists(path)) Files.delete(path);
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("清除循环配置失败", e);
        }
    }
}