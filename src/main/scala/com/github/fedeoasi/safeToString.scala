package com.github.fedeoasi

import scala.annotation.StaticAnnotation
import scala.meta._

class hidden extends StaticAnnotation

class safeToString extends StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val (cls, companion) = defn match {
      case q"${cls: Defn.Class}; ${companion: Defn.Object}" => (cls, companion)
      case cls: Defn.Class => (cls, q"object ${Term.Name(cls.name.value)}")
      case _ => abort(s"@${getClass.getSimpleName} must annotate a class")
    }

    if (!cls.mods.map(_.toString()).contains("case")) {
      abort("Only case classes are supported")
    }

    val params = cls.ctor.paramss.flatten.map { param =>
      val hidden = param.mods.map(_.toString()).contains("@hidden")
      (param.name, hidden)
    }

    val lastIndex = params.size - 1
    val paramStatements = params.zipWithIndex.map { case ((paramName, hidden), index) =>
      if (!hidden) {
        if (index != lastIndex) {
          q"""sb.append(${Term.Name(paramName.value)}).append(${Lit.String(",")})"""
        } else {
          q"""sb.append(${Term.Name(paramName.value)})"""
        }
      } else {
        val stringToAppend = if (index != lastIndex) {
          "****,"
        } else {
          "****"
        }
        q"""sb.append(${Lit.String(stringToAppend)})"""
      }
    }

    val newToString =
      q"""override def toString(): String = {
            val sb = new StringBuilder
            sb.append(${Lit.String(cls.name.toString)})
            sb.append(${Lit.String("(")})
            ..$paramStatements
            sb.append(${Lit.String(")")})
            sb.toString
          }"""

    val newCls = cls.copy(
      templ = cls.templ.copy(stats = Some(cls.templ.stats.getOrElse(Nil) ++ Seq(newToString))))
    q"$newCls; $companion"
  }
}
