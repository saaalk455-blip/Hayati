package com.hayati.app

import android.app.Application

class HayatiApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
