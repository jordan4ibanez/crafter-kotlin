package engine.file_helpers

import utility.option.Some
import utility.option.undecided
import utility.result.Err
import utility.result.Ok
import utility.result.Result
import java.io.File


/**
 * Get a file from a location as a File.
 *
 * @param location The location of the file.
 * @return Result<File> A Result of trying to load the file into memory.
 */
fun getFile(location: String): Result<File> {
  return with(File(location)) {
    when (this.exists() && this.isFile()) {
      true -> Ok(this)
      false -> Err("getFile: $location does not exist.")
    }
  }
}

/**
 * Get a folder from a location as a File.
 *
 * @param location the location of the folder.
 * @return Result<File> A Result of trying to load the file into memory.
 */
fun getFolder(location: String): Result<File> {
  return with(File(location)) {
    when (this.exists() && this.isDirectory()) {
      true -> Ok(this)
      false -> Err("getFolder: $location is not a directory.")
    }
  }
}

/**
 * Get a string from a file
 *
 * @param location the location of the file.
 * @return Result<String> A Result of trying to load the file into memory.
 */
fun getFileString(location: String): Result<String> {
  return when (val fileOption = getFile(location)) {
    is Ok -> Ok(fileOption.unwrap().readText())
    else -> Err("getFileString: $location does not exist.")
  }
}

fun getFolderList(folderLocation: String): Result<Array<String>> {
  return with(getFolder(folderLocation)) {
    when (this) {
      is Ok -> {
        with(undecided(this.unwrap().list { currentFolder, name -> File(currentFolder, name).isDirectory })) {
          when (this) {
            is Some -> Ok(this.unwrap())
            else -> Err("getFolderList: Invalid list filter applied.")
          }
        }
      }

      else -> Err("getFolderList: $folderLocation does not exist.")
    }
  }
}

fun getFileList(folderLocation: String): Array<String> {
  val folder = getFolder(folderLocation)
  return folder.list { currentFile, name ->
    File(currentFile, name).isFile
  }!!
}

fun isFolder(folderLocation: String): Boolean {
  return File(folderLocation).isDirectory
}

fun isFile(fileLocation: String): Boolean {
  return File(fileLocation).isFile
}

fun makeFolder(folderLocation: String): Boolean {
  return File(folderLocation).mkdir()
}

fun makeFile(fileLocation: String): File {
  return File(fileLocation)
}
