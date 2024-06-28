package engine.file_helpers

import utility.result.Err
import utility.result.Ok
import utility.result.Result
import java.io.File


fun getFile(location: String): Result<File> {
  with(File(location)) {
    return when (this.exists()) {
      true -> Ok(this)
      false -> Err("getFile: $location does not exist.")
    }
  }
}

fun getFolder(location: String): File {
  val folder = File(location)
  if (!folder.isDirectory) throw RuntimeException("getFolder: $location is not a directory.")
  return folder
}

fun getFileString(location: String): String {
  return getFile(location).unwrap().readText()
}

fun getFolderList(folderLocation: String): Array<String> {
  val folder = getFolder(folderLocation)
  return folder.list { currentFolder, name ->
    File(currentFolder, name).isDirectory
  }!!
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
