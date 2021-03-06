package cc.abstra.scuby.test

import org.specs2.mutable.SpecificationWithJUnit
import cc.abstra.scuby._
import JRuby._
import org.specs2.specification.BeforeExample
import javax.swing.JLabel
import org.jruby.runtime.builtin.IRubyObject

class BasicTest extends SpecificationWithJUnit  {
  "Scuby" should {
    "evaluate ruby code" in {
      eval[Long]("1 + 1") must beEqualTo(2)
    }

    "send methods to Ruby objects" in {
      val array1:RubyObj = eval("[1,2,3]")
      val length = array1.send[Long]('length)
      length must beEqualTo(3)
    }

    "send methods to Ruby objects when the method exists" in {
      val array1:RubyObj = eval("[1,2,3]")
      val length = array1.sendOpt[Long]('length)
      length must beEqualTo(Some(3))
    }

    "send methods to Ruby objects" in {
      val array1:RubyObj = eval("[1,2,3]")
      val length = array1.sendOpt[Long]('non_existent_method)
      length must beEqualTo(None)
    }

    "create Ruby objects of a given class with new RubyObject" in {
      val array1:RubyObj = eval("[]")
      val array2 = new RubyObject('Array)
      array1 must beEqualTo(array2)
    }

    "create Ruby objects of a given class with RubyClass" in {
      val array1:RubyObj = eval("[]")
      val array2 = RubyClass('Array) ! 'new
      array1 must beEqualTo(array2)
    }

    "create Ruby Symbols" in {
      val sym:RubyObj = eval(":foo")
      %('foo) must beEqualTo(sym)
    }

    "implicitly convert Scala symbols to Ruby symbols" in {
      val sym:RubyObj = eval(":foo")
      val sym2:RubyObj = 'foo
      sym must beEqualTo(sym2)
    }

    "forward require to Ruby" in {
      require("test.rb")
      val array1:RubyObj = eval("[]")
      val array2:RubyObj = eval("get_empty_array")
      array1 must beEqualTo(array2)
    }

    "be able to check if a Ruby object is of a given class" in {
      val array = eval[RubyObj]("[]")
      array.isA_?('Array) must beTrue
      array.isA_?('Hash) must beFalse
      array.isA_?('Object) must beTrue
    }

    "be able to check if a Ruby object responds to a method" in {
      val array = eval[RubyObj]("[]")
      array.respondTo_?('length) must beTrue
      array.respondTo_?('foo) must beFalse
      array.respondTo_?("[]") must beTrue
    }

    "retrieve Ruby Array elements with parentheses" in {
      val array1:RubyObj = eval("['foo','bar','baz']")
      array1(0) must beEqualTo("foo")
      array1(1) must beEqualTo("bar")
      array1(2) must beEqualTo("baz")
    }

    "retrieve Ruby Hash elements with parentheses" in {
      val hash:RubyObj = eval("{ :foo => 1, :bar => 2, :baz => 3 }")
      hash('foo) must beEqualTo(1)
    }

    "chain Hash calls" in {
      val hash:RubyObj = eval(
                                """{ :foo => {:x => 'foo x', :y => 'foo y'},
                                     :bar => {:x => 'bar x', :y => 'bar y'},
                                     :baz => {:x => 'baz x', :y => 'baz y'} }""")
      hash('foo, 'x) must beEqualTo("foo x")
      hash('bar, 'y) must beEqualTo("bar y")
    }
  }
}

class ExtendedTest extends SpecificationWithJUnit with BeforeExample {
  class ScalaPerson(val firstname:String="Anonymous", var greeted: String = null) {
    def greet(greeting: String) { greeted = greeting }
  }

  var backend: RubyObj = null

  def before = {
    require("test.rb")
    backend = RubyClass('Backend)
  }

  "Scuby" should {
    "create an object with parameters using the RubyObject constructor" in {
      val person = new RubyObject('Person, "Eccentrica", "Gallumbits")
      val firstName:String = person.firstname
      val lastName:String = person.lastname
      firstName must beEqualTo("Eccentrica")
      lastName must beEqualTo("Gallumbits")
    }

    "create an object with parameters using the RubyClass new method" in {
      val slarti1 = RubyClass('Person) ! ('new, "Slartibartfast", "<No last name>")
      val slarti2 = new RubyObject('Person, "Slartibartfast", "<No last name>")
      slarti1 must beEqualTo(slarti2)
    }

    "forward toString to to_s" in {
      val deepThought = new RubyObject('Person, "Deep", "Thought")
      deepThought.toString must beEqualTo("Person(Thought, Deep)")
    }

    "forward hashCode to hash" in {
      val vogon = new RubyObject('Person, "Prostetnic", "Vogon Jeltz")
      val hash1 = vogon.hashCode
      val hash2:Long = vogon send 'hash
      hash1 must beEqualTo(hash2)
    }

    "add elements to an Array" in {
      val trillian = new RubyObject('Person, "Tricia", "McMillan")
      val people:RubyObject = backend.get_people
      people(3) = trillian

      val length:Long = people.length
      length must beEqualTo(4)

      people(3) must beEqualTo(trillian)
    }

    "call a method on a Ruby object or class and get back another Ruby object" in {
      val ford1 = backend.get_person("Ford")
      val ford2 = new RubyObject('Person, "Ford", "Prefect")
      ford1 must beEqualTo(ford2)
    }

    "call a method with no parameters" in {
      val people = backend ! 'get_people
      val zaphod1 = people(0).asInstanceOf[RubyObj]
      val zaphod2 = new RubyObject('Person, "Zaphod", "Beeblebrox")
      zaphod1 must beEqualTo(zaphod2)
    }

    "get a reference to a Ruby method that can later be used" in {
      val getPerson = backend --> 'get_person
      val arthur1 = getPerson("Arthur")
      val arthur2 = backend ! ('get_person, "Arthur")
      arthur1 must beEqualTo(arthur2)
    }

    "call a Ruby method which returns a Java object" in {
      val zaphod = new RubyObject('Person, "Zaphod", "Beeblebrox")
      val label:JLabel = zaphod.send[JLabel]('get_label)
      label.getText must beEqualTo("Zaphod Beeblebrox")
    }

    "call a Ruby method that interacts with a Scala object" in {
      val zaphod = new RubyObject('Person, "Zaphod", "Beeblebrox")
      val ford = new ScalaPerson
      zaphod.greet(ford)
      ford.greeted must beEqualTo("Hi, I'm Zaphod")
    }

    "call a Ruby method with multiple parameters" in {
      val zaphod = new RubyObject('Person, "Zaphod", "Beeblebrox")
      val ford = new ScalaPerson("Ford")
      val barney = new ScalaPerson("Barney")
      zaphod.introduce(barney, ford)
      barney.greeted must beEqualTo("Hi, I'm Zaphod. Have you met Ford?")
    }


    "use a Scala function as a Ruby block" in {
      backend.get_person("Zaphod")
      val allPeople = new scala.collection.mutable.MutableList[AnyRef]
      backend.everybody { p: Array[IRubyObject] =>
        allPeople += p.head.asInstanceOf[AnyRef]
        p.head
      }
      val res:RubyObj = backend.get_people
      val javaArr:Array[AnyRef] = res.to_java
      allPeople must beEqualTo(javaArr.to[Vector])
    }

    "wrap a JRuby object in a Scala trait" in {
      trait Person {
        def firstname: String
        def firstname_=(f: String): Unit
        def lastname: String
        def lastname_=(l: String): Unit
        def fullname: String
        def getLabel: JLabel
      }

      val zaphod:Person = new RubyObject('Person, "Zaphod", "Beeblebrox").as[Person]
      zaphod.firstname must beEqualTo("Zaphod")
      zaphod.firstname = "The Zeeb"
      zaphod.firstname must beEqualTo("The Zeeb")
      zaphod.fullname must beEqualTo("The Zeeb Beeblebrox")
      zaphod.getLabel.getText must beEqualTo("The Zeeb Beeblebrox")
    }

    "wrap a JRuby array in a Scala trait" in {
      trait RubyArray {
        def <<(obj: AnyRef): Unit
        def join(s:String): String
      }
      val array = new RubyObject('Array).as[RubyArray]
      array << "Number One"
      array << "Number Two"
      array.join(",") must beEqualTo("Number One,Number Two")
    }
  }
}
