  // // Javascript level Biome Definition registration function.
  // // This is why I avoid singletons, cannot reduce this.
  // crafter.registerBiome = function(newBiomeDefinition) {
  //     BiomeDefinitionContainer.getMainInstance().registerBiome(newBiomeDefinition)
  // }

  // //FIXME!!! THIS NEEDS TO BE FLESHED OUT!
  // //FIXME!!! INCLUDE ALL THE METHODS FROM THE BLOCK DEFINITIONS!
  // blockDefinition.getID = function(input) {
  //     return BlockDefinitionContainer.getMainInstance().getDefinition(input).getID()
  // }

  // // Will get the mod directory of the current mod.
  // // Returns: String
  // crafter.getCurrentModDirectory = API.getCurrentModDirectory

  // // Gets if a chunk is loaded in a position in the world.
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: boolean
  // crafter.isChunkLoaded = ChunkStorage.isChunkLoaded

  // // Gets an iterable collection of all players currently online.
  // // Returns: Collection<Player>
  // crafter.getConnectedPlayers = PlayerStorage.getConnectedPlayers

  // //fixme ----- BEGIN SINGLE BLOCK API -----

  // // Get a block's RAW data using a raw in world position. (Using this in bulk can be very expensive)
  // // ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING!
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: integer
  // crafter.getBlockRAW = ChunkStorage.getBlockRAW

  // // Get a block's internal name using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: String
  // crafter.getBlockName = ChunkStorage.getBlockName

  // // Get a block's ID using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: integer
  // crafter.getBlockID = ChunkStorage.getBlockID

  // // Get a block's light level using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: integer
  // crafter.getBlockLightLevel = ChunkStorage.getBlockLightLevel

  // // Get a block's state using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f]
  // // Returns: integer
  // crafter.getBlockState = ChunkStorage.getBlockState

  // // Set a block's RAW data using a raw in world position. (Using this in bulk can be very expensive)
  // // ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING!
  // // Parameters: [x,y,z or Vector3f] [integer]
  // crafter.setBlockRAW = ChunkStorage.setBlockRAW

  // // Set a block's ID with the internal name of the block using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f] [String]
  // crafter.setBlockName = ChunkStorage.setBlockName

  // // Set a block's ID using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f] [integer]
  // crafter.setBlockID = ChunkStorage.setBlockID

  // // Set a block's light level using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f] [integer]
  // crafter.setBlockLightLevel = ChunkStorage.setBlockLightLevel

  // // Set a block's state using a raw in world position. (Using this in bulk can be very expensive)
  // // Parameters: [x,y,z or Vector3f] [integer]
  // crafter.setBlockState = ChunkStorage.setBlockState

  // //fixme ----- BEGIN ACTIONS API -----

  // // Run some logic when a player joins the game.
  // // Parameters: [OnJoin]
  // crafter.registerOnJoin = ActionStorage.registerOnJoin

  // // Run some logic every game tick.
  // // Parameters: [OnTick]
  // crafter.registerOnTick = ActionStorage.registerOnTick

  // // Run some logic at X second intervals. Or, execute it after X seconds if repeat is off.
  // // Parameters:
  // // [float] interval OR delay. (depends if repeat is on)
  // // [boolean] Repeat. If true, this function will run every X seconds. If false, it will run once, then be deleted.
  // // [OnTimer] OnTimer function.
  // crafter.registerOnTimer = ActionStorage.registerOnTimer

  // //fixme ---- BEGIN BLOCK MANIPULATOR API ----

  // // Set the min and max positions of the Block Manipulator.
  // // Parameters: [Vector3i] [Vector3i] OR  min: [int] [int] [int] max: [int] [int] [int]
  // blockManipulator.setPositions = ChunkStorage.setBlockManipulatorPositions

  // // Make the block manipulator read the map.
  // blockManipulator.readData = ChunkStorage.blockManipulatorReadData

  // // Get RAW data from the Block Manipulator.
  // // Parameters: [Vector3i] OR [int] [int] [int]
  // blockManipulator.getData = ChunkStorage.getBlockManipulatorData

  // // Set RAW data into the Block Manipulator.
  // // Parameters: [Vector3i] [int] OR [int] [int] [int] [int]
  // blockManipulator.setData = ChunkStorage.setBlockManipulatorData

  // // Write the Block Manipulator's data into the map.
  // blockManipulator.writeData = ChunkStorage.writeManipulatorWriteData

  // //fixme ----- BEGIN BLOCK DATA MANIPULATOR API -----

  // // Get the Block ID from raw Block data.
  // // Parameters: [int] Raw block data.
  // // Returns: [int] Block ID.
  // blockData.getID = BitManipulation.getBlockID

  // print("COOL TEST = " + blockData.getID)

  // // Get the Block light level from raw Block data.
  // // Parameters: [int] Raw block data.
  // // Returns: [int] Block light level.
  // blockData.getLightLevel = BitManipulation.getBlockLightLevel

  // // Get the Block state from raw Block data.
  // // Parameters: [int] Raw block data.
  // // Returns: [int] Block state.
  // blockData.getState = BitManipulation.getBlockState

  // // Set the Block ID into raw Block data.
  // // Parameters: [int] Raw data [int] New Block ID.
  // // Returns: [int] Manipulated raw Block data.
  // blockData.setID = BitManipulation.setBlockID

  // // Set the Block light level into raw Block data.
  // // Parameters: [int] Raw data [int] New Block light level.
  // // Returns: [int] Manipulated raw Block data.
  // blockData.setLightLevel = BitManipulation.setBlockLightLevel

  // // Set the Block state into raw Block data.
  // // Parameters: [int] Raw data [int] New Block state.
  // // Returns: [int] Manipulated raw Block data.
  // blockData.setState = BitManipulation.setBlockState