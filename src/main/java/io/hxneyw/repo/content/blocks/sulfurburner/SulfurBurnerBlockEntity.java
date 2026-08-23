package io.hxneyw.repo.content.blocks.sulfurburner;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.AllModSounds;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SulfurBurnerBlockEntity
        extends BlockEntity
        implements IHaveGoggleInformation {

    public static final int WARMUP_TICKS = 100;

    private static Consumer<SulfurBurnerBlockEntity> clientEffectsTick =
            blockEntity -> {};

    private int remainingBurnTicks;
    private int activeBurnTicks;
    private int syncCountdown = 20;
    private ItemStack activeFuelStack =
            ItemStack.EMPTY;

    private final ItemStackHandler fuelInventory =
            new ItemStackHandler(1) {

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return SulfurBurnerFuel.isFuel(stack);
                }

                @Override
                public int getSlotLimit(int slot) {
                    return 15;
                }

                @Override
                protected void onContentsChanged(int slot) {
                    setChanged();
                }
            };


    private final IItemHandler automationFuelHandler =
            new IItemHandler() {

                @Override
                public int getSlots() {
                    return fuelInventory.getSlots();
                }

                @Override
                public @NotNull ItemStack getStackInSlot(
                        int slot
                ) {
                    return fuelInventory.getStackInSlot(slot);
                }

                @Override
                public @NotNull ItemStack insertItem(
                        int slot,
                        @NotNull ItemStack stack,
                        boolean simulate
                ) {
                    return fuelInventory.insertItem(
                            slot,
                            stack,
                            simulate
                    );
                }

                @Override
                public @NotNull ItemStack extractItem(
                        int slot,
                        int amount,
                        boolean simulate
                ) {
                    return ItemStack.EMPTY;
                }

                @Override
                public int getSlotLimit(int slot) {
                    return fuelInventory.getSlotLimit(slot);
                }

                @Override
                public boolean isItemValid(
                        int slot,
                        @NotNull ItemStack stack
                ) {
                    return fuelInventory.isItemValid(
                            slot,
                            stack
                    );
                }
            };

    public SulfurBurnerBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                AllBlockEntities.SULFUR_BURNER.get(),
                pos,
                state
        );
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            SulfurBurnerBlockEntity burner
    ) {
        if (level.isClientSide) {
            return;
        }


        if (burner.remainingBurnTicks <= 0) {
            boolean wasBurning =
                    burner.activeBurnTicks > 0
                            || !burner.activeFuelStack.isEmpty();

            if (!burner.tryStartNextFuel(
                    level,
                    pos
            )) {
                burner.activeBurnTicks = 0;
                burner.activeFuelStack =
                        ItemStack.EMPTY;

                burner.updateHeat(
                        level,
                        pos,
                        HeatLevel.NONE
                );

                if (wasBurning) {
                    level.playSound(
                            null,
                            pos,
                            AllModSounds.SULFUR_BURNER_EXTINGUISH.get(),
                            SoundSource.BLOCKS,
                            0.6F,
                            1.00F
                    );
                }

                burner.markAndSync();
                return;
            }
        }

        burner.activeBurnTicks++;

        HeatLevel targetHeat =
                burner.activeBurnTicks >= WARMUP_TICKS
                        ? HeatLevel.SEETHING
                        : HeatLevel.KINDLED;

        burner.updateHeat(
                level,
                pos,
                targetHeat
        );

        burner.remainingBurnTicks--;

        if (--burner.syncCountdown <= 0) {
            burner.syncCountdown = 20;
            burner.markAndSync();
        } else {
            burner.setChanged();
        }
    }

    private boolean tryStartNextFuel(
            Level level,
            BlockPos pos
    ) {
        ItemStack queued =
                fuelInventory.getStackInSlot(0);

        int burnTicks =
                SulfurBurnerFuel.getBurnTicks(queued);

        if (burnTicks <= 0) {
            return false;
        }

        boolean coldStart =
                activeBurnTicks <= 0
                        && activeFuelStack.isEmpty();

        activeFuelStack =
                queued.copyWithCount(1);

        fuelInventory.extractItem(
                0,
                1,
                false
        );

        remainingBurnTicks =
                burnTicks;

        if (coldStart) {
            level.playSound(
                    null,
                    pos,
                    AllModSounds.SULFUR_BURNER_IGNITE.get(),
                    SoundSource.BLOCKS,
                    1.7F,
                    0.98F
            );
        }

        markAndSync();

        return true;
    }

    private void updateHeat(
            Level level,
            BlockPos pos,
            HeatLevel newHeat
    ) {
        BlockState current =
                level.getBlockState(pos);

        if (!current.hasProperty(
                SulfurBurnerBlock.HEAT_LEVEL
        )) {
            return;
        }

        HeatLevel oldHeat =
                current.getValue(
                        SulfurBurnerBlock.HEAT_LEVEL
                );

        if (oldHeat == newHeat) {
            return;
        }

        level.setBlock(
                pos,
                current.setValue(
                        SulfurBurnerBlock.HEAT_LEVEL,
                        newHeat
                ),
                Block.UPDATE_ALL
        );


        BlockEntity above =
                level.getBlockEntity(pos.above());

        if (above instanceof BasinBlockEntity basin) {
            basin.notifyChangeOfContents();
        }

        markAndSync();
    }

    public boolean insertOneFuel(
            ItemStack heldStack,
            boolean simulate
    ) {
        if (!SulfurBurnerFuel.isFuel(heldStack)) {
            return false;
        }

        ItemStack one = heldStack.copy();
        one.setCount(1);

        ItemStack remainder =
                fuelInventory.insertItem(
                        0,
                        one,
                        simulate
                );

        if (!remainder.isEmpty()) {
            return false;
        }

        if (!simulate) {
            markAndSync();
        }

        return true;
    }

    public IItemHandler getAutomationFuelHandler() {
        return automationFuelHandler;
    }

    public ItemStack drainQueuedFuelForDrop() {
        ItemStack stack =
                fuelInventory.getStackInSlot(0).copy();

        fuelInventory.setStackInSlot(
                0,
                ItemStack.EMPTY
        );

        return stack;
    }

    @Override
    public boolean addToGoggleTooltip(
            List<Component> tooltip,
            boolean isPlayerSneaking
    ) {
        tooltip.add(Component.literal(""));

        HeatLevel heat =
                getBlockState().getValue(
                        SulfurBurnerBlock.HEAT_LEVEL
                );

        Component heatName =
                switch (heat) {
                    case SEETHING ->
                            Component.translatable(
                                    "heat.sulfuricresonance.superheated"
                            );
                    case KINDLED ->
                            Component.translatable(
                                    "heat.sulfuricresonance.heated"
                            );
                    default ->
                            Component.translatable(
                                    "heat.sulfuricresonance.idle"
                            );
                };

        ChatFormatting heatColor =
                switch (heat) {
                    case SEETHING ->
                            ChatFormatting.DARK_RED;
                    case KINDLED ->
                            ChatFormatting.RED;
                    default ->
                            ChatFormatting.GRAY;
                };

        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance."
                                + "sulfur_burner.heat",
                        heatName
                ).withStyle(heatColor)
        );

        if (remainingBurnTicks > 0) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance."
                                    + "sulfur_burner."
                                    + "fuel_remaining",
                            formatTicks(
                                    remainingBurnTicks
                            )
                    ).withStyle(
                            ChatFormatting.GREEN
                    )
            );
        }
        ItemStack queuedFuel =
                fuelInventory.getStackInSlot(0);

        if (!queuedFuel.isEmpty()) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance."
                                    + "sulfur_burner."
                                    + "queued_fuel",
                            queuedFuel.getHoverName(),
                            queuedFuel.getCount()
                    ).withStyle(
                            ChatFormatting.GRAY
                    )
            );
        }
        if (remainingBurnTicks > 0
                && activeBurnTicks < WARMUP_TICKS) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.sulfuricresonance."
                                    + "sulfur_burner."
                                    + "warming"
                    ).withStyle(
                            ChatFormatting.YELLOW
                    )
            );
        }

        tooltip.add(
                Component.translatable(
                        "tooltip.sulfuricresonance."
                                + "sulfur_burner."
                                + "local_only"
                ).withStyle(
                        ChatFormatting.DARK_GRAY
                )
        );

        return true;
    }

    public static void clientTick(
            Level level,
            BlockPos pos,
            SulfurBurnerBlockEntity burner
    ) {
        BlockState state =
                level.getBlockState(pos);

        if (burner.remainingBurnTicks > 0) {
            burner.remainingBurnTicks--;
        }

        if (!state.hasProperty(
                SulfurBurnerBlock.HEAT_LEVEL
        )) {
            return;
        }

        HeatLevel heat =
                state.getValue(
                        SulfurBurnerBlock.HEAT_LEVEL
                );

        if (heat == HeatLevel.KINDLED
                || heat == HeatLevel.SEETHING) {
            burner.activeBurnTicks++;
        }

        clientEffectsTick.accept(burner);
    }

    public static void setClientEffectsTick(
            Consumer<SulfurBurnerBlockEntity> effectsTick
    ) {
        clientEffectsTick =
                Objects.requireNonNull(effectsTick);
    }

    public boolean isBurning() {
        BlockState state =
                getBlockState();

        if (!state.hasProperty(
                SulfurBurnerBlock.HEAT_LEVEL
        )) {
            return false;
        }

        HeatLevel heat =
                state.getValue(
                        SulfurBurnerBlock.HEAT_LEVEL
                );

        return remainingBurnTicks > 0
                && (
                heat == HeatLevel.KINDLED
                        || heat == HeatLevel.SEETHING
        );
    }

    public HeatLevel getHeatLevel() {
        BlockState state =
                getBlockState();

        if (!state.hasProperty(
                SulfurBurnerBlock.HEAT_LEVEL
        )) {
            return HeatLevel.NONE;
        }

        return state.getValue(
                SulfurBurnerBlock.HEAT_LEVEL
        );
    }

    public int getRemainingBurnTicks() {
        return remainingBurnTicks;
    }

    public int getActiveBurnTicks() {
        return activeBurnTicks;
    }

    public float getWarmupProgress() {
        if (!isBurning()) {
            return 0.0F;
        }

        return Math.min(
                1.0F,
                activeBurnTicks
                        / (float) WARMUP_TICKS
        );
    }

    private static String formatTicks(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return minutes > 0
                ? minutes + "m " + seconds + "s"
                : seconds + "s";
    }

    private void markAndSync() {
        setChanged();

        if (level == null || level.isClientSide) {
            return;
        }

        BlockState state = getBlockState();

        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(
            @NotNull HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener>
    getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getRenderedFuelStack() {
        if (remainingBurnTicks <= 0) {
            return ItemStack.EMPTY;
        }

        return activeFuelStack.copy();
    }

    @Override
    protected void saveAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put(
                "FuelInventory",
                fuelInventory.serializeNBT(registries)
        );

        tag.putInt(
                "RemainingBurnTicks",
                remainingBurnTicks
        );

        tag.putInt(
                "ActiveBurnTicks",
                activeBurnTicks
        );

        if (!activeFuelStack.isEmpty()) {
            tag.put(
                    "ActiveFuelStack",
                    activeFuelStack.save(registries)
            );
        }

    }



    @Override
    protected void loadAdditional(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.contains("FuelInventory")) {
            fuelInventory.deserializeNBT(
                    registries,
                    tag.getCompound("FuelInventory")
            );
        }

        remainingBurnTicks =
                tag.getInt("RemainingBurnTicks");

        activeBurnTicks =
                tag.getInt("ActiveBurnTicks");

        activeFuelStack =
                ItemStack.EMPTY;

        if (tag.contains(
                "ActiveFuelStack",
                Tag.TAG_COMPOUND
        )) {
            activeFuelStack =
                    ItemStack.parseOptional(
                            registries,
                            tag.getCompound(
                                    "ActiveFuelStack"
                            )
                    );
        }

    }



}