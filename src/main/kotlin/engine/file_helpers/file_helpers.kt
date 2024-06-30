package engine.file_helpers

import utility.option.None
import utility.option.Option
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
 * @return A Result of trying to load the file into memory.
 */
fun getFile(location: String): Result<File> {
  return when (val fileOption = File(location).someFile()) {
    is Some -> Ok(fileOption.unwrap())
    else -> Err("getFile: $location does not exist.")
  }
}

/**
 * Get a folder from a location as a File.
 *
 * @param location the location of the folder.
 * @return A Result of trying to load the file into memory.
 */
fun getFolder(location: String): Result<File> {
  return with(File(location)) {
    when {
      this.isDirectory() -> Ok(this)
      else -> Err("getFolder: $location is not a directory.")
    }
  }
}

/**
 * Get a string from a file
 *
 * @param location the location of the file.
 * @return A Result of trying to load the file into memory.
 */
fun getFileString(location: String): Result<String> {
  return when (val fileOption = getFile(location)) {
    is Ok -> Ok(fileOption.unwrap().readText())
    else -> Err("getFileString: $location does not exist.")
  }
}

/**
 * Get a list of folders in a directory.
 *
 * @param folderLocation The location of the folder.
 * @return A Result of trying to get the folders into a string array.
 */
fun getFolderList(folderLocation: String): Result<Array<String>> {
  return when (val folderOption = getFolder(folderLocation)) {
    is Ok -> {
      when (val folderArrayOption =
        undecided(folderOption.unwrap().list { currentFolder, name -> File(currentFolder, name).isDirectory })) {

        is Some -> Ok(folderArrayOption.unwrap())
        else -> Err("getFolderList: Invalid list filter applied.")
      }
    }

    else -> Err("getFolderList: $folderLocation does not exist.")
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

/**
 * Check if a File is a file utilizing Option. If it's not, will return None.
 *
 * @return A Result. Some if it's a file. None if it's not.
 */
fun File.someFile(): Option<File> {
  return when (this.isFile) {
    true -> Some(this)
    else -> None()
  }
}

/**
 * Check if a File is a directory utilizing Option. If it's not, will return None.
 *
 * @return A Result. Some if it's a directory. None if it's not.
 */
fun File.someFolder(): Option<File> {
  return when (this.isDirectory) {
    true -> Some(this)
    else -> None()
  }
}