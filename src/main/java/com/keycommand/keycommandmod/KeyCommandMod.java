// KeyCommandMod.java

package com.keycommand.keycommandmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.input.Keyboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mod(modid = KeyCommandMod.MODID, name = KeyCommandMod.NAME, version = KeyCommandMod.VERSION)
public class KeyCommandMod {
    public static final String MODID = "keycommandmod";
    public static final String NAME = "Key Command Mod";
    public static final String VERSION = "1.0";

    public static final Logger LOGGER = LogManager.getLogger(KeyCommandMod.class);
    public static KeyCommandMod instance;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final KeyBinding teleKey = new KeyBinding("传送", Keyboard.KEY_GRAVE, "key.categories.keycommand");
    private static final KeyBinding srpOpenKey = new KeyBinding("灵魂空间", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_I, "key.categories.keycommand");
    private static final KeyBinding menuKey = new KeyBinding("菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_O, "key.categories.keycommand");
    private static final KeyBinding ecKey = new KeyBinding("末影箱", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_U, "key.categories.keycommand");
    private static final KeyBinding hbKey = new KeyBinding("货币兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_P, "key.categories.keycommand");
    private static final KeyBinding nzwKey = new KeyBinding("农作物兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_L, "key.categories.keycommand");
    private static final KeyBinding guiKey = new KeyBinding("快捷菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_T, "key.categories.keycommand");
    
    public static final GuiInventory.PathSequenceManager pathSequenceManager = new GuiInventory.PathSequenceManager();
    static {
        GuiInventory.initializePathSequences();
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        ClientRegistry.registerKeyBinding(teleKey);
        ClientRegistry.registerKeyBinding(srpOpenKey);
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(ecKey);
        ClientRegistry.registerKeyBinding(hbKey);
        ClientRegistry.registerKeyBinding(nzwKey);
        ClientRegistry.registerKeyBinding(guiKey);

        // Register the event listener
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Key Command Mod initialized!");
        
        MinecraftForge.EVENT_BUS.register(new AutoLoopHandler());
        
        GuiInventory.initializePathSequences();
    }

    public static void tryAutoStartLoop() {
        try {
            Path path = Paths.get("config/keycommandmod_autorun.json");
            LOGGER.info("尝试读取配置文件: " + path.toAbsolutePath());
            if (!Files.exists(path)) {
                LOGGER.info("配置文件不存在，跳过自动执行");
                return;
            }

            String s = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            // 简单解析
            boolean autoLoop = s.contains("\"autoLoop\":true");
            String seq = "";
            int loopCount = 1;
            int idx = s.indexOf("\"loopSequence\":\"");
            if (idx != -1) {
                int start = idx + "\"loopSequence\":\"".length();
                int end = s.indexOf("\"", start);
                if (end > start) seq = s.substring(start, end);
            }
            idx = s.indexOf("\"loopCount\":");
            if (idx != -1) {
                int start = idx + "\"loopCount\":".length();
                int end = s.indexOf("}", start);
                if (end == -1) end = s.length();
                try {
                    loopCount = Integer.parseInt(s.substring(start, end).replaceAll("[^\\d\\-]", ""));
                } catch (Exception ignore) {}
            }
            if (autoLoop && !seq.isEmpty() && loopCount == -1) {
                LOGGER.info("检测到需要自动无限循环执行：" + seq);
                final String fSeq = seq;
                final int fLoopCount = loopCount;
                // 直接用 Minecraft.getMinecraft().addScheduledTask 兼容性更好
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    GuiInventory.loopCount = fLoopCount;
                    GuiInventory.loopCounter = 0;
                    GuiInventory.isLooping = true;
                    GuiInventory.runPathSequence(fSeq);
                });
            }
        } catch (Exception e) {
            LOGGER.error("读取自动循环配置失败", e);
        }
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) { // Check if the key is pressed down
            if (teleKey.isPressed()) {
                sendChatMessage("/jump");
                LOGGER.info("Sent command: /jump");
            }
            if (srpOpenKey.isKeyDown()) {
                sendChatMessage("/srp open");
                LOGGER.info("Sent command: /srp open");
            }
            if (ecKey.isKeyDown()) {
                sendChatMessage("/ec");
                LOGGER.info("Sent command: /ec");
            }
            if (menuKey.isKeyDown()) {
                sendChatMessage("/menu");
                LOGGER.info("Sent command: /menu");
            }
            if (hbKey.isKeyDown()) {
                sendChatMessage("/sre open 货币兑换");
                LOGGER.info("Sent command: /sre open 货币兑换");
            }
            if (nzwKey.isKeyDown()) {
                sendChatMessage("/sre open 农作物");
                LOGGER.info("Sent command: /sre open 农作物");
            }
            if (guiKey.isKeyDown()) {
                mc.displayGuiScreen(new GuiInventory());
                LOGGER.info("Opened custom inventory GUI.");
            }
        } else {
            LOGGER.debug("Key released.");
        }
    }

    private void sendChatMessage(String message) {
        if (!mc.player.isSpectator()) {
            mc.player.sendChatMessage(message);
        }
    }

    public static class GuiInventory extends GuiScreen {
        private static final Minecraft mc = Minecraft.getMinecraft();
        // 添加静态变量保存上次的状态
        private static String sLastCategory = "每日";
        private static int sLastPage = 0;
        private static final Map<String, Integer> CATEGORY_PAGE_MAP = new HashMap<>();
        
        private int currentPage = sLastPage;
        private String currentCategory = sLastCategory;
        private final List<String> categories = Arrays.asList("每日", "商店", "传送", "自动操作");
        private final Map<String, List<String>> categoryItems = new HashMap<>();
        private final Map<String, List<String>> categoryItemNames = new HashMap<>();
        // 路径序列管理器
        public static final PathSequenceManager pathSequenceManager = new PathSequenceManager();

        private static int loopCount = 1; // 默认循环1次
        private static int loopCounter = 0; // 当前运行次数计数
        private static boolean isLooping = false; // 是否正在循环中

        public GuiInventory() {
            // 恢复当前分类和页码
            if (CATEGORY_PAGE_MAP.containsKey(currentCategory)) {
                currentPage = CATEGORY_PAGE_MAP.get(currentCategory);
            } else {
                currentPage = sLastPage;
            }
            
            // 初始化路径序列管理器
            initializePathSequences();
            
            // 初始化分类物品
            
            // 每日分类
            List<String> DailyItems = new ArrayList<>();
            List<String> DailyItemNames = new ArrayList<>();
            
            DailyItems.add("/res tp zhanbu");DailyItemNames.add("传占卜");
            DailyItems.add("/res tp viplb");DailyItemNames.add("传礼包");
            DailyItems.add("/res tp yyzh");DailyItemNames.add("传浇花");
            DailyItems.add("/res tp mnrs");DailyItemNames.add("传模拟");
            DailyItems.add("/res tp rw");DailyItemNames.add("传任务");
            DailyItems.add("/res tp pk");DailyItemNames.add("传跑酷");
            DailyItems.add("/res tp wrx");DailyItemNames.add("传温柔");
            DailyItems.add("/res tp pxxd");DailyItemNames.add("传破晓");
            
            categoryItems.put("每日", DailyItems);
            categoryItemNames.put("每日", DailyItemNames);
            
            // 商店分类
            List<String> ShopItems = new ArrayList<>();
            List<String> ShopItemNames = new ArrayList<>();
            
            ShopItems.add("/cshop open 餐厅");ShopItemNames.add("食物");
            ShopItems.add("/cshop open 染料");ShopItemNames.add("染料");
            ShopItems.add("/res tp xyq");ShopItemNames.add("传木牌");
            ShopItems.add("/res tp gezi");ShopItemNames.add("传圆石");
            ShopItems.add("/res tp dp");ShopItemNames.add("传工具");
            
            ShopItems.add("/cshop open 防疫套兑换");ShopItemNames.add("1-1");
            ShopItems.add("/cshop open 寄生者兑换");ShopItemNames.add("1-2");
            ShopItems.add("/cshop open 异变");ShopItemNames.add("1-3");
            ShopItems.add("/cshop open 1-5");ShopItemNames.add("1-4");
            ShopItems.add("/cshop open 信仰");ShopItemNames.add("1-5");
            ShopItems.add("/cshop open 熔岩");ShopItemNames.add("1-6");
            ShopItems.add("/cshop open 2-1");ShopItemNames.add("2-1");
            ShopItems.add("/cshop open 2-2");ShopItemNames.add("2-2");
            ShopItems.add("/cshop open 2-3");ShopItemNames.add("2-3");
            ShopItems.add("/cshop open 2-4");ShopItemNames.add("2-4");
            ShopItems.add("/cshop open 2-5");ShopItemNames.add("2-5");
            ShopItems.add("/cshop open 2-6");ShopItemNames.add("2-6");
            ShopItems.add("/cshop open 2-7");ShopItemNames.add("2-7");
            ShopItems.add("/cshop open 3-1");ShopItemNames.add("3-1");
            ShopItems.add("/cshop open 3-2");ShopItemNames.add("3-2");
            ShopItems.add("/cshop open 3-3");ShopItemNames.add("3-3");
            ShopItems.add("/cshop open 3-5");ShopItemNames.add("3-5");
            ShopItems.add("/cshop open 3-6");ShopItemNames.add("3-6");
            ShopItems.add("/cshop open 3-7");ShopItemNames.add("3-7");
            ShopItems.add("/cshop open 玲珑玉");ShopItemNames.add("玲珑玉");
            ShopItems.add("/cshop open 3-8-2");ShopItemNames.add("3-8");
            ShopItems.add("/cshop open 4-1");ShopItemNames.add("4-1");
            ShopItems.add("/cshop open 4-2");ShopItemNames.add("4-2");
            ShopItems.add("/cshop open 4-3");ShopItemNames.add("4-3");
            ShopItems.add("/cshop open 4-4");ShopItemNames.add("4-4");
            ShopItems.add("/cshop open 4-5");ShopItemNames.add("4-5");
            ShopItems.add("/cshop open 4-6");ShopItemNames.add("4-6");
            ShopItems.add("/cshop open 4-7");ShopItemNames.add("4-7");
            ShopItems.add("/cshop open 4-8");ShopItemNames.add("4-8");
            ShopItems.add("/cshop open 动漫篇碎片兑换");ShopItemNames.add("宙斯弓");
            ShopItems.add("/cshop open 5-1");ShopItemNames.add("5-1");
            ShopItems.add("/cshop open 5-2");ShopItemNames.add("5-2");
            ShopItems.add("/cshop open 5-3");ShopItemNames.add("5-3");
            ShopItems.add("/cshop open 5-3副手");ShopItemNames.add("5-3副手");
            ShopItems.add("/cshop open 5-4");ShopItemNames.add("5-4");
            ShopItems.add("/cshop open 夜之城");ShopItemNames.add("5-5");
            ShopItems.add("/cshop open 6-1");ShopItemNames.add("6-1");
            ShopItems.add("/sre open 6-1");ShopItemNames.add("6-1中转");
            ShopItems.add("/cshop open 6-2");ShopItemNames.add("6-2");
            ShopItems.add("/sre open 6-2");ShopItemNames.add("6-2中转");
            ShopItems.add("/cshop open 6-3");ShopItemNames.add("6-3");
            ShopItems.add("/sre open 6-3");ShopItemNames.add("6-3中转");
            ShopItems.add("/cshop open 6-4");ShopItemNames.add("6-4");
            ShopItems.add("/sre open 6-4");ShopItemNames.add("6-4中转");
            ShopItems.add("/cshop open 6-5");ShopItemNames.add("6-5");
            ShopItems.add("/sre open 6-5");ShopItemNames.add("6-5中转");
            ShopItems.add("/cshop open 6-6");ShopItemNames.add("6-6");
            ShopItems.add("/sre open 6-6");ShopItemNames.add("6-6中转");
            ShopItems.add("/cshop open 6-7");ShopItemNames.add("6-7");
            ShopItems.add("/sre open 6-7");ShopItemNames.add("6-7中转");
            ShopItems.add("/cshop open 7-1");ShopItemNames.add("7-1");
            ShopItems.add("/sre open 7-1");ShopItemNames.add("7-1中转");
            ShopItems.add("/cshop open 7-2");ShopItemNames.add("7-2");
            ShopItems.add("/sre open 7-2");ShopItemNames.add("7-2中转");
            ShopItems.add("/cshop open 7-3");ShopItemNames.add("7-3");
            ShopItems.add("/sre open 7-3");ShopItemNames.add("7-3中转");
            ShopItems.add("/cshop open 7-4");ShopItemNames.add("7-4");
            ShopItems.add("/sre open 7-4");ShopItemNames.add("7-4中转");
            ShopItems.add("/cshop open 7-5");ShopItemNames.add("7-5");
            ShopItems.add("/sre open 7-5");ShopItemNames.add("7-5中转");
            ShopItems.add("/cshop open 7-6");ShopItemNames.add("7-6");
            ShopItems.add("/sre open 7-6");ShopItemNames.add("7-6中转");
            
            ShopItems.add("/cshop open 暗黑之途Ⅰ");ShopItemNames.add("暗黑Ⅰ");
            ShopItems.add("/cshop open 暗黑之途Ⅱ");ShopItemNames.add("暗黑Ⅱ");
            ShopItems.add("/cshop open 暗黑3");ShopItemNames.add("暗黑Ⅲ");
            ShopItems.add("/cshop open 暗黑4");ShopItemNames.add("暗黑Ⅳ");
            ShopItems.add("/cshop open 圣遗物副本");ShopItemNames.add("阿波罗");
            ShopItems.add("/cshop open 龙灵");ShopItemNames.add("仇龙");
            
            ShopItems.add("/cshop open 月光森林");ShopItemNames.add("月光");
            ShopItems.add("/cshop open 破败");ShopItemNames.add("破败");
            
            ShopItems.add("/cshop open 清婉喜好");ShopItemNames.add("温柔币");
            ShopItems.add("/cshop open 温柔乡银行");ShopItemNames.add("万能币");
            ShopItems.add("/cshop open 角色进阶");ShopItemNames.add("升鹏凤");
            ShopItems.add("/cshop open 温柔乡商店");ShopItemNames.add("温柔礼");
            ShopItems.add("/cshop open 结识清婉");ShopItemNames.add("清婉-1");
            ShopItems.add("/cshop open 清婉装备");ShopItemNames.add("清婉-2");
            ShopItems.add("/cshop open 日月之息融合");ShopItemNames.add("清婉-3");
            ShopItems.add("/cshop open 温柔乡研博派");ShopItemNames.add("清婉-4");
            ShopItems.add("/cshop open 红娘饰品");ShopItemNames.add("清婉-5.1");
            ShopItems.add("/cshop open 城镇小卖部");ShopItemNames.add("清婉-5.2");
            ShopItems.add("/cshop open 沙滩小摊摊");ShopItemNames.add("清婉-6");
            
            categoryItems.put("商店", ShopItems);
            categoryItemNames.put("商店", ShopItemNames);
            
            // 传送分类
            List<String> TeleportItems = new ArrayList<>();
            List<String> TeleportItemNames = new ArrayList<>();
            
            TeleportItems.add("/res tp 1");TeleportItemNames.add("传一本");
            TeleportItems.add("/res tp 2");TeleportItemNames.add("传二本");
            TeleportItems.add("/res tp 3");TeleportItemNames.add("传三本");
            TeleportItems.add("/res tp 4");TeleportItemNames.add("传四本");
            TeleportItems.add("/res tp 5");TeleportItemNames.add("传五本");
            TeleportItems.add("/res tp dlz");TeleportItemNames.add("传六本");
            TeleportItems.add("/res tp 7");TeleportItemNames.add("传七本");
            
            TeleportItems.add("/res tp tianben");TeleportItemNames.add("传天本");
            TeleportItems.add("/res tp dmdl");TeleportItemNames.add("传斗喵");
            TeleportItems.add("/res tp hs");TeleportItemNames.add("传海神");
            TeleportItems.add("/res tp ah");TeleportItemNames.add("传暗黑");
            TeleportItems.add("/res tp pxxd");TeleportItemNames.add("传破晓");
            TeleportItems.add("/res tp ygsl");TeleportItemNames.add("传月光");
            TeleportItems.add("/res tp pbzd");TeleportItemNames.add("传破败");
            
            TeleportItems.add("/res tp hy");TeleportItemNames.add("传狐妖");
            TeleportItems.add("/res tp zf");TeleportItemNames.add("传张飞");
            TeleportItems.add("/res tp gy");TeleportItemNames.add("传关羽");
            TeleportItems.add("/res tp huangzhon");TeleportItemNames.add("传黄忠");
            TeleportItems.add("/res tp mc");TeleportItemNames.add("传马超");
            TeleportItems.add("/res tp zhaoyun");TeleportItemNames.add("传赵云");
            
            TeleportItems.add("/res tp szz");TeleportItemNames.add("传狮子");
            TeleportItems.add("/res tp cnz");TeleportItemNames.add("传处女");
            TeleportItems.add("/res tp tcz");TeleportItemNames.add("传天枰");
            TeleportItems.add("/res tp txz");TeleportItemNames.add("传天蝎");
            TeleportItems.add("/res tp ssz");TeleportItemNames.add("传射手");
            TeleportItems.add("/res tp mjz");TeleportItemNames.add("传摩羯");
            TeleportItems.add("/res tp spz");TeleportItemNames.add("传水瓶");
            TeleportItems.add("/res tp syz");TeleportItemNames.add("传双鱼");
            TeleportItems.add("/res tp byz");TeleportItemNames.add("传白羊");
            TeleportItems.add("/res tp jnz");TeleportItemNames.add("传金牛");
            TeleportItems.add("/res tp szz1");TeleportItemNames.add("传双子");
            TeleportItems.add("/res tp jxz");TeleportItemNames.add("传巨蟹");
            
            TeleportItems.add("/res tp boss");TeleportItemNames.add("传世Boss");
            TeleportItems.add("/res tp boss1-1");TeleportItemNames.add("传宙斯");
            TeleportItems.add("/res tp boss1");TeleportItemNames.add("传火神");
            TeleportItems.add("/res tp boss2");TeleportItemNames.add("传战神");
            TeleportItems.add("/res tp hl");TeleportItemNames.add("传天后");
            TeleportItems.add("/res tp boss6");TeleportItemNames.add("传农神");
            TeleportItems.add("/res tp boss7");TeleportItemNames.add("传爱神");
            
            TeleportItems.add("/res tp seyu");TeleportItemNames.add("传色欲");
            TeleportItems.add("/res tp baonu");TeleportItemNames.add("传暴怒");
            TeleportItems.add("/res tp lantan");TeleportItemNames.add("传贪婪");
            TeleportItems.add("/res tp baoshi");TeleportItemNames.add("传暴食");
            TeleportItems.add("/res tp aoman");TeleportItemNames.add("传傲慢");
            TeleportItems.add("/res tp duji");TeleportItemNames.add("传嫉妒");
            TeleportItems.add("/res tp landuo");TeleportItemNames.add("传懒惰");

            categoryItems.put("传送", TeleportItems);
            categoryItemNames.put("传送", TeleportItemNames);

            // 自动操作分类
            List<String> AutoItems = new ArrayList<>();
            List<String> AutoItemNames = new ArrayList<>();
            
            // 新增循环设置按钮(+)
            AutoItems.add("setloop"); AutoItemNames.add("循环次数");
            AutoItems.add("stop"); AutoItemNames.add("停止运行");
            
            AutoItems.add("path:每日"); AutoItemNames.add("做每日");
            AutoItems.add("path:破晓"); AutoItemNames.add("跑破晓");
            AutoItems.add("path:暴怒"); AutoItemNames.add("跑暴怒");
            AutoItems.add("path:6-3/A"); AutoItemNames.add("挂6-3/A");
            AutoItems.add("path:6-3/1"); AutoItemNames.add("挂6-3/1");
            AutoItems.add("path:6-3/2"); AutoItemNames.add("挂6-3/2");
            AutoItems.add("path:6-4/A"); AutoItemNames.add("挂6-4/A");
            AutoItems.add("path:6-4/1"); AutoItemNames.add("挂6-4/1");
            AutoItems.add("path:6-4/2"); AutoItemNames.add("挂6-4/2");
            AutoItems.add("path:6-5/A"); AutoItemNames.add("挂6-5/A");
            AutoItems.add("path:6-5/1"); AutoItemNames.add("挂6-5/1");
            AutoItems.add("path:6-5/2"); AutoItemNames.add("挂6-5/2");
            AutoItems.add("path:6-5/3"); AutoItemNames.add("挂6-5/3");
            AutoItems.add("path:6-5/4"); AutoItemNames.add("挂6-5/4");

            categoryItems.put("自动操作", AutoItems);
            categoryItemNames.put("自动操作", AutoItemNames);
        }

        // 初始化路径序列管理器 - 支持多步操作
        public static void initializePathSequences() {

            // 破晓路径序列
            PathSequence morningSequence = new PathSequence("破晓");

            PathStep morning1 = new PathStep(new double[]{189, 6, -486});
            morning1.addAction(player -> rightClickOnBlock(player, new BlockPos(190, 8, -488)));

            PathStep morning2 = new PathStep(new double[]{142, 6, -475});
            morning2.addAction(player -> rightClickOnBlock(player, new BlockPos(141, 8, -473)));

            PathStep morning3 = new PathStep(new double[]{175, 6, -566});
            morning3.addAction(player -> rightClickOnBlock(player, new BlockPos(173, 8, -567)));

            PathStep morning4 = new PathStep(new double[]{264, 6, -570});
            morning4.addAction(player -> rightClickOnBlock(player, new BlockPos(266, 8, -571)));

            PathStep morning5 = new PathStep(new double[]{310, 6, -627});
            morning5.addAction(player -> rightClickOnBlock(player, new BlockPos(309, 8, -629)));

            PathStep morning6 = new PathStep(new double[]{348, 6, -579});
            morning6.addAction(player -> rightClickOnBlock(player, new BlockPos(346, 8, -581)));

            PathStep morning7 = new PathStep(new double[]{357, 6, -548});
            morning7.addAction(player -> rightClickOnBlock(player, new BlockPos(355, 8, -550)));

            PathStep morning8 = new PathStep(new double[]{367, 6, -547});
            morning8.addAction(player -> rightClickOnBlock(player, new BlockPos(369, 8, -548)));

            PathStep morning9 = new PathStep(new double[]{411, 6, -587});
            morning9.addAction(player -> rightClickOnBlock(player, new BlockPos(412, 8, -589)));

            PathStep morning10 = new PathStep(new double[]{455, 6, -561});
            morning10.addAction(player -> rightClickOnBlock(player, new BlockPos(454, 8, -558)));

            PathStep morning11 = new PathStep(new double[]{463, 7, -479});
            morning11.addAction(player -> rightClickOnBlock(player, new BlockPos(466, 9, -480)));

            PathStep morning12 = new PathStep(new double[]{430, 6, -479});
            morning12.addAction(player -> rightClickOnBlock(player, new BlockPos(432, 8, -478)));

            PathStep morning13 = new PathStep(new double[]{381, 6, -483});
            morning13.addAction(player -> rightClickOnBlock(player, new BlockPos(379, 7, -482)));

            PathStep morning14 = new PathStep(new double[]{312, 8, -480});
            morning14.addAction(player -> rightClickOnBlock(player, new BlockPos(311, 10, -482)));

            PathStep morning15 = new PathStep(new double[]{366, 6, -410});
            morning15.addAction(player -> rightClickOnBlock(player, new BlockPos(370, 7, -407)));

            PathStep morning16 = new PathStep(new double[]{355, 6, -396});
            morning16.addAction(player -> rightClickOnBlock(player, new BlockPos(352, 7, -391)));

            PathStep morning17 = new PathStep(new double[]{377, 13, -395});
            morning17.addAction(player -> rightClickOnBlock(player, new BlockPos(380, 14, -394)));

            PathStep morning18 = new PathStep(new double[]{346, 13, -397});
            morning18.addAction(player -> rightClickOnBlock(player, new BlockPos(343, 14, -399)));

            PathStep morning19 = new PathStep(new double[]{394, 6, -372});
            morning19.addAction(player -> rightClickOnBlock(player, new BlockPos(393, 8, -369)));

            PathStep morning20 = new PathStep(new double[]{438, 6, -400});
            morning20.addAction(player -> rightClickOnBlock(player, new BlockPos(440, 7, -399)));

            PathStep morning21 = new PathStep(new double[]{317, 6, -386});
            morning21.addAction(player -> rightClickOnBlock(player, new BlockPos(320, 8, -386)));

            PathStep morning22 = new PathStep(new double[]{310, 6, -318});
            morning22.addAction(player -> rightClickOnBlock(player, new BlockPos(310, 8, -315)));

            PathStep morning23 = new PathStep(new double[]{266, 6, -363});
            morning23.addAction(player -> rightClickOnBlock(player, new BlockPos(267, 8, -360)));

            PathStep morning24 = new PathStep(new double[]{192, 6, -366});
            morning24.addAction(player -> rightClickOnBlock(player, new BlockPos(191, 8, -364)));

            morningSequence.addStep(morning1);
            morningSequence.addStep(morning2);
            morningSequence.addStep(morning3);
            morningSequence.addStep(morning4);
            morningSequence.addStep(morning5);
            morningSequence.addStep(morning6);
            morningSequence.addStep(morning7);
            morningSequence.addStep(morning8);
            morningSequence.addStep(morning9);
            morningSequence.addStep(morning10);
            morningSequence.addStep(morning11);
            morningSequence.addStep(morning12);
            morningSequence.addStep(morning13);
            morningSequence.addStep(morning14);
            morningSequence.addStep(morning15);
            morningSequence.addStep(morning16);
            morningSequence.addStep(morning17);
            morningSequence.addStep(morning18);
            morningSequence.addStep(morning19);
            morningSequence.addStep(morning20);
            morningSequence.addStep(morning21);
            morningSequence.addStep(morning22);
            morningSequence.addStep(morning23);
            morningSequence.addStep(morning24);

            pathSequenceManager.addSequence(morningSequence);

            // 暴怒路径序列
            PathSequence angerSequence = new PathSequence("暴怒");
            
            PathStep anger1 = new PathStep(new double[]{-43, 85, -34});
            anger1.addAction(player -> rightClickOnBlock(player, new BlockPos(-44, 86, -34)));
            
            PathStep anger2 = new PathStep(new double[]{-44, 17, -59});
            anger2.addAction(player -> rightClickOnBlock(player, new BlockPos(-43, 18, -59)));
            anger2.addAction(new DelayAction(10)); 
            anger2.addAction(player -> setPlayerViewAngles(player, -76.8f, -13.7f));
            anger2.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger3 = new PathStep(new double[]{57, 47, -36});
            anger3.addAction(player -> rightClickOnBlock(player, new BlockPos(60, 46, -37)));
            anger3.addAction(new DelayAction(10)); 
            anger3.addAction(player -> setPlayerViewAngles(player, 55.8f, -57.0f));
            anger3.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger4 = new PathStep(new double[]{-1, 90, 37});
            anger4.addAction(player -> rightClickOnBlock(player, new BlockPos(-2, 91, 38)));
            
            PathStep anger5 = new PathStep(new double[]{-35, 35, 23});
            anger5.addAction(player -> rightClickOnBlock(player, new BlockPos(-34, 35, 23)));
            anger5.addAction(new DelayAction(10)); 
            anger5.addAction(player -> setPlayerViewAngles(player, -35.0f, -66.0f));
            anger5.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger6 = new PathStep(new double[]{-14, 97, 67});
            anger6.addAction(player -> setPlayerViewAngles(player, 128.0f, -24.0f));
            anger6.addAction(player -> sendChatCommand("/jump"));
            anger6.addAction(player -> rightClickOnBlock(player, new BlockPos(-20, 106, 63)));
            anger6.addAction(new DelayAction(10)); 
            anger6.addAction(player -> setPlayerViewAngles(player, -90.0f, 30.0f));
            anger6.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger7 = new PathStep(new double[]{67, 26, 59});
            anger7.addAction(player -> rightClickOnBlock(player, new BlockPos(65, 27, 57)));
            anger7.addAction(new DelayAction(10)); 
            anger7.addAction(player -> setPlayerViewAngles(player, 115.0f, -60.0f));
            anger7.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger8 = new PathStep(new double[]{-25, 106, 102});
            anger8.addAction(player -> rightClickOnBlock(player, new BlockPos(-28, 107, 102)));
            
            PathStep anger9 = new PathStep(new double[]{-30, 117, 124});
            anger9.addAction(player -> rightClickOnBlock(player, new BlockPos(-30, 120, 127)));
            
            PathStep anger10 = new PathStep(new double[]{-32, 128, 111});
            anger10.addAction(player -> rightClickOnBlock(player, new BlockPos(-31, 130, 108)));
            
            PathStep anger11a = new PathStep(new double[]{-19, 110, 110});
            anger11a.addAction(player -> setPlayerViewAngles(player, -80.0f, 55.0f));
            anger11a.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger11b = new PathStep(new double[]{6, 78, 114});
            anger11b.addAction(player -> rightClickOnBlock(player, new BlockPos(6, 79, 115)));
            
            PathStep anger12 = new PathStep(new double[]{8, 57, 121});
            anger12.addAction(player -> rightClickOnBlock(player, new BlockPos(10, 57, 119)));
            
            PathStep anger13 = new PathStep(new double[]{47, 19, 145});
            anger13.addAction(new DelayAction(10)); 
            anger13.addAction(player -> setPlayerViewAngles(player, -130.0f, -23.5f));
            anger13.addAction(player -> sendChatCommand("/jump"));
            anger13.addAction(player -> rightClickOnBlock(player, new BlockPos(53, 25, 140)));
            
            PathStep anger14a = new PathStep(new double[]{61, 19, 136});
            anger14a.addAction(player -> setPlayerViewAngles(player, -55.0f, -85.0f));
            anger14a.addAction(player -> sendChatCommand("/jump"));
            
            PathStep anger14b = new PathStep(new double[]{71, 77, 169});
            anger14b.addAction(player -> rightClickOnBlock(player, new BlockPos(72, 77, 171)));
            
            angerSequence.addStep(anger1);
            angerSequence.addStep(anger2);
            angerSequence.addStep(anger3);
            angerSequence.addStep(anger4);
            angerSequence.addStep(anger5);
            angerSequence.addStep(anger6);
            angerSequence.addStep(anger7);
            angerSequence.addStep(anger8);
            angerSequence.addStep(anger9);
            angerSequence.addStep(anger10);
            angerSequence.addStep(anger11a);
            angerSequence.addStep(anger11b);
            angerSequence.addStep(anger12);
            angerSequence.addStep(anger13);
            angerSequence.addStep(anger14a);
            angerSequence.addStep(anger14b);

            pathSequenceManager.addSequence(angerSequence);
            
            // 6-3点位1路径序列
            PathSequence B631Sequence = new PathSequence("6-3/1");
            
            PathStep B631B1 = new PathStep(new double[]{180, 56, -396});
            B631B1.addAction(new DelayAction(320)); 
            
            PathStep B631B2 = new PathStep(new double[]{200, 56, -414});
            B631B2.addAction(new DelayAction(320)); 
            
            B631Sequence.addStep(B631B1);
            B631Sequence.addStep(B631B2);
            
            pathSequenceManager.addSequence(B631Sequence);
            
            // 6-3点位2路径序列
            PathSequence B632Sequence = new PathSequence("6-3/2");
            
            PathStep B632B1 = new PathStep(new double[]{202, 52, -490});
            B632B1.addAction(new DelayAction(320)); 
            
            PathStep B632B2 = new PathStep(new double[]{203, 52, -502});
            B632B2.addAction(new DelayAction(320)); 
            
            B632Sequence.addStep(B632B1);
            B632Sequence.addStep(B632B2);
            
            pathSequenceManager.addSequence(B632Sequence);
            
            // 6-3点位A路径序列
            PathSequence B63ASequence = new PathSequence("6-3/A");
            
            B63ASequence.addStep(B631B1);
            B63ASequence.addStep(B631B2);
            B63ASequence.addStep(B632B1);
            B63ASequence.addStep(B632B2);
            B63ASequence.addStep(B632B1);
            B63ASequence.addStep(B631B2);
            
            pathSequenceManager.addSequence(B63ASequence);
            
            // 6-4点位1路径序列
            PathSequence B641Sequence = new PathSequence("6-4/1");
            
            PathStep B641B1 = new PathStep(new double[]{-10, 10, -10});
            B641B1.addAction(new DelayAction(320)); 
            
            PathStep B641B2 = new PathStep(new double[]{-9, 10, -25});
            B641B2.addAction(new DelayAction(320)); 
            
            PathStep B641B3 = new PathStep(new double[]{6, 10, -25});
            B641B3.addAction(new DelayAction(320)); 
            
            B641Sequence.addStep(B641B1);
            B641Sequence.addStep(B641B2);
            B641Sequence.addStep(B641B3);
            B641Sequence.addStep(B641B2);
            
            pathSequenceManager.addSequence(B641Sequence);
            
            // 6-4点位2路径序列
            PathSequence B642Sequence = new PathSequence("6-4/2");
            
            PathStep B642B1 = new PathStep(new double[]{30, 13, 40});
            B642B1.addAction(new DelayAction(320)); 
            
            PathStep B642B2 = new PathStep(new double[]{7, 12, 57});
            B642B2.addAction(new DelayAction(320)); 
            
            PathStep B642B3 = new PathStep(new double[]{-15, 11, 49});
            B642B3.addAction(new DelayAction(320)); 
            
            B642Sequence.addStep(B642B1);
            B642Sequence.addStep(B642B2);
            B642Sequence.addStep(B642B3);
            B642Sequence.addStep(B642B2);
            
            pathSequenceManager.addSequence(B642Sequence);
            
            // 6-4点位A路径序列
            PathSequence B64ASequence = new PathSequence("6-4/A");
            
            PathStep B64B1 = new PathStep(new double[]{1, 11, 11});
            B64B1.addAction(new DelayAction(320)); 
            
            B64ASequence.addStep(B641B1);
            B64ASequence.addStep(B641B2);
            B64ASequence.addStep(B641B3);
            B64ASequence.addStep(B64B1);
            B64ASequence.addStep(B642B1);
            B64ASequence.addStep(B642B2);
            B64ASequence.addStep(B642B3);
            B64ASequence.addStep(B642B2);
            B64ASequence.addStep(B642B1);
            B64ASequence.addStep(B64B1);
            B64ASequence.addStep(B641B3);
            B64ASequence.addStep(B641B2);
            
            pathSequenceManager.addSequence(B64ASequence);
            
            // 6-5点位1路径序列
            PathSequence B651Sequence = new PathSequence("6-5/1");
            
            PathStep B651B1 = new PathStep(new double[]{-101, 8, 1348});
            B651B1.addAction(new DelayAction(320)); 
            
            PathStep B651B2 = new PathStep(new double[]{-112, 11, 1339});
            B651B2.addAction(new DelayAction(320)); 
            
            PathStep B651B3 = new PathStep(new double[]{-120, 12, 1325});
            B651B3.addAction(new DelayAction(320)); 
            
            B651Sequence.addStep(B651B1);
            B651Sequence.addStep(B651B2);
            B651Sequence.addStep(B651B3);
            B651Sequence.addStep(B651B2);
            
            pathSequenceManager.addSequence(B651Sequence);
            
            // 6-5点位2路径序列
            PathSequence B652Sequence = new PathSequence("6-5/2");
            
            PathStep B652B1 = new PathStep(new double[]{-98, 15, 1249});
            B652B1.addAction(new DelayAction(320)); 
            
            PathStep B652B2 = new PathStep(new double[]{-68, 18, 1257});
            B652B2.addAction(new DelayAction(320)); 
            
            PathStep B652B3 = new PathStep(new double[]{-54, 19, 1266});
            B652B3.addAction(new DelayAction(320)); 
            
            B652Sequence.addStep(B652B1);
            B652Sequence.addStep(B652B2);
            B652Sequence.addStep(B652B3);
            B652Sequence.addStep(B652B2);
            
            pathSequenceManager.addSequence(B652Sequence);
            
            // 6-5点位3路径序列
            PathSequence B653Sequence = new PathSequence("6-5/3");
            
            PathStep B653B1 = new PathStep(new double[]{-89, 18, 1175});
            B653B1.addAction(new DelayAction(320)); 
            
            PathStep B653B2 = new PathStep(new double[]{-85, 20, 1142});
            B653B2.addAction(new DelayAction(320)); 
            
            PathStep B653B3 = new PathStep(new double[]{-84, 30, 1109});
            B653B3.addAction(new DelayAction(320)); 
            
            PathStep B653B4 = new PathStep(new double[]{-79, 35, 1074});
            B653B4.addAction(new DelayAction(320)); 
            
            B653Sequence.addStep(B653B1);
            B653Sequence.addStep(B653B2);
            B653Sequence.addStep(B653B3);
            B653Sequence.addStep(B653B4);
            B653Sequence.addStep(B653B3);
            B653Sequence.addStep(B653B2);
            
            pathSequenceManager.addSequence(B653Sequence);
            
            // 6-5点位4路径序列
            PathSequence B654Sequence = new PathSequence("6-5/4");
            
            PathStep B654B1 = new PathStep(new double[]{-186, 46, 1315});
            B654B1.addAction(new DelayAction(320)); 
            
            PathStep B654B2a = new PathStep(new double[]{-200, 42, 1300});
            B654B2a.addAction(new DelayAction(320)); 
            
            PathStep B654B2b = new PathStep(new double[]{-200, 42, 1300});
            B654B2b.addAction(new DelayAction(320)); 
            B654B2b.addAction(player -> setPlayerViewAngles(player, -40.0f, -6.4f));
            B654B2b.addAction(player -> sendChatCommand("/jump"));
            
            PathStep B654B3 = new PathStep(new double[]{-202, 42, 1267});
            B654B3.addAction(new DelayAction(320)); 
            
            B654Sequence.addStep(B654B1);
            B654Sequence.addStep(B654B2a);
            B654Sequence.addStep(B654B3);
            B654Sequence.addStep(B654B2b);
            
            pathSequenceManager.addSequence(B654Sequence);
            
            // 6-5点位A路径序列
            PathSequence B65ASequence = new PathSequence("6-5/A");
            
            PathStep B65B1 = new PathStep(new double[]{-103, 16, 1291});
            B65B1.addAction(new DelayAction(320)); 
            
            B65ASequence.addStep(B651B1);
            B65ASequence.addStep(B651B2);
            B65ASequence.addStep(B651B3);
            B65ASequence.addStep(B65B1);
            B65ASequence.addStep(B652B1);
            B65ASequence.addStep(B652B2);
            B65ASequence.addStep(B652B3);
            B65ASequence.addStep(B652B2);
            B65ASequence.addStep(B652B1);
            B65ASequence.addStep(B65B1);
            B65ASequence.addStep(B651B3);
            B65ASequence.addStep(B651B2);
            
            pathSequenceManager.addSequence(B65ASequence);
            
            // 每日路径序列
            PathSequence DailyTaskSequence = new PathSequence("每日");
            
            PathStep DailyTask1a = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
            DailyTask1a.addAction(player -> sendChatCommand("/res tp zhanbu"));
            
            PathStep DailyTask1b = new PathStep(new double[]{45, 105, 51});
            DailyTask1b.addAction(player -> rightClickOnBlock(player, new BlockPos(46, 105, 48)));
            DailyTask1b.addAction(player -> rightClickOnBlock(player, new BlockPos(46, 105, 54)));
            DailyTask1b.addAction(player -> sendChatCommand("/res tp viplb"));
            
            PathStep DailyTask2 = new PathStep(new double[]{-152, 110, -732});
            DailyTask2.addAction(player -> rightClickOnBlock(player, new BlockPos(-148, 110, -731)));
            DailyTask2.addAction(player -> rightClickOnBlock(player, new BlockPos(-150, 110, -731)));
            DailyTask2.addAction(player -> rightClickOnBlock(player, new BlockPos(-152, 110, -731)));
            DailyTask2.addAction(player -> rightClickOnBlock(player, new BlockPos(-154, 110, -731)));
            DailyTask2.addAction(player -> rightClickOnBlock(player, new BlockPos(-156, 110, -731)));
            DailyTask2.addAction(player -> sendChatCommand("/res tp yyzh"));
            
            PathStep DailyTask3 = new PathStep(new double[]{-2524, 161, 97});
            DailyTask3.addAction(player -> rightClickOnNearestEntity(player, new BlockPos(-2526, 162, 98), 0.5)); 
            DailyTask3.addAction(player -> autoVillagerTradeFull(player, 0, 1)); 
            DailyTask3.addAction(player -> rightClickOnBlock(player, new BlockPos(-2522, 162, 94)));
            DailyTask3.addAction(player -> sendChatCommand("/res tp mnrs"));
            
            PathStep DailyTask4 = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
            DailyTask4.addAction(player -> setPlayerViewAngles(player, 91.6f, -3.0f));
            DailyTask4.addAction(new DelayAction(12));
            DailyTask4.addAction(player -> sendChatCommand("/jump"));
            DailyTask4.addAction(player -> rightClickOnBlock(player, new BlockPos(-55, 25, -3)));
            DailyTask4.addAction(new DelayAction(12));
            DailyTask4.addAction(player -> sendChatCommand("/res tp pk"));
            
            PathStep DailyTask5a = new PathStep(new double[]{153, 4, -559});
            DailyTask5a.addAction(player -> setPlayerViewAngles(player, -140.0f, -50.0f));
            DailyTask5a.addAction(player -> sendChatCommand("/jump"));
            
            PathStep DailyTask5b = new PathStep(new double[]{175, 16, -551});
            DailyTask5b.addAction(player -> setPlayerViewAngles(player, -130.0f, -5.0f));
            DailyTask5b.addAction(player -> sendChatCommand("/jump"));
            
            PathStep DailyTask5c = new PathStep(new double[]{190, 18, -569});
            DailyTask5c.addAction(player -> rightClickOnBlock(player, new BlockPos(190, 14, -569)));
            DailyTask5c.addAction(player -> sendChatCommand("/res tp sj"));
            
            PathStep DailyTask6 = new PathStep(new double[]{-85, 104, 43});
            DailyTask6.addAction(player -> rightClickOnBlock(player, new BlockPos(-86, 105, 46)));
            DailyTask6.addAction(player -> rightClickOnBlock(player, new BlockPos(-86, 105, 43)));
            DailyTask6.addAction(player -> rightClickOnBlock(player, new BlockPos(-86, 105, 40)));
            DailyTask6.addAction(player -> sendChatCommand("/res tp wrx3"));
            
            PathStep DailyTask7a = new PathStep(new double[]{262, 83, 123});
            DailyTask7a.addAction(player -> rightClickOnBlock(player, new BlockPos(259, 84, 123)));
            DailyTask7a.addAction(player -> rightClickOnBlock(player, new BlockPos(261, 84, 120)));
            
            PathStep DailyTask7b = new PathStep(new double[]{268, 83, 134});
            DailyTask7b.addAction(player -> rightClickOnBlock(player, new BlockPos(271, 84, 131)));
            DailyTask7b.addAction(player -> rightClickOnBlock(player, new BlockPos(269, 84, 138)));
            
            PathStep DailyTask7c = new PathStep(new double[]{262, 83, 134});
            DailyTask7c.addAction(player -> rightClickOnBlock(player, new BlockPos(262, 84, 138)));
            
            PathStep DailyTask8 = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
            DailyTask8.addAction(player -> sendChatCommand("/menu"));
            DailyTask8.addAction(new DelayAction(20));
            DailyTask8.addAction(player -> autoChestClick(player, 9)); 
            DailyTask8.addAction(new DelayAction(12));
            DailyTask8.addAction(player -> autoChestClick(player, 13)); 
            DailyTask8.addAction(new DelayAction(12));
            DailyTask8.addAction(player -> autoChestClick(player, 15)); 
            DailyTask8.addAction(new DelayAction(12));
            DailyTask8.addAction(player -> autoChestClick(player, 10)); 
            DailyTask8.addAction(player -> autoChestClick(player, 12)); 
            DailyTask8.addAction(player -> autoChestClick(player, 14)); 
            DailyTask8.addAction(player -> autoChestClick(player, 16)); 
            DailyTask8.addAction(player -> autoChestClick(player, 30)); 
            DailyTask8.addAction(player -> autoChestClick(player, 32)); 
            DailyTask8.addAction(player -> autoChestClick(player, 40)); 
            
            DailyTaskSequence.addStep(DailyTask1a);
            DailyTaskSequence.addStep(DailyTask1b);
            DailyTaskSequence.addStep(DailyTask2);
            DailyTaskSequence.addStep(DailyTask3);
            DailyTaskSequence.addStep(DailyTask4);
            DailyTaskSequence.addStep(DailyTask5a);
            DailyTaskSequence.addStep(DailyTask5b);
            DailyTaskSequence.addStep(DailyTask5c);
            DailyTaskSequence.addStep(DailyTask6);
            DailyTaskSequence.addStep(DailyTask7a);
            DailyTaskSequence.addStep(DailyTask7b);
            DailyTaskSequence.addStep(DailyTask7c);
            DailyTaskSequence.addStep(DailyTask8);
            
            pathSequenceManager.addSequence(DailyTaskSequence);
        }
        
        // 设置角度（与游戏中对应） xxx.addAction(player -> setPlayerViewAngles(player, 66.5f, -46.0f));
        // 发送聊天内容（可用于发送指令） xxx.addAction(player -> sendChatCommand("/jump"));
        // 指定坐标方块右键 xxx.addAction(player -> rightClickOnBlock(player, new BlockPos(190, 8, -488)));
        // 手动添加延迟ticks（20tick = 1s） xxx.addAction(new DelayAction(10));
        // 指定坐标范围实体右键 xxx.addAction(player -> rightClickOnNearestEntity(player, new BlockPos(100, 65, 200), 3.0)); 
        // 自动村民交易（第1个交易2次） xxx.addAction(player -> autoVillagerTradeFull(player, 0, 2)); 
        // 自动箱子GUI点击（第31格） xxx.addAction(player -> autoChestClick(player, 30)); 
        
        // 设置玩家视角角度
        private static void setPlayerViewAngles(EntityPlayerSP player, float yaw, float pitch) {
            player.rotationYaw = yaw;
            player.rotationPitch = pitch;
            player.rotationYawHead = yaw;
            player.prevRotationYaw = yaw;
            player.prevRotationPitch = pitch;
            LOGGER.info("Set player view angles: yaw={}, pitch={}", yaw, pitch);
        }
        
        // 发送聊天命令
        private static void sendChatCommand(String command) {
            if (mc.player != null && !mc.player.isSpectator()) {
                mc.player.sendChatMessage(command);
                LOGGER.info("Sent command: " + command);
            }
        }
        
        // 右键点击方块
        private static void rightClickOnBlock(EntityPlayerSP player, BlockPos pos) {
            EnumFacing facing = EnumFacing.UP;
            Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            
            mc.playerController.processRightClickBlock(
                player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND
            );
            player.swingArm(EnumHand.MAIN_HAND);
            LOGGER.info("Right clicked at: " + pos);
        }
        
        // 右键点击实体
        private static void rightClickOnNearestEntity(EntityPlayerSP player, BlockPos pos, double range) {
            Minecraft mc = Minecraft.getMinecraft();
            double px = pos.getX() + 0.5;
            double py = pos.getY() + 0.5;
            double pz = pos.getZ() + 0.5;

            // 查找附近所有实体（不包括玩家自己）
            Entity nearest = null;
            double minDistSq = Double.MAX_VALUE;
            for (Entity entity : mc.world.getEntitiesWithinAABB(
                    Entity.class,
                    new AxisAlignedBB(
                        px - range, py - range, pz - range,
                        px + range, py + range, pz + range
                    ))) {
                if (entity == player) continue;
                double distSq = entity.getDistanceSq(px, py, pz);
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = entity;
                }
            }
            if (nearest != null) {
                mc.playerController.interactWithEntity(player, nearest, EnumHand.MAIN_HAND);
                player.swingArm(EnumHand.MAIN_HAND);
                LOGGER.info("Right clicked entity {} at {}", nearest.getName(), pos);
            } else {
                LOGGER.warn("No entity found near: " + pos);
            }
        }
        
        // 延迟动作类
        private static class DelayAction implements Consumer<EntityPlayerSP> {
        	private final int delayTicks;

        	public DelayAction(int delayTicks) {
        		this.delayTicks = delayTicks;
        	}

        	@Override
        	public void accept(EntityPlayerSP player) {
        		// 延迟动作本身在tick事件中处理，这里不执行任何操作
        	}

        	public int getDelayTicks() {
        	return delayTicks;
        	}
        }
        
        // 自动村民交易类
        /**
         * 自动执行指定村民交易，自动补全输入物品并领取交易物品，支持NBT精确匹配
         * @param tradeIndex    村民交易序号（从0开始）
         * @param tradeCount    执行多少次该交易
         */
        public static void autoVillagerTradeFull(EntityPlayerSP player, int tradeIndex, int tradeCount) {
            LOGGER.info("当前GUI: " + mc.currentScreen.getClass().getName());
            LOGGER.info("当前容器: " + player.openContainer.getClass().getName());
            if (!(mc.currentScreen instanceof GuiMerchant) || tradeCount <= 0) return;
            GuiMerchant gui = (GuiMerchant) mc.currentScreen;
            MerchantRecipeList recipes = gui.getMerchant().getRecipes(player);

            if (recipes == null || tradeIndex < 0 || tradeIndex >= recipes.size()) return;
            MerchantRecipe recipe = recipes.get(tradeIndex);
            if (recipe == null || recipe.isRecipeDisabled()) return;

            // 反射设置GuiMerchant的currentRecipeIndex（适配开发版和混淆版）
            try {
                java.lang.reflect.Field field;
                try {
                    // 开发环境名
                    field = GuiMerchant.class.getDeclaredField("currentRecipeIndex");
                } catch (NoSuchFieldException e) {
                    // 混淆名
                    field = GuiMerchant.class.getDeclaredField("field_147041_z");
                }
                field.setAccessible(true);
                field.setInt(gui, tradeIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (int t = 0; t < tradeCount; t++) {
                // 补全输入物品
                boolean inputOk = fillMerchantInputsWithNBT(gui, recipe);
                if (!inputOk) {
                    LOGGER.warn("背包中缺少交易所需物品（含NBT），无法继续交易");
                    break;
                }
                // 尝试点击输出槽以完成交易
                Slot outputSlot = gui.inventorySlots.getSlot(2);
                if (outputSlot != null && outputSlot.getHasStack()) {
                    int emptySlot = findFirstEmptyInventorySlot(gui);
                    if (emptySlot >= 0) {
                        mc.playerController.windowClick(gui.inventorySlots.windowId, 2, 0, ClickType.PICKUP, player);
                        mc.playerController.windowClick(gui.inventorySlots.windowId, emptySlot, 0, ClickType.PICKUP, player);
                        clearMerchantInputSlot(gui, 0);
                        clearMerchantInputSlot(gui, 1);
                    } else {
                        LOGGER.warn("背包已满，无法领取交易物品！");
                        break;
                    }
                } else {
                    // 没有输出物品时，再点击一次触发交易
                    mc.playerController.windowClick(gui.inventorySlots.windowId, 2, 0, ClickType.PICKUP, player);
                }
            }
            LOGGER.info("自动完成村民交易（含NBT精确匹配），交易序号: " + tradeIndex + "，次数: " + tradeCount);
        }

        /**
         * 补全村民交易输入物品（支持1或2输入物品，且匹配NBT标签）
         * @return 是否成功放入所需数量的输入物品
         */
        private static boolean fillMerchantInputsWithNBT(GuiMerchant gui, MerchantRecipe recipe) {
            // 1. 先清空输入槽（slot 0, slot 1）
            clearMerchantInputSlot(gui, 0);
            clearMerchantInputSlot(gui, 1);

            // 2. 放入输入1
            boolean ok1 = moveItemToInputWithNBT(gui, recipe.getItemToBuy(), 0, recipe.getItemToBuy().getCount());
            // 3. 放入输入2（如果有）
            boolean ok2 = true;
            if (!recipe.getSecondItemToBuy().isEmpty()) {
                ok2 = moveItemToInputWithNBT(gui, recipe.getSecondItemToBuy(), 1, recipe.getSecondItemToBuy().getCount());
            }
            return ok1 && ok2;
        }

        /**
         * 从背包移动指定数量物品（含精确NBT）到村民输入槽
         * @param gui GuiMerchant
         * @param targetStack 目标物品
         * @param inputSlot 输入槽号（0或1）
         * @param neededCount 需要的数量
         * @return 是否足量成功
         */
        private static boolean moveItemToInputWithNBT(GuiMerchant gui, ItemStack targetStack, int inputSlot, int neededCount) {
            int moved = 0;
            for (int i = 9; i <= 35; i++) {
                Slot slot = gui.inventorySlots.getSlot(i);
                if (slot != null && slot.getHasStack()) {
                    ItemStack stack = slot.getStack();
                    if (itemStackNBTEquals(stack, targetStack)) {
                        int toMove = Math.min(stack.getCount(), neededCount - moved);
                        for (int j = 0; j < toMove; j++) {
                            mc.playerController.windowClick(gui.inventorySlots.windowId, i, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(gui.inventorySlots.windowId, inputSlot, 0, ClickType.PICKUP, mc.player);
                            moved++;
                            stack = slot.getStack();
                            if (stack == null || stack.isEmpty()) break;
                        }
                    }
                    if (moved >= neededCount) break;
                }
            }
            return moved >= neededCount;
        }

        /**
         * 精确比较两个ItemStack，包括NBT
         */
        private static boolean itemStackNBTEquals(ItemStack a, ItemStack b) {
            if (a == null || b == null) return false;
            NBTTagCompound nbtA = a.getTagCompound();
            NBTTagCompound nbtB = b.getTagCompound();
            if (nbtA == null && nbtB == null) return true;
            if (nbtA == null || nbtB == null) return false;
            return nbtA.equals(nbtB);
        }

        /**
         * 查找玩家背包中的第一个空槽位
         * @param gui 当前GuiMerchant
         * @return 槽位索引（GUI里的），没有空位返回-1
         */
        private static int findFirstEmptyInventorySlot(GuiMerchant gui) {
            for (int i = 9; i <= 35; i++) {
                Slot slot = gui.inventorySlots.getSlot(i);
                if (slot != null && !slot.getHasStack()) {
                    return i;
                }
            }
            return -1;
        }

        // 清空村民交易输入槽
        private static void clearMerchantInputSlot(GuiMerchant gui, int slotId) {
            Slot slot = gui.inventorySlots.getSlot(slotId);
            if (slot != null && slot.getHasStack()) {
                mc.playerController.windowClick(gui.inventorySlots.windowId, slotId, 0, ClickType.PICKUP, mc.player);
                int empty = findFirstEmptyInventorySlot(gui);
                if (empty >= 0) {
                    mc.playerController.windowClick(gui.inventorySlots.windowId, empty, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
        
        // 自动箱子GUI点击类
        /**
         * 自动点击箱子或大箱子GUI的指定格子
         * @param chestSlotIndex 格子编号（大箱子0-53，小箱子0-26）
         */
        public static void autoChestClick(EntityPlayerSP player, int chestSlotIndex) {
            if (mc.currentScreen instanceof GuiChest) {
                GuiChest gui = (GuiChest) mc.currentScreen;
                if (chestSlotIndex >= 0 && chestSlotIndex < gui.inventorySlots.inventorySlots.size()) {
                    mc.playerController.windowClick(gui.inventorySlots.windowId, chestSlotIndex, 0, ClickType.PICKUP, player);
                    LOGGER.info("自动点击箱子格子: " + chestSlotIndex);
                }
            }
        }
        
        // 路径序列步骤类（支持多操作）
        private static class PathStep {
            private final double[] gotoPoint;
            private final List<Consumer<EntityPlayerSP>> actions = new ArrayList<>();
            
            public PathStep(double[] gotoPoint) {
                this.gotoPoint = gotoPoint;
            }
            
            public void addAction(Consumer<EntityPlayerSP> action) {
                actions.add(action);
            }
            
            public double[] getGotoPoint() {
                return gotoPoint;
            }
            
            public List<Consumer<EntityPlayerSP>> getActions() {
                return actions;
            }
        }
        
        // 路径序列类（支持多步骤）
        private static class PathSequence {
            private final String name;
            private final List<PathStep> steps = new ArrayList<>();
            
            public PathSequence(String name) {
                this.name = name;
            }
            
            public void addStep(PathStep step) {
                steps.add(step);
            }
            
            public String getName() {
                return name;
            }
            
            public List<PathStep> getSteps() {
                return steps;
            }
        }
        
        // 路径序列管理器
        private static class PathSequenceManager {
            private final Map<String, PathSequence> sequences = new HashMap<>();
            
            public void addSequence(PathSequence sequence) {
                sequences.put(sequence.getName(), sequence);
            }
            
            public PathSequence getSequence(String name) {
                return sequences.get(name);
            }
            
            public boolean hasSequence(String name) {
                return sequences.containsKey(name);
            }
        }
        
        private static void saveLoopConfig(String sequenceName, int loopCount) {
            try {
                String json = "{"
                    + "\"autoLoop\":true,"
                    + "\"loopSequence\":\"" + sequenceName.replace("\"", "\\\"") + "\","
                    + "\"loopCount\":" + loopCount
                    + "}";
                Path configDir = Paths.get("config");
                if (!Files.exists(configDir)) Files.createDirectories(configDir);
                Files.write(Paths.get("config/keycommandmod_autorun.json"), json.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                LOGGER.error("保存循环配置失败", e);
            }
        }
        
        // 运行路径序列
        public static void runPathSequence(String sequenceName) {
            if (!pathSequenceManager.hasSequence(sequenceName)) {
                LOGGER.error("未知路径序列: " + sequenceName);
                return;
            }
            
            PathSequence sequence = pathSequenceManager.getSequence(sequenceName);
            if (sequence == null || sequence.getSteps().isEmpty()) {
                LOGGER.error("无效路径序列: " + sequenceName);
                return;
            }
            
            loopCounter = 0;
            isLooping = true;
            
            // 如果循环次数不为0，则开始执行
            if (loopCount != 0) {
                startNextLoop(sequenceName);
            }
            
            // 如果无限循环，则保存自动运行配置
            if (loopCount == -1) {
            	saveLoopConfig(sequenceName, loopCount);
            }
        }
        
        // 开始下一次循环
        private static void startNextLoop(String sequenceName) {
            PathSequence sequence = pathSequenceManager.getSequence(sequenceName);
            
            // 获取路径序列的第一个点
            double[] firstTarget = sequence.getSteps().get(0).getGotoPoint();
            
            // 发送第一个.goto命令
            sendChatCommand(String.format(".goto %.0f %.0f %.0f", firstTarget[0], firstTarget[1], firstTarget[2]));
            
            // 更新计数器
            loopCounter++;
            
            // 设置状态信息
            String loopInfo = "循环 " + loopCounter;
            if (loopCount > 0) {
                loopInfo += "/" + loopCount;
            }
            EventListener.instance.setStatus(sequenceName + " - " + loopInfo);
            
            // 注册事件监听器
            EventListener.instance.startTracking(sequence, loopCount - loopCounter);
            
            // 注册全局事件监听器
            MinecraftForge.EVENT_BUS.register(EventListener.instance);
            LOGGER.info("开始运行序列: " + sequenceName);
        }

@Override
public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F); // 半透明白色背景
    
    // 整体尺寸250x200像素
    int totalWidth = 250;
    int height = 200;
    int x = (this.width - totalWidth) / 2; // 居中
    int y = (this.height - height) / 2; // 居中

    // 绘制半透明白色背景
    drawRect(x, y, x + totalWidth, y + height, 0x7FFFFFFF); // 半透明白色
    
    // 绘制标题（居中显示）
    String title = "快捷菜单 - " + currentCategory;
    drawCenteredString(fontRenderer, title, x + 125, y + 5, 0x555555); // 灰色标题文字

    // 绘制左侧分类区背景 (50x200)
    drawRect(x, y, x + 50, y + height, 0x80DDDDDD); // 浅灰色背景
    
    // 绘制分类按钮（左侧垂直排列） - 调整位置以适应方框
    for (int i = 0; i < categories.size(); i++) {
        String category = categories.get(i);
        // 调整按钮位置：Y位置从15改为25增加顶部空间，间距从45改为40减小间距
        int buttonY = y + 25 + i * 40; // 增加顶部间距，减少按钮间距
        
        // 圆形按钮
        int radius = 18; // 稍微减小半径以适应方框
        int buttonX = x + 25; // 圆心位置
        
        // 高亮当前分类
        float alpha = category.equals(currentCategory) ? 0.8F : 0.5F;
        int color = category.equals(currentCategory) ? 0xFF00DD00 : 0xFFAAAAAA;
        
        // 绘制圆形背景（带透明度）
        drawCircle(buttonX, buttonY, radius, (color & 0xFFFFFF) | ((int)(alpha * 255) << 24));
        
        // 绘制文字（固定在圆形中央）
        int textWidth = fontRenderer.getStringWidth(category);
        fontRenderer.drawStringWithShadow(category, 
            buttonX - textWidth/2, 
            buttonY - 3, 
            0xFFFFFF); // 白色文字
    }

    // 绘制当前分类的物品（右侧区域）
    List<String> items = categoryItems.get(currentCategory);
    List<String> itemNames = categoryItemNames.get(currentCategory);
    
    // 物品区位置
    int itemAreaX = x + 55; // 增加左边距
    int itemAreaY = y + 20;
    
    // 每行显示5个，最多4行
    for (int i = 0; i < 20; i++) { // 最多显示20个（5列×4行）
        int index = currentPage * 20 + i;
        if (index >= items.size()) break;
        
        int col = i % 5; // 列索引 (0-4)
        int row = i / 5; // 行索引 (0-3)
        int itemX = itemAreaX + col * 36; // 简化间距
        int itemY = itemAreaY + row * 40; // 每行40像素高
        
        // 绘制物品名称和图标
        fontRenderer.drawStringWithShadow(itemNames.get(index), itemX, itemY, 0x333333);
        fontRenderer.drawStringWithShadow("\u272A", itemX + 8, itemY + 12, 0xFFDD00); // 金色星星图标
    }

    // 绘制页码信息（上移到物品区中部）
    int totalPages = (items.size() + 19) / 20;
    drawCenteredString(fontRenderer, "第" + (currentPage + 1) + "页/共" + totalPages + "页", 
                      x + 175, y + 165, 0x666666); // 灰色页码文字

    // 绘制上一页按钮（下移）
    drawRect(x + 190, y + 188, x + 220, y + 200, 0xFFDDDDDD);
    drawCenteredString(fontRenderer, "上一页", x + 205, y + 190, 0x333333);

    // 绘制下一页按钮（下移）
    drawRect(x + 220, y + 188, x + 250, y + 200, 0xFFDDDDDD);
    drawCenteredString(fontRenderer, "下一页", x + 235, y + 190, 0x333333);

    // 如果是自动操作分类，显示状态信息
    if (currentCategory.equals("自动操作")) {
        // 显示当前循环设置
        String loopSetting = "循环设置: ";
        if (loopCount == -1) {
            loopSetting += "无限循环";
        } else if (loopCount == 0) {
            loopSetting += "单次执行";
        } else {
            loopSetting += loopCount + "次";
        }
        fontRenderer.drawStringWithShadow(loopSetting, x + 55, y + 180, 0x55FFFF);
        
        String statusText = "状态: ";
        int color = 0xFFFF55; // 黄色
        
        if (EventListener.instance.isTracking()) {
            statusText += EventListener.instance.getStatus();
        } else if (!isLooping) {
            statusText += "就绪";
        } else if (loopCounter > 0 && loopCount > 0 && loopCounter >= loopCount) {
            statusText += "已完成 (" + loopCounter + " 次)";
        } else if (!EventListener.instance.isTracking()) {
            statusText += "空闲";
        }
        
        fontRenderer.drawStringWithShadow(statusText, x + 55, y + 190, color);
    }
    
    super.drawScreen(mouseX, mouseY, partialTicks);
}
        
        // 辅助方法：绘制圆形
        private void drawCircle(int x, int y, int radius, int color) {
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    if (i * i + j * j <= radius * radius) {
                        drawRect(x + i, y + j, x + i + 1, y + j + 1, color);
                    }
                }
            }
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            
            // 整体尺寸250x200像素
            int totalWidth = 250;
            int height = 200;
            int x = (this.width - totalWidth) / 2;
            int y = (this.height - height) / 2;

            // 处理分类按钮点击（左侧区域） - 调整位置以适应方框
            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                // 位置调整与drawScreen方法保持一致
                int buttonY = y + 25 + i * 40; 
                int buttonX = x + 25;
                int radius = 18; // 与绘制时相同的半径
                
                // 计算鼠标到圆心的距离
                int dx = mouseX - buttonX;
                int dy = mouseY - buttonY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= radius) {
                    // 保存当前分类的状态
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                    sLastPage = currentPage;
                    sLastCategory = currentCategory;
                    
                    // 切换到新分类
                    currentCategory = category;
                    // 恢复新分类的页码
                    if (CATEGORY_PAGE_MAP.containsKey(currentCategory)) {
                        currentPage = CATEGORY_PAGE_MAP.get(currentCategory);
                    } else {
                        currentPage = 0;
                    }
                    return;
                }
            }

            // 处理物品点击（右侧物品区）
            List<String> items = categoryItems.get(currentCategory);
            
            // 物品区位置
            int itemAreaX = x + 55;
            int itemAreaY = y + 20;
            
            // 每行5个，最多4行
            for (int i = 0; i < 20; i++) {
                int index = currentPage * 20 + i;
                if (index >= items.size()) break;
                
                int col = i % 5;
                int row = i / 5;
                int itemX = itemAreaX + col * 36;
                int itemY = itemAreaY + row * 40;
                
                // 检测物品点击范围（基于文字的位置）
                if (mouseX >= itemX && mouseX <= itemX + 30 && 
                        mouseY >= itemY && mouseY <= itemY + 20) {
                        String command = items.get(index);
                        
                        if (command.startsWith("path:")) {
                            runPathSequence(command.substring(5));
                            return;
                        } else if (currentCategory.equals("自动操作")) {
                            if (command.equals("stop")) {
                                // 停止运行
                                EventListener.instance.stopTracking();
                                isLooping = false;
                                return;
                            } else if (command.equals("setloop")) {
                                // 打开循环次数设置GUI
                                mc.displayGuiScreen(new LoopCountInputGui(this));
                                return;
                            }
                        } else {
                            sendChatMessage(command);
                            mc.displayGuiScreen(null);
                            return;
                        }
                    }
                }

            // 处理上一页按钮
            if (mouseX >= x + 190 && mouseY >= y + 180 && 
                mouseX <= x + 220 && mouseY <= y + 200) {
                if (currentPage > 0) {
                    currentPage--;
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                    sLastPage = currentPage;
                    sLastCategory = currentCategory;
                }
            }

            // 处理下一页按钮
            if (mouseX >= x + 220 && mouseY >= y + 180 && 
                mouseX <= x + 250 && mouseY <= y + 200) {
                int totalPages = (categoryItems.get(currentCategory).size() + 19) / 20;
                if (currentPage + 1 < totalPages) {
                    currentPage++;
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                    sLastPage = currentPage;
                    sLastCategory = currentCategory;
                }
            }
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        public void onGuiClosed() {
            super.onGuiClosed();
            // 保存当前状态
            CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
            sLastPage = currentPage;
            sLastCategory = currentCategory;

        }

        // 静态的事件监听器类（完全重写以支持多操作和循环）
        public static class EventListener {
            public static final EventListener instance = new EventListener();
            private PathSequence currentSequence;
            private int currentStepIndex = 0;
            private int actionIndex = 0;
            private boolean tracking = false;
            private int tickDelay = 0;
            private boolean atTarget = false;
            private int remainingLoops = 0;
            private String status = "";
            private EventListener() {}

            public boolean isTracking() {
                return tracking;
            }

            public void setStatus(String s) {
                status = s;
            }
            
            public String getStatus() {
                return status;
            }

            public void startTracking(PathSequence sequence, int remainingLoops) {
                this.currentSequence = sequence;
                this.currentStepIndex = 0;
                this.actionIndex = 0;
                this.tracking = true;
                this.atTarget = false;
                this.tickDelay = 0;
                this.remainingLoops = remainingLoops;
                MinecraftForge.EVENT_BUS.register(this);
                LOGGER.info("开始跟踪路径: " + sequence.getName());
            }

            public void stopTracking() {
                if (tracking) {
                    this.tracking = false;
                    this.currentSequence = null;
                    MinecraftForge.EVENT_BUS.unregister(this);
                    status = "已停止";
                    LOGGER.info("路径跟踪已停止");
                    clearLoopConfig();
                }
            }

            private void clearLoopConfig() {
                try {
                    Path configPath = Paths.get("config/keycommandmod_autorun.json");
                    if (Files.exists(configPath)) Files.delete(configPath);
                } catch (Exception e) {
                    LOGGER.error("清除循环配置失败", e);
                }
            }
            
            @SubscribeEvent
            public void onPlayerTick(TickEvent.PlayerTickEvent event) {
                if (!tracking || event.phase != TickEvent.Phase.START || event.side != Side.CLIENT) return;
                if (event.player == null || !event.player.equals(Minecraft.getMinecraft().player)) return;
                
                EntityPlayerSP player = (EntityPlayerSP) event.player;
                
                // 如果有延迟，等待延迟结束
                if (tickDelay > 0) {
                    tickDelay--;
                    return;
                }
                
                List<PathStep> steps = currentSequence.getSteps();
                
                // 检查是否完成序列
                if (currentStepIndex >= steps.size()) {
                    sendChatCommand(".goto cancel");

                    if (remainingLoops != 0 || GuiInventory.loopCount < 0) {
                        if (remainingLoops > 0) {
                            remainingLoops--;
                        }
                        if (remainingLoops != 0 || GuiInventory.loopCount < 0) {
                            status = "等待循环...";
                            // 关键：重新开始下一轮循环
                            tracking = false;
                            MinecraftForge.EVENT_BUS.unregister(this);

                            // 重新启动下一轮循环
                            // 用调度任务方式避免递归调用
                            Minecraft.getMinecraft().addScheduledTask(() -> {
                                if (currentSequence != null) {
                                    GuiInventory.startNextLoop(currentSequence.getName());
                                }
                            });
                            return;
                        } else {
                            // 完成所有循环
                            GuiInventory.isLooping = false;
                            status = "已完成 (" + GuiInventory.loopCounter + " 次)";
                            stopTracking();
                        }
                    } else {
                        stopTracking();
                    }
                    return;
                }
                
                PathStep currentStep = steps.get(currentStepIndex);
                double[] target = currentStep.getGotoPoint();
                
                if (!atTarget) {
                    // 检查玩家是否到达目标点
                    double playerX = player.posX;
                    double playerY = player.posY;
                    double playerZ = player.posZ;
                    
                    double tx = Double.isNaN(target[0]) ? player.posX : target[0];
                    double ty = Double.isNaN(target[1]) ? player.posY : target[1];
                    double tz = Double.isNaN(target[2]) ? player.posZ : target[2];
                    
                    double distanceSq = 
                        Math.pow(playerX - tx, 2) +
                        Math.pow(playerY - ty, 2) +
                        Math.pow(playerZ - tz, 2);
                    
                    if (distanceSq < 4.0) { // 2格距离平方
                        LOGGER.info("到达目标 {} for {}", currentStepIndex, currentSequence.getName());
                        atTarget = true;
                        actionIndex = 0;
                    }
                } else {
                    // 已到达目标点，执行动作
                    List<Consumer<EntityPlayerSP>> actions = currentStep.getActions();
                    
                    // 检查是否完成当前步骤的所有动作
                    if (actionIndex >= actions.size()) {
                        // 移动到下一步
                        currentStepIndex++;
                        actionIndex = 0;
                        atTarget = false;
                        
                        // 如果还有下一步，发送新的.goto命令
                        if (currentStepIndex < steps.size()) {
                            double[] nextTarget = steps.get(currentStepIndex).getGotoPoint();
                            sendChatCommand(".goto cancel");
                            sendChatCommand(String.format(".goto %.0f %.0f %.0f", 
                                nextTarget[0], nextTarget[1], nextTarget[2]));
                        } else {
                            // 序列完成，发送取消指令
                            sendChatCommand(".goto cancel");
                        }
                        return;
                    }
                    
                    // 获取当前动作
                    Consumer<EntityPlayerSP> action = actions.get(actionIndex);
                    
                    // 处理延迟动作
                    if (action instanceof GuiInventory.DelayAction) {
                    	GuiInventory.DelayAction delayAction = (GuiInventory.DelayAction) action;
                    	tickDelay = delayAction.getDelayTicks();
                    	LOGGER.info("延迟 {} tick", tickDelay);
                    	// 移动至下一个动作
                    	actionIndex++;
                    	return; // 等待延迟期间，直接返回
                    }
                    
                    // 执行动作
                    try {
                        action.accept(player);
                        LOGGER.info("执行动作 {} for step {}", actionIndex, currentStepIndex);
                    } catch (Exception e) {
                        LOGGER.error("执行动作失败", e);
                    }
                    
                    // 移动到下一个动作（如果有），添加少量延迟确保服务器处理
                    actionIndex++;
                    tickDelay = 5; // 等待5 ticks (0.25秒) 让服务器处理动作
                }
            }
            
            // 发送临时聊天命令（不依赖GUI的sendChatMessage）
            private static void sendChatCommand(String command) {
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                if (player != null && !player.isSpectator()) {
                    player.sendChatMessage(command);
                    LOGGER.info("发送命令: " + command);
                }
            }
        }


        
        public static class LoopCountInputGui extends GuiScreen {
            private final GuiInventory parent;
            private String inputText = "";
            private GuiTextField numberField;
            
            public LoopCountInputGui(GuiInventory parent) {
                this.parent = parent;
            }
            
            @Override
            public void initGui() {
                super.initGui();
                this.buttonList.clear();
                
                // 创建输入框
                numberField = new GuiTextField(0, fontRenderer, 
                    this.width/2 - 100, this.height/2 - 25, 
                    200, 20);
                numberField.setFocused(true);
                numberField.setCanLoseFocus(false);
                numberField.setMaxStringLength(10);
                numberField.setText(inputText);
                
                // 确认按钮
                this.buttonList.add(new GuiButton(0, this.width/2 - 100, this.height/2, 90, 20, "确认"));
                // 取消按钮
                this.buttonList.add(new GuiButton(1, this.width/2 + 10, this.height/2, 90, 20, "取消"));
                // 无限循环按钮
                this.buttonList.add(new GuiButton(2, this.width/2 - 100, this.height/2 + 30, 200, 20, "设置为无限循环"));
            }
            
            @Override
            protected void keyTyped(char typedChar, int keyCode) throws IOException {
                super.keyTyped(typedChar, keyCode);
                
                if (numberField.textboxKeyTyped(typedChar, keyCode)) {
                    inputText = numberField.getText();
                    return;
                }
            }
            
            @Override
            protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                numberField.mouseClicked(mouseX, mouseY, mouseButton);
            }
            
            @Override
            public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                this.drawDefaultBackground();
                
                // 标题
                drawCenteredString(fontRenderer, "设置循环次数", width/2, height/2 - 50, 0xFFFFFF);
                
                // 提示文字
                drawString(fontRenderer, "输入数字（0=不循环，-1=无限循环）:", 
                    width/2 - 100, height/2 - 40, 0xA0A0A0);
                
                // 绘制输入框
                numberField.drawTextBox();
                
                super.drawScreen(mouseX, mouseY, partialTicks);
            }
            
            @Override
            protected void actionPerformed(GuiButton button) throws IOException {
                if (button.id == 0) { // 确认按钮
                    setLoopCount();
                    mc.displayGuiScreen(parent);
                } 
                else if (button.id == 1) { // 取消按钮
                    mc.displayGuiScreen(parent);
                }
                else if (button.id == 2) { // 无限循环按钮
                    GuiInventory.loopCount = -1; // 修正点：使用 parent.loopCount
                    mc.displayGuiScreen(parent);
                }
            }
            
            private void setLoopCount() {
                try {
                    GuiInventory.loopCount = Integer.parseInt(inputText.trim()); // 修正点：使用 parent.loopCount
                    GuiInventory.loopCounter = 0; // 修正点：使用 parent.loopCounter
                } catch (NumberFormatException e) {
                    // 无效输入处理
                    GuiInventory.loopCount = 1; // 修正点：使用 parent.loopCount
                    Minecraft.getMinecraft().player.sendMessage(
                        new TextComponentString("§c无效输入! 已重置为单次循环"));
                }
            }
        }
    }
}