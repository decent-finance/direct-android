# CEX Direct Client Library

![CircleCI](https://img.shields.io/circleci/build/github/decent-finance/direct-android?token=b6789cb625d20c0f00cd98564e95a2bb2525f811) ![GitHub](https://img.shields.io/github/license/decent-finance/direct-android) [ ![Download](https://api.bintray.com/packages/decent-finance/direct-android/com.cexdirect.lib/images/download.svg) ](https://bintray.com/decent-finance/direct-android/com.cexdirect.lib/_latestVersion) [![Maintainability](https://api.codeclimate.com/v1/badges/e3b58b568f4cc06e7e8b/maintainability)](https://codeclimate.com/github/decent-finance/direct-android/maintainability) [![Gitter](https://badges.gitter.im/decent-finance/community.svg)](https://gitter.im/decent-finance/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

## Before you start

Please note, that this library requires `minSdkVersion` to be at least `21`. This library also requires `AndroidX`.

## Installation

Add the following repository to your `build.gradle`

```gradle
maven {	url  'https://dl.bintray.com/decent-finance/direct-android' }
```

Also add these repositories so that required dendencies are properly resolved

```gradle
maven { url 'https://jitpack.io' }
maven { url 'https://dl.bintray.com/shopify/shopify-android' }
```

Add the following dependency to your module-level `buld.gradle`

```
implementation "com.cexdirect:lib:$someVersion"
```

Available versions are listed [here](https://bintray.com/beta/#/decent-finance/direct-android/com.cexdirect.lib?tab=overview).

You'll also need to enable Data Binding for your project
```gradle
android {
    // ...

    dataBinding {
        enabled true
    }

    // ...
}
```

## Setup

In your `Application` class place the following code

```kotlin
class SomeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // ...
        // Other code
        // ...
        Direct.credentials = Credentials("your_placement_id", "your_placement_secret")
    }
}
```

To start Direct call
```kotlin
Direct.startDirect()
```
somewhere in your app. Please note, that library currenly checks that provided credentials are not blank and will throw `Exception` in case this check fails. This behavior may change in future releases. 

If you're using Java, please refer to this [sample project](https://github.com/decent-finance/direct-android-sample).

## Dependencies

This project depends on several popular 3rd-party libraries like `OkHttp` and `Retrofit`. Direct uses `AndroidX` packages and `Dagger`. These dependencies are usually updated to latest stable versions before releasing new versions of this library.

## Licence

```
   Copyright 2019 CEX.​IO Ltd (UK)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
