import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/** Structural smoke-check for Create's compressed 1.21.1 StructureTemplate NBT. */
public final class VerifyCsrContraptionSchematics {
    private static Object payload(DataInputStream in, int type) throws IOException {
        return switch (type) {
            case 1 -> in.readByte();
            case 2 -> in.readShort();
            case 3 -> in.readInt();
            case 4 -> in.readLong();
            case 5 -> in.readFloat();
            case 6 -> in.readDouble();
            case 7 -> in.readNBytes(in.readInt());
            case 8 -> in.readUTF();
            case 9 -> {
                int elementType = in.readUnsignedByte();
                int length = in.readInt();
                List<Object> list = new ArrayList<>(length);
                for (int i = 0; i < length; i++) list.add(payload(in, elementType));
                yield list;
            }
            case 10 -> {
                var map = new java.util.LinkedHashMap<String, Object>();
                while (true) {
                    int childType = in.readUnsignedByte();
                    if (childType == 0) break;
                    map.put(in.readUTF(), payload(in, childType));
                }
                yield map;
            }
            case 11 -> {
                int length = in.readInt(); int[] array = new int[length];
                for (int i = 0; i < length; i++) array[i] = in.readInt();
                yield array;
            }
            case 12 -> {
                int length = in.readInt(); long[] array = new long[length];
                for (int i = 0; i < length; i++) array[i] = in.readLong();
                yield array;
            }
            default -> throw new IOException("Unsupported NBT tag type " + type);
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> read(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))) {
            if (in.readUnsignedByte() != 10 || !in.readUTF().isEmpty()) throw new IOException("Invalid root compound");
            return (Map<String, Object>) payload(in, 10);
        }
    }

    @SuppressWarnings("unchecked")
    private static void verify(Path path) throws Exception {
        Map<String, Object> root = read(path);
        List<Object> size = (List<Object>) root.get("size");
        List<Object> palette = (List<Object>) root.get("palette");
        List<Object> blocks = (List<Object>) root.get("blocks");
        if (!(root.get("DataVersion") instanceof Integer version) || version != 3955)
            throw new IllegalStateException(path + ": expected DataVersion 3955");
        if (size == null || size.size() != 3 || palette == null || palette.isEmpty() || blocks == null)
            throw new IllegalStateException(path + ": missing StructureTemplate fields");
        int sx = (Integer) size.get(0), sy = (Integer) size.get(1), sz = (Integer) size.get(2);
        Set<String> positions = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (Object entry : palette) names.add((String) ((Map<String, Object>) entry).get("Name"));
        for (Object entry : blocks) {
            Map<String, Object> block = (Map<String, Object>) entry;
            List<Object> pos = (List<Object>) block.get("pos");
            int state = (Integer) block.get("state");
            if (pos == null || pos.size() != 3 || state < 0 || state >= palette.size())
                throw new IllegalStateException(path + ": invalid block position/palette reference");
            int x = (Integer) pos.get(0), y = (Integer) pos.get(1), z = (Integer) pos.get(2);
            if (x < 0 || x >= sx || y < 0 || y >= sy || z < 0 || z >= sz)
                throw new IllegalStateException(path + ": block outside declared bounds: " + x + "," + y + "," + z);
            if (!positions.add(x + "," + y + "," + z))
                throw new IllegalStateException(path + ": duplicate block position");
        }
        if (path.getFileName().toString().contains("Moving_Contraption_Demo")) {
            if (!names.contains("create:mechanical_bearing") || !names.contains("create:shaft")
                    || !names.contains("create:creative_motor") || !names.contains("create:radial_chassis"))
                throw new IllegalStateException(path + ": missing rotating-controller components");
        } else if (!names.contains("create:mechanical_piston")
                || !names.contains("create:piston_extension_pole")
                || !names.contains("create:creative_motor")) {
            throw new IllegalStateException(path + ": missing piston-controller components");
        }
        for (String name : names) {
            if (!(name.startsWith("minecraft:") || name.startsWith("create:") || name.startsWith("sulfuricresonance:")))
                throw new IllegalStateException(path + ": unexpected namespace " + name);
        }
        System.out.printf("PASS %s | size=%dx%dx%d blocks=%d palette=%d dataVersion=%d%n",
                path.getFileName(), sx, sy, sz, blocks.size(), palette.size(), version);
        System.out.println("  CSR blocks: " + names.stream().filter(s -> s.startsWith("sulfuricresonance:")).count());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("Pass one or more .nbt files");
        for (String arg : args) verify(Path.of(arg));
    }
}
