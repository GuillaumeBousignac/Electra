Data Generation — Electra
=========================

Archive contents
- src/main/java/com/electra/mod/datagen/ : 9 Java classes (new folder)
- remove_old_files.sh : deletes the 145 replaced JSON files

No existing file is modified by the archive.

Steps (from the project root)
1. Extract the archive at the project root.
2. ./gradlew runData
   -> must end with BUILD SUCCESSFUL and fill src/generated/resources/
3. Check a few generated files (e.g. assets/electra/blockstates/advanced_furnace.json).
4. ./supprimer-anciens-fichiers.sh
5. ./gradlew runClient and check: block and wire textures, recipes (JEI),
   block drops, English names (English language).
6. Commit src/generated/resources/ (except the .cache folder, already excluded from the build).

Files kept handwritten (Blockbench models and French translation)
- models/block: bare_wire_center, bare_wire_side, insulated_wire_center,
  insulated_wire_side, lightning_collector
- models/item: bare_wire, insulated_wire_inventory, lightning_collector
- lang/fr_fr.json
- data/electra/damage_type/electrocution.json

Adding a block in the future
- blockstate + model: ModBlockStateProvider
- loot: automatic (every block in ModBlocks drops itself)
- required tool: ModBlockTagsProvider
- recipe: ModRecipeProvider (alloying(...) method for the Advanced Furnace)
- English name: ModLanguageProvider; French name: fr_fr.json