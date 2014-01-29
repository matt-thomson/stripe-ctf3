package com.stripe.ctf.instantcodesearch

import scala.collection.JavaConversions._
import com.twitter.concurrent.Broker
import com.google.common.collect.{HashMultimap, Multimaps}

abstract class SearchResult

case class Match(path: String, line: Int) extends SearchResult

case class Done() extends SearchResult


class Index() {
  private val content = Multimaps.synchronizedMultimap(HashMultimap.create[String, Match]())
  private val dictionary = new Dictionary

  def addMatch(word: String, m: Match) = content.put(word, m)

  def search(query: String, b: Broker[SearchResult]) = {
    for (word <- dictionary.findMatches(query); m <- content.get(word)) {
      b !! m
    }

    b !! new Done()
  }
}

