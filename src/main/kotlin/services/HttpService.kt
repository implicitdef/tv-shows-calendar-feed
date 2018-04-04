package services

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import utils.CS
import utils.Utils
import utils.Utils.moshi
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

object HttpService {

    fun <T : Any> httpGetThrottled(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<T> =
        ThrottlingService.withThrottling {
            httpGet(url, kClass, *params)
        }

    private fun <T : Any> httpGet(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<T> =
        httpGetAsString(url, *params).thenApply { str ->
            val obj = moshi.adapter(kClass.java).fromJson(str)
            if (obj === null) {
                throw IllegalStateException("Got null from JSON parsing : $str")
            }
            obj
        }

    private fun httpGetAsString(
        url: String,
        vararg params: Pair<String, String>
    ): CS<String> {
        val future = CompletableFuture<String>()
        Utils.log(">> GET $url")
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

    fun httpPost(
        url: String,
        body: String,
        vararg params: Pair<String, String>
    ): CS<Unit> {
        val future = CompletableFuture<Unit>()
        Utils.log(">> POST $url")
        // Fuel's post() send the params in the body instead of the URL
        val paramsString = params.joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, "UTF-8")}"
        }
        val urlWithParams = listOf(url, paramsString).joinToString("?")
        Utils.threadPool.submit {
            Fuel.Companion
                .post(urlWithParams)
                .body(body)
                .responseString { _, response, result ->
                    Utils.log("<< ${response.statusCode}")
                    when (result) {
                        is Result.Failure -> {
                            future.completeExceptionally(result.getException())
                        }
                        is Result.Success -> {
                            future.complete(Unit)
                        }
                    }
                }
        }
        return future
    }
}
