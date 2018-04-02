package services

import utils.Utils.log
import java.io.File

object FileStockageService {

    val file = File("data.json")

    fun writeToFile(str: String) {
        log("Writing to ${file.absolutePath}...")
        file.writeText(str)
    }

    fun readFromFile(): String {
        log("Reading ${file.absolutePath}...")
        return file.readText()
    }

}