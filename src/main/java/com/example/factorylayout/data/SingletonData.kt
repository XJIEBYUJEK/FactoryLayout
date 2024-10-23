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
    private var objectId = -1

    fun getFactoryLayout() = factory

    fun setFactoryLayout(f: Factory){
        this.factory = f
    }

    fun getFactoryObject() = factoryObject

    fun setFactoryObject(fo: FactoryObject){
        this.factoryObject = fo
    }

    fun getFileName() = fileName

    fun setFileName(name: String){
        this.fileName = name
    }

    fun getObjectId() = objectId

    fun setObjectId(id: Int){
        this.objectId = id
    }

}