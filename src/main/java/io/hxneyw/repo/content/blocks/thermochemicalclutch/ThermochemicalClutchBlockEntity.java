package io.hxneyw.repo.content.blocks.thermochemicalclutch;

import com.simibubi.create.content.kinetics.transmission.ClutchBlockEntity;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatData;
import io.hxneyw.repo.content.blocks.thermochemical.ThermochemicalHeatTickHelper;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ThermochemicalClutchBlockEntity
        extends ClutchBlockEntity {
    private static final float LOCK_STEP = 1.0F / 8.0F;
    private static final int ENGAGE_SOUND_DELAY = 4;
    private static final int RELEASE_SOUND_DELAY = 2;

    private final ThermochemicalHeatData heatData =
            new ThermochemicalHeatData();

    private boolean lockAnimationInitialized;
    private boolean lastPowered;
    private float lockProgress;
    private float previousLockProgress;
    private int soundDelay = -1;
    private boolean pendingSoundPowered;

    public ThermochemicalClutchBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(
                AllBlockEntities.THERMOCHEMICAL_CLUTCH.get(),
                position,
                state
        );
    }

    @Override
    public void tick() {
        super.tick();
        tickLockAnimation();

        ThermochemicalHeatTickHelper.tick(
                this,
                heatData,
                this::sendData
        );
    }

    private void tickLockAnimation() {
        boolean powered = getBlockState().getValue(
                ThermochemicalClutchBlock.POWERED
        );

        if (!lockAnimationInitialized) {
            lockAnimationInitialized = true;
            lastPowered = powered;
            lockProgress = powered ? 1.0F : 0.0F;
            previousLockProgress = lockProgress;
            return;
        }

        previousLockProgress = lockProgress;

        if (powered != lastPowered) {
            lastPowered = powered;
            pendingSoundPowered = powered;
            soundDelay = powered
                    ? ENGAGE_SOUND_DELAY
                    : RELEASE_SOUND_DELAY;
        }

        float target = powered ? 1.0F : 0.0F;
        if (lockProgress < target) {
            lockProgress = Math.min(target, lockProgress + LOCK_STEP);
        } else if (lockProgress > target) {
            lockProgress = Math.max(target, lockProgress - LOCK_STEP);
        }

        tickLockSound();
    }

    private void tickLockSound() {
        if (soundDelay < 0 || level == null || level.isClientSide) {
            return;
        }

        if (soundDelay > 0) {
            soundDelay--;
            return;
        }

        level.playSound(
                null,
                worldPosition,
                pendingSoundPowered
                        ? AllModSounds.THERMOCHEMICAL_CLUTCH_ENGAGE.get()
                        : AllModSounds.THERMOCHEMICAL_CLUTCH_RELEASE.get(),
                SoundSource.BLOCKS,
                pendingSoundPowered ? 0.81F : 0.675F,
                pendingSoundPowered ? 0.92F : 1.04F
        );
        soundDelay = -1;
    }

    public float getLockProgress(float partialTicks) {
        if (!lockAnimationInitialized) {
            return getBlockState().getValue(ThermochemicalClutchBlock.POWERED)
                    ? 1.0F
                    : 0.0F;
        }

        return Mth.lerp(
                partialTicks,
                previousLockProgress,
                lockProgress
        );
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        super.addToGoggleTooltip(
                tooltip,
                isPlayerSneaking
        );

        heatData.addTooltip(
                tooltip,
                isPlayerSneaking,
                "block.sulfuricresonance.thermochemical_clutch",
                getBlockState()
                        .getValue(ThermochemicalClutchBlock.AXIS)
                        .getName()
                        .toUpperCase(),
                -1,
                null
        );

        boolean powered = getBlockState().getValue(
                ThermochemicalClutchBlock.POWERED
        );
        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance.thermochemical_clutch.state",
                        Component.translatable(
                                powered
                                        ? "tooltip.sulfuricresonance.thermochemical_clutch.blocked"
                                        : "tooltip.sulfuricresonance.thermochemical_clutch.passing"
                        ).withStyle(
                                powered
                                        ? ChatFormatting.RED
                                        : ChatFormatting.GREEN
                        )
                ).withStyle(ChatFormatting.GRAY)
        );

        return true;
    }

    @Override
    protected void write(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.write(
                tag,
                provider,
                clientPacket
        );
        heatData.write(
                tag,
                clientPacket
        );
    }

    @Override
    protected void read(
            CompoundTag tag,
            Provider provider,
            boolean clientPacket
    ) {
        super.read(
                tag,
                provider,
                clientPacket
        );
        heatData.read(
                tag,
                clientPacket
        );
    }
}
