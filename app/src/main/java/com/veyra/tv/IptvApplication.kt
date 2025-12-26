package com.veyra.tv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

class IptvApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of available memory for image caching
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of disk space
                    .build()
            }
            .crossfade(true) // Smooth transitions
            .respectCacheHeaders(false) // Cache images regardless of headers (aggressive caching)
            .build()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clear memory caches when the system is under memory pressure
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                // App went to background - free up some UI resources if needed
            }
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // App is running but system is low on memory
                imageLoader.memoryCache?.clear()
            }
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // App is in background and system needs memory
                imageLoader.memoryCache?.clear()
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        imageLoader.memoryCache?.clear()
    }
}
