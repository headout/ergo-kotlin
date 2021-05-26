package headout.oss.ergo.factory

import kotlinx.serialization.json.Json

/**
 * Created by shivanshs9 on 23/05/20.
 */
object JsonFactory {
    val json = Json {
        ignoreUnknownKeys = true
    }
}