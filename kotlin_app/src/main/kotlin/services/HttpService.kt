package services

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import utils.CS
import utils.Utils
import utils.Utils.moshi
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

object HttpService {

    fun <T : Any> httpCallThrottled(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<T> =
        ThrottlingService.withThrottling {
            httpCall(url, kClass, *params)
        }

    fun <T : Any> httpCall(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<T> =
        httpCallAsString(url, *params).thenApply { str ->
            val obj = moshi.adapter(kClass.java).fromJson(str)
            if (obj === null) {
                throw IllegalStateException("Got null from JSON parsing : $str")
            }
            obj
        }

    private fun httpCallAsString(
        url: String,
        vararg params: Pair<String, String>
    ): CS<String> {
        val future = CompletableFuture<String>()
        Utils.log(">> $url")
        Utils.threadPool.submit {
            Fuel.Companion
                .get(url, params.toList())
                .responseString { _, response, result ->
                    Utils.log("<< ${response.statusCode}")
                    when (result) {
                        is Result.Failure -> {
                            future.completeExceptionally(result.getException())
                        }
                        is Result.Success -> {

                            future.complete(result.get())
                        }
                    }
                }
        }
        return future
    }
}
