// * You have to import to get JSDoc working.
import("../../api/api")

const modDir = crafter.getCurrentModDirectory() + "/";
const modFiles = ["blocks", "biomes", "scriptingTest"];

// Load up all the separate mod files.
for (const modFile of modFiles) {
    dofile(modDir + modFile + ".js")
}
