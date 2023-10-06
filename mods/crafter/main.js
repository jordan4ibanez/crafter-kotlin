// * You have to import to get JSDoc working.
import("../../api/api")

const modDir = crafter.getCurrentModDirectory() + "/";

// Load up all the separate mod files.
for (const modFile of ["blocks", "biomes", "scriptingTest"]) {
  dofile(modDir + modFile + ".js");
}
