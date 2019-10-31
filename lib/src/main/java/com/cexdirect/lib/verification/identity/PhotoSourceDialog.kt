/*
 * Copyright 2019 CEX.​IO Ltd (UK)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.cexdirect.lib.verification.identity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseBottomSheetDialog
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.DialogPhotoSourceBinding
import com.cexdirect.lib.di.annotation.PhotoSourceDialogFactory
import com.cexdirect.lib.verification.events.SourceClickEvent
import javax.inject.Inject

class PhotoSourceDialog : BaseBottomSheetDialog() {

    @field:[Inject PhotoSourceDialogFactory]
    lateinit var factory: ViewModelProvider.Factory

    val model by fragmentViewModelProvider<PhotoSourceDialogViewModel> { factory }

    @Inject
    lateinit var event: SourceClickEvent

    lateinit var binding: DialogPhotoSourceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPhotoSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        binding.model = model
        model.sourceClickEvent.observe(this, Observer {
            event.value = it
            dismiss()
        })
    }
}

class PhotoSourceDialogViewModel : BaseObservableViewModel() {

    val sourceClickEvent = SourceClickEvent()

    fun selectSource(type: SourceClickType) {
        sourceClickEvent.value = type
    }

    class Factory : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            PhotoSourceDialogViewModel() as T
    }
}

enum class SourceClickType {
    PHOTO, CAMERA, CANCEL
}
