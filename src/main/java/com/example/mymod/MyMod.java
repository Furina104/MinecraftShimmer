package com.example.mymod;

import com.example.mymod.decompose.FluidDecomposeHandler;
import com.example.mymod.decompose.RecipeDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组主类（入口点）。
 *
 * <p>参考 Fabric 模组模板结构。在 {@link #onInitialize()} 中进行模组初始化：</p>
 * <ul>
 *   <li>触发 {@link ModRegistries} 静态初始化，注册「微光」流体、方块与「微光桶」物品。</li>
 *   <li>注册服务器启动事件，加载全部配方到 {@link RecipeDatabase}。</li>
 *   <li>注册世界结束 tick 事件，由 {@link FluidDecomposeHandler} 执行分解逻辑。</li>
 * </ul>
 */
public class MyMod implements ModInitializer {
    /**
     * 模组的唯一标识符（必须与 fabric.mod.json 中的 "id" 一致）。
     */
    public static final String MOD_ID = "mymod";

    /**
     * 日志记录器，用于输出模组日志。
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("{} 模组已加载！", MOD_ID);

        // 触发 ModRegistries 静态代码块，完成「微光」流体/方块与「微光桶」物品的注册。
        ModRegistries.init();

        // 服务器完全启动后，读取全部游戏配方建立「产物 → 原材料」映射。
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                RecipeDatabase.loadAll(server, server.getOverworld()));

        // 每个服务器世界 tick 结束时，执行微光流体内的分解/转化逻辑。
        ServerTickEvents.END_WORLD_TICK.register(FluidDecomposeHandler::onEndTick);
    }
}
