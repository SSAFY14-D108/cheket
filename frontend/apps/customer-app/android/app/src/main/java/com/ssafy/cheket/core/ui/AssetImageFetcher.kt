package com.ssafy.cheket.core.ui

import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.buffer
import okio.source

/**
 * Coil Fetcher for file:///android_asset/ URIs.
 * AsyncImage(model = "file:///android_asset/posters/aespa.webp") 형태로 사용 가능.
 */
class AssetImageFetcher(
    private val assetPath: String,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val stream = options.context.assets.open(assetPath)
        return SourceResult(
            source = ImageSource(
                source = stream.source().buffer(),
                context = options.context,
            ),
            mimeType = null,
            dataSource = DataSource.DISK,
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme == "file" && data.path?.startsWith("/android_asset/") == true) {
                val assetPath = data.path!!.removePrefix("/android_asset/")
                return AssetImageFetcher(assetPath, options)
            }
            return null
        }
    }
}
