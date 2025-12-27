package io.hxneyw.repo.mixin;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import io.hxneyw.repo.content.blocks.MoltenRotorBlock;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity;
import io.hxneyw.repo.content.recipes.CombustionMixingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BasinRecipe.class, remap = false)
public abstract class BasinRecipeMixin {

    /**
     * Enforce combustion mixing requirements:
     * - Must have Molten Rotor (not Blaze Burner) below basin
     * - Molten Rotor must be at RADIANT heat tier
     */
    @Inject(
            method = "match(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void enforceCombustionRequirements(
            BasinBlockEntity basin,
            Recipe<?> recipe,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(recipe instanceof CombustionMixingRecipe combustion)) {
            return;
        }

        if (combustion.getRequiredHeat() == HeatCondition.SUPERHEATED) {
            if (!createSulfuricResonance$hasRadiantHeat(basin)) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * Check for RADIANT heat from Molten Rotor only
     * Rejects Blaze Burners at any heat level
     */
    @Unique
    private static boolean createSulfuricResonance$hasRadiantHeat(BasinBlockEntity basin) {
        Level level = basin.getLevel();
        if (level == null) return false;

        BlockPos heaterPos = basin.getBlockPos().below();
        BlockState heaterState = level.getBlockState(heaterPos);

        // Must be Molten Rotor (NOT Blaze Burner)
        if (!(heaterState.getBlock() instanceof MoltenRotorBlock)) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(heaterPos);
        if (!(be instanceof MoltenRotorBlockEntity rotor)) {
            return false;
        }

        // Must be at RADIANT tier
        return rotor.getCurrentHeatTier() == MoltenRotorBlockEntity.RotorHeatLevel.RADIANT;
    }
}