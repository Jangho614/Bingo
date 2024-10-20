/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.audio

import android.content.Context
import android.media.AudioRecord
import android.os.SystemClock
import android.util.Log
import com.example.myapp_230604.AudioClassificationListener
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
    var currentDelegate: Int = 0,
    var numThreads: Int = 2
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
        // 기본 옵션 설정 (사용할 쓰레드 수 설정 등)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numThreads)

        // 하드웨어 선택: 기본적으로 CPU 사용, 필요시 NNAPI 사용 설정
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // 기본값으로 CPU 사용
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }

        // 분류기와 반환될 결과에 대한 옵션 설정
        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold) // 분류 임계값 설정
            .setMaxResults(numOfResults) // 반환될 최대 결과 수
            .setBaseOptions(baseOptionsBuilder.build()) // 기본 옵션 적용
            .build()

        try {
            // 분류기 및 관련 객체 생성
            classifier = AudioClassifier.createFromFileAndOptions(context, currentModel, options)
            tensorAudio = classifier.createInputTensorAudio() // 오디오 입력 텐서 생성
            recorder = classifier.createAudioRecord() // 오디오 레코더 생성
            startAudioClassification() // 오디오 분류 시작
        } catch (e: IllegalStateException) {
            listener.onError(
                "오디오 분류기 초기화에 실패했습니다. 자세한 오류는 로그를 확인하세요."
            )
            Log.e("AudioClassification", "TFLite 로드 중 오류 발생: " + e.message)
        }
    }

    fun startAudioClassification() {
        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return // 이미 녹음 중이라면 중복 실행 방지
        }

        recorder.startRecording() // 녹음 시작
        executor = ScheduledThreadPoolExecutor(1) // 스레드 풀 실행

        // 각 모델은 특정한 오디오 길이를 기대함. 이 공식은 입력 버퍼 크기와 샘플 속도로 길이 계산
        // 예: YAMNET 모델은 0.975초 길이의 녹음을 기대함
        val lengthInMilliSeconds = ((classifier.requiredInputBufferSize * 1.0f) /
                classifier.requiredTensorAudioFormat.sampleRate) * 1000

        val interval = (lengthInMilliSeconds * (1 - overlap)).toLong()

        executor.scheduleAtFixedRate(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS) // 분류 작업을 일정 간격으로 실행
    }

    private fun classifyAudio() {
        tensorAudio.load(recorder) // 오디오 데이터를 텐서에 로드
        var inferenceTime = SystemClock.uptimeMillis()
        val output = classifier.classify(tensorAudio) // 오디오 분류 실행
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        listener.onResult(output[0].categories, inferenceTime) // 결과를 리스너에 전달
    }

    fun stopAudioClassification() {
        recorder.stop() // 녹음 중지
        executor.shutdownNow() // 스레드 풀 종료
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_NNAPI = 1
        const val DISPLAY_THRESHOLD = 0.3f
        const val DEFAULT_NUM_OF_RESULTS = 2
        const val DEFAULT_OVERLAP_VALUE = 0.5f
        const val YAMNET_MODEL = "recycle.tflite"
    }
}
