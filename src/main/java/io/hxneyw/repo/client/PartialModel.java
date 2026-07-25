package io.hxneyw.repo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PartialModel {
   private final ResourceLocation modelLocation;
   private BakedModel cachedModel;
   private boolean needsRefresh = true;

   public PartialModel(ResourceLocation modelLocation) {
      this.modelLocation = modelLocation;
   }

   public BakedModel get() {
      if (this.cachedModel == null || this.needsRefresh) {
         this.cachedModel = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(this.modelLocation));
         this.needsRefresh = false;
      }

      return this.cachedModel;
   }

   public void invalidate() {
      this.needsRefresh = true;
      this.cachedModel = null;
   }
}
