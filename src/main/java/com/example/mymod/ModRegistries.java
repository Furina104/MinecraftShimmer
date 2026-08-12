package com.example.mymod;

import com.example.mymod.fluid.WeiguangFluid;
import com.example.mymod.item.WeiguangBucketItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

/**
 * 模组所有注册内容统一管理。
 *
 * <p>注册内容：</p>
 * <ul>
 *   <li>流体「微光」（mymod:weiguang，含静态态和流动态两个实例）</li>
 *   <li>流体方块「微光」（mymod:weiguang_block）</li>
 *   <li>物品「微光桶」（mymod:weiguang_bucket）</li>
 * </ul>
 */
public final class ModRegistries {
    public static final String MOD_ID = MyMod.MOD_ID;

    // ── 流体「微光」──────────────────────────────────────────
    public static final WeiguangFluid.Still WEIGUANG_FLUID;
    public static final WeiguangFluid.Flowing WEIGUANG_FLOWING;
    // ── 流体对应的方块 ────────────────────────────────────────
    public static final WeiguangFluid.Block WEIGUANG_BLOCK;
    // ── 物品「微光桶」 ───────────────────────────────────────
    public static final WeiguangBucketItem WEIGUANG_BUCKET;

    static {
        // 1) 先注册静态态与流动态流体
        WEIGUANG_FLUID = Registry.register(
                Registries.FLUID,
                Identifier.of(MOD_ID, "weiguang"),
                new WeiguangFluid.Still());
        WEIGUANG_FLOWING = Registry.register(
                Registries.FLUID,
                Identifier.of(MOD_ID, "flowing_weiguang"),
                new WeiguangFluid.Flowing());

        // 2) 注册流体对应的方块
        // 注意：不能使用 Block.Settings.copy(Blocks.WATER)，因为 copy()/copyShallow()
        // 都不复制 registryKey 字段，而 AbstractBlock 构造时会调用 getTranslationKey()/
        // getLootTableKey() 读取该字段并 requireNonNull，导致 "Block id not set" NPE。
        // 因此这里用 Block.Settings.create() 手动配置并显式指定 registryKey。
        WEIGUANG_BLOCK = Registry.register(
                Registries.BLOCK,
                Identifier.of(MOD_ID, "weiguang"),
                new WeiguangFluid.Block(WEIGUANG_FLUID,
                        Block.Settings.create()
                                .liquid()
                                .noCollision()
                                .strength(100.0F)
                                .registryKey(RegistryKey.of(
                                        Registries.BLOCK.getKey(),
                                        Identifier.of(MOD_ID, "weiguang")))));

        // 3) 注册「微光桶」物品
        // 注意：与方块同理，MC 1.21.11 的 Item.Settings 也有 registryKey 字段，
        // 且 new Item.Settings() 不会设置它，构造 Item 时调用翻译键方法会 requireNonNull
        // 导致 "Item id not set" NPE。因此需要显式指定 registryKey。
        WEIGUANG_BUCKET = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "weiguang_bucket"),
                new WeiguangBucketItem(WEIGUANG_FLUID,
                        new Item.Settings()
                                .maxCount(1)
                                .registryKey(RegistryKey.of(
                                        Registries.ITEM.getKey(),
                                        Identifier.of(MOD_ID, "weiguang_bucket")))));
    }

    private ModRegistries() {
    }

    /**
     * 触发静态初始化。
     *
     * <p>静态代码块中的注册逻辑会在首次访问本类任意静态成员时自动执行。
     * 此方法仅用于在主类 {@link MyMod#onInitialize()} 中显式确保注册完成。</p>
     */
    public static void init() {
        // 静态代码块已在类加载时执行，这里无需额外逻辑。
    }
}
