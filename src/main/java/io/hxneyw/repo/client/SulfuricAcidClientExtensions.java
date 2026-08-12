package io.hxneyw.repo.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.registry.AllModFluids;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer.FogMode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@EventBusSubscriber(
        modid = CreateSulfuricResonance.MODID,
        value = Dist.CLIENT
)
public final class SulfuricAcidClientExtensions {
    private static final ResourceLocation STILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "block/sulfuric_acid_still"
            );

    private static final ResourceLocation FLOWING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "block/sulfuric_acid_flow"
            );

    private static final int TINT_COLOR = 0xFFDEC83A;

    private SulfuricAcidClientExtensions() {
    }

    @SubscribeEvent
    public static void registerFluidExtensions(
            RegisterClientExtensionsEvent event
    ) {
        event.registerFluidType(
                new IClientFluidTypeExtensions() {
                    @Override
                    public @NotNull ResourceLocation getStillTexture() {
                        return STILL_TEXTURE;
                    }

                    @Override
                    public @NotNull ResourceLocation getFlowingTexture() {
                        return FLOWING_TEXTURE;
                    }

                    @Override
                    public int getTintColor() {
                        return TINT_COLOR;
                    }

                    @Override
                    public int getTintColor(@NotNull FluidStack stack) {
                        return TINT_COLOR;
                    }

                    @Override
                    public int getTintColor(
                            @NotNull FluidState state,
                            @NotNull BlockAndTintGetter getter,
                            @NotNull BlockPos pos
                    ) {
                        return TINT_COLOR;
                    }

                    @Override
                    public @NotNull Vector3f modifyFogColor(
                            @NotNull Camera camera,
                            float partialTick,
                            @NotNull ClientLevel level,
                            int renderDistance,
                            float darkenWorldAmount,
                            @NotNull Vector3f fluidFogColor
                    ) {
                        return new Vector3f(0.784F, 0.765F, 0.059F);
                    }

                    @Override
                    public void modifyFogRender(
                            @NotNull Camera camera,
                            @NotNull FogMode mode,
                            float renderDistance,
                            float partialTick,
                            float nearDistance,
                            float farDistance,
                            @NotNull FogShape shape
                    ) {
                        if (mode != FogMode.FOG_TERRAIN) {
                            return;
                        }

                        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
                        RenderSystem.setShaderFogStart(-8.0F);
                        RenderSystem.setShaderFogEnd(24.0F);
                    }
                },
                AllModFluids.SULFURIC_ACID_TYPE.get()
        );
    }
}
