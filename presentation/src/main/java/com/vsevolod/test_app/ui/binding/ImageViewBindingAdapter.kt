package com.vsevolod.test_app.ui.binding

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.jetbrains.anko.image


@BindingAdapter(
        value = [
            "url",
            "circle",
            "placeholder"
        ],
        requireAll = false
)
fun ImageView.setImage(url: String?, circle: Boolean, placeholder: Drawable?) {

    image = null

    Glide.with(context)
            .load(url)
            .let { builder ->

                RequestOptions().let { requestOptions ->

                    placeholder?.let(requestOptions::placeholder)

                    if (circle) {
                        requestOptions.circleCrop()
                    }

                    builder.apply(requestOptions)
                }

            }
            .into(this)
}
