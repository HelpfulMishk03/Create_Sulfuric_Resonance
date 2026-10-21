#!/usr/bin/env python3
"""Headless source/resource checks for CSR 0.7.0; never starts Minecraft."""

from __future__ import annotations

import json
import gzip
import pathlib
import re
import struct
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
JAVA = ROOT / "src/main/java/io/hxneyw/repo"
SUPPLIED_SCRAPER = pathlib.Path(r"C:\Users\slith\Downloads\scraper.json")


def no_duplicate_keys(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def load_json(path: pathlib.Path):
    return json.loads(path.read_text(encoding="utf-8-sig"), object_pairs_hook=no_duplicate_keys)


class SourceAuditChecks(unittest.TestCase):
    def test_redline_tnt_overload_is_delayed_and_ignores_normal_explosion_config(self):
        interactions = (JAVA / "content/blocks/moltenrotor/MoltenRotorInteractions.java").read_text(encoding="utf-8")
        furnace = (JAVA / "content/blocks/moltenrotor/MoltenRotorBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("furnace.isRedlineTrigger()", interactions)
        self.assertIn("Config.TNT_CAN_EXPLODE.get()", interactions)
        self.assertIn("startRedlineFuse(player)", interactions)
        self.assertIn("REDLINE_FUSE_TICKS = 5 * 20", furnace)
        relay = (JAVA / "content/blocks/thermalrelay/ThermalRelaySwitchBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("hasActiveLinkedRelay", relay)
        self.assertIn("redlineFuseTicks", furnace)

    def test_all_resource_json_is_strict_and_has_no_duplicate_keys(self):
        files = sorted(RESOURCES.rglob("*.json"))
        self.assertGreater(len(files), 0)
        errors = []
        for path in files:
            try:
                load_json(path)
            except (UnicodeError, json.JSONDecodeError, ValueError) as error:
                errors.append(f"{path.relative_to(ROOT)}: {error}")
        self.assertEqual([], errors)

    def test_production_scraper_uses_supplied_geometry(self):
        supplied = load_json(SUPPLIED_SCRAPER)
        production = load_json(RESOURCES / "assets/sulfuricresonance/models/block/scraper.json")
        flap_indices = set(supplied["groups"][3]["children"])
        expected_body = [element for index, element in enumerate(supplied["elements"]) if index not in flap_indices]
        self.assertEqual(len(expected_body), len(production["elements"]))
        for index, (source, actual) in enumerate(zip(expected_body, production["elements"])):
            self.assertEqual(source.get("from"), actual.get("from"))
            expected_to = list(source.get("to", []))
            if index == 0:
                expected_to[2] = 16  # The chest-only connector is rendered separately.
            self.assertEqual(expected_to, actual.get("to"))
            self.assertEqual(source.get("rotation"), actual.get("rotation"))
            self.assertEqual(set(source.get("faces", {})), set(actual.get("faces", {})))
            for face, source_data in source.get("faces", {}).items():
                if "uv" in source_data:
                    self.assertEqual([round(value / 4, 4) for value in source_data["uv"]], actual["faces"][face]["uv"])
        flap_models = [load_json(RESOURCES / f"assets/sulfuricresonance/models/block/scraper_flaps_{i}.json")
                       for i in range(4)]
        actual_flaps = {element["name"]: element for model in flap_models for element in model["elements"]}
        self.assertEqual(15, len(actual_flaps))
        for index in flap_indices:
            source = supplied["elements"][index]
            actual = actual_flaps[source["name"]]
            self.assertEqual(source["from"], actual["from"])
            self.assertEqual(source["to"], actual["to"])
            self.assertEqual(source.get("rotation"), actual.get("rotation"))
            for face, source_data in source.get("faces", {}).items():
                if "uv" in source_data:
                    self.assertEqual([round(value / 4, 4) for value in source_data["uv"]], actual["faces"][face]["uv"])

    def test_transfer_smoothing_does_not_change_caddy_stack_components(self):
        transfer = (JAVA / "mixin/BeltBlockEntityMixin.java").read_text(encoding="utf-8")
        renderer = (JAVA / "mixin/BeltRendererMixin.java").read_text(encoding="utf-8")
        item = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        transition = JAVA / "content/transport/CaddyBeltTransition.java"
        self.assertTrue(transition.is_file())
        self.assertIn("package io.hxneyw.repo.content.transport;", transition.read_text(encoding="utf-8"))
        self.assertFalse((JAVA / "mixin/CaddyBeltTransition.java").exists())
        self.assertNotIn('tag.putDouble("CSRTransferX"', transfer)
        self.assertNotIn('tag.putDouble("CSRTransferY"', transfer)
        self.assertNotIn('tag.putDouble("CSRTransferZ"', transfer)
        self.assertNotIn('transfer.getDouble("CSRTransferX")', renderer)
        self.assertIn("TransportedItemStackMixin", (RESOURCES / "sulfuricresonance.mixins.json").read_text(encoding="utf-8"))
        self.assertIn("BlockCaddyItem.clearBeltTransition(transported.stack)", transfer)
        self.assertIn('tag.remove("CSRTransferX")', item)

    def test_production_scraper_name_has_no_visual_test_label(self):
        locales = sorted((RESOURCES / "assets/sulfuricresonance/lang").glob("*.json"))
        self.assertGreater(len(locales), 0)
        for locale in locales:
            values = load_json(locale)
            self.assertEqual("Scraper", values.get("block.sulfuricresonance.scraper"), str(locale))

    def test_locale_key_sets_match_en_us(self):
        locales = sorted((RESOURCES / "assets/sulfuricresonance/lang").glob("*.json"))
        english = load_json(RESOURCES / "assets/sulfuricresonance/lang/en_us.json")
        errors = []
        for locale in locales:
            values = load_json(locale)
            missing = sorted(set(english) - set(values))
            extra = sorted(set(values) - set(english))
            if missing or extra:
                errors.append(f"{locale.name}: missing={missing}; extra={extra}")
        self.assertEqual([], errors)

    def test_locale_placeholders_match_en_us(self):
        locale_dir = RESOURCES / "assets/sulfuricresonance/lang"
        english = load_json(locale_dir / "en_us.json")
        token_pattern = re.compile(r"%(?:\d+\$)?[sd]")
        errors = []
        for locale in locale_dir.glob("*.json"):
            values = load_json(locale)
            for key in set(english).intersection(values):
                if token_pattern.findall(str(english[key])) != token_pattern.findall(str(values[key])):
                    errors.append(f"{locale.name}:{key}")
        self.assertEqual([], errors)

    def test_local_blockstate_and_model_references_resolve(self):
        errors = []
        assets = RESOURCES / "assets"
        for namespace_dir in assets.iterdir():
            namespace = namespace_dir.name
            for path in (namespace_dir / "blockstates").glob("*.json") if (namespace_dir / "blockstates").exists() else ():
                data = load_json(path)
                for model_id in self._walk_model_ids(data):
                    if model_id.startswith(namespace + ":"):
                        model_path = namespace_dir / "models" / (model_id.split(":", 1)[1] + ".json")
                        if not model_path.is_file():
                            errors.append(f"{path.relative_to(ROOT)} -> missing {model_path.relative_to(ROOT)}")
            for path in (namespace_dir / "models").rglob("*.json") if (namespace_dir / "models").exists() else ():
                data = load_json(path)
                for model_id in self._walk_model_ids(data):
                    if model_id.startswith(namespace + ":"):
                        model_path = namespace_dir / "models" / (model_id.split(":", 1)[1] + ".json")
                        if not model_path.is_file():
                            errors.append(f"{path.relative_to(ROOT)} -> missing {model_path.relative_to(ROOT)}")
                for texture_id in self._walk_texture_ids(data):
                    if texture_id.startswith(namespace + ":"):
                        texture_path = namespace_dir / "textures" / (texture_id.split(":", 1)[1] + ".png")
                        if not texture_path.is_file():
                            errors.append(f"{path.relative_to(ROOT)} -> missing {texture_path.relative_to(ROOT)}")
        self.assertEqual([], errors)

    def test_model_uvs_stay_inside_declared_texture_atlas(self):
        errors = []
        for model in (RESOURCES / "assets").rglob("models/**/*.json"):
            data = load_json(model)
            # Minecraft block-model UV coordinates are expressed in a 16x16 model-space atlas;
            # Blockbench's texture_size extension does not change that coordinate domain.
            width, height = 16, 16
            for element_index, element in enumerate(data.get("elements", [])):
                for direction, face in element.get("faces", {}).items():
                    uv = face.get("uv")
                    if uv is None:
                        continue
                    if len(uv) != 4 or min(uv) < 0 or uv[0] > width or uv[2] > width or uv[1] > height or uv[3] > height:
                        errors.append(f"{model.relative_to(ROOT)} element {element_index} {direction}: UV {uv} exceeds {width}x{height}")
        self.assertEqual([], errors)

    def test_model_element_coordinates_are_finite_and_reasonable(self):
        errors = []
        directions = {"north", "south", "east", "west", "up", "down"}
        for model in (RESOURCES / "assets").rglob("models/**/*.json"):
            data = load_json(model)
            for element_index, element in enumerate(data.get("elements", [])):
                for key in ("from", "to"):
                    values = element.get(key, [])
                    if len(values) != 3 or not all(isinstance(value, (int, float)) and abs(value) <= 64 for value in values):
                        errors.append(f"{model.relative_to(ROOT)} element {element_index}: invalid {key}={values}")
                rotation = element.get("rotation")
                if rotation and (rotation.get("angle") not in (-45, -22.5, 0, 22.5, 45)
                                 or rotation.get("axis") not in ("x", "y", "z")):
                    errors.append(f"{model.relative_to(ROOT)} element {element_index}: unusual rotation {rotation}")
                for face_name, face in element.get("faces", {}).items():
                    if face_name not in directions or face.get("cullface") not in (None, *directions):
                        errors.append(f"{model.relative_to(ROOT)} element {element_index}: invalid face {face_name}={face}")
        self.assertEqual([], errors)

    def test_caddy_and_cut_blade_textures_match_declared_atlas_dimensions(self):
        texture_dir = RESOURCES / "assets/sulfuricresonance/textures"
        expected = {"block/block_caddy.png": (32, 32), "item/cut_blade.png": (16, 16)}
        for relative, dimensions in expected.items():
            with (texture_dir / relative).open("rb") as stream:
                header = stream.read(24)
            self.assertEqual(dimensions, struct.unpack(">II", header[16:24]), relative)

    @staticmethod
    def _walk_model_ids(value):
        if isinstance(value, dict):
            for key, nested in value.items():
                if key in ("model", "parent") and isinstance(nested, str):
                    yield nested
                yield from SourceAuditChecks._walk_model_ids(nested)
        elif isinstance(value, list):
            for nested in value:
                yield from SourceAuditChecks._walk_model_ids(nested)

    @staticmethod
    def _walk_texture_ids(value):
        if isinstance(value, dict):
            for key, nested in value.items():
                if key in ("particle", "layer0", "layer1", "layer2") and isinstance(nested, str):
                    yield nested
                if key == "textures" and isinstance(nested, dict):
                    yield from (texture for texture in nested.values() if isinstance(texture, str))
                yield from SourceAuditChecks._walk_texture_ids(nested)
        elif isinstance(value, list):
            for nested in value:
                yield from SourceAuditChecks._walk_texture_ids(nested)

    def test_png_assets_have_valid_headers_and_nonzero_dimensions(self):
        errors = []
        images = list((RESOURCES / "assets").rglob("*.png"))
        self.assertGreater(len(images), 0)
        signature = b"\x89PNG\r\n\x1a\n"
        for path in images:
            try:
                with path.open("rb") as stream:
                    header = stream.read(24)
                if len(header) != 24 or header[:8] != signature:
                    errors.append(f"{path.relative_to(ROOT)}: invalid PNG header")
                    continue
                width, height = struct.unpack(">II", header[16:24])
                if width <= 0 or height <= 0:
                    errors.append(f"{path.relative_to(ROOT)}: invalid dimensions {width}x{height}")
            except OSError as error:
                errors.append(f"{path.relative_to(ROOT)}: {error}")
        self.assertEqual([], errors)

    def test_production_resources_contain_no_scraper_preview_ids(self):
        candidates = list((RESOURCES / "assets/sulfuricresonance").rglob("*"))
        candidates += list((RESOURCES / "data/sulfuricresonance").rglob("*"))
        offenders = []
        for path in candidates:
            if not path.is_file():
                continue
            if "debug_scraper_preview" in path.name.lower():
                offenders.append(str(path.relative_to(ROOT)))
            elif path.suffix.lower() in (".json", ".mcmeta", ".toml", ".txt"):
                if b"debug_scraper_preview" in path.read_bytes().lower():
                    offenders.append(str(path.relative_to(ROOT)))
        self.assertEqual([], offenders)

    def test_cut_blade_follows_infernal_impeller_in_tab_order(self):
        tabs = (JAVA / "content/ModTabs.java").read_text(encoding="utf-8")
        self.assertLess(tabs.index("output.accept(Items.INFERNAL_IMPELLER.get())"),
                        tabs.index("output.accept(Items.CUT_BLADE.get())"))

    def test_item_caddy_cargo_uses_matching_display_transform_and_three_dimensional_render(self):
        item_renderer = (JAVA / "mixin/ItemRendererMixin.java").read_text(encoding="utf-8")
        cargo_renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        self.assertIn("renderCargoItem", item_renderer)
        self.assertIn("ItemRenderer.class, remap = false", item_renderer)
        self.assertIn("getTransform(context).apply(leftHanded, poseStack)", item_renderer)
        self.assertIn("renderSingleBlock", cargo_renderer)
        self.assertIn("15f / 16f", cargo_renderer)

    def test_belt_deployer_can_load_block_items_into_caddies_without_recipes(self):
        mixins = load_json(RESOURCES / "sulfuricresonance.mixins.json")
        self.assertIn("DeployerBlockEntityMixin", mixins["mixins"])
        callback = (JAVA / "mixin/DeployerBlockEntityMixin.java").read_text(encoding="utf-8")
        self.assertIn("DirectionalKineticBlock.FACING", callback)
        self.assertIn("BlockItem", callback)

    def test_empty_and_filled_caddies_are_registered_as_decorative_blocks(self):
        self.assertTrue((JAVA / "content/blocks/BlockCaddyBlock.java").is_file())
        self.assertTrue((JAVA / "content/blocks/BlockCaddyBlockEntity.java").is_file())
        caddy_item = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        self.assertIn("extends BlockItem", caddy_item)
        self.assertIn("BlockCaddyBlockEntity", caddy_item)
        self.assertTrue((RESOURCES / "assets/sulfuricresonance/blockstates/block_caddy.json").is_file())
        self.assertIn("BLOCK_CADDY", (JAVA / "content/registry/AllModBlocks.java").read_text(encoding="utf-8"))

    def test_belt_cargo_fills_the_caddy_tray(self):
        cargo_renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        self.assertIn("15f / 16f", cargo_renderer)

    def test_caddy_deployer_callbacks_are_registered(self):
        mixins = load_json(RESOURCES / "sulfuricresonance.mixins.json")
        self.assertIn("BeltDeployerCallbacksMixin", mixins["mixins"])

    def test_caddy_cargo_fits_the_tray_and_support_shape(self):
        renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        block = (JAVA / "content/blocks/BlockCaddyBlock.java").read_text(encoding="utf-8")
        self.assertIn("15f / 16f", renderer)
        self.assertIn("Block.box(0.5, 1.1, 0.5, 15.5, 15.1, 15.5)", block)
        self.assertIn("1.1f / 16f", renderer)

    def test_third_person_caddy_transform_is_smaller_than_block_scale(self):
        model = load_json(RESOURCES / "assets/sulfuricresonance/models/item/block_caddy.json")
        display = model["display"]
        for context in ("thirdperson_righthand", "thirdperson_lefthand"):
            self.assertIn(context, display)
            self.assertLess(max(display[context]["scale"]), 0.5)

    def test_chest_caddy_interaction_is_intercepted_with_held_items(self):
        interaction = (JAVA / "content/items/BlockCaddyInteractionHandler.java").read_text(encoding="utf-8")
        self.assertIn("BlockCaddyBlockEntity", interaction)
        self.assertIn("openForCargo", interaction)
        self.assertIn("setCanceled(true)", interaction)

    def test_shift_unload_returns_cargo_for_entire_caddy_stack(self):
        caddy_item = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        self.assertIn("copyWithCount(caddy.getCount())", caddy_item)

    def test_scraper_preserves_storage_cargo_inside_caddy(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        caddy_item = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        self.assertIn("isStorageCargo", scraper)
        self.assertIn("isStorageCargo", caddy_item)

    def test_entity_blocks_render_their_baked_models(self):
        for name in ("ScraperBlock.java", "BlockCaddyBlock.java"):
            source = (JAVA / "content/blocks" / name).read_text(encoding="utf-8")
            self.assertIn("RenderShape.MODEL", source)

    def test_placed_caddy_cargo_is_saved_and_rendered(self):
        entity = (JAVA / "content/blocks/BlockCaddyBlockEntity.java").read_text(encoding="utf-8")
        renderer = (JAVA / "content/blocks/BlockCaddyBlockEntityRenderer.java").read_text(encoding="utf-8")
        client = (JAVA / "client/ClientModEvents.java").read_text(encoding="utf-8")
        self.assertIn('tag.put(CARGO_TAG, cargo.save(registries))', entity)
        self.assertIn("ItemStack.parse(registries", entity)
        self.assertIn("BlockCaddyBeltRenderer.renderCargo", renderer)
        self.assertIn("AllBlockEntities.BLOCK_CADDY.get()", client)

    def test_caddy_cargo_renders_dynamic_block_entity_and_flywheel_shafts(self):
        cargo_renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        self.assertIn("entityBlock.newBlockEntity(BlockPos.ZERO, state)", cargo_renderer)
        self.assertIn("getBlockEntityRenderDispatcher()", cargo_renderer)
        self.assertIn("renderer.render(", cargo_renderer)
        self.assertIn("VisualizationManager.supportsVisualization(minecraft.level)", cargo_renderer)
        self.assertIn("renderFlywheelShaft(state, cargoEntity", cargo_renderer)
        self.assertIn("KineticBlockEntityRenderer.shaft(axis)", cargo_renderer)

    def test_caddy_and_bar_recipes_and_textures_are_in_expected_folders(self):
        for name in ("block_caddy", "resonant_iron_bars", "resonant_copper_bars", "resonant_gold_bars"):
            self.assertTrue((RESOURCES / f"data/sulfuricresonance/recipe/crafting/{name}.json").is_file())
        self.assertFalse((RESOURCES / "data/sulfuricresonance/recipe/block_caddy.json").exists())
        self.assertFalse((RESOURCES / "data/sulfuricresonance/recipe/resonant_iron_bars.json").exists())
        for path in ("block/block_caddy.png", "block/resonant_iron_bars.png", "block/resonant_copper_bars.png",
                     "block/resonant_gold_bars.png", "block/scraper.png", "item/cut_blade.png"):
            self.assertTrue((RESOURCES / f"assets/sulfuricresonance/textures/{path}").is_file())

    def test_scraper_keeps_cargo_when_storage_commit_rejects_it(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("ItemStack cargoBatch = cargo.copyWithCount(transported.stack.getCount())", scraper)
        self.assertIn("ItemStack remainder = insert(storage, cargoBatch)", scraper)
        self.assertRegex(scraper, r"if \(!remainder\.isEmpty\(\)\)[\s\S]{0,250}TransportedResult\.doNothing\(\)")
        self.assertRegex(scraper, r"private static ItemStack insert\(")

    def test_caddy_cargo_fills_the_inner_tray_and_ground_render_is_block_anchored(self):
        renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        placed_renderer = (JAVA / "content/blocks/BlockCaddyBlockEntityRenderer.java").read_text(encoding="utf-8")
        self.assertIn("15f / 16f", renderer)
        self.assertIn("renderCargoOnBlock", placed_renderer)
        self.assertIn("float inset = (1f - cargoSize) / 2f", renderer)
        self.assertIn("centeredOnOrigin ? -cargoSize / 2f : inset", renderer)

    def test_deployer_belt_loading_does_not_depend_on_exact_animation_tick(self):
        mixins = load_json(RESOURCES / "sulfuricresonance.mixins.json")
        self.assertIn("DeployerBlockEntityMixin", mixins["mixins"])
        self.assertIn("BeltDeployerCallbacksMixin", mixins["mixins"])
        callback = (JAVA / "mixin/DeployerBlockEntityMixin.java").read_text(encoding="utf-8")
        self.assertIn('method = "activate"', callback)
        self.assertIn("held.shrink(1)", callback)

    def test_scraper_ejects_loaded_caddy_when_no_storage_is_connected(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertNotRegex(scraper, r"storage == null[\s\S]{0,150}lockedExternally = true")
        self.assertIn("getCargoStack(transported.stack)", scraper)
        self.assertIn("outputPos", scraper)

    def test_scraper_looks_for_storage_at_its_rear_and_outputs_behind(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("BlockPos storagePos = pos.relative(facing)", scraper)
        self.assertIn("Direction storageSide = facing.getOpposite()", scraper)
        self.assertIn("var outputPos = pos.relative(facing).getCenter()", scraper)

    def test_caddy_and_scraper_define_collision_shapes(self):
        caddy = (JAVA / "content/blocks/BlockCaddyBlock.java").read_text(encoding="utf-8")
        scraper = (JAVA / "content/blocks/ScraperBlock.java").read_text(encoding="utf-8")
        self.assertIn("getCollisionShape", caddy)
        self.assertIn("getCollisionShape", scraper)

    def test_deployer_loads_one_caddy_per_activation_on_belts_and_depots(self):
        mixins = load_json(RESOURCES / "sulfuricresonance.mixins.json")
        self.assertIn("DeployerBlockEntityMixin", mixins["mixins"])
        source = (JAVA / "mixin/DeployerBlockEntityMixin.java").read_text(encoding="utf-8")
        self.assertIn('method = "activate"', source)
        self.assertIn("applyToEachWithin", source)
        self.assertIn("convertToAndLeaveHeld", source)
        self.assertIn("held.shrink(1)", source)
        self.assertIn("BeltDeployerCallbacksMixin", mixins["mixins"])

    def test_belt_deployer_fills_one_caddy_from_a_stacked_transport(self):
        source = (JAVA / "mixin/BeltDeployerCallbacksMixin.java").read_text(encoding="utf-8")
        self.assertIn("transported.stack.copyWithCount(1)", source)
        self.assertIn("held.copyWithCount(1)", source)
        self.assertIn("TransportedResult.convertToAndLeaveHeld", source)
        self.assertIn("handler.handleProcessingOnItem(transported, result)", source)
        self.assertNotIn("tryLoadFromBlockItem(transported.stack, held)", source)

    def test_scraper_preserves_all_caddies_when_extracting_and_outputs_at_back(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("copyWithCount(transported.stack.getCount())", scraper)
        self.assertIn("cargo.copyWithCount(transported.stack.getCount())", scraper)
        self.assertIn("pos.relative(facing).getCenter()", scraper)

    def test_chest_cargo_opens_persistent_menu_from_belts_and_caddy_items(self):
        inventory = JAVA / "content/items/BlockCaddyInventory.java"
        interaction = JAVA / "content/items/BlockCaddyInteractionHandler.java"
        self.assertTrue(inventory.is_file())
        self.assertTrue(interaction.is_file())
        self.assertIn("ChestMenu.threeRows", (JAVA / "content/items/BlockCaddyInventory.java").read_text(encoding="utf-8"))
        self.assertIn("RightClickBlock", interaction.read_text(encoding="utf-8"))
        self.assertIn("getTransportedItems", interaction.read_text(encoding="utf-8"))
        self.assertIn("isChestCargo", (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8"))

    def test_chest_cargo_adds_required_block_entity_id_before_serialization(self):
        caddy = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        inventory = (JAVA / "content/items/BlockCaddyInventory.java").read_text(encoding="utf-8")
        entity = (JAVA / "content/blocks/BlockCaddyBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("ensureCargoBlockEntityDataId(cargo)", caddy)
        self.assertIn('blockEntityData.putString("id"', caddy)
        self.assertRegex(inventory, r"CompoundTag cargoData = getBackingData\(\)")
        self.assertIn('cargoData.put(ITEMS_KEY, items)', inventory)
        self.assertIn("repairSerializedCargoStack", entity)
        self.assertLess(entity.index("repairSerializedCargoStack"), entity.index("ItemStack.parse(registries"))

    def test_caddy_held_model_and_cargo_are_scaled_down_and_seated(self):
        item_model = load_json(RESOURCES / "assets/sulfuricresonance/models/item/block_caddy.json")
        first_person_scale = item_model["display"]["firstperson_righthand"]["scale"]
        self.assertLessEqual(max(first_person_scale), 0.5)
        self.assertLessEqual(max(item_model["display"]["ground"]["scale"]), 0.5)
        renderer = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        self.assertIn("centeredOnOrigin ? -cargoSize / 2f : inset, 1.1f / 16f", renderer)
        self.assertNotIn("-1f / 16f", renderer)

    def test_cargo_is_not_drawn_twice_and_animated_blocks_use_their_own_renderer(self):
        belt = (JAVA / "mixin/BeltRendererMixin.java").read_text(encoding="utf-8")
        cargo = (JAVA / "client/BlockCaddyBeltRenderer.java").read_text(encoding="utf-8")
        item = (JAVA / "mixin/ItemRendererMixin.java").read_text(encoding="utf-8")
        self.assertNotIn("BlockCaddyBeltRenderer.renderCargo(", belt)
        self.assertIn("RenderShape.ENTITYBLOCK_ANIMATED", cargo)
        self.assertIn("getTransform(context).apply", item)

    def test_picking_up_placed_caddy_clones_its_cargo_stack(self):
        caddy = (JAVA / "content/blocks/BlockCaddyBlock.java").read_text(encoding="utf-8")
        self.assertIn("getCloneItemStack(BlockState state, HitResult target, LevelReader level", caddy)
        self.assertIn("BlockCaddyItem.createStack(caddy.getCargo())", caddy)

    def test_ground_caddy_voxel_matches_the_15_pixel_cargo(self):
        caddy = (JAVA / "content/blocks/BlockCaddyBlock.java").read_text(encoding="utf-8")
        self.assertIn("Block.box(0.5, 1.1, 0.5, 15.5, 15.1, 15.5)", caddy)

    def test_placed_empty_caddy_accepts_one_held_block_item(self):
        caddy = (JAVA / "content/blocks/BlockCaddyBlock.java").read_text(encoding="utf-8")
        self.assertIn("BlockCaddyItem.tryLoadFromBlockItem", caddy)
        self.assertRegex(caddy, r"if \(!level\.isClientSide\)[\s\S]{0,600}setCargo")
        self.assertIn("player.getAbilities().instabuild", caddy)
        self.assertIn("stack.shrink(1)", caddy)

    def test_scraper_rear_connector_is_chest_only_and_clears_vaults(self):
        assets = RESOURCES / "assets/sulfuricresonance"
        scraper = load_json(assets / "models/block/scraper.json")
        rear_panel = scraper["elements"][0]
        self.assertEqual([0, -2, 14.4], rear_panel["from"])
        self.assertEqual(16, rear_panel["to"][2])

        connector = load_json(assets / "models/block/scraper_chest_connector.json")
        self.assertEqual(17, connector["elements"][0]["to"][2])
        renderer = (JAVA / "content/blocks/ScraperBlockEntityRenderer.java").read_text(encoding="utf-8")
        self.assertIn("instanceof ChestBlock", renderer)
        self.assertIn("SCRAPER_CHEST_CONNECTOR", renderer)

    def test_block_caddy_ponder_is_registered_and_covers_core_use_cases(self):
        ponder = JAVA / "ponder/BlockCaddyScenes.java"
        source = ponder.read_text(encoding="utf-8")
        plugin = (JAVA / "ponder/SulfuricResonancePonderPlugin.java").read_text(encoding="utf-8")
        for scene in ("handLoading", "factoryLoading", "chestCargo"):
            self.assertIn(f"BlockCaddyScenes::{scene}", plugin)
        for topic in ("placeable block item", "Deployer", "27-slot inventory", "Scraper"):
            self.assertIn(topic, source)
        scene_text_counts = {"hand_loading": 4, "factory_loading": 3, "chest_cargo": 3}
        for schematic in ("hand_loading.nbt", "factory_loading.nbt", "chest_cargo.nbt"):
            path = RESOURCES / f"assets/sulfuricresonance/ponder/block_caddy/{schematic}"
            self.assertTrue(path.is_file())
            self.assertTrue(gzip.decompress(path.read_bytes()))
        self.assertIn('"block_caddy/factory_loading"', plugin)
        for language in (RESOURCES / "assets/sulfuricresonance/lang").glob("*.json"):
            strings = load_json(language)
            for chapter, text_count in scene_text_counts.items():
                self.assertIn(f"sulfuricresonance.ponder.block_caddy.{chapter}.header", strings)
                for text_index in range(1, text_count + 1):
                    self.assertIn(f"sulfuricresonance.ponder.block_caddy.{chapter}.text_{text_index}", strings)

    def test_scraper_stores_both_empty_caddy_and_cargo_together(self):
        scraper = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("canFitBoth(storage, cargoBatch, caddyBatch)", scraper)
        self.assertRegex(scraper, r"insert\(storage, cargoBatch\)[\s\S]{0,500}insert\(storage, caddyBatch\)")
        self.assertRegex(scraper, r"insert\(storage, caddyBatch\)[\s\S]{0,400}TransportedResult\.removeItem\(\)")

    def test_scraper_rubber_flaps_use_create_funnel_animation(self):
        renderer = JAVA / "content/blocks/ScraperBlockEntityRenderer.java"
        self.assertTrue(renderer.is_file())
        source = renderer.read_text(encoding="utf-8")
        self.assertIn("FlapStuffs.flapAngle", source)
        self.assertIn("rubber_flaps", source)
        model = load_json(RESOURCES / "assets/sulfuricresonance/models/block/scraper.json")
        self.assertFalse(any(element.get("name", "").startswith("rubber_flaps") for element in model["elements"]))
        flaps = [load_json(RESOURCES / f"assets/sulfuricresonance/models/block/scraper_flaps_{i}.json") for i in range(4)]
        self.assertEqual(15, sum(len(partial["elements"]) for partial in flaps))
        self.assertTrue(all(any(element.get("name", "").startswith("rubber_flaps") for element in partial["elements"])
                            for partial in flaps))

    def test_scraper_triggers_flaps_once_per_incoming_transported_caddy(self):
        source = (JAVA / "content/blocks/ScraperBlockEntity.java").read_text(encoding="utf-8")
        self.assertIn("triggeredFlaps.add(transported)", source)
        self.assertNotIn("flap.getValue(1) < -0.01f", source)

    def test_caddy_chest_tooltip_lists_saved_inventory_preview(self):
        caddy = (JAVA / "content/items/BlockCaddyItem.java").read_text(encoding="utf-8")
        self.assertIn("appendChestContentsPreview", caddy)
        self.assertIn('"Items"', caddy)

    def test_scraper_no_longer_uses_the_reversed_custom_voxel_shape(self):
        scraper = (JAVA / "content/blocks/ScraperBlock.java").read_text(encoding="utf-8")
        self.assertNotIn("private static final VoxelShape SHAPE", scraper)
        self.assertEqual(2, scraper.count("return Shapes.empty()"))


if __name__ == "__main__":
    unittest.main(verbosity=2)
