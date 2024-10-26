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
    private lateinit var classifier: AudioClassifier // 오디오 분류기 객체
    private lateinit var tensorAudio: TensorAudio // 입력 텐서 오디오 객체
    private lateinit var recorder: AudioRecord // 오디오 녹음기 객체
    private lateinit var executor: ScheduledThreadPoolExecutor // 스케줄된 스레드 풀 실행기

    private val classifyRunnable = Runnable {
        classifyAudio() // 오디오 분류 실행
    }

    init {
        initClassifier() // 클래스 초기화 시 분류기 설정
    }

    fun initClassifier() {
        // 기본 옵션 설정
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(numThreads) // 사용할 스레드 수 설정

        // 현재 delegate 설정
        when (currentDelegate) {
            DELEGATE_CPU -> { /* 기본값으로 CPU 사용 */ }
            DELEGATE_NNAPI -> { baseOptionsBuilder.useNnapi() } // NNAPI 사용
        }

        // 오디오 분류기 옵션 설정
        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(classificationThreshold) // 분류 점수 임계값 설정
            .setMaxResults(numOfResults) // 최대 결과 수 설정
            .setBaseOptions(baseOptionsBuilder.build()) // 기본 옵션 설정
            .build()

        try {
            // 오디오 분류기 생성
            classifier = AudioClassifier.createFromFileAndOptions(context, currentModel, options)
            tensorAudio = classifier.createInputTensorAudio() // 입력 텐서 오디오 생성
            recorder = classifier.createAudioRecord() // 오디오 녹음기 생성
            startAudioClassification() // 오디오 분류 시작
        } catch (e: IllegalStateException) {
            listener.onError("오디오 분류기 초기화에 실패했습니다. 자세한 오류는 로그를 확인하세요.") // 오류 처리
            Log.e("AudioClassification", "TFLite 로드 중 오류 발생: ${e.message}") // 로그 출력
        }
    }

    fun startAudioClassification() {
        if (!::recorder.isInitialized) {
            Log.e("AudioClassification", "Recorder is not initialized") // recorder가 초기화되지 않은 경우 오류 로그
            return // 메서드 종료
        }

        if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            return // 이미 녹음 중이라면 중복 실행 방지
        }

        recorder.startRecording() // 오디오 녹음 시작
        executor = ScheduledThreadPoolExecutor(1) // 스케줄러 생성

        // 입력 버퍼 크기에 따라 지연 시간 계산
        val lengthInMilliSeconds = ((classifier.requiredInputBufferSize * 1.0f) /
                classifier.requiredTensorAudioFormat.sampleRate) * 1000

        // 중복 오버랩을 고려하여 간격 계산
        val interval = (lengthInMilliSeconds * (1 - overlap)).toLong()

        // 주기적으로 classifyRunnable 실행
        executor.scheduleAtFixedRate(
            classifyRunnable,
            0,
            interval,
            TimeUnit.MILLISECONDS
        )
    }

    fun stopAudioClassification() {
        // 녹음 중인 경우 녹음 중지
        if (::recorder.isInitialized && recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            recorder.stop()
        }
        // 스케줄러 종료
        if (::executor.isInitialized) {
            executor.shutdownNow()
        }
    }

    private fun classifyAudio() {
        Log.d("AudioTF","ClassificationStart") // 분류 시작 로그
        tensorAudio.load(recorder) // 오디오 데이터 로드
        var inferenceTime = SystemClock.uptimeMillis() // 추론 시작 시간 기록
        val output = classifier.classify(tensorAudio) // 분류 실행
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime // 추론 시간 계산

        // 결과를 Listener에 전달
        listener.onResult(output[0].categories, inferenceTime)
        Log.d("AudioTF", output[0].categories.toString()) // 결과 로그 출력
    }

    // 모델 설정값 지정
    companion object {
        const val DELEGATE_CPU = 0 // CPU delegate
        const val DELEGATE_NNAPI = 1 // NNAPI delegate
        const val DISPLAY_THRESHOLD = 0.8f // 점수 임계값
        const val DEFAULT_NUM_OF_RESULTS = 1 // 기본 결과 수
        const val DEFAULT_OVERLAP_VALUE = 0.5f // 기본 오버랩 값
        const val YAMNET_MODEL = "recycle3.tflite" // 사용할 모델 파일명
    }
}
