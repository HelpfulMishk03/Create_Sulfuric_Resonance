package io.hxneyw.repo;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class Config {
   private static final Builder BUILDER = new Builder();
   public static final BooleanValue RUBBER_PADDING_ENABLED = BUILDER.comment("Enable or disable bouncing on Rubber Padding blocks").define("enabled", true);
   public static final DoubleValue ITEM_BOUNCE_MULTIPLIER = BUILDER.comment(
         "Multiplier for item bounce height (1.0 = normal, 0.0 = no bounce, 2.0 = double bounce)"
      )
      .defineInRange("itemBounceMultiplier", 1.0, 0.0, 3.0);
   public static final DoubleValue ENTITY_BOUNCE_MULTIPLIER = BUILDER.comment(
         "Multiplier for entity/player bounce height (1.0 = normal, 0.0 = no bounce, 2.0 = double bounce)"
      )
      .defineInRange("entityBounceMultiplier", 1.0, 0.0, 3.0);
   public static final BooleanValue RAIN_AFFECTS_MOLTEN_ROTOR = BUILDER.comment("Whether rain slows heating and speeds cooling on the Molten Rotor")
      .define("rainAffectsMoltenRotor", true);
   public static final BooleanValue TNT_CAN_EXPLODE = BUILDER.comment("Allow TNT to explode the Molten Rotor (disable for safe TNT usage)")
      .define("tntCanExplode", true);
   static final ModConfigSpec SPEC = BUILDER.build();

   private static final Builder SERVER_BUILDER = new Builder();
   public static final IntValue MAX_COMBUSTION_BELT_LENGTH = SERVER_BUILDER.comment(
         "Maximum number of blocks in a Combustion Belt (minimum 5; default 20)"
      ).defineInRange("maxCombustionBeltLength", 20, 5, Integer.MAX_VALUE);
   public static final BooleanValue UNLIMITED_COMBUSTION_BELT_LENGTH = SERVER_BUILDER.comment(
         "Allow Combustion Belts to extend without a configured maximum"
      ).define("unlimitedCombustionBeltLength", false);
   static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();

   public static int combustionBeltLengthLimit() {
      return UNLIMITED_COMBUSTION_BELT_LENGTH.get()
            ? Integer.MAX_VALUE
            : Math.max(5, MAX_COMBUSTION_BELT_LENGTH.get());
   }

   public static boolean unlimitedCombustionBeltLength() {
      return UNLIMITED_COMBUSTION_BELT_LENGTH.get();
   }

   public static int combustionBeltTraversalLimit() {
      return unlimitedCombustionBeltLength()
            ? Integer.MAX_VALUE / 4
            : combustionBeltLengthLimit();
   }

   static {
      BUILDER.push("rubber_padding");
      BUILDER.pop();
      BUILDER.push("molten_rotor");
      BUILDER.pop();
   }
}
