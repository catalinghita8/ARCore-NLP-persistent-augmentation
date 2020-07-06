package com.example.arvoice.data

import com.example.arvoice.domain.AugmentedRoom

interface IStorageClient {

    fun storeAugmentedRoom(room: AugmentedRoom): Int

    fun getAugmentedRoom(shortCode: Int): AugmentedRoom?
}
