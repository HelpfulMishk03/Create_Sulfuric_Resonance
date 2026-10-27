package io.hxneyw.repo.content.blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sable's cached collider callback only receives a BlockState and a synthetic
 * BlockGetter that deliberately contains no block entities. Keep the cargo
 * shape identified by the Caddy's state so Sable can reuse the exact shape.
 */
public final class CaddyCollisionShapeRegistry {
    public static final int FALLBACK_ID = 127;
    private static final int MAX_REGISTERED_ID = FALLBACK_ID - 1;
    private static final Map<String, Integer> IDS_BY_SHAPE = new HashMap<>();
    private static final Map<Integer, VoxelShape> SHAPES_BY_ID = new HashMap<>();

    private CaddyCollisionShapeRegistry() { }

    public static synchronized int register(VoxelShape shape, int preferredId) {
        if (shape == null || shape.isEmpty()) return 0;
        String key = shapeKey(shape);
        Integer existing = IDS_BY_SHAPE.get(key);
        if (existing != null) return existing;

        if (preferredId > 0 && preferredId <= MAX_REGISTERED_ID
                && !SHAPES_BY_ID.containsKey(preferredId)) {
            IDS_BY_SHAPE.put(key, preferredId);
            SHAPES_BY_ID.put(preferredId, shape);
            return preferredId;
        }

        for (int id = 1; id <= MAX_REGISTERED_ID; id++) {
            if (!SHAPES_BY_ID.containsKey(id)) {
                IDS_BY_SHAPE.put(key, id);
                SHAPES_BY_ID.put(id, shape);
                return id;
            }
        }
        return FALLBACK_ID;
    }

    public static synchronized void bind(int id, VoxelShape shape) {
        if (id <= 0 || id > MAX_REGISTERED_ID || shape == null || shape.isEmpty()) return;
        String key = shapeKey(shape);
        Integer existing = IDS_BY_SHAPE.get(key);
        if (existing != null && existing != id) return;
        VoxelShape previous = SHAPES_BY_ID.get(id);
        if (previous != null && !shapeKey(previous).equals(key)) return;
        IDS_BY_SHAPE.put(key, id);
        SHAPES_BY_ID.put(id, shape);
    }

    public static synchronized VoxelShape get(int id) {
        return SHAPES_BY_ID.get(id);
    }

    private static String shapeKey(VoxelShape shape) {
        List<AABB> boxes = new ArrayList<>(shape.toAabbs());
        boxes.sort(Comparator.comparingDouble((AABB box) -> box.minX)
                .thenComparingDouble(box -> box.minY)
                .thenComparingDouble(box -> box.minZ)
                .thenComparingDouble(box -> box.maxX)
                .thenComparingDouble(box -> box.maxY)
                .thenComparingDouble(box -> box.maxZ));
        StringBuilder key = new StringBuilder(boxes.size() * 48);
        for (AABB box : boxes) {
            key.append(Double.toHexString(box.minX)).append(',')
                    .append(Double.toHexString(box.minY)).append(',')
                    .append(Double.toHexString(box.minZ)).append(',')
                    .append(Double.toHexString(box.maxX)).append(',')
                    .append(Double.toHexString(box.maxY)).append(',')
                    .append(Double.toHexString(box.maxZ)).append(';');
        }
        return key.toString();
    }
}
