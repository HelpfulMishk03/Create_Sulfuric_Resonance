package io.hxneyw.repo.ponder;

import io.hxneyw.repo.CreateSulfuricResonance;
import io.hxneyw.repo.content.registry.AllModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SulfuricResonancePonderPlugin implements PonderPlugin {

    @NotNull
    @Override
    public String getModId() {
        CreateSulfuricResonance.LOGGER.info(
                "Ponder Plugin loading for: {}",
                "sulfuricresonance"
        );
        return "sulfuricresonance";
    }

    @Override
    public void registerScenes(
            @NotNull PonderSceneRegistrationHelper<ResourceLocation> helper
    ) {
        CreateSulfuricResonance.LOGGER.info(
                "Registering Ponder scenes..."
        );

        try {
            ResourceLocation spritzerId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.PERFORATED_SPRITZER.get()
                    );

            helper.addStoryBoard(
                    spritzerId,
                    "perforated_spritzer/intro",
                    PerforatedSpritzerScenes::intro,
                    AllPonderTags.FLUIDS);

            helper.addStoryBoard(
                    spritzerId,
                    "perforated_spritzer/mob_automation",
                    PerforatedSpritzerScenes::mobAutomation,
                    AllPonderTags.FLUIDS);

            ResourceLocation moltenRotorId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.MOLTEN_ROTOR_FURNACE.get()
                    );

            helper.addStoryBoard(
                    moltenRotorId,
                    "molten_rotor/operation",
                    MoltenRotorScenes::operation
            );

            ResourceLocation sulfurId = ResourceLocation.fromNamespaceAndPath(
                    CreateSulfuricResonance.MODID,
                    "sulfur"
            );

            ResourceLocation thermalRelayId =
                    ResourceLocation.fromNamespaceAndPath(
                            CreateSulfuricResonance.MODID,
                            "thermal_relay_switch"
                    );

            helper.forComponents(thermalRelayId)
                    .addStoryBoard(
                            "relayswitch/thermal_relay_switch",
                            ThermalRelayScenes::thermalRelaySwitch
                    );



            helper.addStoryBoard(
                    sulfurId,
                    "sulfur/compatibility",
                    SulfurScenes::compatibility
            );

            ResourceLocation livingEmberLampId =
                    BuiltInRegistries.BLOCK.getKey(
                            AllModBlocks.LIVING_EMBER_LAMP.get()
                    );

            helper.addStoryBoard(
                    livingEmberLampId,
                    "living_ember_lamp/operation",
                    LivingEmberLampScenes::operation
            );

            CreateSulfuricResonance.LOGGER.info(
                    "Successfully registered 5 Ponder scenes"
            );



        } catch (Exception exception) {
            CreateSulfuricResonance.LOGGER.error(
                    "Failed to register Ponder scenes!",
                    exception
            );

        }
    }



    @Override
    public void registerTags(
            @NotNull PonderTagRegistrationHelper<ResourceLocation> helper
    ) {
        CreateSulfuricResonance.LOGGER.info(
                "Registering Ponder tags..."
        );

        try {
            AllPonderTags.register(helper);
            CreateSulfuricResonance.LOGGER.info(
                    "Successfully registered Ponder tags"
            );
        } catch (Exception exception) {
            CreateSulfuricResonance.LOGGER.error(
                    "Failed to register Ponder tags!",
                    exception
            );
        }
    }
}
