package com.keycommand.keycommandmod.handler;

import com.keycommand.keycommandmod.KeyCommandMod;
import com.keycommand.keycommandmod.gui.GuiInventory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * 服务器连接后自动触发循环的处理器
 * 负责在玩家连接服务器并加载完成后，执行自动循环逻辑
 */
public class AutoLoopHandler {
    // 单例实例（确保全局唯一）
    private static final AutoLoopHandler instance = new AutoLoopHandler();
    private static final String CONFIG_FILE = "auto_loop.json"; // 配置文件名称（位于config/Keycommand目录）

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

    private AutoLoopHandler() {}

    public static AutoLoopHandler getInstance() {
        return instance;
    }

    /**
     * 读取自动循环配置
     */
    public AutoLoopConfig readAutoLoopConfig() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("config/Keycommand/" + CONFIG_FILE);
            KeyCommandMod.LOGGER.info("尝试读取自动循环配置文件: " + path.toAbsolutePath());
            if (!Files.exists(path)) {
                KeyCommandMod.LOGGER.info("自动循环配置文件不存在，跳过自动执行");
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

    /**
     * 保存自动循环配置
     */
    public void saveAutoLoopConfig(String sequenceName, int loopCount) {
        try {
            String json = String.format("{\"autoLoop\":true,\"loopSequence\":\"%s\",\"loopCount\":%d}", 
                sequenceName.replace("\"", "\\\""), loopCount);
            java.nio.file.Path configDir = java.nio.file.Paths.get("config/Keycommand");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            Files.write(Paths.get("config/Keycommand/" + CONFIG_FILE), json.getBytes(StandardCharsets.UTF_8));
            KeyCommandMod.LOGGER.info("自动循环配置保存成功: {}", sequenceName);
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("保存自动循环配置失败", e);
        }
    }

    /**
     * 清除自动循环配置
     */
    public void clearAutoLoopConfig() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("config/Keycommand/" + CONFIG_FILE);
            if (Files.exists(path)) {
                Files.delete(path);
                KeyCommandMod.LOGGER.info("自动循环配置已清除");
            }
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("清除自动循环配置失败", e);
        }
    }

    /**
     * 监听客户端连接服务器事件
     */
    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        KeyCommandMod.LOGGER.info("KeyCommandMod: 已连接服务器，准备启动自动循环...");

        // 启动新线程等待玩家加载（避免主线程阻塞）
        new Thread(() -> {
            int waitedSeconds = 0;
            // 最多等待15秒，防止无限循环
            while (Minecraft.getMinecraft().player == null && waitedSeconds < 15) {
                KeyCommandMod.LOGGER.info("AutoLoopHandler: 玩家尚未加载，已等待{}秒", waitedSeconds);
                try {
                    Thread.sleep(1000); // 每秒检测一次
                } catch (InterruptedException e) {
                    KeyCommandMod.LOGGER.error("等待玩家加载时线程被中断", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                waitedSeconds++;
            }

            // 检查玩家是否加载成功
            if (Minecraft.getMinecraft().player == null) {
                KeyCommandMod.LOGGER.error("等待超时，玩家对象仍为null，自动循环取消");
                return;
            }

            // 在Minecraft主线程中执行自动循环（Forge调度任务）
            Minecraft.getMinecraft().addScheduledTask(() -> {
                KeyCommandMod.LOGGER.info("AutoLoopHandler: 玩家已加载，执行自动循环检测");
                GuiInventory.tryAutoStartLoop(); // 调用GuiInventory的自动循环方法
            });
        }, "KeyCommand-AutoLoop-Waiter").start();
    }
}