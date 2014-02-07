dotty
=====

Dotty is a platform to try out new language concepts and compiler technologies for Scala. The focus is mainly on simplification. We remove extraneous syntax (e.g. no XML literals), and try to boil down Scala's types into a smaller set of more fundamental constructors. The theory behind these constructors is researched in DOT, a calculus for dependent object types. 

The dotty compiler is largely a new design. It takes a more functional approach than current scalac, paired with aggressive caching to achieve good performance. At present, only the frontend (parser and type-checker) exists; the transformation and code generation phases remain to be written.

We expect that, over time, some of the new technologies explored in this project will find their way into future versions of Scala. At present it is too early to say which ones and when.

If you want to try it out, to get started have a look at https://github.com/lampepfl/dotty/wiki/Getting-Started.
