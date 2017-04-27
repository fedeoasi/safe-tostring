package com.github.fedeoasi

import org.scalatest.{FunSpec, Matchers}

class SafeToStringTest extends FunSpec with Matchers {
  it("does not support regular classes") {
    """@safeToString class PlainClass(username: String, @hidden password: String)""" shouldNot compile
  }

  it("works as the old toString for fields that are not hidden") {
    @safeToString case class NoHiddenFields(username: String, age: Int)
    NoHiddenFields("user", 30).toString shouldBe "NoHiddenFields(user,30)"
  }

  it("obscures a field annotated as @hidden") {
    @safeToString case class Credentials(username: String, @hidden password: String)

    Credentials("username", "pwd").toString shouldBe "Credentials(username,****)"
    Credentials("other", "pwd").toString shouldBe "Credentials(other,****)"
  }

  it("obscures a field annotated as @hidden that appears first") {
    @safeToString case class Credentials(@hidden username: String, password: String)

    Credentials("username", "pwd").toString shouldBe "Credentials(****,pwd)"
    Credentials("other", "pwd").toString shouldBe "Credentials(****,pwd)"
  }
}
