/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.verification.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.ActivityQrScannerBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import javax.inject.Inject

class QrScannerActivity : BaseActivity(), ZXingScannerView.ResultHandler {

    @Inject
    lateinit var factory: QrScannerActivityViewModel.Factory

    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var scannerView: ZXingScannerView

    private val model: QrScannerActivityViewModel by viewModelProvider { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_qr_scanner)
        scannerView = ZXingScannerView(this).apply {
            setFormats(listOf(BarcodeFormat.QR_CODE))
            setAutoFocus(true)
            setResultHandler(this@QrScannerActivity)
            setLaserEnabled(true)
            setBorderColor(ResourcesCompat.getColor(resources, R.color.white, context.theme))
        }
        binding.aqsContent.addView(scannerView, 0)

        model.apply {
            cancelEvent.observe(this@QrScannerActivity, Observer {
                setResult(Activity.RESULT_CANCELED)
                finish()
            })
        }.let { binding.model = it }
    }

    override fun onResume() {
        super.onResume()
        scannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        val intent = Intent().apply { putExtra("data", rawResult.text) }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
