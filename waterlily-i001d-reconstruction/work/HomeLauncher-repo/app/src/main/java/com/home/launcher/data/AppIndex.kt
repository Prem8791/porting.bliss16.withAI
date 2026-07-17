package com.home.launcher.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isFavourite: Boolean = false
)

class AppIndex(private val context: Context) {

    private var appMap: MutableMap<Char, MutableList<AppEntry>> = mutableMapOf()
    private var favourites: MutableList<AppEntry> = mutableListOf()
    private var numericApps: MutableList<AppEntry> = mutableListOf()
    private var allApps: List<AppEntry> = emptyList<AppEntry>()
    private var isLoaded = false

    fun load() {
        val pm = context.packageManager
        val prefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE)
        val favSet = prefs.getStringSet("favs", emptySet<String>()) ?: emptySet<String>()
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val launchIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_LAUNCHER)

        val resolveInfos = pm.queryIntentActivities(launchIntent, 0)
        val launcherPackages = resolveInfos.map { it.activityInfo.packageName }.toSet()

        val entries = apps
            .filter { launcherPackages.contains(it.packageName) }
            .map { info ->
                val label = pm.getApplicationLabel(info).toString()
                AppEntry(
                    packageName = info.packageName,
                    label = label,
                    icon = info.loadIcon(pm),
                    isFavourite = info.packageName in favSet
                )
            }
            .sortedBy { it.label.lowercase() }

        allApps = entries
        appMap.clear()
        favourites.clear()
        numericApps.clear()

        for (entry in entries) {
            val firstChar = entry.label.firstOrNull()
                ?.uppercaseChar()?.takeIf { it in 'A'..'Z' }
            if (firstChar != null) {
                appMap.getOrPut(firstChar) { mutableListOf() }.add(entry)
            } else if (entry.label.firstOrNull()?.isDigit() == true) {
                numericApps.add(entry)
            }
        }

        favourites = entries.filter { it.packageName in favSet }
            .toMutableList()

        isLoaded = true
    }

    fun getAppsForLetter(letter: Char): List<AppEntry> {
        if (!isLoaded) load()
        return when {
            letter == '#' -> numericApps
            letter == '*' -> favourites
            letter in 'A'..'Z' -> appMap[letter] ?: emptyList<AppEntry>()
            else -> emptyList<AppEntry>()
        }
    }

    fun toggleFavourite(packageName: String) {
        val entry = allApps.find { it.packageName == packageName } ?: return
        val prefs = context.getSharedPreferences("favourites", Context.MODE_PRIVATE)
        val favSet = prefs.getStringSet("favs", emptySet<String>())?.toMutableSet() ?: mutableSetOf<String>()
        if (favSet.contains(packageName)) {
            favSet.remove(packageName)
            favourites.removeAll { it.packageName == packageName }
        } else {
            favSet.add(packageName)
            favourites.add(entry)
        }
        prefs.edit().putStringSet("favs", favSet).apply()
        load()
    }

    fun isFavourite(packageName: String): Boolean {
        return favourites.any { it.packageName == packageName }
    }

    fun getAvailableLetters(): Set<Char> {
        if (!isLoaded) load()
        val letters = appMap.keys.toMutableSet()
        if (numericApps.isNotEmpty()) letters.add('#')
        if (favourites.isNotEmpty()) letters.add('*')
        return letters
    }

    fun getAllApps(): List<AppEntry> = allApps
    fun isLoaded(): Boolean = isLoaded
}
