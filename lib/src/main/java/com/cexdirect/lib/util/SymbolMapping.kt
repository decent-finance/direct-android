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

package com.cexdirect.lib.util

import com.cexdirect.lib.R

data class Symbol(
    val fullName: String,
    val iconId: Int = R.drawable.ic_coin_default,
    val transactionBrowserAddress: String? = null
)

val symbolMap = mapOf(
    "USD" to Symbol("United States Dollar", R.drawable.ic_coin_usd),
    "EUR" to Symbol("Euro", R.drawable.ic_coin_eur),
    "GBP" to Symbol("Pound Sterling", R.drawable.ic_coin_gbp),
    "RUB" to Symbol("Ruble", R.drawable.ic_coin_rub),
    "XRP" to Symbol("Ripple", R.drawable.ic_coin_xrp, "https://xrpl.org/xrp-ledger-rpc-tool.html#"),
    "BTC" to Symbol("Bitcoin", R.drawable.ic_coin_btc, "https://www.blockchain.com/btc/tx/"),
    "BCH" to Symbol(
        "Bitcoin Cash",
        R.drawable.ic_coin_bch,
        "https://blockchair.com/bitcoin-cash/transaction/"
    ),
    "ZEC" to Symbol("Zcash", R.drawable.ic_coin_zec, "https://explorer.zcha.in/transactions/"),
    "BTG" to Symbol("Bitcoin Gold", R.drawable.ic_coin_btg, "https://btgexplorer.com/tx/"),
    "XLM" to Symbol("Stellar", R.drawable.ic_coin_xlm, "https://stellarchain.io/tx/"),
    "ETH" to Symbol("Ethereum", R.drawable.ic_coin_eth, "https://etherscan.io/tx/"),
    "LTC" to Symbol("Litecoin", R.drawable.ic_coin_ltc, "https://live.blockcypher.com/ltc/tx/"),
    "BSV" to Symbol("BSV"),
    "OMG" to Symbol("OmiseGO"),
    "BAT" to Symbol("Basic Attention Token"),
    "MHC" to Symbol("#MetaHash", R.drawable.ic_coin_mhc, "http://venus.mhscan.com/?page=tx&id="),
    "BTT" to Symbol("BitTorrent", R.drawable.ic_coin_btt, "https://tronscan.org/#/transaction/"),
    "TRX" to Symbol("Tron", R.drawable.ic_coin_trx, "https://tronscan.org/#/transaction/"),
    "DASH" to Symbol("Dash", R.drawable.ic_coin_dash, "https://live.blockcypher.com/dash/tx/"),
    "QASH" to Symbol("QASH", R.drawable.ic_coin_qash),
    "GUSD" to Symbol("Gemini Dollar", R.drawable.ic_coin_gemini, "https://etherscan.io/tx/")
)

fun Symbol?.orDefault() = this ?: Symbol("")
