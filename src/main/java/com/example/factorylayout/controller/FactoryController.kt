package com.example.factorylayout.controller

import com.example.factorylayout.SingletonData

class FactoryController {

    private val data = SingletonData.getInstance()
    private var factory = data.getFactoryLayout()
    private val openedFromFileFlag = data.getCreationFlag()



}
