package com.example.factorylayout.data

import com.example.factorylayout.model.Factory
import com.example.factorylayout.model.FactoryObject

class SingletonData private constructor(){
    companion object {

        @Volatile
        private var instance: SingletonData? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: SingletonData().also { instance = it }
            }
    }

    private lateinit var factory: Factory
    private lateinit var factoryObject: FactoryObject
    private var fileName = ""

    fun getFactoryLayout(): Factory {
        return factory
    }

    fun setFactoryLayout(f: Factory){
        this.factory = f
    }

    fun getFactoryObject(): FactoryObject{
        return factoryObject
    }

    fun setFactoryObject(fo: FactoryObject){
        this.factoryObject = fo
    }

    fun getFileName(): String {
        return fileName
    }

    fun setFileName(name: String){
        this.fileName = name
    }



}