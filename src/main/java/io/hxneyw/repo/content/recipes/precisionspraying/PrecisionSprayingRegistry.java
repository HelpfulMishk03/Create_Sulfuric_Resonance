package io.hxneyw.repo.content.recipes.precisionspraying;

import io.hxneyw.repo.content.registry.AllModFluids;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public final class PrecisionSprayingRegistry {

    public static final int FLUID_PER_SPRAY = 25;
    public static final int SPRAY_CONTACTS_REQUIRED = 3;
    public static final int FLUID_PER_OPERATION = FLUID_PER_SPRAY * SPRAY_CONTACTS_REQUIRED;

    private PrecisionSprayingRegistry() {
    }

    public static boolean isSulfuricAcid(
            @NotNull FluidStack fluid
    ) {
        return !fluid.isEmpty()
                && fluid.getFluid()
                == AllModFluids.SULFURIC_ACID.get();
    }

    public static Optional<ItemStack> getResult(
            @NotNull ItemStack input,
            @NotNull FluidStack fluid
    ) {
        if (!isSulfuricAcid(fluid)
                || !(input.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }

        return getResult(blockItem.getBlock().defaultBlockState())
                .map(state -> new ItemStack(state.getBlock()));
    }

    public static Optional<BlockState> getResult(
            @NotNull BlockState input
    ) {
        return WeatheringCopper.getPrevious(input);
    }

    public static List<PrecisionSprayingDisplay> createDisplays() {
        List<PrecisionSprayingDisplay> displays = new ArrayList<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR || block.asItem() == net.minecraft.world.item.Items.AIR) {
                continue;
            }

            Optional<BlockState> previous = getResult(block.defaultBlockState());
            if (previous.isEmpty()) {
                continue;
            }

            Block resultBlock = previous.get().getBlock();
            if (resultBlock.asItem() == net.minecraft.world.item.Items.AIR) {
                continue;
            }

            displays.add(
                    new PrecisionSprayingDisplay(
                            new ItemStack(block),
                            new ItemStack(resultBlock),
                            FLUID_PER_OPERATION
                    )
            );
        }

        displays.sort(
                Comparator.comparing(display ->
                        BuiltInRegistries.ITEM
                                .getKey(display.input().getItem())
                                .toString()
                )
        );

        return List.copyOf(displays);
    }
}
