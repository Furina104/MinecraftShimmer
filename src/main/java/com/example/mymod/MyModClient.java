package com.example.mymod;

import com.example.mymod.ModRegistries;
import com.example.mymod.fluid.WeiguangFluid;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

/**
 * 客户端入口点。
 *
 * <p>为「微光」流体注册半透明渲染：使用原版水的流动纹理，叠加微光主题色
 * {@code #CC88FF}（紫粉色）。同时把微光流体方块标记为透明，使流体能够
 * 按半透明渲染层绘制。</p>
 */
public class MyModClient implements ClientModInitializer {

    /** 微光主题色：{@code #CC88FF} */
    private static final int WEIGUANG_COLOR = 0xCC88FF;

    @Override
    public void onInitializeClient() {
        // 用原版水的 still / flowing 精灵，叠加微光紫粉色 tint
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(
                SimpleFluidRenderHandler.WATER_STILL,
                SimpleFluidRenderHandler.WATER_FLOWING,
                WEIGUANG_COLOR);

        // 将静态态与流动态微光流体都注册为同一渲染器
        FluidRenderHandlerRegistry.INSTANCE.register(
                ModRegistries.WEIGUANG_FLUID,
                ModRegistries.WEIGUANG_FLOWING,
                handler);

        // 微光流体方块按半透明渲染（否则会显示成不透明的水块）
        FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(
                ModRegistries.WEIGUANG_BLOCK, true);
    }
}
