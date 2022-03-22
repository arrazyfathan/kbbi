<h1 align="center">KBBI</h1>

<p align="center">  
KBBI, aplikasi unofficial kamus besar bahasa indonesia.
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=21"><img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat"/></a>
</p>

<p align="center">
<img src="media/Final.png"/>
</p>

## Download
Go to the [Releases](https://github.com/arrazyfathan/kbbi/releases/download/1.0/app-release.apk) to download the latest APK.

<img src="/media/preview.gif" align="right" width="32%"/>

## Tech stack & Open-source libraries
- Minimum SDK level 21
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- Jetpack
    - Lifecycle - dispose of observing data when lifecycle state changes.
    - ViewModel - UI related data holder, lifecycle aware.
    - Room Persistence - construct the database using the abstract layer.
- Architecture
    - MVVM Architecture
    - Repository pattern
- [Material-Components](https://github.com/material-components/material-components-android) - Material design components for building ripple animation, and CardView.

## API by [@btrianurdin](https://github.com/btrianurdin)
```
https://new-kbbi-api.herokuapp.com
``` 

## MAD Score
![summary](media/summary.png "Summary")
![kotlin](media/kotlin.png "Kotlin")
![jetpack](media/jetpack.png "Jetpack")

# License
```xml
Designed and developed by 2022 arrazyfathan (Ar Razy Fathan Rabbani)

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