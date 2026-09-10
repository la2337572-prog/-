package com.example

import com.example.ui.screens.formatSar
import com.example.viewmodel.SortOption
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun sarFormatting_isCorrect() {
    val formatted = formatSar(1250.0)
    assertEquals("1250 ر.س", formatted)
  }

  @Test
  fun sortOptionTitles_areArabic() {
    assertEquals("الأكثر شعبية", SortOption.POPULAR.titleAr)
    assertEquals("السعر: من الأقل", SortOption.PRICE_ASC.titleAr)
    assertEquals("السعر: من الأعلى", SortOption.PRICE_DESC.titleAr)
  }
}
