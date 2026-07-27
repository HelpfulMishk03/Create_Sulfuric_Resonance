package io.hxneyw.repo.content.blocks.behaviour;

import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import io.hxneyw.repo.content.blocks.moltenrotor.MoltenRotorBlockEntity;

public class CombustionHeatingBehaviour extends BlockEntityBehaviour {
   public static final BehaviourType<CombustionHeatingBehaviour> TYPE =
           new BehaviourType<>();

   public CombustionHeatingBehaviour(MoltenRotorBlockEntity furnace) {
      super(furnace);
   }

   @Override
   public BehaviourType<?> getType() {
      return TYPE;
   }
}
