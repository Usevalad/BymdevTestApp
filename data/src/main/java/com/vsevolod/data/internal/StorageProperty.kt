package com.vsevolod.data.internal

import com.vsevolod.core.gateways.LocalStorage
import com.orhanobut.hawk.Hawk
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty


private object StorageEngine {

    private val keys = mutableMapOf<Class<*>, ClassKeys>()

    fun <T> get(
            owner: Any,
            property: KProperty<*>,
            suffix: String = "",
            defaultValue: T?
    ): T =
            Hawk.get<T>(
                    getKey(owner, property, suffix),
                    defaultValue
            )

    fun <T> set(
            owner: Any,
            property: KProperty<*>,
            suffix: String = "",
            value: T
    ) {
        Hawk.put(
                getKey(owner, property, suffix),
                value
        )
    }

    private fun getKey(owner: Any, property: KProperty<*>, suffix: String) =
            keys.getOrPut(owner.javaClass) { ClassKeys(owner.javaClass.name) }
                    .keyBySuffix(property, suffix)


    private class ClassKeys(
            private val className: String
    ) {

        private val propertiesKeys = mutableMapOf<KProperty<*>, PropertyKeys>()

        fun keyBySuffix(
                property: KProperty<*>,
                suffix: String
        ) = propertiesKeys
                .getOrPut(property) { PropertyKeys(className, property.name) }
                .keyBySuffix(suffix)
    }


    private class PropertyKeys(
            private val className: String,
            private val propertyName: String
    ) {

        private val suffixesKeys = mutableMapOf<String, String>()

        fun keyBySuffix(suffix: String) =
                suffixesKeys.getOrPut(suffix) { "$className$$propertyName$suffix" }
    }
}


internal class StorageProperty<T>(
        private val defaultValue: T? = null
) {

    operator fun getValue(thisRef: Any, property: KProperty<*>): T =
            StorageEngine.get(thisRef, property, defaultValue = defaultValue)

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        StorageEngine.set(thisRef, property, value = value)
    }
}


internal class ExpirableProperty<T : Any>(
        private val defaultValue: T?,
        expiresAfter: Long,
        expirationTimeUnit: TimeUnit
) {

    private val expiresAfterMillis = expirationTimeUnit.toMillis(expiresAfter)

    operator fun getValue(thisRef: Any, property: KProperty<*>): LocalStorage.ExpirableValue<T> {

        val value = StorageEngine.get(thisRef, property, defaultValue = defaultValue)
        val timeStamp = StorageEngine.get(thisRef, property, SUFFIX_TIME_STAMP, 0L)

        return LocalStorage.ExpirableValue(
                value,
                System.currentTimeMillis() - timeStamp >= expiresAfterMillis
        )
    }

    operator fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            expirableValue: LocalStorage.ExpirableValue<T>
    ) {
        StorageEngine.set(thisRef, property, value = expirableValue.value)
        StorageEngine.set(thisRef, property, SUFFIX_TIME_STAMP, System.currentTimeMillis())
    }


    companion object {
        const val SUFFIX_TIME_STAMP = ".timeStamp"
    }
}
