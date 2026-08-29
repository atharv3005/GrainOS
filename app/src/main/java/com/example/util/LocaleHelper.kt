package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import java.util.Locale

class LocaleWrapper(base: Context, private val configContext: Context) : ContextWrapper(base) {
    
    override fun getResources(): Resources = configContext.resources
    
    override fun getSystemService(name: String): Any? {
        if (Context.LAYOUT_INFLATER_SERVICE == name) {
            val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return inflater.cloneInContext(this)
        }
        return super.getSystemService(name)
    }
}

object LocaleHelper {
    fun getLocalizedContextWrapper(activityContext: Context, languageTag: String): Context {
        try {
            val locale = Locale(languageTag)
            Locale.setDefault(locale)
            val config = Configuration(activityContext.resources.configuration)
            config.setLocale(locale)
            val configContext = activityContext.createConfigurationContext(config)
            return LocaleWrapper(activityContext, configContext)
        } catch (e: Exception) {
            return activityContext
        }
    }
}
