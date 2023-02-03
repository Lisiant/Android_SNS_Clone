package online.dailyq.api

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

/**
 * Content Resolver 로 MultipartBody.Part를 생성할 때 필요한(value 값으로) RequestBody를 리턴
 *
 * writeTo의 sink에 데이터를 넣어주면 이미지를 넣을 수 있다.
 */
fun Uri.asRequestBody(cr: ContentResolver): RequestBody {
    return object : RequestBody() {
        override fun contentType(): MediaType? = cr.getType(this@asRequestBody)?.toMediaTypeOrNull()

        override fun contentLength(): Long = -1

        override fun writeTo(sink: BufferedSink) {
            val source = cr.openInputStream(this@asRequestBody)?.source()
            source?.use { sink.writeAll(it) }
        }

    }
}