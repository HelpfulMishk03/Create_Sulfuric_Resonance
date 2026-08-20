package io.hxneyw.repo.content.items;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeBlockEntity;
import io.hxneyw.repo.content.blocks.thermalgauge.ThermalGaugeHostPayload;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ThermalGaugeItem extends ThermalRelaySwitchItem {

    private static final ThreadLocal<Vec3> PLACEMENT_CLICK =
            new ThreadLocal<>();

    public ThermalGaugeItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(
            @NotNull ItemStack stack,
            @NotNull net.minecraft.world.item.context.UseOnContext context
    ) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!AllBlocks.FACTORY_GAUGE.has(state)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        PanelSlot slot = FactoryPanelBlock.getTargetedSlot(
                pos,
                state,
                context.getClickLocation()
        );

        BlockEntity current = level.getBlockEntity(pos);
        if (current instanceof ThermalGaugeBlockEntity gauge
                && gauge.isOccupied(slot)) {
            return InteractionResult.FAIL;
        }
        if (current instanceof FactoryPanelBlockEntity factory
                && factory.panels.get(slot).isActive()) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ThermalGaugeBlockEntity gauge =
                ThermalGaugeBlockEntity.upgradeFactoryGauge(level, pos);
        if (gauge == null || gauge.isOccupied(slot)) {
            return InteractionResult.FAIL;
        }

        UUID networkId = ThermalRelaySwitchItem.getNetworkId(stack);
        ThermalRelaySwitchItem.FurnaceLink link =
                ThermalRelaySwitchItem.getLinkedFurnace(stack);

        if (!gauge.addGauge(slot, networkId, link)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel,
                    new ChunkPos(pos),
                    ThermalGaugeHostPayload.create(gauge)
            );
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult place(
            @NotNull BlockPlaceContext context
    ) {
        PLACEMENT_CLICK.set(context.getClickLocation());

        try {
            return super.place(context);
        } finally {
            PLACEMENT_CLICK.remove();
        }
    }

    @Nullable
    public static Vec3 getPlacementClickLocation() {
        return PLACEMENT_CLICK.get();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown() || !hasConnections(stack)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            clearConnections(stack);
            player.getInventory().setChanged();
            player.displayClientMessage(
                    Component.translatable(
                            "message.sulfuricresonance.thermal_gauge.network_removed"
                    ),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
        );
    }
}
