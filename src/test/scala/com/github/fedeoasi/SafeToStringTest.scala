package com.github.fedeoasi

import org.scalatest.{FunSpec, Matchers}

@safeToString case class Credentials(username: String, @hidden password: String)

class SafeToStringTest extends FunSpec with Matchers {
  it("does not support regular classes") {
    """@safeToString class PlainClass(username: String, @hidden password: String)""" shouldNot compile
  }

  it("does not support annotating methods") {
    """case class CredentialsWithAnnotatedMethod(username: String, @hidden password: String) {
      |  @safeToString def hello = s"Hello $username"
      |}""".stripMargin shouldNot compile
  }

  it("does not compile when a toString is already overridden") {
    """@safeToString case class CredentialsWithOverriddenToString(username: String, @hidden password: String) {
      |  override def toString: String = s"Hello $username"
      |}""".stripMargin shouldNot compile
  }

  it("works as the old toString for fields that are not hidden") {
    @safeToString case class NoHiddenFields(username: String, age: Int)
    NoHiddenFields("user", 30).toString shouldBe "NoHiddenFields(user,30)"
  }

  it("obscures a field annotated as @hidden") {
    Credentials("username", "pwd").toString shouldBe "Credentials(username,****)"
    Credentials("other", "pwd").toString shouldBe "Credentials(other,****)"
  }

  it("obscures a field annotated as @hidden that appears first") {
    @safeToString case class OtherCredentials(@hidden username: String, password: String)
    OtherCredentials("username", "pwd").toString shouldBe "OtherCredentials(****,pwd)"
  }

  it("works with nested case classes without the need to annotate outer classes") {
    case class Config(credentials: Credentials)
    Config(Credentials("username", "pwd")).toString shouldBe "Config(Credentials(username,****))"
  }
}
