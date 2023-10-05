/*
 Crafter - A blocky game (engine) written in Java with LWJGL.
 Copyright (C) 2023  jordan4ibanez

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
TODO: Figure out more things like mobs & items & entities
TODO! VERY IMPORTANT! Add comments! See if there is a way to link the java documentation into javascript....somehow?
*/

/*
API is the base of the API which loads up class variables into the crafter array.
This allows for mods to index DIRECTLY into the engine without needing to be compiled.
Limitations: Ecmascript 6. Class architecture is not implemented into nashorn yet.
Possible implementations: Typescript (one day)
*/

// Global java types assignment.
//

const api = Java.type("engine.api").INSTANCE
const block = Java.type("engine.block").INSTANCE
const fileHelpers = Java.type("engine.File_helpersKt")

/**
 * A block DrawType enumerator.
 */
const DrawType = {
  AIR: Java.type("engine.DrawType").AIR,
  BLOCK: Java.type("engine.DrawType").BLOCK,
  BLOCK_BOX: Java.type("engine.DrawType").BLOCK_BOX,
  TORCH: Java.type("engine.DrawType").TORCH,
  LIQUID_SOURCE: Java.type("engine.DrawType").LIQUID_SOURCE,
  LIQUID_FLOR: Java.type("engine.DrawType").LIQUID_FLOW,
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

const crafter = {

  //!fixme: id needs to be dispatched internally.
  /**
   * Register a block into the game.
   * @param {number} id 
   * @param {string} name 
   * @param {string} inventoryName 
   * @param {Array<string>} textures 
   * @param {DrawType} drawtype 
   */
  registerBlock: function(id, name, inventoryName, textures, drawtype) {
    block.newBlock(id, name, inventoryName, textures, drawtype)
  }
}

crafter.registerBlock(0, "Air", "Air", ["air.png","air.png","air.png","air.png","air.png","air.png"], DrawType.AIR)
