package org.davidbild.tristate

import org.scalacheck.*

import scala.annotation.targetName
import scala.reflect.ClassTag

class SpecLite extends Properties("") {
  
 // override val name: String = this.getClass.getName.stripSuffix("$")

  def checkAll(name: String, props: Properties): Unit = {
    for ((name2, prop) <- props.properties) yield {
      property(name + ":" + name2) = prop
    }
  }

  def checkAll(props: Properties): Unit = {
    for ((name, prop) <- props.properties) yield {
      property(name) = prop
    }
  }

  class PropertyOps(props: Properties) {
    def withProp(propName: String, prop: Prop): Properties = {
      val p = new Properties(props.name)
      for {(name, x) <- props.properties} property(name) = x
      p.property(propName) = prop
      p
    }
  }

  implicit def enrichProperties(props: Properties): PropertyOps = new PropertyOps(props)

  private var context: String = ""

  class StringOps(s: String) {
    def should[A](a: => Any): Unit = {
      val saved = context
      context = s; try a finally context = saved
    }

    def ![A](a: => A)(implicit ev: (A) => Prop): Unit = in(a)

    def in[A](a: => A)(implicit ev: (A) => Prop): Unit = property(context + ":" + s) = Prop(ev(a)(_))
  }

  implicit def enrichString(s: String): StringOps = new StringOps(s)

  def check(x: => Boolean): Prop = {
    Prop.secure(x must_== true)
  }

  def fail(msg: String): Nothing = throw new AssertionError(msg)

  class AnyOps[A](actual: => A) {
    @targetName("mustEqual")
    def must_==(expected: A): Unit = {
      val act = actual
      def test = expected == act
      def koMessage = "%s !== %s".format(act, expected)
      if (!test)
        fail(koMessage)
    }

    def mustMatch(f: PartialFunction[A, Boolean]): Unit = {
      val act = actual
      def test = f.isDefinedAt(act) && f(act)
      def koMessage = "%s does not satisfy partial function".format(act)
      if (!test)
        fail(koMessage)
    }

    def and[B](b: => B): B = {
      actual
      b
    }

    @targetName("mustBeInferiorTo")
    def mustBe_<(x: Int)(implicit ev: A <:< Int): Unit = {
      val act = actual
      def test = ev(act) < x
      def koMessage = "%s <! %s".format(actual, x)
      if (!test)
        fail(koMessage)
    }

    def mustThrowA[T <: Throwable](implicit man: ClassTag[T]): Unit = {
      val erasedClass = man.runtimeClass
      try {
        actual
        fail("no exception thrown, expected " + erasedClass)
      } catch {
        case ex: Throwable =>
          if (!erasedClass.isInstance(ex))
            fail("wrong exception thrown, expected: " + erasedClass + " got: " + ex)
      }
    }
  }
  implicit def enrichAny[A](actual: => A): AnyOps[A] = new AnyOps(actual)

  def prop[T, R](result: T => R)(implicit toProp: (=>R) => Prop, a: Arbitrary[T], s: Shrink[T]): Prop = check1(result)
  implicit def propToProp(p: => Prop): Prop = p
  implicit def check1[T, R](result: T => R)(implicit toProp: (=>R) => Prop, a: Arbitrary[T], s: Shrink[T]): Prop = Prop.forAll((t: T) => toProp(result(t)))
  implicit def unitToProp(u: => Unit): Prop = booleanToProp({u; true})
  implicit def unitToProp2(u: Unit): Prop = booleanToProp(true)
  implicit def booleanToProp(b: => Boolean): Prop = Prop.secure(b)

  /**
   * Most of our scalacheck tests use (Int => Int). This generator includes non-constant
   * functions (id, inc), to have a better chance at catching bugs.
   */
  implicit def Function1IntInt[A](implicit A: Arbitrary[Int]): Arbitrary[Int => Int] =
    Arbitrary(Gen.frequency[Int => Int](
      (1, Gen.const((x: Int) => x)),
      (1, Gen.const((x: Int) => x + 1)),
      (3, A.arbitrary.map(a => (_: Int) => a))
    ))

}
