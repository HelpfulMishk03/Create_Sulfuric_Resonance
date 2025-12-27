package io.hxneyw.repo.content.blocks;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.hxneyw.repo.content.blocks.behaviour.MoltenRotorShapes;
import io.hxneyw.repo.content.registry.ModSounds;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import io.hxneyw.repo.content.registry.ModBlockEntities;
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
 * A kinetic generator powered by combustion_mixing that can heat adjacent machines
 */
public class MoltenRotorBlock extends DirectionalKineticBlock implements IBE<MoltenRotorBlockEntity>, IWrenchable {

    public static final EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;
    private BlockPlaceContext ctx;

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
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        // Only rotate horizontally, ignore which face was clicked
        Direction currentFacing = originalState.getValue(FACING);
        Direction newFacing = currentFacing.getClockWise();

        // Keep rotating until we get a horizontal direction
        while (newFacing.getAxis().isVertical()) {
            newFacing = newFacing.getClockWise();
        }

        return originalState.setValue(FACING, newFacing);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, direction)
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE);
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
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                    @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.box(0.0625, 0, 0.0625, 0.9375, 1, 0.9375);
    }

    // 2. Keep detailed visual shape
    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return MoltenRotorShapes.getShape(facing);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addLandingEffects(@NotNull BlockState state1, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state2,
                                     @NotNull LivingEntity entity, int numberOfParticles) {
        return true; // Prevent landing particles
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addRunningEffects(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        return true; // Prevent running particles
    }

    // For break particles - override at WorldRenderer level
    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos,
                              @NotNull BlockState state, @javax.annotation.Nullable BlockEntity blockEntity,
                              @NotNull ItemStack tool) {
        VoxelShape shape = getCollisionShape(state, level, pos, CollisionContext.empty());
        System.out.println("Collision shape boxes: " + shape.toAabbs().size());
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        // Default particles now use simplified collision shape = ~8 particles
    }

    public static BlazeBurnerBlock.HeatLevel getHeatLevelOf(BlockState state) {
        if (state.hasProperty(HEAT_LEVEL)) {
            return state.getValue(HEAT_LEVEL);
        }
        return BlazeBurnerBlock.HeatLevel.NONE;
    }



    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MoltenRotorBlockEntity furnace && !level.isClientSide) {
                // Drop fuel items based on what's burning
                FuelType fuelType = furnace.getActiveFuelType();
                int fuelCount = furnace.getActiveFuelCount();

                if (fuelType != FuelType.NONE && fuelCount > 0) {
                    ItemStack fuelStack = getFuelItemStack(fuelType, fuelCount);
                    if (!fuelStack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), fuelStack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private ItemStack getFuelItemStack(FuelType fuelType, int count) {
        return switch (fuelType) {
            case STICK -> new ItemStack(Items.STICK, count);
            case LOG -> new ItemStack(Items.OAK_LOG, count); // Default to oak
            case COAL -> new ItemStack(Items.COAL, count);
            case CHARCOAL -> new ItemStack(Items.CHARCOAL, count);
            case COAL_BLOCK -> new ItemStack(Items.COAL_BLOCK, count);
            case KELP_BLOCK -> new ItemStack(Items.DRIED_KELP_BLOCK, count);
            case TNT -> new ItemStack(Items.TNT, count);
            default -> ItemStack.EMPTY;
        };
    }

    // ========== FUEL SYSTEM ==========

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        // Handle wrench interactions
        if (stack.getItem() instanceof WrenchItem) {
            UseOnContext context = new UseOnContext(player, hand, hit);

            if (player.isShiftKeyDown()) {
                // Shift + wrench = remove block
                InteractionResult result = onSneakWrenched(state, context);
                return result == InteractionResult.SUCCESS ?
                        ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            } else {
                // Regular wrench = rotate block
                InteractionResult result = onWrenched(state, context);
                return result == InteractionResult.SUCCESS ?
                        ItemInteractionResult.SUCCESS : ItemInteractionResult.FAIL;
            }
        }

        // CRITICAL: Allow block placement on top BEFORE any other checks
        if (hit.getDirection() == Direction.UP) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }



        MoltenRotorBlockEntity furnace = getBlockEntity(level, pos);
        if (furnace == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        ItemStack held = player.getItemInHand(hand);

        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;
        // CREATIVE BLAZE CAKE - Enable infinite mode OR cycle tier
        if (held.is(AllItems.CREATIVE_BLAZE_CAKE.get())) {
            if (!furnace.isCreativeMode()) {
                // First use: Enable creative mode at SMOULDERING
                furnace.setCreativeMode(true);
            } else {
                // Already in creative mode: Cycle to next tier
                furnace.cycleCreativeTier();
            }
            return ItemInteractionResult.SUCCESS;
        }

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
            ItemInteractionResult result = tryAddFuel(furnace, player, held, FuelType.LOG);
            if (result == ItemInteractionResult.SUCCESS) {
                level.playSound(null, pos, ModSounds.LOG_INSERT.get(),
                        SoundSource.BLOCKS, 1.0f, level.random.nextFloat() * 0.5f + 0.7f);
            }
            return result;
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


        // BLAZE CAKE
        if (held.is(AllItems.BLAZE_CAKE.get())) {
            furnace.addSpecialFuel(FuelType.BLAZE_CAKE, 3000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§d+Blaze Cake §7(+150s)"));
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
            return ItemInteractionResult.SUCCESS;
        }

        // SOUL FIRED BLAZE CAKE (NEW - ADD THIS ENTIRE BLOCK)
        if (held.is(io.hxneyw.repo.content.Items.SOUL_FIRED_BLAZE_CAKE.get())) {
            // Check if furnace is at 85% of SEETHING tier minimum (800°C)
            float seethingMin = 1200f;
            float requiredTemp = seethingMin * 0.85f; // 1020°C

            if (furnace.getDisplayTemperature() < requiredTemp) {
                player.sendSystemMessage(Component.literal("§c✖ Requires §6SEETHING §ctier heat! §7(1015°C+)"));
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 0.5f);
                return ItemInteractionResult.FAIL;
            }

            furnace.addSpecialFuel(FuelType.SOUL_FIRED_BLAZE_CAKE, 3500);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§5§l★ SOUL FIRED BLAZE CAKE §7(+175s) §d§l[MAX HEAT!]"));
            return ItemInteractionResult.SUCCESS;
        }

        if (held.is(Items.TNT)) {
            if (furnace.tntCooldown > 0) {
                return ItemInteractionResult.FAIL; // Still on cooldown
            }

            if (level.random.nextFloat() < 0.10f) {
                // Set cooldown to prevent spam
                furnace.tntCooldown = 20; // 1 second cooldown

                // Destroy the furnace block WITHOUT dropping it
                level.removeBlock(pos, false);

                // THEN create explosion
                level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1.5f, Level.ExplosionInteraction.BLOCK);

                player.sendSystemMessage(Component.literal("§4§l✖ FURNACE EXPLODED!"));
                return ItemInteractionResult.SUCCESS;
            }

            ItemInteractionResult result = tryAddFuel(furnace, player, held, FuelType.TNT);
            if (result == ItemInteractionResult.SUCCESS) {
                furnace.tntCooldown = 5; // 0.25s cooldown on successful add
            }
            return result;
        }


        // NETHER STAR
        if (held.is(Items.NETHER_STAR)) {
            furnace.addUltimateFuel(6000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§5§l☆ NETHER STAR §7(+300s) §d§l[RADIANT!]"));
            return ItemInteractionResult.SUCCESS;
        }

        // DRAGON BREATH
        if (held.is(Items.DRAGON_BREATH)) {
            furnace.addUltimateFuel(4000);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.sendSystemMessage(Component.literal("§5+Dragon Breath §7(+200s) §d[RADIANT!]"));
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

        Direction facing = state.getValue(FACING);

        // Sound effects
        if (random.nextInt(15) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    0.5f + random.nextFloat() * 0.2f,
                    random.nextFloat() * 0.7f + 0.6f,
                    false
            );
        }

        // Large smoke from top
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.05, 0
            );
        }

        // Flame particles from top
        if (heat.isAtLeast(BlazeBurnerBlock.HeatLevel.KINDLED) && random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.5,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5,
                    0, 0, 0
            );
        }

        // NEW: Small smoke from the three back-top slits
        if (random.nextInt(2) == 0) { // Spawn frequently
            // Calculate slit positions based on facing direction
            // The slits are on the BACK TOP of the furnace

            // Define the three slit positions (relative to back face, top area)
            // Left slit, middle slit, right slit
            double[][] slitOffsets = {
                    {-0.25, 0.85},  // Left slit (x offset, y height near top)
                    {0.0, 0.85},    // Middle slit
                    {0.25, 0.85}    // Right slit
            };

            // Pick a random slit
            double[] slit = slitOffsets[random.nextInt(3)];

            // Calculate world position based on facing direction
            double x, y, z;
            double velX, velY, velZ;

            y = pos.getY() + slit[1]; // Height near top

            // Adjust position and velocity based on facing direction
            // Back is OPPOSITE of facing direction
            switch (facing) {
                case NORTH: // Front faces north, back is SOUTH
                    x = pos.getX() + 0.5 + slit[0];
                    z = pos.getZ() + 0.8; // Back edge (south side)
                    velX = (random.nextDouble() - 0.5) * 0.02;
                    velY = 0.03;
                    velZ = 0.02; // Smoke drifts south (away from back)
                    break;
                case SOUTH: // Front faces south, back is NORTH
                    x = pos.getX() + 0.5 - slit[0]; // Flip horizontal offset
                    z = pos.getZ() + 0.2; // Back edge (north side)
                    velX = (random.nextDouble() - 0.5) * 0.02;
                    velY = 0.03;
                    velZ = -0.02; // Smoke drifts north (away from back)
                    break;
                case EAST: // Front faces east, back is WEST
                    x = pos.getX() + 0.2; // Back edge (west side)
                    z = pos.getZ() + 0.5 + slit[0];
                    velX = -0.02; // Smoke drifts west (away from back)
                    velY = 0.03;
                    velZ = (random.nextDouble() - 0.5) * 0.02;
                    break;
                case WEST: // Front faces west, back is EAST
                    x = pos.getX() + 0.8; // Back edge (east side)
                    z = pos.getZ() + 0.5 - slit[0]; // Flip horizontal offset
                    velX = 0.02; // Smoke drifts east (away from back)
                    velY = 0.03;
                    velZ = (random.nextDouble() - 0.5) * 0.02;
                    break;
                default:
                    return; // Skip if facing up/down
            }

            // Spawn small smoke particle
            level.addParticle(ParticleTypes.SMOKE,
                    x, y, z,
                    velX, velY, velZ
            );
        }
    }
}