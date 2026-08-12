package com.example.mymod.item;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;

/**
 * 「微光桶」物品。
 *
 * <p>继承自 {@link BucketItem}，用于承载（放置/盛取）微光流体。</p>
 */
public class WeiguangBucketItem extends BucketItem {

    public WeiguangBucketItem(Fluid fluid, Item.Settings settings) {
        super(fluid, settings);
    }
}
