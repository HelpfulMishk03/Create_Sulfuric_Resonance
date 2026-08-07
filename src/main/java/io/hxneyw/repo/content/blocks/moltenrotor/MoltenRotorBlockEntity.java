package io.hxneyw.repo.content.blocks.moltenrotor;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.blocks.behaviour.CombustionHeatingBehaviour;
import io.hxneyw.repo.content.registry.AllBlockEntities;
import io.hxneyw.repo.content.registry.ModParticles;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class MoltenRotorBlockEntity extends GeneratingKineticBlockEntity implements IHaveGoggleInformation {
   private static final String FURNACE_IDENTITY_TAG = "FurnaceIdentity";

   private static Consumer<MoltenRotorBlockEntity> clientSoundTick =
           blockEntity -> {};

   private int clientUpdateCounter = 0;
   private int lastNotifiedFuelCount = 0;
   private RotorHeatLevel lastSentHeatTier =
           RotorHeatLevel.NONE;
   private boolean creativeMode = false;
   private boolean kineticsInitialized = false;
   private UUID furnaceIdentity = UUID.randomUUID();
   public int tntCooldown = 0;

   private final MoltenRotorFuelController fuelController =
           new MoltenRotorFuelController(this);
   private final MoltenRotorTemperatureController temperatureController =
           new MoltenRotorTemperatureController(this, this.fuelController);

   private final IItemHandler unsidedFuelHandler =
           new MoltenRotorFuelHandler(this, null);
   private final IItemHandler[] sidedFuelHandlers =
           new IItemHandler[Direction.values().length];

   public IItemHandler getAutomationFuelHandler(@Nullable Direction side) {
      if (side == null) {
         return this.unsidedFuelHandler;
      }

      int index = side.ordinal();
      IItemHandler handler = this.sidedFuelHandlers[index];
      if (handler == null) {
         handler = new MoltenRotorFuelHandler(this, side);
         this.sidedFuelHandlers[index] = handler;
      }

      return handler;
   }

   public boolean canAutomationInsertFrom(@Nullable Direction side) {
      if (side == null) {
         return true;
      }

      Direction facing =
              this.getBlockState().getValue(MoltenRotorBlock.FACING);

      return side != Direction.DOWN
              && side != facing.getOpposite();
   }


   public MoltenRotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public MoltenRotorBlockEntity(BlockPos pos, BlockState state) {
      this(AllBlockEntities.MOLTEN_ROTOR.get(), pos, state);
   }

   public static void setClientSoundTick(
           Consumer<MoltenRotorBlockEntity> soundTick
   ) {
      clientSoundTick = Objects.requireNonNull(soundTick);
   }

   public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
      super.addBehaviours(behaviours);
      behaviours.add(new CombustionHeatingBehaviour(this));
   }

   public float getImpellerRpm() {
      return this.temperatureController.getImpellerRpm();
   }

   public float getGeneratedSpeed() {
      float baseSpeed = this.getCurrentHeatTier().rpmCap;
      if (baseSpeed == 0.0F) {
         return 0.0F;
      } else {
         Direction facing =
                 this.getBlockState().getValue(MoltenRotorBlock.FACING);
         return facing != Direction.NORTH && facing != Direction.EAST ? baseSpeed : -baseSpeed;
      }
   }

   public float calculateAddedStressCapacity() {
      float speed = Math.abs(this.getGeneratedSpeed());
      return speed == 0.0F ? 0.0F : (this.lastCapacityProvided = this.getCurrentHeatTier().baseStressCapacity / speed);
   }

   public float calculateStressApplied() {
      return 0.0F;
   }

   public boolean isCreativeMode() {
      return this.creativeMode;
   }

   public @NotNull UUID getFurnaceIdentity() {
      return this.furnaceIdentity;
   }

   public float getTotalStressOutput() {
      return this.getCurrentHeatTier().baseStressCapacity;
   }

   public RotorHeatLevel getCurrentHeatTier() {
      return this.temperatureController.getHeatTier();
   }

   public int getDisplayTemperature() {
      return this.temperatureController.getDisplayTemperature();
   }

   public int getDisplayFuelTime() {
      return this.fuelController.getDisplayFuelTime();
   }

   public boolean isFuelQueueEmpty() {
      return this.fuelController.isFuelQueueEmpty();
   }

   public ItemStack getRenderedFuelStack() {
      return this.fuelController.getRenderedFuelStack();
   }

   public FuelType getRenderedFuelType() {
      return this.fuelController.getRenderedFuelType();
   }

   public List<ItemStack> getRenderedLogStickStacks() {
      return this.fuelController.getRenderedLogStickStacks();
   }

   public int getRenderedFuelUnitCount() {
      return this.fuelController.getRenderedFuelUnitCount();
   }

   public String getHeatTierName() {
      return this.temperatureController.getHeatTierName();
   }

   @SuppressWarnings("unused")
   public boolean isCombustionActive() {
      return this.temperatureController.isCombustionActive();
   }

   public boolean shouldShowStatus() {
      return this.fuelController.hasFuelRemaining()
              || this.temperatureController.isAboveAmbient();
   }

   public void setCreativeMode(boolean creative) {
      if (this.creativeMode == creative)
         return;

      this.creativeMode = creative;
      this.fuelController.clearAllFuel();

      this.temperatureController.setCreativeModeState(creative);

      this.updateBlockVisuals();
      this.notifyKineticNetworkOfChange();
      this.setChanged();
      this.sendData();
   }

   public void cycleCreativeTier() {
      if (!this.creativeMode) {
         return;
      }

      RotorHeatLevel newTier =
              this.applyNextCreativeTier();

      if (this.level instanceof ServerLevel serverLevel
              && newTier == RotorHeatLevel.RADIANT) {
         this.spawnRadiantParticles(serverLevel);
      }
   }

   private RotorHeatLevel applyNextCreativeTier() {
      RotorHeatLevel newTier = getNewTier();

      float newTemperature = switch (newTier) {
         case SMOULDERING -> 400.0F;
         case SEETHING -> 1000.0F;
         case RADIANT -> 1450.0F;
         default -> 20.0F;
      };

      this.temperatureController.setState(newTemperature, newTier);
      this.updateBlockVisuals();
      this.notifyKineticNetworkOfChange();
      this.setChanged();
      this.sendData();
      return newTier;
   }

   private @NotNull RotorHeatLevel getNewTier() {
      RotorHeatLevel previousTier =
              this.getCurrentHeatTier();

       return switch (previousTier) {
          case NONE, SMOULDERING, FADING ->
                  RotorHeatLevel.SEETHING;
          case KINDLED, SEETHING ->
                  RotorHeatLevel.RADIANT;
          case RADIANT ->
                  RotorHeatLevel.SMOULDERING;
       };
   }

   public void initializeKinetics() {
      if (!this.kineticsInitialized && this.level != null && !this.level.isClientSide) {
         this.kineticsInitialized = true;
         this.attachKinetics();
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.setChanged();
      }
   }

   public void tick() {
      super.tick();

      if (this.level != null && this.level.isClientSide) {
         clientSoundTick.accept(this);
         MoltenRotorParticles.tickClient(this);
         return;
      }

      if (this.level != null) {
         if (this.tntCooldown > 0) {
            this.tntCooldown--;
         }

         if (this.creativeMode) {
            this.updateBlockVisuals();
            this.setChanged();

            if (++this.clientUpdateCounter >= 1) {
               this.clientUpdateCounter = 0;
               this.sendData();
            }
         } else {
            this.tickTemperatureSystem();
         }
      }
   }

   private void tickTemperatureSystem() {
      MoltenRotorTemperatureController.TickResult temperatureTick =
              this.temperatureController.tick();
      RotorHeatLevel previousTier =
              temperatureTick.previousTier();
      RotorHeatLevel newTier =
              this.applyTemperatureTick(temperatureTick);

      if (newTier != this.lastSentHeatTier) {
         this.lastSentHeatTier = newTier;
         this.clientUpdateCounter = 0;
         this.sendData();

         if (newTier == RotorHeatLevel.RADIANT
                 && previousTier
                 != RotorHeatLevel.RADIANT
                 && this.level instanceof ServerLevel serverLevel) {
            this.spawnRadiantParticles(serverLevel);
         }
      } else if (++this.clientUpdateCounter >= 1) {
         this.clientUpdateCounter = 0;
         this.sendData();
      }
   }

   private RotorHeatLevel applyTemperatureTick(
           MoltenRotorTemperatureController.TickResult temperatureTick
   ) {
      RotorHeatLevel previousTier =
              temperatureTick.previousTier();
      RotorHeatLevel newTier =
              temperatureTick.currentTier();

      if (temperatureTick.tierChanged()) {
         this.updateBlockVisuals();

         if (this.tierAffectsRotation(previousTier, newTier)) {
            this.notifyKineticNetworkOfChange();
         }
      }

      if (temperatureTick.changed()) {
         this.setChanged();
      }

      return newTier;
   }

   public int getDisplayCooldownTime() {
      return this.temperatureController.getDisplayCooldownTime(
              this.creativeMode
      );
   }

   private boolean tierAffectsRotation(RotorHeatLevel from, RotorHeatLevel to) {
      return from == RotorHeatLevel.NONE != (to == RotorHeatLevel.NONE) || from.rpmCap != to.rpmCap;
   }

   private void notifyKineticNetworkOfChange() {
      if (this.level != null && !this.level.isClientSide && this.kineticsInitialized) {
         super.updateGeneratedRotation();
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         this.setChanged();
      }
   }

   private void updateBlockVisuals() {
      if (this.level != null) {
         HeatLevel visualHeat = switch (this.getCurrentHeatTier()) {
            case NONE -> HeatLevel.NONE;
            case SMOULDERING, FADING, KINDLED -> HeatLevel.KINDLED;
            case SEETHING, RADIANT -> HeatLevel.SEETHING;
         };
         BlockState state = this.getBlockState();
         if (state.getValue(MoltenRotorBlock.HEAT_LEVEL) != visualHeat) {
            this.level.setBlock(
                    this.worldPosition,
                    state.setValue(MoltenRotorBlock.HEAT_LEVEL, visualHeat),
                    3
            );
            this.level.updateNeighborsAt(this.worldPosition.above(), this.getBlockState().getBlock());
         }
      }
   }

   public boolean insertFuel(ItemStack stack, boolean simulate) {
      return this.fuelController.insertFuel(stack, simulate);
   }

   public List<ItemStack> drainPendingFuelForDrop() {
      return this.fuelController.drainPendingFuelForDrop();
   }

   public void addUltimateFuel(int ticks) {
      if (this.level != null) {
         this.fuelController.addBurnTime(ticks);
         this.temperatureController.raiseToAtLeast(1300.0F);
         this.setChanged();
         this.sendData();
      }
   }


   void syncFuelState() {
      this.setChanged();
      this.sendData();
   }

   void markFuelStateChanged() {
      this.setChanged();
   }

   private void spawnRadiantParticles(ServerLevel sl) {
      


      double x = this.worldPosition.getX() + 0.5;
      double y = this.worldPosition.getY() + 0.28;
      double z = this.worldPosition.getZ() + 0.5;

      sl.sendParticles(
              ModParticles.COMBUSTION_PURPLE_FLAME.get(),
              x,
              y,
              z,
              4,
              0.12,
              0.05,
              0.12,
              0.002
      );

      sl.sendParticles(
              ParticleTypes.SOUL_FIRE_FLAME,
              x,
              y,
              z,
              2,
              0.10,
              0.04,
              0.10,
              0.001
      );

      sl.sendParticles(
              ParticleTypes.END_ROD,
              x,
              y,
              z,
              1,
              0.08,
              0.04,
              0.08,
              0.001
      );
   }

   @Override
   public boolean addToGoggleTooltip(
           List<Component> tooltip,
           boolean isPlayerSneaking
   ) {
      tooltip.add(Component.literal(""));

      if (this.creativeMode) {
         tooltip.add(
                 Component.literal("★ CREATIVE MODE")
                         .withStyle(
                                 ChatFormatting.LIGHT_PURPLE,
                                 ChatFormatting.BOLD
                         )
         );

         tooltip.add(
                 Component.literal(
                                 "  (Infinite heat - right-click to cycle)"
                         )
                         .withStyle(ChatFormatting.DARK_GRAY)
         );
      }

      ChatFormatting heatColor =
              switch (this.getCurrentHeatTier()) {
                 case NONE -> ChatFormatting.GRAY;
                 case SMOULDERING, FADING ->
                         ChatFormatting.YELLOW;
                 case KINDLED -> ChatFormatting.RED;
                 case SEETHING -> ChatFormatting.DARK_RED;
                 case RADIANT -> ChatFormatting.DARK_PURPLE;
              };

      tooltip.add(
              Component.literal(
                              "Heat: "
                                      + this.getHeatTierName()
                                      + " ("
                                      + this.getDisplayTemperature()
                                      + "°C)"
                      )
                      .withStyle(heatColor)
      );

      int displayedStress =
              this.getCurrentHeatTier()
                      != RotorHeatLevel.NONE
                      ? (int)this.getTotalStressOutput()
                      : 0;

      tooltip.add(
              Component.literal(
                              "Stress Capacity: "
                                      + displayedStress
                                      + " su"
                      )
                      .withStyle(
                              displayedStress > 0
                                      ? ChatFormatting.GOLD
                                      : ChatFormatting.GRAY
                      )
      );

      int generatedRpm =
              Math.round(
                      Math.abs(this.getGeneratedSpeed())
              );

      tooltip.add(
              Component.literal(
                              "Generated Speed: "
                                      + generatedRpm
                                      + " RPM"
                      )
                      .withStyle(
                              generatedRpm > 0
                                      ? ChatFormatting.AQUA
                                      : ChatFormatting.GRAY
                      )
      );

      if (this.fuelController.hasFuelRemaining()
              && this.fuelController.getRenderedFuelType() != FuelType.NONE) {

         tooltip.add(
                 Component.literal("Burning: ")
                         .withStyle(ChatFormatting.GRAY)
                         .append(
                                 this.fuelController.getActiveFuelDisplayName()
                                         .copy()
                                         .withStyle(
                                                 ChatFormatting.WHITE
                                         )
                         )
         );
      }

      this.fuelController.addQueuedFuelTooltip(tooltip);

      if (this.fuelController.hasFuelRemaining()) {
         int totalSeconds =
                 this.fuelController.getRemainingBurnTime() / 20;

         int minutes =
                 totalSeconds / 60;

         int seconds =
                 totalSeconds % 60;

         String displayedTime =
                 minutes > 0
                         ? minutes + "m " + seconds + "s"
                         : seconds + "s";

         tooltip.add(
                 Component.literal(
                                 "Fuel Remaining: "
                                         + displayedTime
                         )
                         .withStyle(ChatFormatting.GREEN)
         );
      } else if (this.getDisplayCooldownTime() > 0) {
         int totalSeconds =
                 this.getDisplayCooldownTime() / 20;

         int minutes =
                 totalSeconds / 60;

         int seconds =
                 totalSeconds % 60;

         String displayedTime =
                 minutes > 0
                         ? minutes + "m " + seconds + "s"
                         : seconds + "s";

         tooltip.add(
                 Component.literal(
                                 "Cooling Down: "
                                         + displayedTime
                         )
                         .withStyle(ChatFormatting.YELLOW)
         );
      }

      if (this.getCurrentHeatTier()
              == RotorHeatLevel.RADIANT) {

         tooltip.add(
                 Component.literal("✦ RADIANT TIER ACTIVE")
                         .withStyle(ChatFormatting.DARK_PURPLE)
         );

         tooltip.add(
                 Component.literal(
                                 "  (Combustion recipes enabled)"
                         )
                         .withStyle(ChatFormatting.DARK_GRAY)
         );
      }

      return true;
   }

   protected void write(@NotNull CompoundTag tag, @NotNull Provider provider, boolean clientPacket) {
      super.write(tag, provider, clientPacket);
      this.temperatureController.write(tag);
      this.fuelController.write(tag, provider);
      tag.putUUID(FURNACE_IDENTITY_TAG, this.furnaceIdentity);
      tag.putBoolean("KineticsInit", this.kineticsInitialized);
      tag.putBoolean("CreativeMode", this.creativeMode);
      tag.putInt("LastNotifiedFuel", this.lastNotifiedFuelCount);
   }

   protected void read(@NotNull CompoundTag tag, @NotNull Provider provider, boolean clientPacket) {
      super.read(tag, provider, clientPacket);
      this.temperatureController.read(tag);
      this.fuelController.read(tag, provider);
      if (tag.hasUUID(FURNACE_IDENTITY_TAG)) {
         this.furnaceIdentity = tag.getUUID(FURNACE_IDENTITY_TAG);
      } else {
         this.furnaceIdentity = UUID.randomUUID();
      }
      this.kineticsInitialized = tag.getBoolean("KineticsInit");
      this.creativeMode = tag.getBoolean("CreativeMode");
      this.lastNotifiedFuelCount = tag.getInt("LastNotifiedFuel");

      if (clientPacket) {
         this.lastSentHeatTier = this.getCurrentHeatTier();
         if (this.level != null && this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   public enum FuelType {
      NONE("none", 0.0F, 0.0F, 0, 0.0F, 1.0F),
      STICK("stick", 2.5F, 150.0F, 32, 100.0F, 1.0F),
      LOG("log", 8.5F, 475.0F, 8, 700.0F, 1.0F),
      COAL("coal", 13.0F, 550.0F, 32, 350.0F, 1.0F),
      CHARCOAL("charcoal", 14.0F, 650.0F, 32, 350.0F, 0.8F),
      COAL_BLOCK("coal_block", 25.0F, 900.0F, 1, 2400.0F, 1.0F),
      KELP_BLOCK("kelp_block", 15.5F, 750.0F, 3, 400.0F, 1.0F),
      GENERIC_LOW(
              "generic_low",
              6.0F,
              400.0F,
              16,
              300.0F,
              1.0F
      ),
      GENERIC_MEDIUM(
              "generic_medium",
              12.0F,
              600.0F,
              16,
              600.0F,
              1.0F
      ),
      GENERIC_HIGH(
              "generic_high",
              20.0F,
              850.0F,
              8,
              1200.0F,
              1.0F
      ),
      TNT("tnt", 25.0F, 600.0F, 1, 750.0F, 1.2F),
      BLAZE_CAKE("blaze_cake", 40.0F, 1200.0F, 1, 3000.0F, 1.0F),
      SOUL_FIRED_BLAZE_CAKE(
              "soul_fired_blaze_cake",
              45.5F,
              1599.0F,
              1,
              3500.0F,
              1.0F
      ),

      MOLTEN_EMBER_PELLET(
              "molten_ember_pellet",
              25.0F,
              950.0F,
              32,
              1000.0F,
              1.0F
      ),

      COKE(
        "coke",
                13.0F,
                850.0F,
                32,
                613.0F,
                1.0F
      ),

      INFERNAL_COKE(
        "infernal_coke",
                26.0F,
                1400.0F,
                8,
                1600.0F,
                1.0F
      ),

      CARBON_DEPOSIT_BLOCK(
        "carbon_deposit_block",
                13.0F,
                1100.0F,
                4,
                5517.0F,
                1.0F
      ),

      INFERNAL_CARBON_DEPOSIT_BLOCK(
        "infernal_carbon_deposit_block",
                26.0F,
                1400.0F,
                4,
                14400.0F,
                1.0F
      );

      public final String serializedId;
      public final float celsiusPerSecond;
      public final float maxTempReachable;
      public final int maxStackSize;
      public final float baseBurnTimeTicks;
      public final float burnRateMultiplier;

      FuelType(
              String serializedId,
              float cps,
              float maxTemp,
              int maxStack,
              float ticks,
              float burnMult
      ) {
         this.serializedId = serializedId;
         this.celsiusPerSecond = cps;
         this.maxTempReachable = maxTemp;
         this.maxStackSize = maxStack;
         this.baseBurnTimeTicks = ticks;
         this.burnRateMultiplier = burnMult;
      }

      public String getDisplayName() {
         String[] words = this.serializedId.split("_");
         StringBuilder result = new StringBuilder();

         for (String word : words) {
            if (!result.isEmpty()) {
               result.append(' ');
            }

            if (!word.isEmpty()) {
               result.append(Character.toUpperCase(word.charAt(0)));

               if (word.length() > 1) {
                  result.append(word.substring(1));
               }
            }
         }

         return result.toString();
      }

      public static FuelType fromSerializedId(String serializedId) {
         for (FuelType fuelType : values()) {
            if (fuelType.serializedId.equals(serializedId)) {
               return fuelType;
            }
         }

         return NONE;
      }
   }

   public enum RotorHeatLevel {
      NONE("none", 0, 0, 0.0F, 0.0F, "Unheated"),
      SMOULDERING("smouldering", 1, 300, 32.0F, 2048.0F, "Heated"),
      FADING("fading", 1, 325, 24.0F, 2048.0F, "Heated"),
      KINDLED("kindled", 2, 650, 64.0F, 2048.0F, "Heated"),
      SEETHING("seething", 3, 950, 128.0F, 8196.0F, "Superheated"),
      RADIANT("radiant", 4, 1400, 256.0F, 12294.0F, "Combustion");

      public final String serializedId;
      public final int rank;
      public final int displayTemp;
      public final float rpmCap;
      public final float baseStressCapacity;
      public final String displayName;

      RotorHeatLevel(
              String serializedId,
              int rank,
              int displayTemp,
              float rpmCap,
              float baseStressCapacity,
              String displayName
      ) {
         this.serializedId = serializedId;
         this.rank = rank;
         this.displayTemp = displayTemp;
         this.rpmCap = rpmCap;
         this.baseStressCapacity = baseStressCapacity;
         this.displayName = displayName;
      }

      public boolean isAtLeast(RotorHeatLevel other) {
         return this.rank >= other.rank;
      }

      public static RotorHeatLevel fromSerializedId(String serializedId) {
         for (RotorHeatLevel heatLevel : values()) {
            if (heatLevel.serializedId.equals(serializedId)) {
               return heatLevel;
            }
         }

         return NONE;
      }
   }
}
