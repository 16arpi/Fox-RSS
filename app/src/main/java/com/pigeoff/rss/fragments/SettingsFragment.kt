package com.pigeoff.rss.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.pigeoff.rss.R
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val developer: Preference = findPreference("libraries")!!
        developer.setOnPreferenceClickListener {
            val notices = Notices()
            notices.addNotice(Notice("Android Jetpack Library", "https://developer.android.com/jetpack", "", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Google Material Components", "https://material.io/develop/android", "", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Jsoup", "https://jsoup.org/", "Copyright 2009 - 2001 Jonathan Hedley", MITLicense()))
            notices.addNotice(Notice("RSS Parser", "https://github.com/prof18/RSS-Parser", "Copyright 2016-2020 Marco Gomiero", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("CardStackView", "https://github.com/yuyakaido/CardStackView", "Copyright 2018 yuyakaido", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Picasso", "https://square.github.io/picasso/", "Copyright 2013 Square, Inc.", ApacheSoftwareLicense20()))
            notices.addNotice(Notice("Readability4J", "https://github.com/dankito/Readability4J", "Copyright 2017 dankito", ApacheSoftwareLicense20()))


            LicensesDialog.Builder(requireActivity())
                    .setTitle(R.string.item_libraries)
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show()
            true
        }
    }
}