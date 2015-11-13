# Introduction #

The Spider Web Framework is a framework for building web applications in Java. It was developed with a set of specific goals:

  * Make it trivially easy to write good test cases
  * Reduce boilerplate code to a minimum
  * Avoid static state through dependency injection
  * Strict M-V-C separation
  * Prefer convention over configuration

Also worth mentioning is that the framework itself is small, about 10 classes and interfaces and a little over 1000 lines of code. The JAR file includes the source code and it is thus easy to understand what is going on.

The sample application also includes Jetty and can thus be started on a local port with a single command (Java and Ant required).

## Unit testing ##

One of the most important feature of a framework is to facilitate testing; Spider includes
a testing framework that makes it trivial to write comprehensive test cases; a complete test,
excluding the class and method declarations, can be written as:

```
assertHasContent(action(), "foo", "bar");
```

This will actually test the whole stack; HttpServletRequest and HttpServletResponse objects are
instantiated and passed in to a servlet instance, and the data written to the response object is what is actually checked. The above checks that rendering a page without any request parameters returns a
page that includes the strings "foo" and "bar".

## Request parameter parsing ##

The HTTP protocol is text based, which means that any web application needs to parse request parameters
into proper data types; this includes integers, enums and any custom data types defined by each
application. This code tends to either be duplicated or at the very least need a method call for each
request parameter to convert it into the right data type. Spider handles this via a proxy interface
which is dependency injected. Custom parsers can be also be easily added.

## Dependency injection ##

A web application often has several services and / or background tasks that are configured and instantiated
when the servlet starts, typically in the Servlet.init() method. Since each module of
the application typically needs to use a different set of these services a way to get references to
the objects is needed. Often this is done by putting the references into static variables or having an
object that has references to all the services and pass this object to all modules of the app. Both
approaches make it difficult to determine which services a given module needs.

Spider solves this problem by dependency injecting the needed services; the dependencies are thus
documented simply as a list of arguments.

## M-V-C separation ##

M-V-C is the preferred approach for an application that presents a UI, however, it turns out that, for
a number of reasons, it is hard to keep this separation in practice. Spider attempts to solve this
problem by using StringTemplate as its templating language. StringTemplate was developed specifically
to make a templating language with enough expressive power to make it useful, but no more. The author
of StringTemplate wrote a [paper](http://www.cs.usfca.edu/~parrt/papers/mvc.templates.pdf) going into detail on the motivation for StringTemplate.

Spider goes a step further requiring all attributes used in the template be listed using TypeTags;
these tags serve both as documentation for the template as well as a type safe way to set attributes.

## Convention over configuration ##

Paradoxically having no choice can often be liberating; if there is only one way to do something
the focus can simply be on actually getting it done.

Configuration for a web application tends to be very repetitive since most teams, if they are well
organized, will adopt conventions to avoid having to check configuration files all the time. The
configuration is thus copied and pasted each time a new module is added. In addition to being extra
work this duplication also increases the maintenance burden.

Spider solves this by having no mandatory configuration (beyond the minimum required for a Java Servlet).
A URI maps to a name of a class and the URI is valid if that class exists. Various parameters
can be changed, however, but this is typically done by method overriding instead of external configuration
files (which cannot be automatically refactored and are not checked at compile time).