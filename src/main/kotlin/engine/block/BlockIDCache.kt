package engine.block

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import engine.file_helpers.*
import utility.result.Ok

/**
 * This object's soul purpose is to parse and map the block_cache.json file for the BlockDefinitionContainer to get
 * existing block values, or to assign new block values!
 * IDs are stored numerically, 0,1,2,3,4, etc. If there is a gap then there is a problem!
 *!FIXME: This could be slow if there are thousands of blocks, consider using a database!
 */
internal object BlockIDCache {

  private val nameToIDMap = HashMap<String, Int>()

  // 0 is reserved for air.
  private var nextFreeSlot = 1


  fun initialize() {
    folderCheck()
    fileCheck()
  }

  fun assign(name: String): Int {
    if (name.lowercase() == "air" && !nameToIDMap.containsKey("air")) {
      nameToIDMap[name.lowercase()] = 0
    }
    return getID(name)
  }

  private fun folderCheck() {
    if (!cacheFolder.isFolder()) {
      println("blockIDCache: Creating cache folder.")
      // If this fails, we need this to crash.
      cacheFolder.makeFolder().unwrap()
    } else {
      println("blockIDCache: Cache folder exists.")
    }
  }

  private fun fileCheck() {
    if (cacheFile.isFile()) {
      println("blockIDCache: Parsing cache file.")
      try {
        processJSONNodes(ObjectMapper().readTree(getFileString(cacheFile).unwrap()))
      } catch (e: Exception) {
        throw RuntimeException("blockIDCache: $e")
      }
    }
  }

  private fun processJSONNodes(nodes: JsonNode) {
    // Crawl the JSON tree.

    val keys: MutableIterator<String> =
      nodes.fieldNames() ?: throw RuntimeException("blockIDCache: Failed to get JSON keys.")

    val test: MutableIterator<JsonNode> =
      nodes.elements() ?: throw RuntimeException("blockIDCache: Failed to get JSON elements.")

    keys.asSequence().zip(test.asSequence()).forEach {
      val (key, value) = it

      val type = value.nodeType

      if (type != JsonNodeType.NUMBER) {
        throw RuntimeException("blockIDCache: $key was type $type. Did you modify the cache?")
      }

      duplicateCheck(key)

      val rawValue = value.asDouble()
      val numericValue = value.asInt()

      if ((rawValue * 100.0).toInt() != (numericValue * 100)) {
        throw RuntimeException("blockIDCache: $key was floating. Did you modify the cache?")
      }

      // Automatically tick up the free slot for the next numeric value.
      //! fixme: testme: this should automatically do this?
      //! or, this should be ticked up to the next val with a gap check.
      //! or, this should test the gotten value with the expected next value.
      //! this needs to be tested with a broken cache!
      if (numericValue >= nextFreeSlot) {
        nextFreeSlot = numericValue + 1
      }

      nameToIDMap[key] = numericValue
    }
  }

  private fun getID(name: String): Int {
    return nameToIDMap[name] ?: tickUp(name)
  }

  private fun tickUp(name: String): Int {
    val newID = nextFreeSlot
    nameToIDMap[name] = newID
    nextFreeSlot++
    return newID
  }

  private fun duplicateCheck(name: String) {
    if (nameToIDMap.contains(name)) {
      throw RuntimeException("blockIDCache: Duplicate detected. $name")
    }
  }

  fun write() {
    println("blockIDCache: Writing cache.")

    when (val path = cacheFile.makeOrGetFile()) {
      is Ok -> ObjectMapper().writeValue(path.unwrap().toFile(), nameToIDMap)
      else -> throw path.unwrapErr()
    }

    println("blockIDCache: Cache write successful.")
  }
}