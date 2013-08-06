package cc.abstra.scuby

import org.jruby.runtime.BlockCallback
import org.jruby.runtime.ThreadContext
import org.jruby.runtime.builtin.IRubyObject
import org.jruby.runtime.Block

class ScalaFunctionAsRubyBlockCallback(func: Array[IRubyObject] => IRubyObject) extends BlockCallback {
	def call(context: ThreadContext, args: Array[IRubyObject], block: Block): IRubyObject = {
	  func(args)
	}
}