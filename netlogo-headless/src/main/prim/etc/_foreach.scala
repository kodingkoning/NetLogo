// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.I18N
import org.nlogo.nvm.{ AnonymousProcedure, Command, Context, NonLocalExit }
import org.nlogo.nvm.RuntimePrimitiveException

class _foreach extends Command {
  override def perform(context: Context) {
    var size = 0
    val n = args.length - 1
    val task = argEvalAnonymousCommand(context, n)

    if (args.length == 1) {
      return
    }

    val iters = for (i <- 0 until n) yield {
      val list = argEvalList(context, i)
      if (i == 0) size = list.size
      else if (size != list.size)
        throw new RuntimePrimitiveException(context, this,
          I18N.errors.get("org.nlogo.prim.etc._foreach.listsMustBeSameLength"))
      list.javaIterator
    }
    if (n < task.syntax.minimum)
      throw new RuntimePrimitiveException(
        context, this, AnonymousProcedure.missingInputs(task, n))
    var i = 0
    val actuals = new Array[AnyRef](n)
    try {
      while(i < size && !context.finished) {
        var j = 0
        while(j < n) {
          actuals(j) = iters(j).next()
          j += 1
        }
        task.perform(context, actuals)
        i += 1
      }
      context.ip = next
    }
    catch {
      case _: NonLocalExit if !context.activation.procedure.isReporter =>
        context.stop()
    }
  }
}
