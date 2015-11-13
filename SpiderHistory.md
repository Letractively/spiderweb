# How Spider came to be #

The delivery platform for the applications developed by [Medallia](http://medallia.com) is the web; the Express project, which is actually the second generation Medallia platform, started out using Struts back in 2005. We discovered (as did the Struts developers; they used a different codebase for Struts 2) that it was not an ideal framework and had several limitations. About two years later the first prototype of what has now become Spider was written.

Spider was actually developed first for the broadcaster web interface (see ScreenShots); later a compatibility layer between the Action class from Struts and Spider was written which allows us to move to the new framework piece-by-piece. The testrunner application was developed exclusively using Spider, however.