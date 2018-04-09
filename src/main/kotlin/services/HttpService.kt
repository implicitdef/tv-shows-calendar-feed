package services

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import org.funktionale.either.Either.Left
import org.funktionale.either.Either.Right
import utils.CS
import utils.E
import utils.Utils
import utils.Utils.moshi
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

object HttpService {

    object NotFound

    fun <T : Any> httpGetThrottled(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<E<NotFound, T>> =
        ThrottlingService.withThrottling {
            httpGet(url, kClass, *params)
        }

    private fun <T : Any> httpGet(
        url: String,
        kClass: KClass<T>,
        vararg params: Pair<String, String>
    ): CS<E<NotFound, T>> =
        httpGetAsString(url, *params).thenApply { either ->
            either.right().map { str ->
                val obj = moshi.adapter(kClass.java).fromJson(str)
                if (obj === null) {
                    throw IllegalStateException("Got null from JSON parsing : $str")
                } else obj
            }
        }

    // returns NotFound if 404, but fails on all other exceptions
    private fun httpGetAsString(
        url: String,
        vararg params: Pair<String, String>
    ): CS<E<NotFound, String>> {
        val future = CompletableFuture<E<NotFound, String>>()
        Utils.log(">> GET $url")
        Utils.threadPool.submit {
            Fuel.Companion
                .get(url, params.toList())
                .responseString { request, response, result ->
                    Utils.log("<< ${response.statusCode}")
                    when (result) {
                        is Result.Failure -> {
                            if (response.statusCode == 404)
                                future.complete(Left(NotFound))
                            else {
                                val e = RuntimeException("Failed to get ${request.url}", result.getException())
                                future.completeExceptionally(e)
                            }
                        }
                        is Result.Success -> {
                            future.complete(Right(result.get()))
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
