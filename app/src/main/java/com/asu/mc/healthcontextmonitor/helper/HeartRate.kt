package com.asu.mc.healthcontextmonitor.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore

class HeartRate(private val context: Context) {

    fun convertMediaUriToPath(uri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        uri?.let {
            val cursor = context.contentResolver.query(it, proj, null, null, null) ?: return null
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(column_index)
            cursor.close()
            return path
        }
        return null
    }

    inner class SlowTask : AsyncTask<String, Void, String?>() {
        override fun doInBackground(vararg params: String?): String? {
            val retriever = MediaMetadataRetriever()
            val frameList = ArrayList<Bitmap>()

            try {
                retriever.setDataSource(params[0])
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                        ?.toInt() ?: 0
                var i = 10
                while (i < duration) {
                    retriever.getFrameAtIndex(i)?.let { frameList.add(it) }
                    i += 5
                }
            } catch (m_e: Exception) {
                // Handle exception
            } finally {
                retriever.release()

                var redBucket: Long = 0
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (bitmap in frameList) {
                    redBucket = 0
                    for (y in 550 until 650) {
                        for (x in 550 until 650) {
                            val c: Int = bitmap.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }
                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    val temp = (a[i] + a[i + 1] + a[i + 2] + a[i + 3] + a[i + 4]) / 4
                    b.add(temp)
                }
                var x = b[0]
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b[i]
                    if ((p - x) > 3500) {
                        count += 1
                    }
                    x = p
                }
                val rate = ((count.toFloat() / 45) * 60).toInt()
                return (rate / 2).toString()
            }
        }
    }
}
