package com.stripe.ctf.instantcodesearch

import com.twitter.util.{Future, Promise, FuturePool}
import com.twitter.concurrent.Broker
import org.jboss.netty.handler.codec.http.{HttpResponseStatus, HttpResponse}

class SearchServer(port : Int, id : Int) extends AbstractSearchServer(port, id) {
  case class Query(q : String, broker : Broker[SearchResult])
  @volatile var index: Option[Index] = None

  override def healthcheck() = {
    Future.value(successResponse())
  }

  override def isIndexed() = index match {
    case Some(_) => Future.value(successResponse())
    case None => Future.value(errorResponse(HttpResponseStatus.OK, "Still indexing"))
  }

  override def index(path: String) = {
    val indexer = new Indexer(path, id % 3)

    FuturePool.unboundedPool {
      System.err.println("[node #" + id + "] Indexing path: " + path)
      index = Some(indexer.index())
    }

    Future.value(successResponse())
  }

  override def query(q: String) = {
    System.err.println("[node #" + id + "] Searching for: " + q)
    handleSearch(q)
  }

  def handleSearch(q: String) = {
    val searches = new Broker[Query]()
    searches.recv foreach { q =>
      FuturePool.unboundedPool {index.get.search(q.q, q.broker)}
    }

    val matches = new Broker[SearchResult]()
    searches ! new Query(q, matches)

    val promise = Promise[HttpResponse]()
    var results = Set[Match]()

    matches.recv foreach {
      case m: Match => results = results + m
      case Done() => promise.setValue(querySuccessResponse(results))
    }

    promise
  }
}
