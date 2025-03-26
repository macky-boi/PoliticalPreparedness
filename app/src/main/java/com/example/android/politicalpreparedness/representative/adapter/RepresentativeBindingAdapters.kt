package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.android.politicalpreparedness.R
import timber.log.Timber

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    Timber.d("fetchImage | src: $src")
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        Timber.d("fetchImage | uri: $uri")
        //TODO: Add Glide call to load image and circle crop - user ic_profile as a placeholder and for errors.
        Glide.with(view.context)
            .load(uri)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(view)
    }
}

@BindingAdapter("stateValue")
fun Spinner.setNewValue(value: String?) {
    val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
    val position = when (adapter.getItem(0)) {
        is String -> adapter.getPosition(value)
        else -> this.selectedItemPosition
    }
    if (position >= 0) {
        setSelection(position)
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T>{
    return adapter as ArrayAdapter<T>
}
