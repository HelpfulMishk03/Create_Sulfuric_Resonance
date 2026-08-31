package io.hxneyw.repo;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public class Config {
   private static final Builder BUILDER = new Builder();
   public static final BooleanValue RUBBER_PADDING_ENABLED;
   public static final DoubleValue ITEM_BOUNCE_MULTIPLIER;
   public static final DoubleValue ENTITY_BOUNCE_MULTIPLIER;
   public static final BooleanValue RAIN_AFFECTS_MOLTEN_ROTOR;
   public static final BooleanValue TNT_CAN_EXPLODE;
   public static final BooleanValue PYROCLAST_BOMB_BLOCK_DAMAGE;
   static final ModConfigSpec SPEC;

   static {
      BUILDER.push("rubber_padding");
      RUBBER_PADDING_ENABLED = BUILDER.comment("Enable or disable bouncing on Rubber Padding blocks")
              .define("enabled", true);
      ITEM_BOUNCE_MULTIPLIER = BUILDER.comment(
                    "Multiplier for item bounce height (1.0 = normal, 0.0 = no bounce, 2.0 = double bounce)"
              )
              .defineInRange("itemBounceMultiplier", 1.0, 0.0, 3.0);
      ENTITY_BOUNCE_MULTIPLIER = BUILDER.comment(
                    "Multiplier for entity/player bounce height (1.0 = normal, 0.0 = no bounce, 2.0 = double bounce)"
              )
              .defineInRange("entityBounceMultiplier", 1.0, 0.0, 3.0);
      BUILDER.pop();

      BUILDER.push("molten_rotor");
      RAIN_AFFECTS_MOLTEN_ROTOR = BUILDER.comment("Whether rain slows heating and speeds cooling on the Molten Rotor")
              .define("rainAffectsMoltenRotor", true);
      TNT_CAN_EXPLODE = BUILDER.comment("Allow TNT to explode the Molten Rotor (disable for safe TNT usage)")
              .define("tntCanExplode", true);
      BUILDER.pop();

      BUILDER.push("pyroclast_bomb");
      PYROCLAST_BOMB_BLOCK_DAMAGE = BUILDER.comment("Allow Pyroclast Bombs to destroy blocks and ignite terrain")
              .define("blockDamage", true);
      BUILDER.pop();

      SPEC = BUILDER.build();
   }
}
