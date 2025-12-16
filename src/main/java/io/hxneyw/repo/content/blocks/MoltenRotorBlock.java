package io.hxneyw.repo.content.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.ModBlockEntities;
import io.hxneyw.repo.content.blocks.MoltenRotorBlockEntity.FuelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * MOLTEN ROTOR FURNACE BLOCK
 * A kinetic generator powered by combustion that can heat adjacent machines
 */
public class MoltenRotorBlock extends DirectionalKineticBlock implements IBE<MoltenRotorBlockEntity> {

    public static final EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;

    public MoltenRotorBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState()
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HEAT_LEVEL);
    }

    // ========== KINETIC PROPERTIES ==========

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction leftSide = facing.getCounterClockWise();
        return leftSide.getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(FACING);
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

    @Override
    public int getLightEmission(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);
        return switch (heat) {
            case NONE -> 0;
            case SMOULDERING, FADING -> 8;
            case KINDLED -> 12;
            case SEETHING -> 15;
        };
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    // ========== FUEL SYSTEM ==========

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
        if (furnace == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack held = player.getItemInHand(hand);

        // STICKS (only work with logs burning)
        if (held.is(Items.STICK)) {
            ItemInteractionResult result = tryAddFuel(furnace, player, held, FuelType.STICK);
            if (result == ItemInteractionResult.FAIL) {
                player.sendSystemMessage(Component.literal("§l§e✖ Sticks require logs to be burning!"));
            }
            return result;
        }

        // LOGS
        if (held.is(ItemTags.LOGS)) {
            return tryAddFuel(furnace, player, held, FuelType.LOG);
        }

        // COAL
        if (held.is(Items.COAL)) {
            return tryAddFuel(furnace, player, held, FuelType.COAL);
        }

        // CHARCOAL
        if (held.is(Items.CHARCOAL)) {
            return tryAddFuel(furnace, player, held, FuelType.CHARCOAL);
        }

        // COAL BLOCK
        if (held.is(Items.COAL_BLOCK)) {
            return tryAddFuel(furnace, player, held, FuelType.COAL_BLOCK);
        }

        // KELP BLOCK
        if (held.is(Items.DRIED_KELP_BLOCK)) {
            return tryAddFuel(furnace, player, held, FuelType.KELP_BLOCK);
        }

        // LAVA BUCKET
        if (held.is(Items.LAVA_BUCKET)) {
            furnace.addSpecialFuel(FuelType.LAVA, 2000);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
                player.getInventory().add(new ItemStack(Items.BUCKET));
            }
            player.sendSystemMessage(Component.literal("§d+Lava Bucket §7(+100s)"));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.SUCCESS;
        }

        // BLAZE CAKE
        if (held.is(AllItems.BLAZE_CAKE.get())) {
            furnace.addSpecialFuel(FuelType.BLAZE_CAKE, 3000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§d+Blaze Cake §7(+150s)"));
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
            return ItemInteractionResult.SUCCESS;
        }

        // TNT (explosive fuel)
        if (held.is(Items.TNT)) {
            if (level.random.nextFloat() < 0.3f) {
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1.5f, Level.ExplosionInteraction.BLOCK);
                player.sendSystemMessage(Component.literal("§4§l✖ FURNACE EXPLODED!"));
                return ItemInteractionResult.SUCCESS;
            }
            return tryAddFuel(furnace, player, held, FuelType.TNT);
        }

        // NETHER STAR
        if (held.is(Items.NETHER_STAR)) {
            furnace.addUltimateFuel(6000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§5§l☆ NETHER STAR §7(+300s) §d§l[RADIANT!]"));
            level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.2f);
            return ItemInteractionResult.SUCCESS;
        }

        // DRAGON BREATH
        if (held.is(Items.DRAGON_BREATH)) {
            furnace.addUltimateFuel(4000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§5+Dragon Breath §7(+200s) §d[RADIANT!]"));
            level.playSound(null, pos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.BLOCKS, 0.5f, 1.5f);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult tryAddFuel(MoltenRotorBlockEntity furnace, Player player,
                                             ItemStack held, FuelType fuelType) {
        int used = Math.min(1, fuelType.maxStackSize);

        if (furnace.addFuel(fuelType, used)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(used);
            }

            String name = fuelType.name().toLowerCase().replace('_', ' ');
            float cps = fuelType.celsiusPerSecond;
            int maxTemp = (int) fuelType.maxTempReachable;
            float burnTimeSeconds = (fuelType.baseBurnTimeTicks * used) / 20f;

            player.sendSystemMessage(Component.literal(
                    String.format("§6+%s x%d §8(%.1f°C/s, max %d°C, %.1fs)",
                            capitalize(name), used, cps, maxTemp, burnTimeSeconds)
            ));

            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // ========== STATUS DISPLAY ==========

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
        if (furnace == null) return InteractionResult.FAIL;

        if (!furnace.shouldShowStatus()) {
            return InteractionResult.SUCCESS;
        }

        int temp = furnace.getDisplayTemperature();
        int fuel = furnace.getDisplayFuelTime();
        int retention = furnace.getDisplayRetentionTime();
        int cooldown = furnace.getDisplayCooldownTime();
        float rpm = furnace.getGeneratedSpeed();
        float stressUnits = furnace.getTotalStressOutput();
        String tierName = furnace.getHeatTierName();

        String color = switch (furnace.getCurrentHeatTier()) {
            case NONE -> "§7";
            case SMOULDERING, FADING -> "§e";
            case KINDLED -> "§6";
            case SEETHING -> "§c";
            case RADIANT -> "§5";
        };

        player.sendSystemMessage(Component.literal(color + tierName + " §7(" + temp + "°C)"));

        if (fuel > 0) {
            int fuelSeconds = fuel / 20;
            player.sendSystemMessage(Component.literal(
                    String.format("§7Fuel: %ds §8| §7RPM: %d §8| §7SU: %d",
                            fuelSeconds, (int) rpm, (int) stressUnits)
            ));
        } else if (retention > 0) {
            int retentionSeconds = retention / 20;
            player.sendSystemMessage(Component.literal(
                    String.format("§3Heat Retention: %ds §8| §7RPM: %d §8| §7SU: %d",
                            retentionSeconds, (int) rpm, (int) stressUnits)
            ));
        } else if (cooldown > 0) {
            int cooldownSeconds = cooldown / 20;
            player.sendSystemMessage(Component.literal(
                    String.format("§eCooling: %ds §8| §7RPM: %d §8| §7SU: %d",
                            cooldownSeconds, (int) rpm, (int) stressUnits)
            ));
        } else if (rpm > 0) {
            player.sendSystemMessage(Component.literal(
                    String.format("§7RPM: %d §8| §7SU: %d", (int) rpm, (int) stressUnits)
            ));
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    private MoltenRotorBlockEntity getBlockEntity(Level level, BlockPos pos) {
        var blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MoltenRotorBlockEntity moltenRotor ? moltenRotor : null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, direction)
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE);
    }

    // ========== KINETIC INITIALIZATION ==========

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide && !oldState.is(this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);

        if (!level.isClientSide) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MoltenRotorBlockEntity furnace) {
                furnace.initializeKinetics();
            }
        }
    }

    // ========== PARTICLES ==========

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);

        if (!heat.isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING)) {
            return;
        }

        if (random.nextInt(15) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5f + random.nextFloat() * 0.2f,
                    random.nextFloat() * 0.7f + 0.6f,
                    false
            );
        }

        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.05, 0
            );
        }

        if (heat.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED) && random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.5,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5,
                    0, 0, 0
            );
        }
    }
}