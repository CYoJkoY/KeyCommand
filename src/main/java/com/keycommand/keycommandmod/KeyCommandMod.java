package com.keycommand.keycommandmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

    private static final Logger LOGGER = LogManager.getLogger(KeyCommandMod.class);

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final KeyBinding teleKey = new KeyBinding("传送", Keyboard.KEY_GRAVE, "key.categories.keycommand");
    private static final KeyBinding srpOpenKey = new KeyBinding("灵魂空间", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_I, "key.categories.keycommand");
    private static final KeyBinding menuKey = new KeyBinding("菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_O, "key.categories.keycommand");
    private static final KeyBinding ecKey = new KeyBinding("末影箱", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_U, "key.categories.keycommand");
    private static final KeyBinding hbKey = new KeyBinding("货币兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_P, "key.categories.keycommand");
    private static final KeyBinding nzwKey = new KeyBinding("农作物兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_L, "key.categories.keycommand");
    private static final KeyBinding guiKey = new KeyBinding("快捷菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_T, "key.categories.keycommand");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
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
        // 添加静态变量保存上次的状态
        private static String sLastCategory = "每日";
        private static int sLastPage = 0;
        private static final Map<String, Integer> CATEGORY_PAGE_MAP = new HashMap<>();
        
        private int currentPage = sLastPage;
        private String currentCategory = sLastCategory;
        private final List<String> categories = Arrays.asList("每日", "商店", "传送");
        private final Map<String, List<String>> categoryItems = new HashMap<>();
        private final Map<String, List<String>> categoryItemNames = new HashMap<>();
        // 路径序列管理器
        private final PathSequenceManager pathSequenceManager = new PathSequenceManager();

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
            DailyItems.add("path:破晓"); DailyItemNames.add("跑破晓");
            DailyItems.add("/res tp baonu");DailyItemNames.add("传暴怒");
            DailyItems.add("path:暴怒"); DailyItemNames.add("跑暴怒");
            
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
            ShopItems.add("/cshop open 信仰");ShopItemNames.add("1-4");
            ShopItems.add("/cshop open 1-5");ShopItemNames.add("1-5");
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
        }

        // 初始化路径序列管理器 - 支持多步操作
        private void initializePathSequences() {
            
            // 设置角度（与游戏中对应） xxx.addAction(player -> setPlayerViewAngles(player, 66.5f, -46.0f));
            // 发送聊天内容（可用于发送指令） xxx.addAction(player -> sendChatCommand("/jump"));
            // 指定坐标右键 xxx.addAction(player -> rightClickOnBlock(player, new BlockPos(190, 8, -488)));
            // 手动添加延迟ticks（1tick = 50ms） xxx.addAction(new DelayAction(10)); 
        	
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

            PathStep morning15 = new PathStep(new double[]{368, 6, -409});
            morning15.addAction(player -> rightClickOnBlock(player, new BlockPos(370, 7, -407)));

            PathStep morning16 = new PathStep(new double[]{354, 6, -394});
            morning16.addAction(player -> rightClickOnBlock(player, new BlockPos(352, 7, -391)));

            PathStep morning17 = new PathStep(new double[]{378, 13, -395});
            morning17.addAction(player -> rightClickOnBlock(player, new BlockPos(380, 14, -394)));

            PathStep morning18 = new PathStep(new double[]{345, 13, -398});
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
        }
        
        // 设置玩家视角角度
        private void setPlayerViewAngles(EntityPlayerSP player, float yaw, float pitch) {
            player.rotationYaw = yaw;
            player.rotationPitch = pitch;
            player.rotationYawHead = yaw;
            player.prevRotationYaw = yaw;
            player.prevRotationPitch = pitch;
            LOGGER.info("Set player view angles: yaw={}, pitch={}", yaw, pitch);
        }
        
        // 发送聊天命令
        private void sendChatCommand(String command) {
            if (mc.player != null && !mc.player.isSpectator()) {
                mc.player.sendChatMessage(command);
                LOGGER.info("Sent command: " + command);
            }
        }
        
        // 右键点击方块
        private void rightClickOnBlock(EntityPlayerSP player, BlockPos pos) {
            EnumFacing facing = EnumFacing.UP;
            Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            
            mc.playerController.processRightClickBlock(
                player, mc.world, pos, facing, hitVec, EnumHand.MAIN_HAND
            );
            player.swingArm(EnumHand.MAIN_HAND);
            LOGGER.info("Right clicked at: " + pos);
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
        
        // 运行路径序列
        private void runPathSequence(String sequenceName) {
            if (!pathSequenceManager.hasSequence(sequenceName)) {
                LOGGER.error("Unknown path sequence: " + sequenceName);
                return;
            }
            
            PathSequence sequence = pathSequenceManager.getSequence(sequenceName);
            if (sequence == null || sequence.getSteps().isEmpty()) {
                LOGGER.error("Invalid path sequence: " + sequenceName);
                return;
            }
            
            // 获取路径序列的第一个点
            double[] firstTarget = sequence.getSteps().get(0).getGotoPoint();
            
            // 发送第一个.goto命令
            sendChatCommand(String.format(".goto %.0f %.0f %.0f", firstTarget[0], firstTarget[1], firstTarget[2]));
            
            // 注册事件监听器
            EventListener.instance.startTracking(sequence);
            
            // 注册全局事件监听器
            MinecraftForge.EVENT_BUS.register(EventListener.instance);
            LOGGER.info("Started path sequence: " + sequenceName);
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
            drawRect(x + 130, y + 180, x + 150, y + 195, 0xFFDDDDDD);
            drawCenteredString(fontRenderer, "上一页", x + 140, y + 185, 0x333333);

            // 绘制下一页按钮（下移）
            drawRect(x + 160, y + 180, x + 180, y + 195, 0xFFDDDDDD);
            drawCenteredString(fontRenderer, "下一页", x + 170, y + 185, 0x333333);

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
                    
                    // 检查是否为路径序列命令
                    if (command.startsWith("path:")) {
                        runPathSequence(command.substring(5));
                    } else {
                        sendChatMessage(command);
                        mc.displayGuiScreen(null);
                    }
                    return;
                }
            }

            // 处理上一页按钮
            if (mouseX >= x + 130 && mouseY >= y + 180 && 
                mouseX <= x + 150 && mouseY <= y + 195) {
                if (currentPage > 0) {
                    currentPage--;
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                    sLastPage = currentPage;
                    sLastCategory = currentCategory;
                }
            }

            // 处理下一页按钮
            if (mouseX >= x + 160 && mouseY >= y + 180 && 
                mouseX <= x + 180 && mouseY <= y + 195) {
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

        // 静态的事件监听器类（完全重写以支持多操作）
        public static class EventListener {
            public static final EventListener instance = new EventListener();
            private PathSequence currentSequence;
            private int currentStepIndex = 0;
            private int actionIndex = 0;
            private boolean tracking = false;
            private int tickDelay = 0;
            private boolean atTarget = false;

            private EventListener() {}

            public void startTracking(PathSequence sequence) {
                this.currentSequence = sequence;
                this.currentStepIndex = 0;
                this.actionIndex = 0;
                this.tracking = true;
                this.atTarget = false;
                this.tickDelay = 0;
                MinecraftForge.EVENT_BUS.register(this);
                LOGGER.info("Tracking started for path: " + sequence.getName());
            }

            public void stopTracking() {
                if (tracking) {
                    this.tracking = false;
                    this.currentSequence = null;
                    MinecraftForge.EVENT_BUS.unregister(this);
                    LOGGER.info("Path tracking stopped");
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
                    stopTracking();
                    return;
                }
                
                PathStep currentStep = steps.get(currentStepIndex);
                double[] target = currentStep.getGotoPoint();
                
                if (!atTarget) {
                    // 检查玩家是否到达目标点
                    double playerX = player.posX;
                    double playerY = player.posY;
                    double playerZ = player.posZ;
                    
                    double distanceSq = 
                        Math.pow(playerX - target[0], 2) +
                        Math.pow(playerY - target[1], 2) +
                        Math.pow(playerZ - target[2], 2);
                    
                    if (distanceSq < 4.0) { // 2格距离平方
                        LOGGER.info("Reached target {} for {}", currentStepIndex, currentSequence.getName());
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
                            // 序列完成
                            sendChatCommand(".goto cancel");
                            stopTracking();
                        }
                        return;
                    }
                    
                    // 获取当前动作
                    Consumer<EntityPlayerSP> action = actions.get(actionIndex);
                    
                    // 处理延迟动作
                    if (action instanceof GuiInventory.DelayAction) {
                    	GuiInventory.DelayAction delayAction = (GuiInventory.DelayAction) action;
                    	tickDelay = delayAction.getDelayTicks();
                    	LOGGER.info("Delaying for {} ticks", tickDelay);
                    	// 移动至下一个动作
                    	actionIndex++;
                    	return; // 等待延迟期间，直接返回
                    }
                    
                    // 执行动作
                    try {
                        action.accept(player);
                        LOGGER.info("Executed action {} for step {}", actionIndex, currentStepIndex);
                    } catch (Exception e) {
                        LOGGER.error("Failed to execute action", e);
                    }
                    
                    // 移动到下一个动作（如果有），添加少量延迟确保服务器处理
                    actionIndex++;
                    tickDelay = 5; // 等待5 ticks (0.25秒) 让服务器处理动作
                }
            }
            
            // 发送临时聊天命令（不依赖GUI的sendChatMessage）
            private void sendChatCommand(String command) {
                EntityPlayerSP player = Minecraft.getMinecraft().player;
                if (player != null && !player.isSpectator()) {
                    player.sendChatMessage(command);
                    LOGGER.info("Sent command: " + command);
                }
            }
        }
    }
}