package com.keycommand.keycommandmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class LocationTriggerHandler {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static LocationTriggerHandler instance;
    
    // 配置相关
    private boolean enabled = true;
    private double triggerX = 0.0;
    private double triggerY = 0.0;
    private double triggerZ = 0.0;
    private double triggerRadius = 3.0; // 触发半径
    private int initialDelayTicks = 20; // 初始延迟 ticks (20 ticks = 1秒)
    private int repeatIntervalTicks = 200; // 重复间隔 ticks (200 ticks = 10秒)
    
    private boolean hasExecutedInitial = false;
    private boolean hasExecutedRepeat = false;
    private int ticksInArea = 0;
    private static final String CONFIG_FILE = "config/keycommandmod_location.cfg";
    
    public static LocationTriggerHandler getInstance() {
        if (instance == null) {
            instance = new LocationTriggerHandler();
        }
        return instance;
    }
    
    public void init() {
        loadConfig();
        MinecraftForge.EVENT_BUS.register(this);
        KeyCommandMod.LOGGER.info("LocationTriggerHandler initialized");
    }
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!enabled || event.side != Side.CLIENT || event.phase != TickEvent.Phase.END) {
            return;
        }
        
        EntityPlayerSP player = mc.player;
        if (player == null) return;
        
        // 检查玩家是否在触发区域内
        boolean isInTriggerArea = isPlayerInTriggerArea(player);
        
        if (isInTriggerArea) {
            ticksInArea++;
            
            // 初始延迟执行
            if (!hasExecutedInitial && ticksInArea >= initialDelayTicks) {
                executeBackCommand();
                hasExecutedInitial = true;
            }
            
            // 重复执行（如果玩家在区域内停留时间超过设定间隔）
            if (hasExecutedInitial && !hasExecutedRepeat && 
                ticksInArea >= initialDelayTicks + repeatIntervalTicks) {
                executeBackCommand();
                hasExecutedRepeat = true;
                // 重置计数器以实现持续重复执行
                ticksInArea = initialDelayTicks; // 重置为初始延迟时间，这样下一次重复会在repeatIntervalTicks后执行
            } else if (hasExecutedInitial && hasExecutedRepeat && 
                      ticksInArea >= initialDelayTicks + repeatIntervalTicks) {
                executeBackCommand();
                ticksInArea = initialDelayTicks; // 重置计数器以实现持续重复执行
            }
        } else {
            // 玩家离开区域，重置状态
            ticksInArea = 0;
            hasExecutedInitial = false;
            hasExecutedRepeat = false;
        }
    }
    
    private boolean isPlayerInTriggerArea(EntityPlayerSP player) {
        double dx = player.posX - triggerX;
        double dy = player.posY - triggerY;
        double dz = player.posZ - triggerZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance <= triggerRadius;
    }
    
    private void executeBackCommand() {
        if (mc.player != null && !mc.player.isSpectator()) {
            mc.player.sendChatMessage("/back");
            KeyCommandMod.LOGGER.info("Executed /back command");
        }
    }
    
    public void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            saveConfig(); // 创建默认配置文件
            return;
        }
        
        Properties props = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            props.load(reader);
            
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            triggerX = Double.parseDouble(props.getProperty("triggerX", "0.0"));
            triggerY = Double.parseDouble(props.getProperty("triggerY", "0.0"));
            triggerZ = Double.parseDouble(props.getProperty("triggerZ", "0.0"));
            triggerRadius = Double.parseDouble(props.getProperty("triggerRadius", "3.0"));
            initialDelayTicks = Integer.parseInt(props.getProperty("initialDelayTicks", "20"));
            repeatIntervalTicks = Integer.parseInt(props.getProperty("repeatIntervalTicks", "200"));
            
            KeyCommandMod.LOGGER.info("Location trigger config loaded");
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("Failed to load location trigger config", e);
        }
    }
    
    public void saveConfig() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("triggerX", String.valueOf(triggerX));
        props.setProperty("triggerY", String.valueOf(triggerY));
        props.setProperty("triggerZ", String.valueOf(triggerZ));
        props.setProperty("triggerRadius", String.valueOf(triggerRadius));
        props.setProperty("initialDelayTicks", String.valueOf(initialDelayTicks));
        props.setProperty("repeatIntervalTicks", String.valueOf(repeatIntervalTicks));
        
        try {
            File configFile = new File(CONFIG_FILE);
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(configFile)) {
                props.store(writer, "KeyCommandMod Location Trigger Configuration");
            }
            KeyCommandMod.LOGGER.info("Location trigger config saved");
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("Failed to save location trigger config", e);
        }
    }
    
    // Getters and setters for config values
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; saveConfig(); }
    
    public double getTriggerX() { return triggerX; }
    public void setTriggerX(double triggerX) { this.triggerX = triggerX; saveConfig(); }
    
    public double getTriggerY() { return triggerY; }
    public void setTriggerY(double triggerY) { this.triggerY = triggerY; saveConfig(); }
    
    public double getTriggerZ() { return triggerZ; }
    public void setTriggerZ(double triggerZ) { this.triggerZ = triggerZ; saveConfig(); }
    
    public double getTriggerRadius() { return triggerRadius; }
    public void setTriggerRadius(double triggerRadius) { this.triggerRadius = triggerRadius; saveConfig(); }
    
    public int getInitialDelayTicks() { return initialDelayTicks; }
    public void setInitialDelayTicks(int initialDelayTicks) { this.initialDelayTicks = initialDelayTicks; saveConfig(); }
    
    public int getRepeatIntervalTicks() { return repeatIntervalTicks; }
    public void setRepeatIntervalTicks(int repeatIntervalTicks) { this.repeatIntervalTicks = repeatIntervalTicks; saveConfig(); }
}
