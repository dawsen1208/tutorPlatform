package com.example.teacher

import org.junit.Test

import org.junit.Assert.*
import com.example.teacher.ui.screens.buildAddressText

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun buildAddressText_joinsPoiAndDetail() {
        val text = buildAddressText(poiName = "某某小区", poiAddress = "北京市朝阳区某街道", detail = "1号楼2单元301")
        assertEquals("某某小区 北京市朝阳区某街道 1号楼2单元301", text)
    }

    @Test
    fun buildAddressText_allowsOnlyDetail() {
        val text = buildAddressText(poiName = null, poiAddress = null, detail = "1号楼2单元301")
        assertEquals("1号楼2单元301", text)
    }

    @Test
    fun buildAddressText_deduplicatesSameNameAndAddress() {
        val text = buildAddressText(poiName = "北京市朝阳区某街道", poiAddress = "北京市朝阳区某街道", detail = "")
        assertEquals("北京市朝阳区某街道", text)
    }
}
