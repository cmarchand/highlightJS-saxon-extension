# HighLight JS Saxon extension

This library is a Saxon XPath extension function that transform source code to highlighted
HTML, as [highlight.js](https://highlightjs.org/) does, but without any JavaScript embeded
in HTML.

To use this library, declare it as dependency :
```xml
  <dependency>
    <groupId>top.marchand.xml.saxon.extension</groupId>
    <artifactId>highlight</artifactId>
    <version>1.00.00</version>
  </dependency>
```

Then, declare this extension when creating `Processor` :
```java
  Configuration configuration = Configuration.newConfiguration();
  Processor proc = new Processor(configuration);
  proc.registerExtensionFunction(new top.marchand.xml.saxon.extension.highlight.HighlightExtension());
```

Then in XPath, function is available :
```XPath
  Q{top:marchand:xml:extfunctions}:highlight(language as xs:string, sourceCode as xs:string)
  Q{top:marchand:xml:extfunctions}:highlight(language as xs:string, sourceCode as xs:string, config as map(xs:string,xs:string))
```

First form, with two parameters, generates a item()* sequence of `span` elements and `text()`, in `http://www.w3.org/1999/xhtml`namespace.
If you need a different namespace, use the second form with a map as third argument. The only supported map-entry is `'result-ns'`, and its value is the required target namespace.
