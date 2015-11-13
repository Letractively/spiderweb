# Static resource caching (CSS, JS and images) #

Resources that do not change should be cached by the browser for as long as possible, but when a new version of the webapp is deployed those resources must be re-downloaded.

CalculatorSampleApplication includes an image like this:

```
<img src="$"line.png":cached()$">
```

In the rendered HTML this will become:

```
<img src="line.png?41fb493805ae79965460863b5f2d1239">
```

The string after the `?` is the md5 of the content of the file `line.png`. Spiderweb will instruct the browser to cache any CSS, JS or image file forever, but by including the md5 in the URL the browser is forced to download an updated version whenever the actual content changes.

# Paths to static resources #

In response to a request for an image file (.gif, .jpg or .png) Spiderweb will try to find the file in the package `images` relative to the package where the Servlet class is. Paths are supported, thus:

```
<img src="$"foo/bar/line.png":cached()$">
```

Will try to find a file in the package `images.foo.bar`.

.css files are located in the package `css`, while .js files are located in the package `js`.