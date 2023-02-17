import java.io.File
import java.io.OutputStreamWriter

fun File.getEnvValue(key: String): String? {
    val envMap = this.readLines().associate { Pair(it.split("=")[0], it.split("=")[1]) }
    println("PRINTING ENV MAP")
    envMap.forEach { s, s2 ->
        println("$s -> $s2")
    }
    return envMap[key]
}

fun File.setEnvValue(key: String, value: String) {
    this.writer().let {
        it.write("$key=$value")
        it.write("\n")
        it.close()
    }
}

fun OutputStreamWriter.writeEnvValue(key: String, value: String): OutputStreamWriter {
    write("$key=$value")
    write("\n")
    return this
}

fun File.setEnvValues(list: List<Pair<String, String>>) {
    this.writer().let {
        list.forEach { (key, value) ->
            it.write("$key=$value")
            it.write("\n")
        }
        it.close()
    }
}
