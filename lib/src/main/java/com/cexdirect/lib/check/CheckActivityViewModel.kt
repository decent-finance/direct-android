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

package com.cexdirect.lib.check

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.util.PlacementValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class CheckActivityViewModel(
    private val placementApi: PlacementApi,
    private val placementValidator: PlacementValidator
) : BaseObservableViewModel() {

    val result: LiveData<Resource<Boolean>> = placementApi.checkResult

    fun loadPlacementData() {
        placementApi.loadPlacementData(this) {
            canLaunch(it.activityStatus, it.placementUris)
        }
    }

    private fun canLaunch(placementActive: Boolean, placementUris: List<String>) =
        placementActive && isUriAllowed(placementUris)

    private fun isUriAllowed(placementUris: List<String>) =
        placementUris.find { placementValidator.isPlacementUriAllowed(it) }?.isNotEmpty()
            ?: false

    class Factory(
        private val placementApi: PlacementApi,
        private val placementValidator: PlacementValidator
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CheckActivityViewModel(placementApi, placementValidator) as T
    }
}
