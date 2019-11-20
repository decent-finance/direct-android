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

package com.cexdirect.lib.order.confirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.cexdirect.lib.BaseBottomSheetDialog
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.StringLiveEvent
import com.cexdirect.lib.databinding.DialogChangeEmailBinding
import javax.inject.Inject

class ChangeEmailDialog : BaseBottomSheetDialog() {

    @Inject
    lateinit var emailChangedEvent: StringLiveEvent

    @Inject
    lateinit var factory: ChangeEmailDialogViewModel.Factory

    private val model by fragmentViewModelProvider<ChangeEmailDialogViewModel> { factory }

    private lateinit var binding: DialogChangeEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.EditEmailDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogChangeEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)

        binding.model = model

        model.saveEvent.observe(viewLifecycleOwner, Observer {
            emailChangedEvent.value = model.userEmail.email
            dismiss()
        })
        model.cancelEvent.observe(viewLifecycleOwner, Observer {
            dismiss()
        })
    }
}
