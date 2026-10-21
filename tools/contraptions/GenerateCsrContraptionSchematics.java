import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Writes 1.21.1 vanilla StructureTemplate NBT used by Create 6.0.10 schematics.
 * No block-entity inventories are embedded: Create creates fresh block entities
 * when the blueprint is placed, avoiding duplicated player cargo or machine stock.
 */
public final class GenerateCsrContraptionSchematics {
    private static final int DATA_VERSION = 3955;
    private static final Pattern BLOCK_REGISTRATION = Pattern.compile(
            "BLOCKS\\.(?:register|registerSimpleBlock)\\(\\s*\"([^\"]+)\"");

    private record State(String name, Map<String, String> properties) {
        State(String name) { this(name, Map.of()); }
    }
    private record Cell(int x, int y, int z, State state) {}

    private final LinkedHashMap<State, Integer> palette = new LinkedHashMap<>();
    private final List<Cell> cells = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        Path repo = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath().normalize();
        Path output = repo.resolve("docs/contraptions");
        Files.createDirectories(output);
        List<String> registered = readRegisteredBlocks(repo.resolve(
                "src/main/java/io/hxneyw/repo/content/registry/AllModBlocks.java"));
        writeDemo(output.resolve("CSR_070_Moving_Contraption_Demo.nbt"));
        writeFullTest(output.resolve("CSR_070_Full_Machine_Contraption_Test.nbt"), registered);
        System.out.println("Registered CSR blocks: " + registered.size());
        System.out.println("Demo: " + output.resolve("CSR_070_Moving_Contraption_Demo.nbt"));
        System.out.println("Full test: " + output.resolve("CSR_070_Full_Machine_Contraption_Test.nbt"));
    }

    private static List<String> readRegisteredBlocks(Path source) throws IOException {
        String text = Files.readString(source);
        Matcher matcher = BLOCK_REGISTRATION.matcher(text);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            if (!result.contains(id)) result.add(id);
        }
        if (result.size() != 58) {
            throw new IllegalStateException("Expected to audit all 58 registered blocks; found " + result.size());
        }
        return result;
    }

    private static void writeDemo(Path path) throws IOException {
        GenerateCsrContraptionSchematics schematic = new GenerateCsrContraptionSchematics();
        schematic.fill(0, 0, 0, 11, 1, 14, new State("minecraft:stone_bricks"));

        // A Create Mechanical Bearing is the controller. Its default Creative
        // Motor speed is 16 RPM; the upward shaft train drives the bearing, and
        // the radial chassis above it carries the CSR blocks around the bearing.
        schematic.add(5, 1, 7, state("create:creative_motor", "facing", "up"));
        schematic.add(5, 2, 7, state("create:shaft", "axis", "y"));
        schematic.add(5, 3, 7, state("create:mechanical_bearing", "facing", "up"));
        schematic.add(5, 4, 7, state("create:radial_chassis", "axis", "y",
                "sticky_north", "true", "sticky_south", "true", "sticky_east", "true", "sticky_west", "true"));
        schematic.add(4, 4, 7, new State("sulfuricresonance:scraper"));
        schematic.add(6, 4, 7, new State("sulfuricresonance:block_caddy"));
        schematic.add(5, 4, 6, new State("sulfuricresonance:perforated_spritzer"));
        schematic.write(path, 11, 6, 14);
    }

    private static void writeFullTest(Path path, List<String> registered) throws IOException {
        GenerateCsrContraptionSchematics schematic = new GenerateCsrContraptionSchematics();
        List<String> movable = registered.stream().filter(id -> !id.equals("sulfuric_acid")).toList();
        int next = 0;
        // 11x11 checkerboard of radial chassis and all movable CSR registrations.
        // Chassis sticky faces create one connected payload. Acid is deliberately
        // excluded because its registered LiquidBlock has PushReaction.DESTROY.
        for (int z = 1; z <= 11; z++) {
            for (int x = 1; x <= 11; x++) {
                if ((x == 6 && z == 11) || ((x - 1) + (z - 1)) % 2 == 0) {
                    schematic.add(x, 1, z, state("create:radial_chassis", "axis", "y",
                            "sticky_north", "true", "sticky_south", "true", "sticky_east", "true", "sticky_west", "true"));
                } else if (next < movable.size()) {
                    String id = movable.get(next++);
                    if (id.equals("rotary_leacher")) {
                        schematic.add(x, 1, z, state("sulfuricresonance:rotary_leacher",
                                "facing", "east", "half", "lower", "horizontal_facing", "north"));
                        schematic.add(x, 2, z, state("sulfuricresonance:rotary_leacher",
                                "facing", "east", "half", "upper", "horizontal_facing", "north"));
                    } else {
                        schematic.add(x, 1, z, new State("sulfuricresonance:" + id));
                    }
                }
            }
        }

        // Fixed controller sits beyond the south edge; its piston nose touches
        // the chassis payload at (6,1,11). All machine inventories start empty.
        for (int z = 12; z <= 14; z++) {
            for (int x = 4; x <= 8; x++) schematic.add(x, 0, z, new State("minecraft:stone_bricks"));
        }
        schematic.fill(1, 0, 1, 12, 1, 12, new State("minecraft:stone_bricks"));
        schematic.add(6, 1, 12, state("create:mechanical_piston", "facing", "north", "state", "retracted"));
        schematic.add(6, 1, 13, state("create:piston_extension_pole", "facing", "north"));
        schematic.add(6, 1, 14, state("create:creative_motor", "facing", "north"));
        schematic.write(path, 13, 4, 15);
        if (next != movable.size()) {
            throw new IllegalStateException("Only placed " + next + " of " + movable.size() + " movable registrations");
        }
    }

    private static State state(String name, String... keyValues) {
        Map<String, String> properties = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) properties.put(keyValues[i], keyValues[i + 1]);
        return new State(name, properties);
    }

    private void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, State state) {
        for (int y = minY; y < maxY; y++)
            for (int z = minZ; z < maxZ; z++)
                for (int x = minX; x < maxX; x++) add(x, y, z, state);
    }

    private void add(int x, int y, int z, State state) {
        cells.add(new Cell(x, y, z, state));
        palette.computeIfAbsent(state, ignored -> palette.size());
    }

    private void write(Path path, int sizeX, int sizeY, int sizeZ) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(path)))) {
            compoundStart(out, "");
            listStart(out, "size", 3, 3);
            out.writeInt(sizeX); out.writeInt(sizeY); out.writeInt(sizeZ);
            listStart(out, "entities", 10, 0);
            listStart(out, "blocks", 10, cells.size());
            for (Cell cell : cells) {
                listStart(out, "pos", 3, 3);
                out.writeInt(cell.x()); out.writeInt(cell.y()); out.writeInt(cell.z());
                intTag(out, "state", palette.get(cell.state()));
                out.writeByte(0);
            }
            listStart(out, "palette", 10, palette.size());
            for (State state : palette.keySet()) {
                if (!state.properties().isEmpty()) {
                    compoundStart(out, "Properties");
                    for (Map.Entry<String, String> property : state.properties().entrySet())
                        stringTag(out, property.getKey(), property.getValue());
                    out.writeByte(0);
                }
                stringTag(out, "Name", state.name());
                out.writeByte(0);
            }
            intTag(out, "DataVersion", DATA_VERSION);
            out.writeByte(0);
        }
    }

    private static void compoundStart(DataOutputStream out, String name) throws IOException {
        out.writeByte(10); out.writeUTF(name);
    }
    private static void listStart(DataOutputStream out, String name, int elementType, int length) throws IOException {
        out.writeByte(9); out.writeUTF(name); out.writeByte(elementType); out.writeInt(length);
    }
    private static void intTag(DataOutputStream out, String name, int value) throws IOException {
        out.writeByte(3); out.writeUTF(name); out.writeInt(value);
    }
    private static void stringTag(DataOutputStream out, String name, String value) throws IOException {
        out.writeByte(8); out.writeUTF(name); out.writeUTF(value);
    }
}
