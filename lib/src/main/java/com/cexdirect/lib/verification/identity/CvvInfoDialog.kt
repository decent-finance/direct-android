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
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.databinding.DialogCvvInfoBinding
import javax.inject.Inject

class CvvInfoDialog : BaseBottomSheetDialog() {

    @Inject
    lateinit var factory: CvvInfoDialogViewModel.Factory

    private val model by fragmentViewModelProvider<CvvInfoDialogViewModel> { factory }

    private lateinit var binding: DialogCvvInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        DataBindingUtil.inflate<DialogCvvInfoBinding>(
            inflater,
            R.layout.dialog_cvv_info,
            container,
            false
        ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        binding.model = model
        model.closeEvent.observe(viewLifecycleOwner, Observer { dismiss() })
    }
}

class CvvInfoDialogViewModel :
    BaseObservableViewModel() {

    val closeEvent = VoidLiveEvent()

    fun close() {
        closeEvent.call()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = CvvInfoDialogViewModel() as T
    }
}
