package com.stripe.ctf.instantcodesearch

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import com.google.common.io.Files.readLines
import com.google.common.base.{CharMatcher, Splitter, Charsets}
import com.google.common.io.LineProcessor
import scala.collection.JavaConversions._

class Indexer(indexPath: String, id: Int) {
  private val root = FileSystems.getDefault.getPath(indexPath)

  def index() : Index = {
    val idx = new Index
    var i = 0

    Files.walkFileTree(root, new SimpleFileVisitor[Path] {
      override def preVisitDirectory(dir : Path, attrs : BasicFileAttributes) : FileVisitResult = {
        if (Files.isHidden(dir) && dir.toString != ".")
          return FileVisitResult.SKIP_SUBTREE
        FileVisitResult.CONTINUE
      }
      override def visitFile(file : Path, attrs : BasicFileAttributes) : FileVisitResult = {
        if (Files.isHidden(file))
          return FileVisitResult.CONTINUE
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
          return FileVisitResult.CONTINUE
        if (Files.size(file) > (1 << 20))
          return FileVisitResult.CONTINUE

        i += 1
        if (i % 3 != id)
          return FileVisitResult.CONTINUE

        readLines(file.toFile, Charsets.UTF_8, new IndexLineProcessor(idx, root.relativize(file).toString))

        FileVisitResult.CONTINUE
      }
    })

    idx
  }
}

class IndexLineProcessor(idx: Index, path: String) extends LineProcessor[Boolean] {
  private val splitter = Splitter.on(" ").trimResults(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings()
  private var lineNum = 1

  def processLine(line: String): Boolean = {
    val m = Match(path, lineNum)
    toWords(line).foreach { word => idx.addMatch(word, m) }
    lineNum += 1

    true
  }

  def getResult = true

  private def toWords(line: String) = splitter.split(line.trim).toList
}
