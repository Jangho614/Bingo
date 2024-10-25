/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapp_230604

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.core.BaseOptions

class AudioClassificationHelper(
    val context: Context,
    val listener: AudioClassificationListener,
    var currentModel: String = YAMNET_MODEL,
    var classificationThreshold: Float = DISPLAY_THRESHOLD,
    var overlap: Float = DEFAULT_OVERLAP_VALUE,
    var numOfResults: Int = DEFAULT_NUM_OF_RESULTS,
    var currentDelegate: Int = DELEGATE_CPU,
    var numThreads: Int = 2,
    val adapter: UserAdapter // UserAdapter 추가
) {
    private lateinit var classifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private lateinit var recorder: AudioRecord
    private lateinit var executor: ScheduledThreadPoolExecutor

    private val classifyRunnable = Runnable {
        classifyAudio()
    }

    init {
        initClassifier() // 클래스 초기화 시 분류기 설정
    }

    fun initClassifier() {
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numThreads)

        when (currentDelegate) {
            DELEGATE_CPU -> { /* 기본값으로 CPU 사용 */ }
            DELEGATE_NNAPI -> { baseOptionsBuilder.useNnapi() }
        }

        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold)
            .setMaxResults(numOfResults)
            .setBaseOptions(baseOptionsBuilder.build())
            .build()

        try {
            classifier = AudioClassifier.createFromFileAndOptions(context, currentModel, options)
            tensorAudio = classifier.createInputTensorAudio()
            recorder = classifier.createAudioRecord()
            startAudioClassification()
        } catch (e: IllegalStateException) {
            listener.onError("오디오 분류기 초기화에 실패했습니다. 자세한 오류는 로그를 확인하세요.")
            Log.e("AudioClassification", "TFLite 로드 중 오류 발생: ${e.message}")
        }
    }

    fun startAudioClassification() {
        if (!::recorder.isInitialized) {
            Log.e("AudioClassification", "Recorder is not initialized")
            return // recorder가 초기화되지 않은 경우 메서드 종료
        }

        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return // 이미 녹음 중이라면 중복 실행 방지
        }

        recorder.startRecording()
        executor = ScheduledThreadPoolExecutor(1)

        val lengthInMilliSeconds = ((classifier.requiredInputBufferSize * 1.0f) /
                classifier.requiredTensorAudioFormat.sampleRate) * 1000

        val interval = (lengthInMilliSeconds * (1 - overlap)).toLong()

        executor.scheduleAtFixedRate(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS
        )
    }

    fun stopAudioClassification() {
        if (::recorder.isInitialized && recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            recorder.stop()
        }
        if (::executor.isInitialized) {
            executor.shutdownNow()
        }
    }

    private fun classifyAudio() {
        Log.d("AudioTF","ClassificationStart")
        tensorAudio.load(recorder)
        var inferenceTime = SystemClock.uptimeMillis()
        val output = classifier.classify(tensorAudio)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

//        adapter.addItem(UserAdapter.Item("", output[0].categories.toString(), "$inferenceTime ms"))
        listener.onResult(output[0].categories, inferenceTime)
        Log.d("AudioTF", output[0].categories.toString())
    }

    // 모델 설정값 지정
    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_NNAPI = 1
        const val DISPLAY_THRESHOLD = 0.7f
        const val DEFAULT_NUM_OF_RESULTS = 1
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        const val YAMNET_MODEL = "recycle2.tflite"
    }
}
