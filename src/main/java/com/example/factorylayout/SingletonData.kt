package com.example.factorylayout

import com.example.factorylayout.model.Factory

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
    private var openedFromFile = true

    fun getFactoryLayout(): Factory {
        return factory
    }

    fun setFactoryLayout(f: Factory){
        this.factory = f
    }

    fun getCreationFlag(): Boolean{
        return openedFromFile
    }

    fun setCreationFlag(flag: Boolean){
        this.openedFromFile = flag
    }
}