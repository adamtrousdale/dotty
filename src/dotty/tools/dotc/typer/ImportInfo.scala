package dotty.tools
package dotc
package typer

import ast.{tpd, untpd}
import ast.Trees._
import core._
import util.SimpleMap
import Symbols._, Names._, Denotations._, Types._, Contexts._, StdNames._, Flags._

object ImportInfo {
  /** The import info for a root import from given symbol `sym` */
  def rootImport(sym: Symbol)(implicit ctx: Context) = {
    val expr = tpd.Ident(sym.symTermRef)
    val selectors = untpd.Ident(nme.WILDCARD) :: Nil
    val imp = tpd.Import(expr, selectors)
    new ImportInfo(imp.symbol, selectors, rootImport = true)
  }
}

/** Info relating to an import clause
 *  @param   sym        The import symbol defined by the clause
 *  @param   selectors  The selector clauses
 *  @param   rootImport true if this is one of the implicit imports of scala, java.lang
 *                      or Predef in the start context, false otherwise.
 */
class ImportInfo(val sym: Symbol, val selectors: List[untpd.Tree], val rootImport: Boolean = false)(implicit ctx: Context) {

  /** The (TermRef) type of the qualifier of the import clause */
  def site(implicit ctx: Context): Type = {
    val ImportType(expr) = sym.info
    expr.tpe
  }

  /** The names that are excluded from any wildcard import */
  def excluded: Set[TermName] = { ensureInitialized(); myExcluded }

  /** A mapping from renamed to original names */
  def reverseMapping: SimpleMap[TermName, TermName] = { ensureInitialized(); myMapped }

  /** The original names imported by-name before renaming */
  def originals: Set[TermName] = { ensureInitialized(); myOriginals }

  /** Does the import clause end with wildcard? */
  def wildcardImport = { ensureInitialized(); myWildcardImport }

  private var myExcluded: Set[TermName] = null
  private var myMapped: SimpleMap[TermName, TermName] = null
  private var myOriginals: Set[TermName] = null
  private var myWildcardImport: Boolean = false

  /** Compute info relating to the selector list */
  private def ensureInitialized(): Unit = if (myExcluded == null) {
    myExcluded = Set()
    myMapped = SimpleMap.Empty
    myOriginals = Set()
    def recur(sels: List[untpd.Tree]): Unit = sels match {
      case sel :: sels1 =>
        sel match {
          case Pair(Ident(name: TermName), Ident(nme.WILDCARD)) =>
            myExcluded += name
          case Pair(Ident(from: TermName), Ident(to: TermName)) =>
            myMapped = myMapped.updated(to, from)
            myOriginals += from
          case Ident(nme.WILDCARD) =>
            myWildcardImport = true
          case Ident(name: TermName) =>
            myMapped = myMapped.updated(name, name)
            myOriginals += name
        }
        recur(sels1)
      case nil =>
    }
    recur(selectors)
  }

  /** The implicit references imported by this import clause */
  def importedImplicits: Set[TermRef] = {
    val pre = site
    if (wildcardImport) {
      val refs = pre.implicitMembers
      if (excluded.isEmpty) refs
      else refs filterNot (ref => excluded contains ref.name.toTermName)
    } else
      for {
        name <- originals
        denot <- pre.member(name).altsWith(_ is Implicit)
      } yield TermRef(pre, name) withDenot denot
  }
}