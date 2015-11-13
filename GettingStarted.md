# Getting started with Spider #

The easiest way to get started is to download the [sample application](http://spiderweb.googlecode.com/files/spiderweb-example-1.0.tar.gz) and play with it while reading CalculatorSampleApplication. This application is well-suited to use as a starting point since it includes only the minimum necessary and can be expanded as needed.

For an existing project only [spiderweb-1.0.jar](http://spiderweb.googlecode.com/files/spiderweb-1.0.jar), the library itself, need be downloaded. Note that it has a few library dependencies (which are already included with the sample application):

  * commons-lang (for HTML escaping)
  * JavaBeans Activation Framework (JAF) (for MIME type mapping)
  * log4j + commons-logging
  * junit
  * antlr + stringtemplate
  * jetty (only for starting a local instance)

Here is an overview of the steps required to get started with Spider:

  * Create a subclass of `SpiderServlet`. This class should be referenced in the `web.xml` file. For a project called `Foo` this class will typically be located in a package called `foo.web`, although this is not a requirement.

  * Create a class that inherits `RenderTaskBase`. See doc on `RenderTask`. By convention this class should be located in a package called `st` relative to where the servlet class is, e.g. `foo.web.st`. The task class name must end with `Task`, e.g. `FooBarTask`.

  * Create a .st file which holds the StringTemplate source. See doc on `StRenderable`. This file should be a resource file located in the same package as the task class and its name should be the same as that of the class excluding the `Task` prefix, and unlike the class name the first character should be lower case. E.g. if the task class name is `FooBarTask` the .st file should be named `fooBar.st`.

  * Additionally a test case should be created for each subclass of `RenderTaskBase`; see doc on `RenderTaskTestCase`. By convention the test classes should be placed in a package called `st.test` relative to where the servlet class is, and the name of the test class for each task should be the same as that of the task plus the postfix `Test`. E.g. `FooBarTaskTest` in package `foo.web.st.test`.

  * The URI `fooBar` will point to `FooBarTask`.