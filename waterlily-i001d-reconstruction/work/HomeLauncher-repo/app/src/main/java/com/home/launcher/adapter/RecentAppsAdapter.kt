package com.home.launcher.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.home.launcher.R

data class RecentTaskTile(
    val taskId: Int,
    val packageName: String,
    val appLabel: String?,
    val userId: Int
)

class RecentAppsAdapter(
    private val context: Context,
    private val onClose: (RecentTaskTile) -> Unit,
    private val onResume: (RecentTaskTile) -> Unit,
    private val thumbnailLoader: (Int) -> Bitmap?
) : RecyclerView.Adapter<RecentAppsAdapter.TileViewHolder>() {

    private val tiles = mutableListOf<RecentTaskTile>()
    private val thumbnailCache = mutableMapOf<Int, Bitmap>()
    private var tileHeight = 0

    fun setTileHeight(height: Int) {
        tileHeight = height
    }

    fun updateTiles(newTiles: List<RecentTaskTile>) {
        val existingIds = tiles.map { it.taskId }.toSet()
        val newIds = newTiles.map { it.taskId }.toSet()
        val sameOrder = tiles.map { it.taskId } == newTiles.map { it.taskId }

        val added = newTiles.filter { it.taskId !in existingIds }
        val removed = tiles.filter { it.taskId !in newIds }

        for (r in removed) {
            val idx = tiles.indexOfFirst { it.taskId == r.taskId }
            if (idx >= 0) {
                tiles.removeAt(idx)
                thumbnailCache.remove(r.taskId)
            }
        }

        val staleCache = thumbnailCache.keys.filter { it !in newIds }
        staleCache.forEach { thumbnailCache.remove(it) }

        tiles.clear()
        tiles.addAll(newTiles)

        if (!sameOrder || added.isNotEmpty() || removed.isNotEmpty()) {
            notifyDataSetChanged()
        }

        loadMissingThumbnails()
    }

    fun removeTile(taskId: Int) {
        val idx = tiles.indexOfFirst { it.taskId == taskId }
        if (idx >= 0) {
            tiles.removeAt(idx)
            thumbnailCache.remove(taskId)
            notifyItemRemoved(idx)
        }
    }

    fun refreshThumbnail(taskId: Int) {
        val index = tiles.indexOfFirst { it.taskId == taskId }
        if (index < 0) return

        val bitmap = thumbnailLoader(taskId) ?: return
        thumbnailCache[taskId] = bitmap
        notifyItemChanged(index)
    }

    fun clearAll() {
        tiles.clear()
        thumbnailCache.clear()
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean = tiles.isEmpty()
    fun getTileAt(position: Int): RecentTaskTile = tiles[position]

    private fun loadMissingThumbnails() {
        for (i in tiles.indices) {
            val tile = tiles[i]
            if (tile.taskId > 0 && !thumbnailCache.containsKey(tile.taskId)) {
                val bmp = thumbnailLoader(tile.taskId)
                if (bmp != null) {
                    thumbnailCache[tile.taskId] = bmp
                    notifyItemChanged(i)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recent_tile, parent, false)
        if (tileHeight > 0) {
            view.layoutParams = view.layoutParams?.also { it.height = tileHeight }
        }
        return TileViewHolder(view)
    }

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        if (tileHeight > 0) {
            holder.itemView.layoutParams?.height = tileHeight
        }
        holder.bind(tiles[position])
    }

    override fun getItemCount(): Int = tiles.size

    inner class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById<ImageView>(R.id.tileThumbnail)!!
        private val appIcon: ImageView = itemView.findViewById<ImageView>(R.id.tileAppIcon)!!
        private val appLabel: TextView = itemView.findViewById<TextView>(R.id.tileAppLabel)!!
        private val closeButton: ImageButton = itemView.findViewById<ImageButton>(R.id.tileCloseButton)!!

        fun bind(tile: RecentTaskTile) {
            val label = tile.appLabel ?: getAppLabel(tile.packageName)
            appLabel.text = label

            val icon = getAppIcon(tile.packageName)
            if (icon != null) {
                appIcon.setImageDrawable(icon)
            }

            val snapshot = thumbnailCache[tile.taskId]
            if (snapshot != null) {
                thumbnail.setImageBitmap(snapshot)
                appIcon.visibility = View.GONE
            } else {
                thumbnail.setImageDrawable(null)
                appIcon.visibility = View.VISIBLE
            }

            closeButton.setOnClickListener { onClose(tile) }
            itemView.setOnClickListener { onResume(tile) }

            val taskIdView: TextView = itemView.findViewById<TextView>(R.id.tileTaskId)!!
            taskIdView.text = "#${tile.taskId}"
        }

        private fun getAppIcon(packageName: String) = try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        private fun getAppLabel(packageName: String): String = try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }
}
