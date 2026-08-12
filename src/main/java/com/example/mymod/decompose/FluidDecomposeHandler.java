package com.example.mymod.decompose;

import com.example.mymod.ModRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

/**
 * 微光流体的分解处理器。
 *
 * <p>在服务器每个世界结束时（{@code ServerTickEvents.END_WORLD_TICK}）被调用：
 * <ul>
 *   <li>当有 {@link ItemEntity} 掉落物进入「微光」流体时，若物品可作为某种配方
 *       的产物，就把它分解成该配方的原材料；物品有多种配方时随机选择一种。</li>
 *   <li>水桶（{@link Items#WATER_BUCKET}）是例外：会被直接转化为「微光桶」物品。</li>
 *   <li>分解得到的原材料会生成在流体表面上方 1 格的位置，并悬浮在那里，直到玩家拾取。</li>
 * </ul></p>
 */
public final class FluidDecomposeHandler {

    /** 每隔多少个游戏刻扫描一次，用于分摊性能开销。 */
    private static final int SCAN_INTERVAL = 10;

    /** 被分解产物悬浮时设置的拾取延迟（游戏刻）。延迟结束后玩家可正常拾取。 */
    private static final int PICKUP_DELAY = 40;

    private static int tickCounter = 0;

    private FluidDecomposeHandler() {
    }

    /**
     * 委托方法，由 {@code ServerTickEvents.END_WORLD_TICK} 调用。
     *
     * @param world 服务器世界
     */
    public static void onEndTick(World world) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }
        if (!RecipeDatabase.isLoaded()) {
            return;
        }

        tickCounter++;
        if (tickCounter % SCAN_INTERVAL != 0) {
            return;
        }

        scanAndDecompose(serverWorld);
    }

    private static void scanAndDecompose(ServerWorld world) {
        Predicate<ItemEntity> inWeiguang = e -> {
            BlockPos pos = e.getBlockPos();
            return world.getFluidState(pos).isOf(ModRegistries.WEIGUANG_FLUID)
                    || world.getFluidState(pos.up()).isOf(ModRegistries.WEIGUANG_FLUID);
        };

        List<? extends ItemEntity> items = world.getEntitiesByType(EntityType.ITEM, inWeiguang);
        for (ItemEntity entity : items) {
            // 防止同一掉落物被处理两次后实体已无效。
            if (entity.isRemoved()) {
                continue;
            }
            ItemStack stack = entity.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.isOf(Items.WATER_BUCKET)) {
                // 例外：水桶 → 微光桶
                convertWaterBucket(world, entity);
            } else {
                decomposeItem(world, entity);
            }
        }
    }

    /**
     * 把落入微光流体中的水桶转化为「微光桶」物品。
     *
     * @param world  所在服务器世界
     * @param entity 水桶掉落物实体
     */
    private static void convertWaterBucket(ServerWorld world, ItemEntity entity) {
        ItemStack old = entity.getStack();
        old.decrement(1);
        if (old.isEmpty()) {
            entity.discard();
        }
        spawnFloatingProduct(world, entity.getBlockPos(),
                new ItemStack(ModRegistries.WEIGUANG_BUCKET));
    }

    /**
     * 把掉落物按配方分解成原材料。若多种配方则随机选择一种。
     *
     * @param world  所在服务器世界
     * @param entity 待分解的掉落物实体
     */
    private static void decomposeItem(ServerWorld world, ItemEntity entity) {
        Item item = entity.getStack().getItem();
        List<List<ItemStack>> candidates = RecipeDatabase.getMaterialsFor(item);
        if (candidates.isEmpty()) {
            return; // 无配方，无法分解，保持原样
        }

        // 随机选择一种配方。
        List<ItemStack> materials = candidates.get(world.random.nextInt(candidates.size()));

        ItemStack old = entity.getStack();
        old.decrement(1);
        if (old.isEmpty()) {
            entity.discard();
        }

        // 在流体表面上方 1 格生成原材料掉落物。
        for (ItemStack material : materials) {
            spawnFloatingProduct(world, entity.getBlockPos(), material.copy());
        }
    }

    /**
     * 在指定格子的上方 1 格生成悬浮的原材料掉落物（无重力、短暂拾取延迟）。
     *
     * @param world  服务器世界
     * @param origin 基准格子坐标
     * @param stack  要生成的物品
     */
    private static void spawnFloatingProduct(ServerWorld world, BlockPos origin, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double x = origin.getX() + 0.5;
        double y = origin.getY() + 1.0;
        double z = origin.getZ() + 0.5;

        ItemEntity product = new ItemEntity(world, x, y + 1.0, z, stack, 0, 0, 0);
        product.setNoGravity(true);      // 悬浮不落地
        product.setPickupDelay(PICKUP_DELAY);
        product.setGlowing(true);        // 微光效果，突出显示
        world.spawnEntity(product);
    }
}