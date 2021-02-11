package jp.techacademy.chisaki.yoshioka.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目（null = 全項目）
                null, // フィルタ条件（null = フィルタなし）
                null, // フィルタ用パラメータ
                null // ソート (nullソートなし）
        )


        var photo_num = -1
        val photo_uri_array = arrayListOf<String>()
        var photo_select = 0
        var count_start = 0

        var mTimer: Timer? = null
        var mHandler = Handler()

        if (cursor!!.moveToFirst()) {
            do {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photo_num += 1
                //Log.d("AN_LOG", "URI : " + imageUri.toString())
                photo_uri_array.add(imageUri.toString())
                //imageView.setImageURI(imageUri)
                //Log.d("AN_LOG","array["+photo_num.toString()+"]:"+photo_uri_array[photo_num])
            } while (cursor.moveToNext())
        }
        cursor.close()
        imageView.setImageURI(Uri.parse(photo_uri_array[photo_select]))

        next_button.setOnClickListener{
            photo_select += 1
            Log.d("AN_LOG", "photo_select : " + photo_select.toString())
            if(photo_select>photo_num){//array[枚数]は存在しない arrayは0から始まるから
                photo_select =0//array[0] 最初の写真を指定
            }
            imageView.setImageURI(Uri.parse(photo_uri_array[photo_select]))
        }

        back_button.setOnClickListener{
            photo_select += -1
            Log.d("AN_LOG", "photo_select : " + photo_select.toString())
            if(photo_select<0){//array[-1]は存在しない
                photo_select =photo_num//array[枚数-1] 最後の写真を指定
            }
            imageView.setImageURI(Uri.parse(photo_uri_array[photo_select]))
        }

        start_button.setOnClickListener{

            count_start +=1
            if(count_start % 2 == 1) {

                next_button.isEnabled=false
                back_button.isEnabled=false

                start_button.text="停止"

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            photo_select += 1
                            Log.d("AN_LOG", "photo_select : " + photo_select.toString())
                            if(photo_select>photo_num){//array[枚数]は存在しない arrayは0から始まるから
                                photo_select =0//array[0] 最初の写真を指定
                            }
                            imageView.setImageURI(Uri.parse(photo_uri_array[photo_select]))
                        }
                    }
                }, 2000, 2000)// 始動させるまで2秒, ループ間隔を2秒に設定
            }else if(count_start % 2 == 0) {
                next_button.isEnabled=true
                back_button.isEnabled=true
                start_button.text="再生"
                mTimer!!.cancel()
            }
        }




    }

}
