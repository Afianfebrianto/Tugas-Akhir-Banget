package com.afian.tugasakhir.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix

object BarcodeUtils {

    private const val TAG = "BarcodeUtils"

    
    fun generateBarcodeBitmap(
        data: String,
        format: BarcodeFormat = BarcodeFormat.CODE_128, // Default ke CODE_128 (umum untuk NIM)
        width: Int = 400, // Lebar default (bisa disesuaikan)
        height: Int = 100 // Tinggi default (bisa disesuaikan)
    ): Bitmap? {
        if (data.isBlank()) {
            Log.w(TAG, "Cannot generate barcode from empty data.")
            return null
        }
        // Pastikan width dan height > 0
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Barcode dimensions must be positive. Width: $width, Height: $height")
            return null
        }

        val bitMatrix: BitMatrix
        try {
            // Gunakan MultiFormatWriter untuk encode data ke BitMatrix
            bitMatrix = MultiFormatWriter().encode(
                data,
                format,
                width,
                height,
                null // hints (opsional)
            )
        } catch (e: WriterException) {
            Log.e(TAG, "Error encoding barcode data: '$data'", e)
            return null // Kembalikan null jika encode gagal
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during barcode encoding: '$data'", e)
            return null
        }


        // Konversi BitMatrix (hitam/putih) menjadi Bitmap Android
        val bmpWidth = bitMatrix.width
        val bmpHeight = bitMatrix.height
        val pixels = IntArray(bmpWidth * bmpHeight)
        for (y in 0 until bmpHeight) {
            val offset = y * bmpWidth
            for (x in 0 until bmpWidth) {
                // Jika bitMatrix true (hitam), set pixel ke hitam, jika false (putih), set ke putih transparan/putih solid
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE // Atau Color.TRANSPARENT
            }
        }

        // Buat Bitmap dari array pixel
        return try {
            val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight)
            bitmap
        } catch(e: Exception) {
            Log.e(TAG, "Error creating bitmap from pixels", e)
            null
        }
    }
}