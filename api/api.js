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
 * @readonly
 * @enum {number}
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

/**
 * A java record that holds the data for a block definition.
 * If you want to create one, you MUST include all properties, in order.
 * @property {number} id
 * @property {string} name
 * @property {string} inventoryName
 * @property {Array<string>} textures
 * @property {DrawType} drawType
 * @property {boolean} walkable
 * @property {boolean} liquid
 * @property {number} flow
 * @property {number} viscosity
 * @property {boolean} climbable
 * @property {boolean} sneakJumpClimbable
 * @property {boolean} falling
 * @property {boolean} clear
 * @property {number} damagePerSecond
 * @property {number} light
 */
const BlockDefinition = Java.type("engine.BlockDefinition")


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
  },

  /**
   * Set the inventory name of a block.
   * @param {number | string} key ID or name.
   * @param {string} newName 
   */
  setInventoryName: function(key, newName) {
    jvmBlockController.setInventoryName(key, newName)
  },


  /**
   * Set the textures of a block.
   * @param {number | string} key ID or name.
   * @param {Array<string>} newTextures 
   */
  setTextures: function(key, newTextures) {
    jvmBlockController.setTextures(key, newTextures)
  },

  /**
   * Set the drawtype of a block.
   * @param {number | string} key ID or name. 
   * @param {DrawType} newDrawType 
   */
  setDrawType: function(key, newDrawType) {
    jvmBlockController.setDrawType(key, newDrawType)
  },

  /**
   * Set if a block is walkable.
   * @param {number | string} key ID or name. 
   * @param {boolean} isWalkable 
   */
  setWalkable: function(key, isWalkable) {
    jvmBlockController.setWalkable(key, isWalkable)
  },

  /**
   * Set if a block is liquid.
   * @param {number | string} key ID or name.
   * @param {boolean} isLiquid 
   */
  setLiquid: function(key, isLiquid) {
    jvmBlockController.setLiquid(key, isLiquid)
  },

  /**
   * Set the flow level of a block.
   * @param {number | string} key ID or name. 
   * @param {number} flowLevel Higher level makes liquid flow faster.
   */
  setFlow: function(key, flowLevel) {
    jvmBlockController.setFlow(key, flowLevel)
  },

  /**
   * Set the viscosity of a block.
   * @param {number | string} key ID or name.
   * @param {number} newViscosity Higher viscosity makes liquid harder to move through.
   */
  setViscosity: function(key, newViscosity) {
    jvmBlockController.setViscosity(key, newViscosity)
  },

  /**
   * Set if a block is climbable.
   * @param {number | string} key ID or name.
   * @param {number} isClimbable 
   */
  setClimbable: function(key, isClimbable) {
    jvmBlockController.setClimbable(key, isClimbable)
  },

  /**
   * Set if a block is sneak jump climbable.
   * @param {number | string} key ID or name.
   * @param {number} isSneakJumpClimbable 
   */
  setSneakJumpClimbable: function(key, isSneakJumpClimbable) {
    jvmBlockController.setSneakJumpClimbable(key, isSneakJumpClimbable)
  },

  /**
   * Set if a block is a falling block.
   * @param {number | string} key ID or name.
   * @param {boolean} isFalling 
   */
  setFalling: function(key, isFalling) {
    jvmBlockController.setFalling(key, isFalling)
  },

  /**
   * Set if a block is clear.
   * @param {number | string} key 
   * @param {boolean} isClear 
   */
  setClear: function(key, isClear) {
    jvmBlockController.setClear(key, isClear)
  },

  /**
   * Set a blocks damage per second.
   * @param {number | string} key ID or name.
   * @param {number} dps Damage Per Second.
   */
  setDamagePerSecond: function(key, dps) {
    jvmBlockController.setDamagePerSecond(key, dps)
  },

  /**
   * Set a blocks light level.
   * @param {number | string} key ID or name.
   * @param {number} newLight 
   */
  setLight: function(key, newLight) {
    jvmBlockController.setLight(key, newLight)
  },

  /**
   * Get a block ID from name.
   * @param {string} name 
   * @returns {number} 
   */
  getID: function(name) {
    return jvmBlockController.getID(name)
  },

  /**
   * Get a block name from ID.
   * @param {number} id
   * @returns {string}
   */
  getName: function(id) {
    return jvmBlockController.getName(id)
  },

  /**
   * Get a blocks inventory name.
   * @param {number | string} key ID or name.
   * @returns {string}
   */
  getInventoryName: function(key) {
    return jvmBlockController.getInventoryName(key)
  },

  /**
   * Get a blocks texture array.
   * @param {number | string} key ID or name.
   * @returns {Array<string>}
   */
  getTextures: function(key) {
    return jvmBlockController.getTextures(key)
  },

  /**
   * Get a blocks drawtype.
   * @param {number | string} key ID or name.
   * @returns {DrawType}
   */
  getDrawType: function(key) {
    return jvmBlockController.getDrawType(key)
  },

  /**
   * Get if a block is walkable.
   * @param {number | string} key ID or name.
   * @returns {boolean}
   */
  isWalkable: function(key) {
    return jvmBlockController.isWalkable(key)
  },

  /**
   * Get if a block is a liquid.
   * @param {number | string} key ID or name. 
   * @returns {boolean}
   */
  isLiquid: function(key) {
    return jvmBlockController.isLiquid(key)
  },

  /**
   * Get the flow level of a block. Higher flows faster.
   * @param {number | string} key ID or name. 
   * @returns {number}
   */
  getFlow: function(key) {
    return jvmBlockController.getFlow(key)
  },

  /**
   * Get the viscosity of a block. Higher is harder to move through.
   * @param {number | string} key ID or name.
   * @returns {number}
   */
  getViscosity: function(key) {
    return jvmBlockController.getViscosity(key)
  },

  /**
   * Get if a block is normally climbable.
   * @param {number | string} key ID or name.
   * @returns {boolean}
   */
  isClimbable: function(key) {
    return jvmBlockController.isClimbable(key)
  },

  /**
   * Get if a block is climbable with sneak and jump.
   * @param {number | string} key ID or name.
   * @returns {boolean}
   */
  isSneakJumpClimbable: function(key) {
    return jvmBlockController.isSneakJumpClimbable(key)
  },

  /**
   * Get if a block is affected by gravity.
   * @param {number | string} key ID or name.
   * @returns {boolean}
   */
  isFalling: function(key) {
    return jvmBlockController.isFalling(key)
  },

  /**
   * Get if light passes through a block.
   * @param {number | string} key ID or name.
   * @returns {boolean}
   */
  isClear: function(key) {
    return jvmBlockController.isClear(key)
  },

  /**
   * Get how much a block damages entities per second.
   * @param {number | string} key ID or name.
   * @returns {number}
   */
  getDamagePerSecond: function(key) {
    return jvmBlockController.getDamagePerSecond(key)
  },

  /**
   * Get the light level of a block.
   * @param {number | string} key ID or name.
   * @returns {number}
   */
  getLight: function(key) {
    return jvmBlockController.getLight(key)
  },






};


// Air is automatically registered here.
block.register({
  name: "air",
  inventoryName: "air",
  textures: ["air.png","air.png","air.png","air.png","air.png","air.png"],
  drawtype: DrawType.AIR
})