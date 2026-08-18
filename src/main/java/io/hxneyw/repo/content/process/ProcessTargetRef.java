package io.hxneyw.repo.content.process;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ProcessTargetRef(
        @NotNull BlockPos position,
        @NotNull String dimension,
        @NotNull UUID identity
) {
    private static final String POSITION_TAG = "Position";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String IDENTITY_TAG = "Identity";

    public @NotNull CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(POSITION_TAG, position.asLong());
        tag.putString(DIMENSION_TAG, dimension);
        tag.putUUID(IDENTITY_TAG, identity);
        return tag;
    }

    public static @Nullable ProcessTargetRef load(CompoundTag tag) {
        if (!tag.contains(POSITION_TAG, Tag.TAG_LONG)
                || !tag.contains(DIMENSION_TAG, Tag.TAG_STRING)
                || !tag.hasUUID(IDENTITY_TAG)) {
            return null;
        }

        return new ProcessTargetRef(
                BlockPos.of(tag.getLong(POSITION_TAG)),
                tag.getString(DIMENSION_TAG),
                tag.getUUID(IDENTITY_TAG)
        );
    }
}
