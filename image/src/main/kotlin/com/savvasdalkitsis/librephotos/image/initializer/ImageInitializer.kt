package com.savvasdalkitsis.librephotos.image.initializer

import coil.Coil
import coil.ImageLoader
import com.savvasdalkitsis.librephotos.initializer.ApplicationCreated
import javax.inject.Inject

class ImageInitializer @Inject constructor(
    private val imageLoader: ImageLoader,
) : ApplicationCreated {

    override fun onAppCreated() {
        Coil.setImageLoader(imageLoader)
    }
}