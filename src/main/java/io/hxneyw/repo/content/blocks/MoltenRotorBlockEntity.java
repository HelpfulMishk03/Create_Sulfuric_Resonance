package io.hxneyw.repo.content.blocks;

import io.hxneyw.repo.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MoltenRotorBlockEntity extends BlockEntity {

    private int temperature = 0;
    private int fuelTime = 0;

    public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOLTEN_ROTOR.get(), pos, state);
    }

    public void tick() {
        boolean dirty = false;

        // Burn fuel
        int targetTemp = 0;
        if (fuelTime > 0) {
            fuelTime--;
            targetTemp = 800; // Target temperature while burning
            dirty = true;
        }

        // Gradually heat up or cool down
        if (temperature < targetTemp) {
            temperature += 5; // Heat up
            dirty = true;
        } else if (temperature > targetTemp) {
            temperature -= 3; // Cool down
            dirty = true;
        }

        if (dirty) {
            setChanged();
        }
    }

    public void addFuel(int ticks) {
        fuelTime += ticks;
        setChanged();
    }

    public int getTemperature() {
        return temperature;
    }

    // Minecraft 1.21+ requires HolderLookup.Provider parameter for saveAdditional
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("temp", temperature);
        tag.putInt("fuel", fuelTime);
    }

    // Minecraft 1.21+ requires HolderLookup.Provider parameter for loadAdditional
    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        temperature = tag.getInt("temp");
        fuelTime = tag.getInt("fuel");
    }
}