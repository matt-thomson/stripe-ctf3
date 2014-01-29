package com.stripe.ctf.instantcodesearch

import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}
import org.json4s._
import org.json4s.jackson.Serialization._
import com.google.common.base.Charsets
import java.io.File

case class SearchResponse(success: Boolean, results: List[String])

class SearchMasterServer(port: Int, id: Int) extends AbstractSearchServer(port, id) {
  implicit val formats = DefaultFormats

  val NumNodes = 3

  def this(port: Int) { this(port, 0) }

  val clients = (1 to NumNodes)
    .map { id => new SearchServerClient(port + id, id)}
    .toArray

  override def isIndexed() = {
    val responsesF = Future.collect(clients.map {client => client.isIndexed()})
    val successF = responsesF.map {responses => responses.forall { response =>

        (response.getStatus == HttpResponseStatus.OK
          && response.getContent.toString(Charsets.UTF_8).contains("true"))
      }
    }
    successF.map {success =>
      if (success) {
        successResponse()
      } else {
        errorResponse(HttpResponseStatus.BAD_GATEWAY, "Nodes are not indexed")
      }
    }.rescue {
      case ex: Exception => Future.value(
        errorResponse(HttpResponseStatus.BAD_GATEWAY, "Nodes are not indexed")
      )
    }
  }

  override def healthcheck() = {
    val responsesF = Future.collect(clients.map {client => client.healthcheck()})
    val successF = responsesF.map {responses => responses.forall { response =>
        response.getStatus == HttpResponseStatus.OK
      }
    }
    successF.map {success =>
      if (success) {
        successResponse()
      } else {
        errorResponse(HttpResponseStatus.BAD_GATEWAY, "All nodes are not up")
      }
    }.rescue {
      case ex: Exception => Future.value(
        errorResponse(HttpResponseStatus.BAD_GATEWAY, "All nodes are not up")
      )
    }
  }

  override def index(path: String) = {
    System.err.println(
      "[master] Requesting " + NumNodes + " nodes to index path: " + path
    )

    val responses = clients.map { client => client.index(path) }

    Future.collect(responses).map {_ => successResponse() }
  }

  override def query(q: String) = {
    val responses = clients.map {client => client.query(q)}

    Future.collect(responses).map(aggregate)
  }

  private def aggregate(responses: Seq[HttpResponse]) = {
    val results = responses.flatMap { rsp => read[SearchResponse](rsp.getContent.toString(Charsets.UTF_8)).results }
      .map (toMatch)
      .toSet

    querySuccessResponse(results)
  }

  private def toMatch(result: String): Match = {
    val parts = result.split(":")
    Match(parts(0), parts(1).toInt)
  }
}

