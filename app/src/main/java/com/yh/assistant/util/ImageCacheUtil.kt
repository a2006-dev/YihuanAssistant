package com.yh.assistant.util
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.fetch.Fetcher
import coil.memory.MemoryCache
import coil.request.Options
import java.io.File
import java.io.InputStream
object ImageCacheUtil {
    private const val CACHE_DIR = "images"
    private const val MAX_SIZE = 50L * 1024 * 1024
    fun getImageLoader(ctx: Context): ImageLoader {
        return ImageLoader.Builder(ctx)
            .memoryCache {
                MemoryCache.Builder(ctx)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(ctx.cacheDir, CACHE_DIR))
                    .maxSizeBytes(MAX_SIZE)
                    .build()
            }
            .crossfade(300)
            .build()
    }
}
class CacheImageLoaderFactory(private val ctx: Context) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader = ImageCacheUtil.getImageLoader(ctx)
}