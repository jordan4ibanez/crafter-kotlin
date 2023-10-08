package engine

import java.io.File
import javax.xml.stream.Location

//note: A few file helper functions.

fun getFile(location: String): File {
  val file = File(location)
  if (!file.exists()) throw RuntimeException("getFile: $location does not exist.")
  return file
}
fun String.getFile(): File = getFile(this)

fun getFolder(location: String): File {
  val folder = File(location)
  if (!folder.isDirectory) throw RuntimeException("getFolder: $location is not a directory.")
  return folder
}
fun String.getFolder(): File = getFolder(this)

fun getFileString(location: String): String {
  return getFile(location).readText()
}
fun String.getFileString(): String = getFileString(this)

fun getFolderList(folderLocation: String): Array<String> {
  val folder = getFolder(folderLocation)
  return folder.list { currentFolder, name ->
    File(currentFolder, name).isDirectory
  }!!
}
fun String.getFolderList(): Array<String> = getFolderList(this)

fun getFileList(folderLocation: String): Array<String> {
  val folder = getFolder(folderLocation)
  return folder.list { currentFile, name ->
    File(currentFile, name).isFile
  }!!
}
fun String.getFileList(): Array<String> = getFileList(this)

fun isFolder(folderLocation: String): Boolean {
  return File(folderLocation).isDirectory
}
fun String.isFolder(): Boolean = isFolder(this)

fun isFile(fileLocation: String): Boolean {
  return File(fileLocation).isFile
}
fun String.isFile(): Boolean = isFile(this)

fun makeFolder(folderLocation: String): Boolean {
  return File(folderLocation).mkdir()
}
fun String.makeFolder(): Boolean = makeFolder(this)

fun makeFile(fileLocation: String): File {
  return File(fileLocation)
}
fun String.makeFile(): File = makeFile(this)
