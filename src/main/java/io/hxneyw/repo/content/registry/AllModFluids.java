package io.hxneyw.repo.content.registry;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hxneyw.repo.content.Items;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Source;
import net.neoforged.neoforge.fluids.FluidType.Properties;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class AllModFluids {
   public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, "sulfuricresonance");
   public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, "sulfuricresonance");
   public static final DeferredHolder<FluidType, FluidType> SULFURIC_ACID_TYPE = FLUID_TYPES.register(
      "sulfuric_acid",
      () -> new FluidType(Properties.create().density(1500).viscosity(4000).temperature(300).canConvertToSource(false).canDrown(true).canExtinguish(false))
   );
   public static final DeferredHolder<Fluid, Source> SULFURIC_ACID = FLUIDS.register("sulfuric_acid", () -> new Source(makeProperties()));
   public static final DeferredHolder<Fluid, Flowing> SULFURIC_ACID_FLOWING = FLUIDS.register("sulfuric_acid_flowing", () -> new Flowing(makeProperties()));

   private static net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties makeProperties() {
      return new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(SULFURIC_ACID_TYPE, SULFURIC_ACID, SULFURIC_ACID_FLOWING)
         .bucket(Items.SULFURIC_ACID_BUCKET)
         .block(AllModBlocks.SULFURIC_ACID_BLOCK)
         .levelDecreasePerBlock(2)
         .tickRate(20)
         .slopeFindDistance(3)
         .explosionResistance(100.0F);
   }

   public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
      event.registerFluidType(
         new IClientFluidTypeExtensions() {
            @NotNull
            public ResourceLocation getStillTexture() {
               return ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/sulfuric_acid_still");
            }

            @NotNull
            public ResourceLocation getFlowingTexture() {
               return ResourceLocation.fromNamespaceAndPath("sulfuricresonance", "block/sulfuric_acid_flow");
            }

            public int getTintColor() {
               return -2176966;
            }

            public int getTintColor(@NotNull FluidStack stack) {
               return -2176966;
            }

            public int getTintColor(@NotNull FluidState state, @NotNull BlockAndTintGetter getter, @NotNull BlockPos pos) {
               return -2176966;
            }

            @NotNull
            public Vector3f modifyFogColor(
               @NotNull Camera camera,
               float partialTick,
               @NotNull ClientLevel level,
               int renderDistance,
               float darkenWorldAmount,
               @NotNull Vector3f fluidFogColor
            ) {
               return new Vector3f(0.784F, 0.765F, 0.059F);
            }

            public void modifyFogRender(
               @NotNull Camera camera,
               @NotNull FogMode mode,
               float renderDistance,
               float partialTick,
               float nearDistance,
               float farDistance,
               @NotNull FogShape shape
            ) {
               if (mode == FogMode.FOG_TERRAIN) {
                  float fogStart = -8.0F;
                  float fogEnd = 24.0F;
                  RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                  RenderSystem.setShaderFogStart(fogStart);
                  RenderSystem.setShaderFogEnd(fogEnd);
               }
            }
         },
              SULFURIC_ACID_TYPE.get()
      );
   }

   public static void register(IEventBus eventBus) {
      FLUID_TYPES.register(eventBus);
      FLUIDS.register(eventBus);
   }
}
