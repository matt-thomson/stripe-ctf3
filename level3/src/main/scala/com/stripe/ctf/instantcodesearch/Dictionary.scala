package com.stripe.ctf.instantcodesearch

import com.google.common.io.Resources
import com.google.common.base.Charsets
import scala.collection.mutable
import scala.collection.JavaConversions._

class Dictionary {
  private val words = Resources.readLines(Resources.getResource("words"), Charsets.UTF_8).map(w => w.trim)
  private val cache = mutable.Map[String, List[String]]()

  def findMatches(word: String): List[String] = cache.get(word) match {
    case Some(matches) => matches
    case None =>
      val matches = words.filter(w => w.contains(word)).toList
      cache += word -> matches
      matches
  }
}
