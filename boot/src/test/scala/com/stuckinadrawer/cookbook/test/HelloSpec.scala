package com.stuckinadrawer.cookbook.test

import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HelloSpec extends AnyFlatSpec with Matchers {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should be(2)
    stack.pop() should be(1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a[NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }

  "Hello" should "be printed" in {
    assert(true)
  }
}
