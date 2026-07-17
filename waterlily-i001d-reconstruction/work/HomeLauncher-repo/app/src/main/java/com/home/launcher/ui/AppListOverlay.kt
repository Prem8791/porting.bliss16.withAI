package com.home.launcher.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.R
import com.home.launcher.data.AppEntry

class AppListOverlay {

    companion object {
        fun show(activity: Activity, anchor: View, title: String, apps: List<AppEntry>, onDismiss: (() -> Unit)? = null) {
            val builder = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            val view = LayoutInflater.from(activity).inflate(R.layout.overlay_app_list, null)

            val titleView: TextView = view.findViewById<TextView>(R.id.overlayTitle)!!
            val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.overlayAppList)!!
            val closeButton: View = view.findViewById<View>(R.id.overlayClose)!!

            titleView.text = title

            val dialog = builder.setView(view).create()

            recyclerView.layoutManager = GridLayoutManager(activity, 4)
            recyclerView.adapter = AppListAdapter(
                apps = apps,
                onTap = { entry ->
                    try {
                        val intent = activity.packageManager.getLaunchIntentForPackage(entry.packageName)
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            dialog.dismiss()
                            activity.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AppListOverlay", "Failed to launch ${entry.packageName}", e)
                    }
                },
                onLongPress = { itemView, entry, onFavouriteChanged ->
                    showAppContextMenu(activity, itemView, entry, onFavouriteChanged)
                }
            )

            val loc = IntArray(2)
            anchor.getLocationOnScreen(loc)

            dialog.window?.let { w ->
                val lp = w.attributes
                lp.gravity = Gravity.TOP or Gravity.LEFT
                lp.x = loc[0]
                lp.y = loc[1]
                lp.width = anchor.width
                lp.height = anchor.height
                w.attributes = lp
                w.setBackgroundDrawable(ColorDrawable(Color.parseColor("#CC000000")))
            }

            dialog.setOnDismissListener { onDismiss?.invoke() }
            dialog.show()

            closeButton.setOnClickListener { dialog.dismiss() }
        }

        private fun showAppContextMenu(
            activity: Activity,
            anchor: View,
            entry: AppEntry,
            onFavouriteChanged: (Boolean) -> Unit
        ) {
            val menu = PopupMenu(activity, anchor)
            val shortcuts = getPublishedShortcuts(activity, entry.packageName)
            for ((index, shortcut) in shortcuts.withIndex()) {
                val label = shortcut.shortLabel?.toString()
                    ?: shortcut.longLabel?.toString()
                    ?: continue
                menu.menu.add(0, index + 1, index, label)
            }
            menu.menu.add(if (entry.isFavourite) "Remove favourite" else "Add favourite")
            menu.menu.add("Launch")
            menu.menu.add("App info")
            menu.menu.add("Uninstall")
            menu.setOnMenuItemClickListener { item ->
                val shortcutIndex = item.itemId - 1
                if (shortcutIndex in shortcuts.indices) {
                    startShortcut(activity, shortcuts[shortcutIndex])
                    return@setOnMenuItemClickListener true
                }

                when (item.title.toString()) {
                    "Launch" -> {
                        launchApp(activity, entry.packageName)
                        true
                    }
                    "Add favourite", "Remove favourite" -> {
                        toggleFavourite(activity, entry.packageName)
                        onFavouriteChanged(!entry.isFavourite)
                        true
                    }
                    "App info" -> {
                        openAppInfo(activity, entry.packageName)
                        true
                    }
                    "Uninstall" -> {
                        requestUninstall(activity, entry.packageName)
                        true
                    }
                    else -> false
                }
            }
            menu.show()
        }

        private fun getPublishedShortcuts(activity: Activity, packageName: String): List<ShortcutInfo> {
            val launcherApps = activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val query = LauncherApps.ShortcutQuery()
                .setPackage(packageName)
                .setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            return try {
                launcherApps.getShortcuts(query, Process.myUserHandle())
                    ?.filter { it.isEnabled }
                    ?.sortedBy { it.rank }
                    ?.take(4)
                    ?: emptyList()
            } catch (e: SecurityException) {
                emptyList()
            } catch (e: RuntimeException) {
                emptyList()
            }
        }

        private fun startShortcut(activity: Activity, shortcut: ShortcutInfo) {
            val launcherApps = activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            try {
                launcherApps.startShortcut(
                    shortcut.`package`,
                    shortcut.id,
                    null,
                    null,
                    shortcut.userHandle
                )
            } catch (e: Exception) {
                Toast.makeText(activity, "Cannot open shortcut", Toast.LENGTH_SHORT).show()
            }
        }

        private fun launchApp(activity: Activity, packageName: String) {
            try {
                val intent = activity.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Failed to launch app", Toast.LENGTH_SHORT).show()
            }
        }

        private fun toggleFavourite(activity: Activity, packageName: String) {
            val prefs = activity.getSharedPreferences("favourites", Activity.MODE_PRIVATE)
            val favSet = prefs.getStringSet("favs", emptySet<String>())?.toMutableSet()
                ?: mutableSetOf<String>()
            if (!favSet.add(packageName)) {
                favSet.remove(packageName)
            }
            prefs.edit().putStringSet("favs", favSet).apply()
        }

        private fun openAppInfo(activity: Activity, packageName: String) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Cannot open app info", Toast.LENGTH_SHORT).show()
            }
        }

        private fun requestUninstall(activity: Activity, packageName: String) {
            try {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:$packageName")
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Cannot uninstall app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class AppListAdapter(
        apps: List<AppEntry>,
        private val onTap: (AppEntry) -> Unit,
        private val onLongPress: (View, AppEntry, (Boolean) -> Unit) -> Unit
    ) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
        private val apps: MutableList<AppEntry> = apps.toMutableList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = apps[position]
            holder.bind(entry, onTap, onLongPress) { isFavourite ->
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    apps[currentPosition] = apps[currentPosition].copy(isFavourite = isFavourite)
                    notifyItemChanged(currentPosition)
                }
            }
        }

        override fun getItemCount(): Int = apps.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val icon: ImageView = itemView.findViewById<ImageView>(R.id.appItemIcon)!!
            private val label: TextView = itemView.findViewById<TextView>(R.id.appItemLabel)!!
            private val favIndicator: View = itemView.findViewById<View>(R.id.appItemFav)!!

            fun bind(
                entry: AppEntry,
                onTap: (AppEntry) -> Unit,
                onLongPress: (View, AppEntry, (Boolean) -> Unit) -> Unit,
                onFavouriteChanged: (Boolean) -> Unit
            ) {
                label.text = entry.label
                if (entry.icon != null) {
                    icon.setImageDrawable(entry.icon)
                }
                favIndicator.visibility = if (entry.isFavourite) View.VISIBLE else View.GONE
                itemView.setOnClickListener { onTap(entry) }
                itemView.setOnLongClickListener {
                    onLongPress(itemView, entry) { isFavourite ->
                        favIndicator.visibility = if (isFavourite) View.VISIBLE else View.GONE
                        onFavouriteChanged(isFavourite)
                    }
                    true
                }
            }
        }
    }
}
