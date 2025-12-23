package com.keycommand.keycommandmod.gui;

import com.keycommand.keycommandmod.KeyCommandMod;
import com.keycommand.keycommandmod.gui.path.PathSequence;
import com.keycommand.keycommandmod.gui.path.PathSequenceManager;
import com.keycommand.keycommandmod.gui.path.PathStep;
import com.keycommand.keycommandmod.handler.PathTrackingListener;
import com.keycommand.keycommandmod.util.ActionUtils;
import com.keycommand.keycommandmod.util.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiInventory extends GuiScreen {
    private static final Minecraft mc = Minecraft.getMinecraft();
    // 静态状态变量
    private static String sLastCategory = "每日";
    private static int sLastPage = 0;
    private static final Map<String, Integer> CATEGORY_PAGE_MAP = new HashMap<>();
    // 循环控制变量
    public static int loopCount = 1;
    public static int loopCounter = 0;
    public static boolean isLooping = false;

    // 当前GUI状态
    private int currentPage = sLastPage;
    private String currentCategory = sLastCategory;
    private final List<String> categories = Arrays.asList("每日", "商店", "传送", "自动操作");
    private final Map<String, List<String>> categoryItems = new HashMap<>();
    private final Map<String, List<String>> categoryItemNames = new HashMap<>();

    // 路径序列管理器
    public static final PathSequenceManager pathSequenceManager = new PathSequenceManager();

    public GuiInventory() {
        // 初始化分类物品
        initCategoryItems();
        // 恢复页码状态
        if (CATEGORY_PAGE_MAP.containsKey(currentCategory)) {
            currentPage = CATEGORY_PAGE_MAP.get(currentCategory);
        } else {
            currentPage = sLastPage;
        }
    }

    // 初始化分类物品
    private void initCategoryItems() {
        // 每日分类
    	List<String> DailyItems = new ArrayList<>();
        List<String> DailyItemNames = new ArrayList<>();
        
        DailyItems.add("/res tp zhanbu");DailyItemNames.add("传占卜");
        DailyItems.add("/res tp viplb");DailyItemNames.add("传礼包");
        DailyItems.add("/res tp yyzh");DailyItemNames.add("传浇花");
        DailyItems.add("/res tp mnrs");DailyItemNames.add("传模拟");
        DailyItems.add("/res tp rw");DailyItemNames.add("传任务");
        DailyItems.add("/res tp pk");DailyItemNames.add("传跑酷");
        DailyItems.add("/res tp wrx3");DailyItemNames.add("传温柔");
        
        categoryItems.put("每日", DailyItems);
        categoryItemNames.put("每日", DailyItemNames);
        
        // 商店分类
        List<String> ShopItems = new ArrayList<>();
        List<String> ShopItemNames = new ArrayList<>();
        
        ShopItems.add("/cshop open 餐厅");ShopItemNames.add("食物");
        ShopItems.add("/cshop open 染料");ShopItemNames.add("染料");
        ShopItems.add("/cshop open 攻击幻灵");ShopItemNames.add("攻击幻灵");
        ShopItems.add("/cshop open 防御幻灵");ShopItemNames.add("防御幻灵");
        ShopItems.add("/res tp xyq");ShopItemNames.add("传木牌");
        ShopItems.add("/res tp gezi");ShopItemNames.add("传圆石");
        ShopItems.add("/res tp dp");ShopItemNames.add("传工具");
        ShopItems.add("/cshop open 建筑材料兑换");ShopItemNames.add("建筑材料");
        
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
        TeleportItems.add("/res tp 34");TeleportItemNames.add("传防御");
        TeleportItems.add("/res tp 5");TeleportItemNames.add("传五本");
        TeleportItems.add("/res tp dlz");TeleportItemNames.add("传六本");
        TeleportItems.add("/res tp 7");TeleportItemNames.add("传七本");
        
        TeleportItems.add("/res tp wk");TeleportItemNames.add("传挖矿");
        TeleportItems.add("/res tp wrx");TeleportItemNames.add("传温柔");
        TeleportItems.add("/res tp tianben");TeleportItemNames.add("传天本");
        TeleportItems.add("/res tp dmdl");TeleportItemNames.add("传斗喵");
        TeleportItems.add("/res tp ah");TeleportItemNames.add("传暗黑");
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
        AutoItems.add("location_config"); AutoItemNames.add("自动返回配置");
        
        AutoItems.add("path:每日"); AutoItemNames.add("做每日");
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

    // 初始化路径序列
    public static void initializePathSequences() {

        // 暴怒路径序列
        PathSequence angerSequence = new PathSequence("暴怒");
        
        PathStep anger0 = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
        anger0.addAction(player -> ActionUtils.sendChatCommand("/res tp baonu"));
        
        PathStep anger1 = new PathStep(new double[]{-43, 85, -34});
        anger1.addAction(new ActionUtils.DelayAction(10));
        anger1.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-44, 86, -34)));
        
        PathStep anger2 = new PathStep(new double[]{-44, 17, -59});
        anger2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-43, 18, -59)));
        anger2.addAction(new ActionUtils.DelayAction(10)); 
        anger2.addAction(player -> ActionUtils.setPlayerViewAngles(player, -76.8f, -13.7f));
        anger2.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger3 = new PathStep(new double[]{57, 47, -36});
        anger3.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(60, 46, -37)));
        anger3.addAction(new ActionUtils.DelayAction(10)); 
        anger3.addAction(player -> ActionUtils.setPlayerViewAngles(player, 55.8f, -57.0f));
        anger3.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger4 = new PathStep(new double[]{-1, 90, 37});
        anger4.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-2, 91, 38)));
        
        PathStep anger5 = new PathStep(new double[]{-35, 35, 23});
        anger5.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-34, 35, 23)));
        anger5.addAction(new ActionUtils.DelayAction(10)); 
        anger5.addAction(player -> ActionUtils.setPlayerViewAngles(player, -35.0f, -66.0f));
        anger5.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger6 = new PathStep(new double[]{-14, 97, 67});
        anger6.addAction(player -> ActionUtils.setPlayerViewAngles(player, 128.0f, -24.0f));
        anger6.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        anger6.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-20, 106, 63)));
        anger6.addAction(new ActionUtils.DelayAction(10)); 
        anger6.addAction(player -> ActionUtils.setPlayerViewAngles(player, -90.0f, 30.0f));
        anger6.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger7 = new PathStep(new double[]{67, 26, 59});
        anger7.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(65, 27, 57)));
        anger7.addAction(new ActionUtils.DelayAction(10)); 
        anger7.addAction(player -> ActionUtils.setPlayerViewAngles(player, 115.0f, -60.0f));
        anger7.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger8 = new PathStep(new double[]{-25, 106, 102});
        anger8.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-28, 107, 102)));
        
        PathStep anger9 = new PathStep(new double[]{-30, 117, 124});
        anger9.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-30, 120, 127)));
        
        PathStep anger10 = new PathStep(new double[]{-32, 128, 111});
        anger10.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-31, 130, 108)));
        
        PathStep anger11a = new PathStep(new double[]{-19, 110, 110});
        anger11a.addAction(player -> ActionUtils.setPlayerViewAngles(player, -80.0f, 55.0f));
        anger11a.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger11b = new PathStep(new double[]{6, 78, 114});
        anger11b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(6, 79, 115)));
        
        PathStep anger12 = new PathStep(new double[]{8, 57, 121});
        anger12.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(10, 57, 119)));
        
        PathStep anger13 = new PathStep(new double[]{47, 19, 145});
        anger13.addAction(new ActionUtils.DelayAction(10)); 
        anger13.addAction(player -> ActionUtils.setPlayerViewAngles(player, -130.0f, -23.5f));
        anger13.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        anger13.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(53, 25, 140)));
        
        PathStep anger14a = new PathStep(new double[]{61, 19, 136});
        anger14a.addAction(player -> ActionUtils.setPlayerViewAngles(player, -55.0f, -85.0f));
        anger14a.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep anger14b = new PathStep(new double[]{71, 77, 169});
        anger14b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(72, 77, 171)));
        
        angerSequence.addStep(anger0);
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
        B631B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B631B2 = new PathStep(new double[]{200, 56, -414});
        B631B2.addAction(new ActionUtils.DelayAction(320)); 
        
        B631Sequence.addStep(B631B1);
        B631Sequence.addStep(B631B2);
        
        pathSequenceManager.addSequence(B631Sequence);
        
        // 6-3点位2路径序列
        PathSequence B632Sequence = new PathSequence("6-3/2");
        
        PathStep B632B1 = new PathStep(new double[]{202, 52, -490});
        B632B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B632B2 = new PathStep(new double[]{203, 52, -502});
        B632B2.addAction(new ActionUtils.DelayAction(320)); 
        
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
        B641B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B641B2 = new PathStep(new double[]{-9, 10, -25});
        B641B2.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B641B3 = new PathStep(new double[]{6, 10, -25});
        B641B3.addAction(new ActionUtils.DelayAction(320)); 
        
        B641Sequence.addStep(B641B1);
        B641Sequence.addStep(B641B2);
        B641Sequence.addStep(B641B3);
        B641Sequence.addStep(B641B2);
        
        pathSequenceManager.addSequence(B641Sequence);
        
        // 6-4点位2路径序列
        PathSequence B642Sequence = new PathSequence("6-4/2");
        
        PathStep B642B1 = new PathStep(new double[]{30, 13, 40});
        B642B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B642B2 = new PathStep(new double[]{7, 12, 57});
        B642B2.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B642B3 = new PathStep(new double[]{-15, 11, 49});
        B642B3.addAction(new ActionUtils.DelayAction(320)); 
        
        B642Sequence.addStep(B642B1);
        B642Sequence.addStep(B642B2);
        B642Sequence.addStep(B642B3);
        B642Sequence.addStep(B642B2);
        
        pathSequenceManager.addSequence(B642Sequence);
        
        // 6-4点位A路径序列
        PathSequence B64ASequence = new PathSequence("6-4/A");
        
        PathStep B64B1 = new PathStep(new double[]{1, 11, 11});
        B64B1.addAction(new ActionUtils.DelayAction(320)); 
        
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
        B651B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B651B2 = new PathStep(new double[]{-112, 11, 1339});
        B651B2.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B651B3 = new PathStep(new double[]{-120, 12, 1325});
        B651B3.addAction(new ActionUtils.DelayAction(320)); 
        
        B651Sequence.addStep(B651B1);
        B651Sequence.addStep(B651B2);
        B651Sequence.addStep(B651B3);
        B651Sequence.addStep(B651B2);
        
        pathSequenceManager.addSequence(B651Sequence);
        
        // 6-5点位2路径序列
        PathSequence B652Sequence = new PathSequence("6-5/2");
        
        PathStep B652B1 = new PathStep(new double[]{-98, 15, 1249});
        B652B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B652B2 = new PathStep(new double[]{-68, 18, 1257});
        B652B2.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B652B3 = new PathStep(new double[]{-54, 19, 1266});
        B652B3.addAction(new ActionUtils.DelayAction(320)); 
        
        B652Sequence.addStep(B652B1);
        B652Sequence.addStep(B652B2);
        B652Sequence.addStep(B652B3);
        B652Sequence.addStep(B652B2);
        
        pathSequenceManager.addSequence(B652Sequence);
        
        // 6-5点位3路径序列
        PathSequence B653Sequence = new PathSequence("6-5/3");
        
        PathStep B653B1 = new PathStep(new double[]{-89, 18, 1175});
        B653B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B653B2 = new PathStep(new double[]{-85, 20, 1142});
        B653B2.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B653B3 = new PathStep(new double[]{-84, 30, 1109});
        B653B3.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B653B4 = new PathStep(new double[]{-79, 35, 1074});
        B653B4.addAction(new ActionUtils.DelayAction(320)); 
        
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
        B654B1.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B654B2a = new PathStep(new double[]{-200, 42, 1300});
        B654B2a.addAction(new ActionUtils.DelayAction(320)); 
        
        PathStep B654B2b = new PathStep(new double[]{-200, 42, 1300});
        B654B2b.addAction(new ActionUtils.DelayAction(320)); 
        B654B2b.addAction(player -> ActionUtils.setPlayerViewAngles(player, -40.0f, -6.4f));
        B654B2b.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep B654B3 = new PathStep(new double[]{-202, 42, 1267});
        B654B3.addAction(new ActionUtils.DelayAction(320)); 
        
        B654Sequence.addStep(B654B1);
        B654Sequence.addStep(B654B2a);
        B654Sequence.addStep(B654B3);
        B654Sequence.addStep(B654B2b);
        
        pathSequenceManager.addSequence(B654Sequence);
        
        // 6-5点位A路径序列
        PathSequence B65ASequence = new PathSequence("6-5/A");
        
        PathStep B65B1 = new PathStep(new double[]{-103, 16, 1291});
        B65B1.addAction(new ActionUtils.DelayAction(320)); 
        
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
        
        // 占卜
        PathStep DailyTask1a = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
        DailyTask1a.addAction(player -> ActionUtils.sendChatCommand("/res tp zhanbu"));
        
        PathStep DailyTask1b = new PathStep(new double[]{45, 105, 51});
        DailyTask1b.addAction(new ActionUtils.DelayAction(10));
        DailyTask1b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(46, 105, 48)));
        DailyTask1b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(46, 105, 54)));
        DailyTask1b.addAction(player -> ActionUtils.sendChatCommand("/res tp viplb"));
        
        // 礼包
        PathStep DailyTask2 = new PathStep(new double[]{-152, 110, -732});
        DailyTask2.addAction(new ActionUtils.DelayAction(10));
        DailyTask2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-148, 110, -731)));
        DailyTask2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-150, 110, -731)));
        DailyTask2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-152, 110, -731)));
        DailyTask2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-154, 110, -731)));
        DailyTask2.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-156, 110, -731)));
        DailyTask2.addAction(player -> ActionUtils.sendChatCommand("/res tp yyzh"));
        
        // 浇花
        PathStep DailyTask3 = new PathStep(new double[]{-2524, 161, 97});
        DailyTask3.addAction(new ActionUtils.DelayAction(10));
        DailyTask3.addAction(player -> ActionUtils.rightClickOnNearestEntity(player, new BlockPos(-2526, 162, 98), 0.5)); 
        DailyTask3.addAction(player -> ActionUtils.autoVillagerTradeFull(player, 0, 1)); 
        DailyTask3.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-2522, 162, 94)));
        DailyTask3.addAction(player -> ActionUtils.sendChatCommand("/res tp mnrs"));
        
        // 模拟人生（姻缘）
        PathStep DailyTask4 = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
        DailyTask4.addAction(new ActionUtils.DelayAction(10));
        DailyTask4.addAction(player -> ActionUtils.setPlayerViewAngles(player, 91.6f, -3.0f));
        DailyTask4.addAction(new ActionUtils.DelayAction(12));
        DailyTask4.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        DailyTask4.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-55, 25, -3)));
        DailyTask4.addAction(new ActionUtils.DelayAction(12));
        DailyTask4.addAction(player -> ActionUtils.sendChatCommand("/res tp pk"));
        
        // 跑酷
        PathStep DailyTask5a = new PathStep(new double[]{153, 4, -559});
        DailyTask5a.addAction(new ActionUtils.DelayAction(10));
        DailyTask5a.addAction(player -> ActionUtils.setPlayerViewAngles(player, -140.0f, -50.0f));
        DailyTask5a.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep DailyTask5b = new PathStep(new double[]{175, 16, -551});
        DailyTask5b.addAction(player -> ActionUtils.setPlayerViewAngles(player, -130.0f, -5.0f));
        DailyTask5b.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        
        PathStep DailyTask5c = new PathStep(new double[]{190, 18, -569});
        DailyTask5c.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(190, 14, -569)));
        DailyTask5c.addAction(player -> ActionUtils.sendChatCommand("/res tp 2025ss"));
        
        // 限时活动1（鼠鼠）
        PathStep DailyTask6a = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
        DailyTask6a.addAction(new ActionUtils.DelayAction(10));
        DailyTask6a.addAction(player -> ActionUtils.setPlayerViewAngles(player, -175.6f, 1.5f));
        DailyTask6a.addAction(new ActionUtils.DelayAction(12));
        DailyTask6a.addAction(player -> ActionUtils.sendChatCommand("/jump"));
        DailyTask6a.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(-855, 156, 1305)));
        DailyTask6a.addAction(player -> ActionUtils.sendChatCommand("/res tp 2024gqj1"));
        
        // 限时活动2（军衔）
        PathStep DailyTask6b = new PathStep(new double[]{582, 65, 1057});
        DailyTask6b.addAction(new ActionUtils.DelayAction(10));
        DailyTask6b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(581, 66, 1061)));
        DailyTask6b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(581, 66, 1057)));
        DailyTask6b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(581, 66, 1053)));
        DailyTask6b.addAction(player -> ActionUtils.sendChatCommand("/res tp wrx3"));
        
        // 温柔乡
        PathStep DailyTask7a = new PathStep(new double[]{262, 83, 123});
        DailyTask7a.addAction(new ActionUtils.DelayAction(10));
        DailyTask7a.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(259, 84, 123)));
        DailyTask7a.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(261, 84, 120)));
        
        PathStep DailyTask7b = new PathStep(new double[]{268, 83, 134});
        DailyTask7b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(271, 84, 131)));
        DailyTask7b.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(269, 84, 138)));
        
        PathStep DailyTask7c = new PathStep(new double[]{262, 83, 134});
        DailyTask7c.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(262, 84, 138)));
        
        // 菜单每日
        PathStep DailyTask8 = new PathStep(new double[]{Double.NaN, Double.NaN, Double.NaN});
        DailyTask8.addAction(player -> ActionUtils.sendChatCommand("/menu"));
        DailyTask8.addAction(new ActionUtils.DelayAction(20));
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 9)); 
        DailyTask8.addAction(new ActionUtils.DelayAction(12));
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 13)); 
        DailyTask8.addAction(new ActionUtils.DelayAction(12));
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 15)); 
        DailyTask8.addAction(new ActionUtils.DelayAction(12));
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 10)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 12)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 14)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 16)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 30)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 32)); 
        DailyTask8.addAction(player -> ActionUtils.autoChestClick(player, 40)); 
        
        DailyTaskSequence.addStep(DailyTask1a);
        DailyTaskSequence.addStep(DailyTask1b);
        DailyTaskSequence.addStep(DailyTask2);
        DailyTaskSequence.addStep(DailyTask3);
        DailyTaskSequence.addStep(DailyTask4);
        DailyTaskSequence.addStep(DailyTask5a);
        DailyTaskSequence.addStep(DailyTask5b);
        DailyTaskSequence.addStep(DailyTask5c);
        DailyTaskSequence.addStep(DailyTask6a);
        DailyTaskSequence.addStep(DailyTask6b);
        DailyTaskSequence.addStep(DailyTask7a);
        DailyTaskSequence.addStep(DailyTask7b);
        DailyTaskSequence.addStep(DailyTask7c);
        DailyTaskSequence.addStep(DailyTask8);
        
        pathSequenceManager.addSequence(DailyTaskSequence);
    }

    // 尝试自动启动循环
    public static void tryAutoStartLoop() {
        ConfigUtils.AutoLoopConfig config = ConfigUtils.readAutoLoopConfig();
        if (config.isAutoLoop() && !config.getLoopSequence().isEmpty() && config.getLoopCount() == -1) {
            KeyCommandMod.LOGGER.info("检测到需要自动无限循环执行：" + config.getLoopSequence());
            mc.addScheduledTask(() -> {
                loopCount = config.getLoopCount();
                loopCounter = 0;
                isLooping = true;
                runPathSequence(config.getLoopSequence());
            });
        }
    }

    // 运行路径序列
    public static void runPathSequence(String sequenceName) {
        if (!pathSequenceManager.hasSequence(sequenceName)) {
            KeyCommandMod.LOGGER.error("未知路径序列: " + sequenceName);
            return;
        }

        PathSequence sequence = pathSequenceManager.getSequence(sequenceName);
        if (sequence == null || sequence.getSteps().isEmpty()) {
            KeyCommandMod.LOGGER.error("无效路径序列: " + sequenceName);
            return;
        }

        loopCounter = 0;
        isLooping = true;

        if (loopCount != 0) {
            startNextLoop(sequenceName);
        }

        if (loopCount == -1) {
            ConfigUtils.saveAutoLoopConfig(sequenceName, loopCount);
        }
    }

    // 开始下一次循环
    public static void startNextLoop(String sequenceName) {
        PathSequence sequence = pathSequenceManager.getSequence(sequenceName);
        double[] firstTarget = sequence.getSteps().get(0).getGotoPoint();
        ActionUtils.sendChatCommand(String.format(".goto %.0f %.0f %.0f", firstTarget[0], firstTarget[1], firstTarget[2]));

        loopCounter++;
        String loopInfo = "循环 " + loopCounter;
        if (loopCount > 0) {
            loopInfo += "/" + loopCount;
        }
        PathTrackingListener.getInstance().setStatus(sequenceName + " - " + loopInfo);
        PathTrackingListener.getInstance().startTracking(sequence, loopCount - loopCounter);
        MinecraftForge.EVENT_BUS.register(PathTrackingListener.getInstance());
        KeyCommandMod.LOGGER.info("开始运行序列: " + sequenceName);
    }

    // GUI渲染
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
        int totalWidth = 250;
        int height = 200;
        int x = (this.width - totalWidth) / 2;
        int y = (this.height - height) / 2;

        // 绘制背景
        drawRect(x, y, x + totalWidth, y + height, 0x7FFFFFFF);
        drawCenteredString(fontRenderer, "快捷菜单 - " + currentCategory, x + 125, y + 5, 0x555555);
        drawRect(x, y, x + 50, y + height, 0x80DDDDDD);

        // 绘制分类按钮
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            int buttonY = y + 25 + i * 40;
            int buttonX = x + 25;
            int radius = 18;
            float alpha = category.equals(currentCategory) ? 0.8F : 0.5F;
            int color = category.equals(currentCategory) ? 0xFF00DD00 : 0xFFAAAAAA;
            drawCircle(buttonX, buttonY, radius, (color & 0xFFFFFF) | ((int) (alpha * 255) << 24));
            int textWidth = fontRenderer.getStringWidth(category);
            fontRenderer.drawStringWithShadow(category, buttonX - textWidth / 2, buttonY - 3, 0xFFFFFF);
        }

        // 绘制物品列表
        List<String> items = categoryItems.get(currentCategory);
        List<String> itemNames = categoryItemNames.get(currentCategory);
        int itemAreaX = x + 55;
        int itemAreaY = y + 20;
        for (int i = 0; i < 20; i++) {
            int index = currentPage * 20 + i;
            if (index >= items.size()) break;
            int col = i % 5;
            int row = i / 5;
            int itemX = itemAreaX + col * 36;
            int itemY = itemAreaY + row * 40;
            fontRenderer.drawStringWithShadow(itemNames.get(index), itemX, itemY, 0x333333);
            fontRenderer.drawStringWithShadow("\u272A", itemX + 8, itemY + 12, 0xFFDD00);
        }

        // 绘制页码
        int totalPages = (items.size() + 19) / 20;
        drawCenteredString(fontRenderer, "第" + (currentPage + 1) + "页/共" + totalPages + "页", x + 175, y + 165, 0x666666);
        drawRect(x + 190, y + 188, x + 220, y + 200, 0xFFDDDDDD);
        drawCenteredString(fontRenderer, "上一页", x + 205, y + 190, 0x333333);
        drawRect(x + 220, y + 188, x + 250, y + 200, 0xFFDDDDDD);
        drawCenteredString(fontRenderer, "下一页", x + 235, y + 190, 0x333333);

        // 绘制自动操作状态
        if (currentCategory.equals("自动操作")) {
            String loopSetting = "循环设置: " + (loopCount == -1 ? "无限循环" : (loopCount == 0 ? "单次执行" : loopCount + "次"));
            fontRenderer.drawStringWithShadow(loopSetting, x + 55, y + 180, 0x55FFFF);
            String statusText = "状态: " + (PathTrackingListener.getInstance().isTracking() ? PathTrackingListener.getInstance().getStatus() : (isLooping ? "运行中" : "就绪"));
            fontRenderer.drawStringWithShadow(statusText, x + 55, y + 190, 0xFFFF55);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // 绘制圆形辅助方法
    private void drawCircle(int x, int y, int radius, int color) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    drawRect(x + i, y + j, x + i + 1, y + j + 1, color);
                }
            }
        }
    }

    // 鼠标点击事件
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            int totalWidth = 250;
            int height = 200;
            int x = (this.width - totalWidth) / 2;
            int y = (this.height - height) / 2;

            // 处理分类切换
            for (int i = 0; i < categories.size(); i++) {
                String category = categories.get(i);
                int buttonY = y + 25 + i * 40;
                int buttonX = x + 25;
                int radius = 18;
                double distance = Math.sqrt(Math.pow(mouseX - buttonX, 2) + Math.pow(mouseY - buttonY, 2));
                if (distance <= radius) {
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                    sLastPage = currentPage;
                    sLastCategory = currentCategory;
                    currentCategory = category;
                    currentPage = CATEGORY_PAGE_MAP.getOrDefault(currentCategory, 0);
                    return;
                }
            }

            // 处理物品点击
            List<String> items = categoryItems.get(currentCategory);
            int itemAreaX = x + 55;
            int itemAreaY = y + 20;
            for (int i = 0; i < 20; i++) {
                int index = currentPage * 20 + i;
                if (index >= items.size()) break;
                int col = i % 5;
                int row = i / 5;
                int itemX = itemAreaX + col * 36;
                int itemY = itemAreaY + row * 40;
                if (mouseX >= itemX && mouseX <= itemX + 30 && mouseY >= itemY && mouseY <= itemY + 20) {
                    String command = items.get(index);
                    if (command.startsWith("path:")) {
                        runPathSequence(command.substring(5));
                        return;
                    } else if (currentCategory.equals("自动操作")) {
                        if (command.equals("stop")) {
                            PathTrackingListener.getInstance().stopTracking();
                            isLooping = false;
                            return;
                        } else if (command.equals("setloop")) {
                            mc.displayGuiScreen(new LoopCountInputGui(this));
                            return;
                        } else if (command.equals("location_config")) {
                            mc.displayGuiScreen(new LocationConfigGui(this));
                            return;
                        }
                    } else {
                        ActionUtils.sendChatCommand(command);
                        mc.displayGuiScreen(null);
                        return;
                    }
                }
            }

            // 处理页码切换
            if (mouseX >= x + 190 && mouseY >= y + 188 && mouseX <= x + 220 && mouseY <= y + 200) {
                if (currentPage > 0) {
                    currentPage--;
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                }
            }
            if (mouseX >= x + 220 && mouseY >= y + 188 && mouseX <= x + 250 && mouseY <= y + 200) {
                int totalPages = (items.size() + 19) / 20;
                if (currentPage + 1 < totalPages) {
                    currentPage++;
                    CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
                }
            }
        } catch (Exception e) {
            KeyCommandMod.LOGGER.error("鼠标点击事件异常", e);
        }
    }

    // GUI关闭时保存状态
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        CATEGORY_PAGE_MAP.put(currentCategory, currentPage);
        sLastPage = currentPage;
        sLastCategory = currentCategory;
    }

    // GUI不暂停游戏
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}