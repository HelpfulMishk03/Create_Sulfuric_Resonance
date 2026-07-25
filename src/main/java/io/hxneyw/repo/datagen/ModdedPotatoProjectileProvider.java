package io.hxneyw.repo.datagen;

import com.simibubi.create.api.registry.CreateRegistries;
import io.hxneyw.repo.compat.create.ModdedPotatoProjectileTypes;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class ModdedPotatoProjectileProvider extends DatapackBuiltinEntriesProvider {
   private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
      .add(CreateRegistries.POTATO_PROJECTILE_TYPE, ModdedPotatoProjectileTypes::bootstrap);

   public ModdedPotatoProjectileProvider(PackOutput output, CompletableFuture<Provider> registries, String modId) {
      super(output, registries, BUILDER, Set.of(modId));
   }
}
