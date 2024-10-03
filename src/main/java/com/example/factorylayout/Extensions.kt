package com.example.factorylayout

import com.example.factorylayout.model.FactoryObject
import java.time.LocalDate

fun dateCheck(fo: FactoryObject, date: LocalDate) = fo.dateStart <= date && fo.dateEnd >= date