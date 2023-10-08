package engine

import java.io.File
import javax.xml.stream.Location

//note: A few file helper functions.

fun getFile(location: String): File {
  val file = File(location)
  if (!file.exists()) throw RuntimeException("getFile: $location does not exist.")
  return file
}

fun getFolder(location: String): File {
  val folder = File(location)
  if (!folder.isDirectory) throw RuntimeException("getFolder: $location is not a directory.")
  return folder
}

fun getFileString(location: String): String {
  return getFile(location).readText()
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
