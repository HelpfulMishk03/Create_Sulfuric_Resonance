package io.hxneyw.repo.content.blocks;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class MoltenRotorBlock extends DirectionalKineticBlock implements IBE<MoltenRotorBlockEntity> {

    // Store heat level in block state (like Blaze Burner)
    public static final EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL =
            BlazeBurnerBlock.HEAT_LEVEL;

    public MoltenRotorBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState()
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_LEVEL);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(FACING);
        // BOTH left and right sides output rotation
        Direction leftSide = facing.getCounterClockWise();
        Direction rightSide = facing.getClockWise();
        return face == leftSide || face == rightSide;
    }

    @Override
    public Class<MoltenRotorBlockEntity> getBlockEntityClass() {
        return MoltenRotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MoltenRotorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MOLTEN_ROTOR.get();
    }

    // ========== DYNAMIC LIGHT LEVEL (NOT STATIC!) ==========

    @Override
    public int getLightEmission(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        // Read heat level from block state
        BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);
        return switch (heat) {
            case NONE -> 0;
            case SMOULDERING -> 8;
            case FADING, KINDLED, SEETHING -> 15;
        };
    }

    // ========== FUEL ADDING ==========

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
        if (furnace == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack held = player.getItemInHand(hand);

        // Coal = 1600 ticks (80 seconds)
        if (held.is(Items.COAL)) {
            furnace.addFuel(1600);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.sendSystemMessage(Component.literal("§6Added coal! §7(+80s)"));
            return ItemInteractionResult.SUCCESS;
        }

        // Blaze powder = 2400 ticks (120 seconds)
        if (held.is(Items.BLAZE_POWDER)) {
            furnace.addFuel(2400);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.sendSystemMessage(Component.literal("§6Added blaze powder! §7(+120s)"));
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    private MoltenRotorBlockEntity getBlockEntity(Level level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MoltenRotorBlockEntity moltenRotor) {
            return moltenRotor;
        }
        return null;
    }

    // ========== STATUS DISPLAY ==========

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
        if (furnace == null) return InteractionResult.FAIL;

        BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);
        int fuel = furnace.getRemainingBurnTime();
        int fuelSeconds = fuel / 20;
        float rpm = furnace.getGeneratedSpeed();
        boolean isCombustion = furnace.isCombustion();

        // Color based on heat level
        String color = switch (heat) {
            case NONE -> "§7";           // Gray
            case SMOULDERING -> "§e";    // Yellow
            case FADING -> "§6";         // Gold
            case KINDLED -> "§c";        // Red
            case SEETHING -> "§4";       // Dark Red
        };

        if (isCombustion) {
            color = "§5"; // Purple (COMBUSTION!)
        }

        String heatName = isCombustion ? "COMBUSTION" : heat.name();
        player.sendSystemMessage(Component.literal(
                color + heatName + " §7| Fuel: " + fuelSeconds + "s | RPM: " + (int)rpm
        ));

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE);
    }

    // ========== PARTICLES & SOUNDS ==========

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);

        if (!heat.isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING)) {
            return;
        }

        // Occasional crackle sound
        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.5f + random.nextFloat() * 0.2f,
                    random.nextFloat() * 0.7f + 0.6f,
                    false);
        }

        // Smoke
        if (random.nextInt(10) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.05, 0);
        }

        // Flame particles when hot
        if (heat.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            if (random.nextInt(5) == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        pos.getX() + 0.5,
                        pos.getY() + 0.6,
                        pos.getZ() + 0.5,
                        0, 0, 0);
            }
        }

        // Soul fire for COMBUSTION
        MoltenRotorBlockEntity be = getBlockEntity(level, pos);
        if (be != null && be.isCombustion() && random.nextInt(3) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    pos.getX() + 0.5,
                    pos.getY() + 0.7,
                    pos.getZ() + 0.5,
                    (random.nextFloat() - 0.5) * 0.1,
                    0.05,
                    (random.nextFloat() - 0.5) * 0.1);
        }
    }
}