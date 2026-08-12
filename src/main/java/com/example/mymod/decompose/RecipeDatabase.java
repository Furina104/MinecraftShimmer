package com.example.mymod.decompose;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配方数据库。
 *
 * <p>在服务器启动（{@code ServerLifecycleEvents.SERVER_STARTED}）时，读取当前
 * 游戏的全部配方，并建立「产物物品 → 候选配方」的映射。每个候选配方对应一组
 * 可分解出的原材料（{@code List<ItemStack>}）。当掉落物被丢入「微光」流体时，
 * {@link FluidDecomposeHandler} 会依据此数据库把产物分解回原材料。</p>
 */
public final class RecipeDatabase {

    /**
     * 产物物品 → 候选配方列表。每个配方表示该物品可被分解出的一组原材料。
     * 同一个物品可能对应多个配方（例如多种合成方式），分解时随机选择其一。
     */
    private static final Map<Item, List<List<ItemStack>>> PRODUCT_TO_MATERIALS = new HashMap<>();

    private static boolean loaded = false;

    private RecipeDatabase() {
    }

    /**
     * 从服务器的配方管理器中加载全部配方并建立映射。
     *
     * @param server 服务器实例
     * @param world  一个可供构造 {@code ContextParameterMap} 的世界
     */
    public static synchronized void loadAll(MinecraftServer server, World world) {
        PRODUCT_TO_MATERIALS.clear();
        var context = SlotDisplayContexts.createParameters(world);

        for (RecipeEntry<?> entry : server.getRecipeManager().values()) {
            Recipe<?> recipe = entry.value();

            // 获取配方产物（用第一个 display 的结果）。
            List<RecipeDisplay> displays = recipe.getDisplays();
            if (displays.isEmpty()) {
                continue;
            }
            ItemStack product = displays.get(0).result().getFirst(context);
            if (product.isEmpty()) {
                continue;
            }

            // 获取配方原材料（按各槽位展开）。
            List<ItemStack> materials = materialize(recipe);
            if (materials.isEmpty()) {
                continue;
            }

            PRODUCT_TO_MATERIALS
                    .computeIfAbsent(product.getItem(), k -> new ArrayList<>())
                    .add(materials);
        }

        loaded = true;
    }

    /**
     * 把配方的原料部分转换为具体的 {@link ItemStack} 列表。
     *
     * <p>利用 {@link IngredientPlacement#getPlacementSlots()} 把原料按实际合成格
     * 位置展开，使得例如「木镐 = 2 木棍 + 3 木板」能正确得到 2 个木棍和 3 个木板。
     * 每个原料槽位若可匹配多种物品，则取首个匹配项。</p>
     */
    private static List<ItemStack> materialize(Recipe<?> recipe) {
        IngredientPlacement placement = recipe.getIngredientPlacement();
        if (placement.hasNoPlacement()) {
            return List.of();
        }
        List<Ingredient> ingredients = placement.getIngredients();
        IntList slots = placement.getPlacementSlots();

        List<ItemStack> materials = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            int idx = slots.getInt(i);
            if (idx < 0 || idx >= ingredients.size()) {
                continue;
            }
            Ingredient ingredient = ingredients.get(idx);
            if (ingredient.isEmpty()) {
                continue;
            }
            Item item = ingredient.getMatchingItems()
                    .map(RegistryEntry::value)
                    .findFirst()
                    .orElse(null);
            if (item != null) {
                materials.add(new ItemStack(item));
            }
        }
        return materials;
    }

    /**
     * 判断某物品是否可被分解（即是否作为某种配方的产物出现）。
     */
    public static boolean canDecompose(Item item) {
        return loaded && PRODUCT_TO_MATERIALS.containsKey(item);
    }

    /**
     * 获取某物品对应的全部候选原材料组。组元素之间按配方区分，随机取一组即可
     * 实现「多配方随机选择一种」的效果。
     *
     * @param item 产物物品
     * @return 候选配方（原材料组）列表，可能为空
     */
    public static List<List<ItemStack>> getMaterialsFor(Item item) {
        if (!loaded) {
            return List.of();
        }
        return PRODUCT_TO_MATERIALS.getOrDefault(item, List.of());
    }

    /**
     * 是否已完成加载。
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * 清除映射（主要用于调试/重载）。
     */
    public static synchronized void clear() {
        PRODUCT_TO_MATERIALS.clear();
        loaded = false;
    }
}