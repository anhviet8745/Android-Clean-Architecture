package com.example.domain.base

import com.example.domain.base.exception.EmptyOutputException
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class UseCaseOutput<SuccessOutput, FailOutput> internal constructor(private val useCaseExecution: UseCaseExecution){
    private var disposables: CompositeDisposable = CompositeDisposable()

    internal abstract fun buildUseCaseObservable(): Single<SuccessOutput>
    internal abstract fun createFailOutput(throwable: Throwable): FailOutput

    fun executeAsync(resultListener: ResultListener<SuccessOutput, FailOutput>): UseCaseTask {
        val observer: DefaultObserver<SuccessOutput, FailOutput> = DefaultObserver(this::createFailOutput)
        observer.addResultListener(resultListener)

        val observable = this.buildUseCaseObservable()
            .subscribeOn(useCaseExecution.execution)
            .observeOn(useCaseExecution.postExecution)

        val disposable = observable.subscribeWith(observer)
        addDisposable(disposable)
        return UseCaseTask(disposable)
    }

    fun executeAsync(
        singleRequest: Single<SuccessOutput>,
        resultListener: ResultListener<SuccessOutput, FailOutput>
    ): UseCaseTask {
        val observer: DefaultObserver<SuccessOutput, FailOutput> = DefaultObserver(this::createFailOutput)
        observer.addResultListener(resultListener)

        val observable = singleRequest
            .subscribeOn(useCaseExecution.execution)
            .observeOn(useCaseExecution.postExecution)

        val disposable = observable.subscribeWith(observer)
        addDisposable(disposable)
        return UseCaseTask(disposable)
    }

    inner class UseCaseResult(val isSuccess: Boolean, val successOutput: SuccessOutput?, val failOutput: FailOutput?)

    fun execute(): UseCaseResult {
        val outputObservable = DefaultObserver<SuccessOutput, FailOutput>(this::createFailOutput)
        val resultListener = ExecuteResultListener<SuccessOutput, FailOutput>()
        outputObservable.addResultListener(resultListener)

        this.buildUseCaseObservable().subscribeWith(outputObservable)
        if (resultListener.successOutput == null && resultListener.failOutput == null) {
            throw EmptyOutputException()
        }
        return UseCaseResult(resultListener.isSuccess, resultListener.successOutput, resultListener.failOutput)
    }

    fun cancel() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        disposables = CompositeDisposable()
    }

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }
}