package com.example.mymod.fluid;

import com.example.mymod.ModRegistries;
import com.example.mymod.item.WeiguangBucketItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * 「微光」流体。
 *
 * <p>一种特殊流体，当掉落物落入其中时会被分解为原材料（见
 * {@link com.example.mymod.decompose.FluidDecomposeHandler}）。水桶例外，会被
 * 转化为「微光桶」物品。</p>
 *
 * <p>结构参考原版 {@code WaterFluid}：抽象流体类 + {@link Still}（静止态）+
 * {@link Flowing}（流动态）+ {@link Block}（流体方块）。</p>
 */
public abstract class WeiguangFluid extends FlowableFluid {

    @Override
    public Fluid getFlowing() {
        return ModRegistries.WEIGUANG_FLOWING;
    }

    @Override
    public Fluid getStill() {
        return ModRegistries.WEIGUANG_FLUID;
    }

    @Override
    public Item getBucketItem() {
        return ModRegistries.WEIGUANG_BUCKET;
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        // 对齐原版 WaterFluid.matchesType（返回 fluid==WATER || fluid==FLOWING_WATER，
        // 使 Still 与 Flowing 两个实例互认）。
        // 微光的 Still(WEIGUANG_FLUID) 与 Flowing(WEIGUANG_FLOWING) 是两个不同实例，
        // 基类默认实现是严格相等（fluid==this），会导致流动态邻居无法被 Still 实例识别
        // （getUpdatedState 用 matchesType 判断邻居是否同类流体），从而把流动态误判为
        // EMPTY 清空，表现为"流出一格后消失又重复"。
        // 用 instanceof WeiguangFluid 等价于 getStill()==fluid || getFlowing()==fluid。
        return fluid instanceof WeiguangFluid;
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        // 对齐原版 WaterFluid.isInfinite（水返回 true，是无限源）。
        // 这样微光源块就能与水一样形成无限水源，扩散/流动行为完全一致。
        return true;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        // 微光流体不破坏方块，直接传递为默认行为。
    }

    @Override
    public int getMaxFlowDistance(WorldView world) {
        return 4;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return ModRegistries.WEIGUANG_BLOCK.getDefaultState()
                .with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    @Override
    public int getLevelDecreasePerBlock(WorldView world) {
        return 1;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 5;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
                                        Fluid fluid, Direction direction) {
        // 对齐原版 WaterFluid 的逻辑：
        //   仅当有流体从上方（DOWN 方向）流入、且目标流体不是微光自身时，才允许被替换；
        //   其余情况一律返回 false，避免微光被随意覆盖/破坏。
        return direction == Direction.DOWN && !(fluid instanceof WeiguangFluid);
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }

    /**
     * 获取给定 {@link FluidState} 对应的微光流体方块 {@link BlockState} 的等级属性值。
     *
     * @param state 流体状态
     * @return 对应 BlockState 的等级值（{@link Properties#LEVEL_15}）
     */
    protected static int getBlockStateLevel(FluidState state) {
        // 必须与原版 FlowableFluid.getBlockStateLevel 保持一致：
        //   still        -> 0
        //   flowing      -> 8 - min(level, 8)
        //   flowing+FALL -> 8 - min(level, 8) + 8
        // 因为 FluidBlock.getFluidState 会用 (LEVEL_15 值) 查
        // statesByLevel[min(level,8)]：index 0 是 still，index 8 是 falling 流动态。
        if (state.isStill()) {
            return 0;
        }
        int level = 8 - Math.min(state.getLevel(), 8);
        if (state.get(FALLING)) {
            level += 8;
        }
        return level;
    }

    /**
     * 静止态流体（完整一格的微光流体）。
     */
    public static class Still extends WeiguangFluid {
        public Still() {
        }

        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }

    /**
     * 流动态流体（会扩散的微光流体）。
     */
    public static class Flowing extends WeiguangFluid {
        public Flowing() {
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    /**
     * 微光流体对应的方块 {@link net.minecraft.block.FluidBlock} 子类。
     */
    public static class Block extends FluidBlock {
        public Block(FlowableFluid fluid, Block.Settings settings) {
            super(fluid, settings);
        }
    }
}
