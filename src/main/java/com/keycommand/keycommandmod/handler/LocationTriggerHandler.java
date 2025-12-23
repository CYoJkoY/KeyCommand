package com.keycommand.keycommandmod.handler;

import com.keycommand.keycommandmod.KeyCommandMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * 位置触发处理器
 * 检测玩家是否进入指定区域，自动执行/back命令
 */
public class LocationTriggerHandler {
    // 单例实例
    private static final LocationTriggerHandler instance = new LocationTriggerHandler();
    private static final Minecraft mc = Minecraft.getMinecraft();

    // 配置项
    private boolean enabled = true;
    private double triggerX = 0.0;
    private double triggerY = 0.0;
    private double triggerZ = 0.0;
    private double triggerRadius = 3.0; // 触发半径（方块）
    private int initialDelayTicks = 20; // 初始延迟（20ticks=1秒）
    private int repeatIntervalTicks = 200; // 重复执行间隔（200ticks=10秒）

    // 状态变量（移除了未使用的hasExecutedRepeat）
    private boolean hasExecutedInitial = false;
    private int ticksInArea = 0;
    private static final String CONFIG_FILE = "config/keycommandmod_location.cfg"; // 配置文件路径

    private LocationTriggerHandler() {}

    public static LocationTriggerHandler getInstance() {
        return instance;
    }

    /**
     * 初始化处理器（注册事件、加载配置）
     */
    public void init() {
        loadConfig(); // 加载配置
        MinecraftForge.EVENT_BUS.register(this); // 注册事件
        KeyCommandMod.LOGGER.info("LocationTriggerHandler 初始化完成，配置文件路径: {}", CONFIG_FILE);
    }

    /**
     * 监听玩家Tick事件，检测位置并执行逻辑
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 过滤条件：功能禁用、非客户端、非END阶段，直接返回
        if (!enabled || event.side != Side.CLIENT || event.phase != TickEvent.Phase.END) {
            return;
        }

        EntityPlayerSP player = mc.player;
        if (player == null || player.isSpectator()) { // 玩家为空或旁观者模式，不执行
            return;
        }

        // 检测玩家是否在触发区域内
        boolean isInTriggerArea = isPlayerInTriggerArea(player);

        if (isInTriggerArea) {
            ticksInArea++; // 玩家在区域内，累计tick数

            // 初始延迟后执行第一次/back
            if (!hasExecutedInitial && ticksInArea >= initialDelayTicks) {
                executeBackCommand();
                hasExecutedInitial = true;
            }

            // 重复执行逻辑：初始延迟+间隔后执行，之后每隔间隔重复
            if (hasExecutedInitial && ticksInArea >= initialDelayTicks + repeatIntervalTicks) {
                executeBackCommand();
                ticksInArea = initialDelayTicks; // 重置计数器，实现循环执行
            }
        } else {
            // 玩家离开区域，重置所有状态
            ticksInArea = 0;
            hasExecutedInitial = false;
        }
    }

    /**
     * 检测玩家是否在触发区域内
     */
    private boolean isPlayerInTriggerArea(EntityPlayerSP player) {
        double dx = player.posX - triggerX;
        double dy = player.posY - triggerY;
        double dz = player.posZ - triggerZ;
        double distanceSq = dx * dx + dy * dy + dz * dz; // 平方距离（避免开根号，提升性能）
        return distanceSq <= triggerRadius * triggerRadius;
    }

    /**
     * 执行/back命令
     */
    private void executeBackCommand() {
        EntityPlayerSP player = mc.player; // 修复：从Minecraft实例获取玩家对象
        if (player != null && !player.isDead) {
            player.sendChatMessage("/back");
            KeyCommandMod.LOGGER.info("位置触发：执行/back命令，玩家位置({},{},{})",
                    player.posX, player.posY, player.posZ);
        }
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        // 配置文件不存在则创建默认配置
        if (!configFile.exists()) {
            saveConfig();
            KeyCommandMod.LOGGER.info("位置触发配置文件不存在，已创建默认配置");
            return;
        }

        Properties props = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            props.load(reader);

            // 读取配置项（带默认值）
            enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            triggerX = Double.parseDouble(props.getProperty("triggerX", "0.0"));
            triggerY = Double.parseDouble(props.getProperty("triggerY", "0.0"));
            triggerZ = Double.parseDouble(props.getProperty("triggerZ", "0.0"));
            triggerRadius = Double.parseDouble(props.getProperty("triggerRadius", "3.0"));
            initialDelayTicks = Integer.parseInt(props.getProperty("initialDelayTicks", "20"));
            repeatIntervalTicks = Integer.parseInt(props.getProperty("repeatIntervalTicks", "200"));

            KeyCommandMod.LOGGER.info("位置触发配置加载成功：启用={}, 触发位置({},{},{}), 半径={}",
                    enabled, triggerX, triggerY, triggerZ, triggerRadius);
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("加载位置触发配置失败", e);
        } catch (NumberFormatException e) {
            KeyCommandMod.LOGGER.error("配置文件格式错误，使用默认值", e);
        }
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        Properties props = new Properties();
        // 设置配置项
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("triggerX", String.valueOf(triggerX));
        props.setProperty("triggerY", String.valueOf(triggerY));
        props.setProperty("triggerZ", String.valueOf(triggerZ));
        props.setProperty("triggerRadius", String.valueOf(triggerRadius));
        props.setProperty("initialDelayTicks", String.valueOf(initialDelayTicks));
        props.setProperty("repeatIntervalTicks", String.valueOf(repeatIntervalTicks));

        try {
            File configFile = new File(CONFIG_FILE);
            // 创建配置文件所在目录
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            // 写入配置文件
            try (FileWriter writer = new FileWriter(configFile)) {
                props.store(writer, "KeyCommandMod Location Trigger Configuration");
            }
            KeyCommandMod.LOGGER.info("位置触发配置保存成功");
        } catch (IOException e) {
            KeyCommandMod.LOGGER.error("保存位置触发配置失败", e);
        }
    }

    // ========== 配置项的Getter/Setter（用于外部修改配置） ==========
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        saveConfig();
    }

    public double getTriggerX() {
        return triggerX;
    }

    public void setTriggerX(double triggerX) {
        this.triggerX = triggerX;
        saveConfig();
    }

    public double getTriggerY() {
        return triggerY;
    }

    public void setTriggerY(double triggerY) {
        this.triggerY = triggerY;
        saveConfig();
    }

    public double getTriggerZ() {
        return triggerZ;
    }

    public void setTriggerZ(double triggerZ) {
        this.triggerZ = triggerZ;
        saveConfig();
    }

    public double getTriggerRadius() {
        return triggerRadius;
    }

    public void setTriggerRadius(double triggerRadius) {
        this.triggerRadius = triggerRadius;
        saveConfig();
    }

    public int getInitialDelayTicks() {
        return initialDelayTicks;
    }

    public void setInitialDelayTicks(int initialDelayTicks) {
        this.initialDelayTicks = initialDelayTicks;
        saveConfig();
    }

    public int getRepeatIntervalTicks() {
        return repeatIntervalTicks;
    }

    public void setRepeatIntervalTicks(int repeatIntervalTicks) {
        this.repeatIntervalTicks = repeatIntervalTicks;
        saveConfig();
    }
}