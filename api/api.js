/*
API is the base of the API which loads up class variables into the crafter array.
This allows for mods to index DIRECTLY into the engine without needing to be compiled.
Limitations: Ecmascript 6. Class architecture is not implemented into nashorn yet.
Possible implementations: Typescript (one day)
*/

// Global java types assignment.
//
const api = Java.type("engine.api").INSTANCE
const jvmBlockController = Java.type("engine.block").INSTANCE
const fileHelpers = Java.type("engine.File_helpersKt")
const RuntimeException = Java.type("java.lang.RuntimeException")

/**
 * A block DrawType enumerator.
 */
const DrawType = {
  AIR: Java.type("engine.DrawType").AIR,
  BLOCK: Java.type("engine.DrawType").BLOCK,
  BLOCK_BOX: Java.type("engine.DrawType").BLOCK_BOX,
  TORCH: Java.type("engine.DrawType").TORCH,
  LIQUID_SOURCE: Java.type("engine.DrawType").LIQUID_SOURCE,
  LIQUID_FLOW: Java.type("engine.DrawType").LIQUID_FLOW,
  GLASS: Java.type("engine.DrawType").GLASS,
  PLANT: Java.type("engine.DrawType").PLANT,
  LEAVES: Java.type("engine.DrawType").LEAVES
};

// Lua equivalents
//
/**
 * Execute a file.
 * @param {string} fileDirectory 
 */
const dofile = function(fileDirectory) { api.runFile(fileDirectory) };

/**
 * Execute raw code.
 * @param {string} rawCode 
 */
const loadstring = function(rawCode) { api.runCode(rawCode) };

/** 
 * Get string of a text file.
 * @param {string} fileLocation
 */
const getFileString = fileHelpers.getFileString;

/**
 * JOML math library.
 */
const math = Java.type("org.joml.Math");

// Kotlin equivalents.
//
const println = print;

// JOML types
//
// Mutable.
const Vector2f = Java.type("org.joml.Vector2f");
const Vector3f = Java.type("org.joml.Vector3f");
const Vector2i = Java.type("org.joml.Vector2i");
const Vector3i = Java.type("org.joml.Vector3i");
// Readonly.
const Vector2fc = Java.type("org.joml.Vector2fc");
const Vector3fc = Java.type("org.joml.Vector3fc");
const Vector2ic = Java.type("org.joml.Vector2ic");
const Vector3ic = Java.type("org.joml.Vector3ic");

// Custom JS types.
//
// ? todo:

//? I'm not sure what crafter object interface should hold yet.
const crafter = {
};


/**
 * Functional interface into the engine's block definitions.
 */
const requiredBlockProperties = ["name", "inventoryName", "textures", "drawtype"];
const optionalBlockProperties = ["walkable", "liquid", "flow", "viscosity", "climbable", "sneakJumpClimbable", "falling", "clear", "damagePerSecond", "light"]

const block = {
  
  /**
   * Define a block definition via an object.
   * d stands for "definition".
   * Please see the following constants for more info
   * @constant requiredBlockProperties
   * @constant optionalBlockProperties.
   * @param {object} d Definition Object. 
   */
  register: function(d) {
    
    //* This is essentially functional hidden behind OOP.
    
    for (key of requiredBlockProperties) {
      if (!d.hasOwnProperty(key)) {
        throw RuntimeException(`API: Definition missing ${key}`)
      }
    }
    jvmBlockController.register(d.name, d.inventoryName, d.textures, d.drawtype)

    // Now assign optionals.
    for (key of optionalBlockProperties) {
      if (d.hasOwnProperty(key)) {
        // Then it gets injected here automatically
      }
    }

  }

}

block.register({
  name: "air",
  inventoryName: "air",
  textures: ["air.png","air.png","air.png","air.png","air.png","air.png"],
  drawtype: DrawType.AIR
})


