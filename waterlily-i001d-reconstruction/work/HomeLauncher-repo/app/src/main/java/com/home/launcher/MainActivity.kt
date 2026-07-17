package com.home.launcher

import android.app.ActivityManager
import android.Manifest
import android.content.ContentUris
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.provider.CalendarContract
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.animation.MorphConfig
import com.home.launcher.animation.MorphingEngine
import com.home.launcher.adapter.RecentAppsAdapter
import com.home.launcher.adapter.RecentTaskTile
import kotlin.math.roundToInt
import com.home.launcher.data.AppIndex
import com.home.launcher.task.RecentTasksRepository
import com.home.launcher.task.TaskListenerRegistration
import com.home.launcher.ui.AppListOverlay
import com.home.launcher.ui.SystemStatsBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private companion object {
        const val REQUEST_PICK_WALLPAPER_IMAGE = 1001
        const val PREFS_DOCK = "dock"
        const val KEY_DOCK_HEIGHT_PERCENT = "dock_height_percent"
        const val OMNIJAWS_PACKAGE = "org.omnirom.omnijaws"
        const val OMNIJAWS_SETTINGS_ACTIVITY = "org.omnirom.omnijaws.SettingsActivity"
        val OMNIJAWS_WEATHER_URI: Uri =
            Uri.parse("content://org.omnirom.omnijaws.provider/weather")
        const val DEFAULT_DOCK_HEIGHT_PERCENT = 10
        const val STATUS_HEIGHT_PERCENT = 10
        const val DOCK_SLOT_COUNT = 5
    }

    // Layout references
    private lateinit var leftColumn: LinearLayout
    private lateinit var centerColumn: LinearLayout
    private lateinit var rightColumn: LinearLayout
    private lateinit var rootLayout: LinearLayout
    private lateinit var recentAppsRow: View
    private lateinit var recentAppsGrid: RecyclerView
    private lateinit var recentAppsAdapter: RecentAppsAdapter
    private lateinit var killAllButton: TextView
    private lateinit var statusArea: LinearLayout
    private lateinit var dockContainer: LinearLayout
    private lateinit var todayDate: TextView
    private lateinit var todayEvent: TextView
    private lateinit var todayTasks: TextView
    private lateinit var clockTime: TextView
    private lateinit var clockMeta: TextView
    private lateinit var weatherSummary: TextView
    private var weatherObserver: ContentObserver? = null
    private var todayEventId: Long? = null
    private var todayTaskEventId: Long? = null
    private lateinit var morphingEngine: MorphingEngine

    // Data
    private val appIndex by lazy { AppIndex(this) }
    private val recentTasksRepository by lazy { RecentTasksRepository(this) }
    private lateinit var statsBar: SystemStatsBar
    private var taskListenerRegistration: TaskListenerRegistration? = null
    private val pendingSnapshotRefreshes = mutableSetOf<Int>()
    private var pollingActive = false
    private val handler = Handler(Looper.getMainLooper())
    private var launcherApps: LauncherApps? = null
    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: android.os.UserHandle) {
            refreshAppIndex()
        }
        override fun onPackageRemoved(packageName: String, user: android.os.UserHandle) {
            refreshAppIndex()
        }
        override fun onPackageChanged(packageName: String, user: android.os.UserHandle) {
            refreshAppIndex()
        }
        override fun onPackagesAvailable(packageNames: Array<String>, user: android.os.UserHandle, replacing: Boolean) {
            refreshAppIndex()
        }
        override fun onPackagesUnavailable(packageNames: Array<String>, user: android.os.UserHandle, replacing: Boolean) {
            refreshAppIndex()
        }
    }
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refreshRecentTasks()
            if (pollingActive) handler.postDelayed(this, 3000)
        }
    }
    private val clockRefreshRunnable = object : Runnable {
        override fun run() {
            refreshClock()
            if (pollingActive) handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // === Startup diagnostics ===
        Log.i("HomeLauncher", "aospBuild=HomeLauncher system image build")
        Log.i("HomeLauncher", "package=$packageName uid=${android.os.Process.myUid()}")
        Log.i("HomeLauncher", "sdk=${Build.VERSION.SDK_INT} release=${Build.VERSION.RELEASE}")
        logPrivilegedPermission("REAL_GET_TASKS")
        logPrivilegedPermission("MANAGE_ACTIVITY_TASKS")
        logPrivilegedPermission("START_TASKS_FROM_RECENTS")
        logPrivilegedPermission("REMOVE_TASKS")
        logPrivilegedPermission("READ_FRAME_BUFFER")
        logPrivilegedPermission("FORCE_STOP_PACKAGES")
        logPrivilegedPermission("BATTERY_STATS")
        logPrivilegedPermission("DEVICE_POWER")
        logPrivilegedPermission("STATUS_BAR")
        logPrivilegedPermission("INTERACT_ACROSS_USERS")

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        setContentView(R.layout.activity_main)
        initMorphingEngine()

        initViews()
        initAlphabetColumns()
        initRecentApps()
        initKillAll()
        initSettings()
        initToday()
        initClock()
        initWeather()
        initStatsBar()

        appIndex.load()
        launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        initDock()
        updateAlphabetAvailability()
        registerTaskListener()
    }

    private fun logPrivilegedPermission(name: String) {
        val permission = "android.permission.$name"
        val result = checkSelfPermission(permission)
        Log.i("PermCheck", "$permission -> ${
            when (result) {
                PackageManager.PERMISSION_GRANTED -> "GRANTED"
                PackageManager.PERMISSION_DENIED -> "DENIED"
                else -> "UNKNOWN($result)"
            }
        }")
    }

    override fun onResume() {
        super.onResume()
        leftColumn.visibility = View.VISIBLE
        rightColumn.visibility = View.VISIBLE
        pollingActive = true
        refreshRecentTasks()
        refreshPendingSnapshots()
        refreshAppIndex()
        handler.removeCallbacks(refreshRunnable)
        handler.postDelayed(refreshRunnable, 3000)
        statsBar.start()
        handler.removeCallbacks(clockRefreshRunnable)
        handler.post(clockRefreshRunnable)
        refreshWeather()
        launcherApps?.registerCallback(launcherAppsCallback, handler)
    }

    override fun onPause() {
        super.onPause()
        pollingActive = false
        handler.removeCallbacks(refreshRunnable)
        statsBar.stop()
        handler.removeCallbacks(clockRefreshRunnable)
        launcherApps?.unregisterCallback(launcherAppsCallback)
    }

    override fun onDestroy() {
        weatherObserver?.let { contentResolver.unregisterContentObserver(it) }
        weatherObserver = null
        super.onDestroy()
        unregisterTaskListener()
    }

    @Deprecated("Uses legacy back handling for platform compatibility with the AOSP build target.")
    override fun onBackPressed() {
        if (::morphingEngine.isInitialized && morphingEngine.isExpanded) {
            morphingEngine.collapse()
            return
        }
        super.onBackPressed()
    }

    // ============ VIEW INITIALIZATION ============

    private fun initMorphingEngine() {
        morphingEngine = MorphingEngine(this)
        addContentView(
            morphingEngine,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun initViews() {
        rootLayout = findViewById<LinearLayout>(R.id.rootLayout)!!
        leftColumn = findViewById<LinearLayout>(R.id.leftColumn)!!
        centerColumn = findViewById<LinearLayout>(R.id.centerColumn)!!
        rightColumn = findViewById<LinearLayout>(R.id.rightColumn)!!
        recentAppsRow = findViewById<View>(R.id.recentAppsRow)!!
        statusArea = findViewById<LinearLayout>(R.id.statusArea)!!
        dockContainer = findViewById<LinearLayout>(R.id.dockContainer)!!
    }

    // ============ ALPHABET COLUMNS ============

    private fun initAlphabetColumns() {
        val leftLetters = listOf('A','B','C','D','E','F','G','H','I','J','K','L','M')
        val rightLetters = listOf('N','O','P','Q','R','S','T','U','V','W','X','Y','Z')

        for (letter in leftLetters) {
            val id = resources.getIdentifier("letter_$letter", "id", packageName)
            findViewById<TextView>(id)?.setOnClickListener { onLetterTap(letter) }
        }
        findViewById<TextView>(R.id.letter_HASH)?.setOnClickListener { onLetterTap('#') }

        for (letter in rightLetters) {
            val id = resources.getIdentifier("letter_$letter", "id", packageName)
            findViewById<TextView>(id)?.setOnClickListener { onLetterTap(letter) }
        }
        findViewById<TextView>(R.id.letter_STAR)?.setOnClickListener { onLetterTap('*') }
    }

    private fun updateAlphabetAvailability() {
        val available = appIndex.getAvailableLetters()
        val letterIds = ('A'..'Z').map { it to resources.getIdentifier("letter_$it", "id", packageName) } +
            listOf('#' to R.id.letter_HASH, '*' to R.id.letter_STAR)
        for ((letter, id) in letterIds) {
            val tv = findViewById<TextView>(id) ?: continue
            if (letter in available) {
                tv.setTextColor(resources.getColor(R.color.alphabet_letter, theme))
            } else {
                tv.setTextColor(resources.getColor(R.color.alphabet_letter_disabled, theme))
            }
        }
    }

    private fun refreshAppIndex() {
        appIndex.load()
        updateAlphabetAvailability()
        refreshDock()
    }

    private fun onLetterTap(letter: Char) {
        val apps = appIndex.getAppsForLetter(letter)
        val title = when (letter) {
            '#' -> getString(R.string.numbers)
            '*' -> getString(R.string.favourites)
            else -> "\"$letter\""
        }
        if (apps.isEmpty()) {
            return
        }
        leftColumn.visibility = View.INVISIBLE
        rightColumn.visibility = View.INVISIBLE
        AppListOverlay.show(this, centerColumn, title, apps) {
            leftColumn.visibility = View.VISIBLE
            rightColumn.visibility = View.VISIBLE
        }
    }

    // ============ RECENT APPS ============

    private fun initRecentApps() {
        recentAppsGrid = findViewById<RecyclerView>(R.id.recentAppsGrid)!!
        killAllButton = findViewById<TextView>(R.id.killAllButton)!!

        recentAppsAdapter = RecentAppsAdapter(
            context = this,
            onClose = { tile -> closeTask(tile) },
            onResume = { tile -> resumeTask(tile) },
            thumbnailLoader = { taskId -> recentTasksRepository.getTaskSnapshot(taskId, false) }
        )

        val glm = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        recentAppsGrid.layoutManager = glm
        recentAppsGrid.adapter = recentAppsAdapter

        updateRecentTileHeight()
    }

    private fun updateRecentTileHeight() {
        recentAppsGrid.post {
            val gridW = recentAppsGrid.width
            val padLeft = recentAppsGrid.paddingLeft
            val padRight = recentAppsGrid.paddingRight
            val cols = 3
            val colWidth = (gridW - padLeft - padRight) / cols

            val density = resources.displayMetrics.density
            val tilePadding = (4 * density).roundToInt()
            val displayRatio = resources.displayMetrics.widthPixels.toFloat() /
                resources.displayMetrics.heightPixels.toFloat()
            val tileH = ((colWidth - tilePadding) / displayRatio + tilePadding).roundToInt()

            if (tileH > 0) {
                recentAppsAdapter.setTileHeight(tileH)
                recentAppsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun refreshRecentTasks() {
        val tasks = recentTasksRepository.getRecentTasks(30)
        val tiles = tasks.map { task ->
            RecentTaskTile(
                taskId = task.taskId,
                packageName = task.packageName,
                appLabel = task.label,
                userId = task.userId
            )
        }
        runOnUiThread {
            recentAppsAdapter.updateTiles(tiles)
        }
    }

    private fun closeTask(tile: RecentTaskTile) {
        recentTasksRepository.removeTask(tile.taskId)
        recentAppsAdapter.removeTile(tile.taskId)
    }

    private fun resumeTask(tile: RecentTaskTile) {
        val started = recentTasksRepository.startTaskFromRecents(tile.taskId)
        if (!started) {
            try {
                val intent = packageManager.getLaunchIntentForPackage(tile.packageName)
                if (intent != null) startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to launch app", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initKillAll() {
        killAllButton.setOnClickListener {
            val tiles = (0 until recentAppsAdapter.itemCount).map { recentAppsAdapter.getTileAt(it) }
            if (tiles.isEmpty()) {
                return@setOnClickListener
            }

            for (tile in tiles) {
                recentTasksRepository.forceStopPackage(tile.packageName)
                recentTasksRepository.removeTask(tile.taskId)
            }
            recentTasksRepository.removeAllVisibleRecentTasks()
            recentAppsAdapter.clearAll()
        }
    }

    // ============ SETTINGS ============

    private fun initSettings() {
        findViewById<View>(R.id.settingsButton)!!.setOnClickListener { showSettingsDialog() }
    }

    private fun showSettingsMorph(anchor: View) {
        val content = createSettingsMorphContent()
        morphingEngine.expand(
            anchor,
            content,
            MorphConfig(maxWidthFraction = 0.78f, durationMs = 340L)
        )
    }

    private fun createSettingsMorphContent(): View {
        val scrollView = ScrollView(this)
        scrollView.isFillViewport = false

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(dp(18), dp(16), dp(18), dp(16))
        scrollView.addView(
            container,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val title = TextView(this)
        title.text = "Home Settings"
        title.setTextColor(resources.getColor(R.color.text_primary, theme))
        title.textSize = 18f
        title.setTypeface(title.typeface, android.graphics.Typeface.BOLD)
        container.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSettingsAction(container, "Manage Permissions") {
            morphingEngine.collapse()
            openAppPermissions()
        }
        addSettingsAction(container, "Set Wallpaper") {
            morphingEngine.collapse()
            showWallpaperOptions()
        }
        addSettingsAction(container, "Weather Settings") {
            morphingEngine.collapse()
            showWeatherSettings()
        }
        addSettingsAction(container, "Dock Apps") {
            morphingEngine.collapse()
            showDockSettings()
        }
        addSettingsAction(container, "Dock Height") {
            morphingEngine.collapse()
            showDockHeightDialog()
        }

        return scrollView
    }

    private fun addSettingsAction(container: LinearLayout, label: String, action: () -> Unit) {
        val row = TextView(this)
        row.text = label
        row.setTextColor(resources.getColor(R.color.text_primary, theme))
        row.textSize = 14f
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(12), 0, dp(12), 0)
        row.background = resources.getDrawable(R.drawable.tile_bg, theme)
        row.setOnClickListener { action() }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(46)
        )
        params.topMargin = dp(10)
        container.addView(row, params)
    }

    private fun showSettingsDialog() {
        val items = arrayOf(
            "Manage Permissions",
            "Set Wallpaper",
            "Weather Settings",
            "Dock Apps",
            "Dock Height"
        )

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Home Settings")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openAppPermissions()
                    1 -> showWallpaperOptions()
                    2 -> showWeatherSettings()
                    3 -> showDockSettings()
                    4 -> showDockHeightDialog()
                }
            }
            .show()
    }

    private fun openAppPermissions() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWallpaperOptions() {
        val items = arrayOf(
            "System wallpapers",
            "Choose from gallery",
            "Choose image file"
        )

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Set Wallpaper")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> openSystemWallpaperPicker()
                    1 -> openImagePicker(Intent.ACTION_GET_CONTENT)
                    2 -> openImagePicker(Intent.ACTION_OPEN_DOCUMENT)
                }
            }
            .show()
    }

    private fun openSystemWallpaperPicker() {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (e2: Exception) {
                Toast.makeText(this, "Wallpaper picker not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker(action: String) {
        try {
            val intent = Intent(action).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (action == Intent.ACTION_OPEN_DOCUMENT) {
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
            }
            startActivityForResult(Intent.createChooser(intent, "Choose wallpaper image"), REQUEST_PICK_WALLPAPER_IMAGE)
        } catch (e: Exception) {
            Toast.makeText(this, "No image picker available", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Uses legacy callback for platform compatibility with the AOSP build target.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_WALLPAPER_IMAGE || resultCode != RESULT_OK) return

        val imageUri = data?.data
        if (imageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (flags != 0) {
            try {
                contentResolver.takePersistableUriPermission(imageUri, flags)
            } catch (_: SecurityException) {
                // ACTION_PICK providers often grant transient access only.
            }
        }

        try {
            contentResolver.openInputStream(imageUri)?.use { stream ->
                WallpaperManager.getInstance(this).setStream(stream)
            } ?: throw IllegalArgumentException("Unable to open selected image")
        } catch (e: Exception) {
            Log.e("HomeLauncher", "Failed to set wallpaper from $imageUri", e)
            Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
        }
    }

    // ============ DOCK ============

    private fun initDock() {
        applyDockHeight()
        refreshDock()
    }

    private fun refreshDock() {
        dockContainer.removeAllViews()
        for (slot in 0 until DOCK_SLOT_COUNT) {
            dockContainer.addView(createDockSlot(slot, getDockPackage(slot)))
        }
    }

    private fun createDockSlot(slot: Int, packageName: String?): View {
        val slotView = LinearLayout(this)
        slotView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        )
        slotView.gravity = Gravity.CENTER
        slotView.orientation = LinearLayout.VERTICAL
        slotView.setPadding(4, 2, 4, 2)

        val icon = ImageView(this)
        val iconSize = dp(42)
        icon.layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)

        val label = TextView(this)
        label.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        label.gravity = Gravity.CENTER
        label.maxLines = 1
        label.setTextColor(resources.getColor(R.color.text_secondary, theme))
        label.textSize = 9f

        if (packageName == null) {
            icon.setImageResource(android.R.drawable.ic_input_add)
            label.text = "Set"
            slotView.setOnClickListener { showDockAppPicker(slot) }
            slotView.contentDescription = "Set dock app ${slot + 1}"
        } else {
            try {
                icon.setImageDrawable(packageManager.getApplicationIcon(packageName))
                label.text = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, 0)
                )
                slotView.setOnClickListener { launchDockApp(packageName) }
                slotView.setOnLongClickListener {
                    showDockAppPicker(slot)
                    true
                }
                slotView.contentDescription = "Launch ${label.text}"
            } catch (e: PackageManager.NameNotFoundException) {
                icon.setImageResource(android.R.drawable.sym_def_app_icon)
                label.text = "Missing"
                slotView.setOnClickListener { showDockAppPicker(slot) }
                slotView.contentDescription = "Replace missing dock app ${slot + 1}"
            }
        }

        slotView.addView(icon)
        slotView.addView(label)
        return slotView
    }

    private fun launchDockApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Toast.makeText(this, "App unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot launch app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDockSettings() {
        if (!appIndex.isLoaded()) appIndex.load()
        val items = Array(DOCK_SLOT_COUNT + 1) { index ->
            if (index == DOCK_SLOT_COUNT) {
                "Clear dock"
            } else {
                "Slot ${index + 1}: ${getDockLabel(getDockPackage(index)) ?: "Empty"}"
            }
        }

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Dock Apps")
            .setItems(items) { _, which ->
                if (which == DOCK_SLOT_COUNT) {
                    clearDock()
                } else {
                    showDockAppPicker(which)
                }
            }
            .show()
    }

    private fun showDockAppPicker(slot: Int) {
        if (!appIndex.isLoaded()) appIndex.load()
        val apps = appIndex.getAllApps()
        val labels = arrayOf("Empty slot") + apps.map { it.label }.toTypedArray()

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Dock Slot ${slot + 1}")
            .setItems(labels) { _, which ->
                if (which == 0) {
                    setDockPackage(slot, null)
                } else {
                    setDockPackage(slot, apps[which - 1].packageName)
                }
                refreshDock()
            }
            .show()
    }

    private fun showDockHeightDialog() {
        val heights = intArrayOf(6, 8, 10, 12, 14, 16, 18, 20)
        val current = getDockHeightPercent()
        val currentIndex = heights.indexOf(current).takeIf { it >= 0 } ?: 2
        val labels = heights.map { "$it% of center area" }.toTypedArray()

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
            .setTitle("Dock Height")
            .setSingleChoiceItems(labels, currentIndex) { dialog, which ->
                setDockHeightPercent(heights[which])
                applyDockHeight()
                dialog.dismiss()
            }
            .show()
    }

    private fun applyDockHeight() {
        val dockPercent = getDockHeightPercent().coerceIn(6, 20)
        val recentPercent = 100 - STATUS_HEIGHT_PERCENT - dockPercent

        (recentAppsRow.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
            params.weight = recentPercent.toFloat()
            recentAppsRow.layoutParams = params
        }
        (dockContainer.layoutParams as? LinearLayout.LayoutParams)?.let { params ->
            params.weight = dockPercent.toFloat()
            dockContainer.layoutParams = params
        }
        centerColumn.requestLayout()
        updateRecentTileHeight()
    }

    private fun getDockPackage(slot: Int): String? {
        return getSharedPreferences(PREFS_DOCK, Context.MODE_PRIVATE)
            .getString("dock_pkg_$slot", null)
    }

    private fun setDockPackage(slot: Int, packageName: String?) {
        val editor = getSharedPreferences(PREFS_DOCK, Context.MODE_PRIVATE).edit()
        if (packageName == null) {
            editor.remove("dock_pkg_$slot")
        } else {
            editor.putString("dock_pkg_$slot", packageName)
        }
        editor.apply()
    }

    private fun clearDock() {
        val editor = getSharedPreferences(PREFS_DOCK, Context.MODE_PRIVATE).edit()
        for (slot in 0 until DOCK_SLOT_COUNT) {
            editor.remove("dock_pkg_$slot")
        }
        editor.apply()
        refreshDock()
    }

    private fun getDockLabel(packageName: String?): String? {
        if (packageName == null) return null
        return try {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "Missing"
        }
    }

    private fun getDockHeightPercent(): Int {
        return getSharedPreferences(PREFS_DOCK, Context.MODE_PRIVATE)
            .getInt(KEY_DOCK_HEIGHT_PERCENT, DEFAULT_DOCK_HEIGHT_PERCENT)
    }

    private fun setDockHeightPercent(percent: Int) {
        getSharedPreferences(PREFS_DOCK, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_DOCK_HEIGHT_PERCENT, percent)
            .apply()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    // ============ TASK LISTENER ============

    private fun registerTaskListener() {
        if (taskListenerRegistration == null) {
            taskListenerRegistration = recentTasksRepository.registerTaskChangeListener { snapshotTaskId ->
                runOnUiThread {
                    if (snapshotTaskId != null) {
                        pendingSnapshotRefreshes.add(snapshotTaskId)
                    }
                    if (pollingActive) {
                        refreshRecentTasks()
                        refreshPendingSnapshots()
                    }
                }
            }
        }
    }

    private fun refreshPendingSnapshots() {
        if (pendingSnapshotRefreshes.isEmpty()) return
        val taskIds = pendingSnapshotRefreshes.toList()
        pendingSnapshotRefreshes.clear()
        taskIds.forEach { taskId -> recentAppsAdapter.refreshThumbnail(taskId) }
    }

    private fun unregisterTaskListener() {
        taskListenerRegistration?.unregister()
        taskListenerRegistration = null
    }

    // ============ TODAY SECTION ============

    private fun initClock() {
        clockTime = findViewById<TextView>(R.id.clockTime)!!
        clockMeta = findViewById<TextView>(R.id.clockMeta)!!
        refreshClock()
    }

    private fun refreshClock() {
        val now = Date()
        clockTime.text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now)
        clockMeta.text = SimpleDateFormat("a  EEE, MMM d", Locale.getDefault()).format(now)
    }

    private fun initWeather() {
        weatherSummary = findViewById<TextView>(R.id.weatherSummary)!!
        findViewById<View>(R.id.weatherContainer)!!.setOnClickListener { showWeatherSettings() }
        weatherObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                refreshWeather()
            }
        }.also {
            contentResolver.registerContentObserver(OMNIJAWS_WEATHER_URI, true, it)
        }
        refreshWeather()
    }

    private fun refreshWeather() {
        try {
            contentResolver.query(
                OMNIJAWS_WEATHER_URI,
                arrayOf("city", "condition", "temperature"),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val city = cursor.getString(cursor.getColumnIndexOrThrow("city"))
                    val condition = cursor.getString(cursor.getColumnIndexOrThrow("condition"))
                    val temperature = cursor.getString(cursor.getColumnIndexOrThrow("temperature"))
                    weatherSummary.text = formatWeather(city, temperature, condition)
                    return
                }
            }
        } catch (e: Exception) {
            Log.w("HomeLauncher", "Unable to read OmniJaws weather", e)
        }
        weatherSummary.text = getString(R.string.weather_unavailable)
    }

    private fun showWeatherSettings() {
        try {
            startActivity(Intent().setClassName(OMNIJAWS_PACKAGE, OMNIJAWS_SETTINGS_ACTIVITY))
        } catch (e: Exception) {
            Log.w("HomeLauncher", "Unable to open OmniJaws settings", e)
            Toast.makeText(this, getString(R.string.weather_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatWeather(location: String, temperature: String, condition: String): String {
        return "$temperature°  $condition\n$location\nOmniJaws"
    }

    private fun initToday() {
        todayDate = findViewById<TextView>(R.id.todayDate)!!
        todayEvent = findViewById<TextView>(R.id.todayEvent)!!
        todayTasks = findViewById<TextView>(R.id.todayTasks)!!
        todayDate.setOnClickListener { openCalendarDay() }
        todayEvent.setOnClickListener { todayEventId?.let { openCalendarEvent(it) } }
        todayTasks.setOnClickListener { todayTaskEventId?.let { openCalendarEvent(it) } }
        refreshToday()
    }

    private fun refreshToday() {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        todayDate.text = sdf.format(Date())
        todayEventId = null
        todayTaskEventId = null
        updateTodayClickTargets()

        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            todayEvent.text = "Calendar permission needed"
            todayTasks.text = ""
            updateTodayClickTargets()
            return
        }

        try {
            val resolver = contentResolver
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.timeInMillis

            val instancesUri = CalendarContract.Instances.CONTENT_URI.buildUpon().also { builder ->
                ContentUris.appendId(builder, dayStart)
                ContentUris.appendId(builder, dayEnd)
            }.build()

            val cursor = resolver.query(
                instancesUri,
                arrayOf(
                    CalendarContract.Instances.EVENT_ID,
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN
                ),
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            if (cursor == null) {
                todayEvent.text = "No events today"
            } else cursor.use {
                val events = mutableListOf<String>()
                while (it.moveToNext() && events.size < 3) {
                    val eventId = it.getLong(0)
                    val title = it.getString(1) ?: "Event"
                    val start = it.getLong(2)
                    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(start))
                    if (todayEventId == null) todayEventId = eventId
                    events.add("$time $title")
                }
                todayEvent.text = if (events.isEmpty()) "No events today" else events.joinToString("\n")
            }
        } catch (e: Exception) {
            Log.e("HomeLauncher", "Calendar event query failed", e)
            todayEvent.text = "Calendar access needed"
        }

        try {
            val resolver = contentResolver
            val cursor = resolver.query(
                CalendarContract.Reminders.CONTENT_URI,
                arrayOf(CalendarContract.Reminders.EVENT_ID, CalendarContract.Reminders.MINUTES),
                "${CalendarContract.Reminders.MINUTES} >= 0",
                null,
                "${CalendarContract.Reminders.MINUTES} ASC"
            )

            if (cursor == null) {
                todayTasks.text = ""
            } else cursor.use {
                val tasks = mutableListOf<String>()
                while (it.moveToNext() && tasks.size < 3) {
                    val eventId = it.getLong(0)
                    val title = getCalendarEventTitle(eventId) ?: "Reminder"
                    if (todayTaskEventId == null) todayTaskEventId = eventId
                    tasks.add("☐ $title")
                }
                todayTasks.text = tasks.joinToString("\n")
            }
        } catch (e: Exception) {
            Log.e("HomeLauncher", "Calendar reminder query failed", e)
            todayTasks.text = ""
        }
        updateTodayClickTargets()
    }

    private fun updateTodayClickTargets() {
        todayDate.isClickable = true
        todayEvent.isClickable = todayEventId != null
        todayTasks.isClickable = todayTaskEventId != null
        todayDate.alpha = 1f
        todayEvent.alpha = if (todayEventId != null) 1f else 0.75f
        todayTasks.alpha = if (todayTaskEventId != null) 1f else 0.75f
    }

    private fun openCalendarDay() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
                .appendPath(Calendar.getInstance().timeInMillis.toString())
                .build()
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open calendar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCalendarEvent(eventId: Long) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open event", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCalendarEventTitle(eventId: Long): String? {
        return runCatching {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                arrayOf(CalendarContract.Events.TITLE),
                "${CalendarContract.Events._ID} = ?",
                arrayOf(eventId.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    // ============ SYSTEM STATS ============

    private fun initStatsBar() {
        statsBar = SystemStatsBar(
            context = this,
            batteryView = findViewById<TextView>(R.id.statBattery)!!,
            ramView = findViewById<TextView>(R.id.statRam)!!,
            cpuView = findViewById<TextView>(R.id.statCpu)!!,
            tempView = findViewById<TextView>(R.id.statTemp)!!,
            storageView = findViewById<TextView>(R.id.statStorage)!!
        )

        findViewById<LinearLayout>(R.id.statusStatsCenter)!!.isClickable = false
    }
}
