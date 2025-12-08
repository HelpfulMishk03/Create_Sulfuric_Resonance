package io.hxneyw.repo.content.entities;

import io.hxneyw.repo.CreateSulfuricResonance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// ModEntities - Registers all custom entities for your mod
// In Minecraft modding, EVERYTHING must be registered with the game before it can be used
// This class handles registering entity types (like our throwable powder)
public class ModEntities {

    // WHAT IS A DEFERRED REGISTER?
    // Think of it like a "to-do list" for Minecraft. Instead of registering things immediately,
    // we add them to this list, and NeoForge registers them at the right time during game startup.
    // This prevents timing issues and crashes.

    // Create a DeferredRegister for Enti ty Type
    // Enti ty Type<?> means "any type of entity" (the ? is a wildcard)
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(
                    Registries.ENTITY_TYPE,           // What we're registering (entity types)
                    CreateSulfuricResonance.MODID     // Your mod ID ("sulfuricresonance")
            );
    // This tells Minecraft: "Hey, this mod wants to register some entities!"

    // WHAT IS A DEFERRED HOLDER?
    // A DeferredHolder is like a "promise" - it says "this will contain an EntityType eventually"
    // You can't use the entity immediately, but once registration happens, this will hold your entity

    // Register the Pyroclastic Powder entity
    // This creates the EntityType and adds it to Minecraft's registry
    public static final DeferredHolder<EntityType<?>, EntityType<PyroclastBombEntity>> PYROCLAST_BOMB =
            ENTITY_TYPES.register(
                    "pyroclastic_powder",              // Registry name (must be lowercase, no spaces)
                    // Full ID will be "sulfuricresonance:pyroclastic_powder"

                    // WHAT IS A LAMBDA (->)?
                    // The () -> part is called a lambda - it's a function that runs later
                    // We don't create the EntityType NOW, we give Minecraft instructions on HOW to create it
                    () -> EntityType.Builder.<PyroclastBombEntity>of(

                                    // WHAT IS A METHOD REFERENCE ::)?
                                    // PyroclastBombEntity::new is shorthand for "use the constructor"
                                    // It tells Minecraft: "When you need to create this entity, call new PyroclastBombEntity(...)"
                                    PyroclastBombEntity::new,

                                    // WHAT IS MOB CATEGORY?
                                    // Categories organize entities and affect spawning behavior:
                                    // - MONSTER (hostile mobs, spawns in darkness)
                                    // - CREATURE (passive animals)
                                    // - AMBIENT (bats)
                                    // - WATER_CREATURE (fish, dolphins)
                                    // - MISC (everything else - items, projectiles, minecarts)
                                    MobCategory.MISC           // Our powder is miscellaneous (it's a projectile)
                            )

                            // Set the hitbox size (width and height in block)
                            // 0.25F means 1/4 of a block (same size as a snowball)
                            .sized(0.25F, 0.25F)               // Width: 0.25 block, Height: 0.25 block
                            // F means "float" (decimal number)

                            // WHAT IS CLIENT TRACKING RANGE?
                            // How far away (in chunks) clients can see this entity
                            // 4 chunks = 64 block (same as thrown items like snowballs)
                            // Lower = better performance, higher = can see entity from farther away
                            .clientTrackingRange(4)            // Render distance: 4 chunks (64 block)

                            // WHAT IS UPDATE INTERVAL?
                            // How often (in ticks) the server sends position updates to clients
                            // 10 ticks = 0.5 seconds (20 ticks = 1 second)
                            // Higher = less network traffic but jerkier movement
                            // Lower = smoother but more network usage
                            .updateInterval(10)                // Update position every 10 ticks (twice per second)

                            // Build the EntityType with the registry name
                            .build("pyroclastic_powder")       // Must match the registry name above
            );

    // WHY DO WE NEED A REGISTER METHOD?
    // This method is called from your main mod class during startup
    // It tells NeoForge: "Hey, process all the entities in my ENTITY_TYPES list!"
    public static void register(IEventBus eventBus) {
        // Register all entity types with NeoForge's event bus
        // The event bus is like a message system - mods send messages about what they want to register
        ENTITY_TYPES.register(eventBus);

        // Log to console so you know registration happened successfully
        CreateSulfuricResonance.LOGGER.info("Entity types registered for Sulfuric Resonance");
    }

    // HOW THIS ALL WORKS TOGETHER:
    // 1. Game starts up
    // 2. Your main mod class calls ModEntities.register(eventBus)
    // 3. NeoForge processes ENTITY_TYPES and calls the lambda () -> EntityType.Builder...
    // 4. EntityType is created and registered with ID "sulfuricresonance:pyroclastic_powder"
    // 5. PYROCLASTIC_POWDER.get() now returns the registered EntityType
    // 6. You can now use it in code: new PyroclastBombEntity(level, player)
}

// COMMON MISTAKES TO AVOID:
// ❌ Forgetting to call .register(eventBus) in your main mod class → entities won't exist
// ❌ Using PYROCLASTIC_POWDER.get() before registration happens → NullPointerException
// ❌ Registry name has uppercase/spaces → game crashes
// ❌ Not importing the right packages → compilation errors