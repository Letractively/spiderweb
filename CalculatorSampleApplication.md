# Calculator - A Spider Sample Application #

Before reading this tutorial it is a good idea to download the [sample application](http://spiderweb.googlecode.com/files/spiderweb-example-1.0.tar.gz) and play with it while reading. After unpacking the file run the command `ant run-jetty` in the project directory to start a Jetty instance on port 8080 and browse to http://localhost:8080/ to play with the application.

Since Spider uses convention over configuration a URI is automatically mapped to the name of a class. The URI:

```
/calculator
```

is mapped to a class called CalculatorTask. Each class with the Task postfix represents one page in the application.

The classes are also expected to be placed in specific packages. The four classes and one StringTemplate file in Calculator are:

```
calculator.web: CalculatorServlet.java
calculator.web.st: CalculatorTask.java
calculator.web.st.pages: calculator.st
calculator.web.st.test: CalculatorTestCase.java, CalculatorTaskTest.java
```

These five files are the minimum that will be present in a Spider application; three new files are required for each
new page (the Task class, the StringTemplate file and the test class).

Let's look at each file in detail, starting with the servlet implemented in CalculatorServlet.java:

```
public class CalculatorServlet extends SpiderServlet {
	@Override protected String getDefaultURI() { return "calculator"; }
}
```

The only required method is to set the default URI; this is the URI clients will be redirected to if they
go to URI that does not map to a task (typically the / URI). This will usually be the start page of the app.

Next let's look at the class implementing the actual logic, CalculatorTask.java:

```
public class CalculatorTask extends RenderTask {
```

Each task should inherit RenderTask, however, it is possible to implement the IRenderTask interface as well.

```
/** the supported operators */
enum Operator {
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	MOD,
	;
	
	/** @return the result of applying this operator to the two operands */
	int apply(int operand1, int operand2) {
		switch (this) {
			case PLUS: return operand1 + operand2;
			case MINUS: return operand1 - operand2;
			case MULTIPLY: return operand1 * operand2;
			case DIVIDE: return operand1 / operand2;
			case MOD: return operand1 % operand2;
		}
		throw new AssertionError(this);
	}
}
```

This is the actual logic of the calculator; you could write "PLUS.apply(2,2)" which would return 4. In order to access this from a web page there are two things we need to do: retrieve the operator and operands and print the result. The request parameters are accessed through an interface:

```
@Input interface Params {
	/** @return the first operand */
	Integer operandOne();
	/** @return the second operand */
	Integer operandTwo();
	/** @return the selected operator */
	Operator operator();
}
```

The name of the method is the name of the request parameter (for accessing dynamic request parameters see DynamicRequestParameters).

Before we look at how this is used let's look at how the web page is produced. The StringTemplate, calculator.st, is:

```
$if(has_error)$
<b>$error$</b>
$else$

<span class="heading">
Input values:
</span>

<form method="get" action="">
 <input type="text" name="operandOne" value="$operand_one$" size="4" />
 <select name="operator">
 $operators:{
 <option value="$it.value$"$if(it.selected)$selected$endif$>$it.text$
 }$
 </select>
 <input type="text" name="operandTwo" value="$operand_two$" size="4" />
 
 <br/>
 <input type="submit" value="Calculate"/>
</form>

$if(has_result)$
Result: <b>$result$</b>
<br>
$endif$

$endif$

<img src="$"line.png":cached()$">
```

Rendering this page without request parameters produces this (both has\_error and has\_result are false):

![http://spiderweb.googlecode.com/svn/wiki/calculator-empty.png](http://spiderweb.googlecode.com/svn/wiki/calculator-empty.png)

Typing in a couple of numbers and pressing the button results in (has\_result is now true):

![http://spiderweb.googlecode.com/svn/wiki/calculator.png](http://spiderweb.googlecode.com/svn/wiki/calculator.png)

The attributes used in this StringTemplate are produced using TypeTags:

```
@Output interface Values {
	V<List<OperatorWebView>> OPERATORS = v();
	
	V<String> OPERAND_ONE = v();
	V<String> OPERAND_TWO = v();
	V<Operator> OPERATOR = v();
	
	V<Boolean> HAS_RESULT = v();
	V<Integer> RESULT = v();
	
	V<Boolean> HAS_ERROR = v();
	V<String> ERROR = v();
}
```

The name of the TypeTag is lowercased in the StringTemplate. These attributes are set in the code with:

```
attr(Values.HAS_RESULT, false);
```

Every Spider Task class has a method called "action" which is invoked in response to a request. The parameters of this method are dependency injected; the complete method is:

```
void action(Params p) {
	// parse the request parameter named "operator"; null is returned if not present 
	Operator op = p.operator();
	
	// set default values (every used attribute must be explicitly set)
	attr(Values.HAS_RESULT, false);
	attr(Values.HAS_ERROR, false);
	
	if (op == null) {
		// no operator request parameter is present; set default values
		// for the text boxes on the page
		attr(Values.OPERAND_ONE, "");
		attr(Values.OPERAND_TWO, "");
	} else {
		try {
			// perform computation with the given operator and store the result in an attribute
			attr(Values.RESULT, p.operator().apply(p.operandOne(), p.operandTwo()));
			attr(Values.HAS_RESULT, true);
			
			// store the given operands in attributes as well so we can maintain
			// the same values in the text boxes
			attr(Values.OPERAND_ONE, String.valueOf(p.operandOne()));
			attr(Values.OPERAND_TWO, String.valueOf(p.operandTwo()));
		} catch (NumberFormatException e) {
			// one or both of the operands were not valid numbers; print the
			// error message to the client
			attr(Values.ERROR, e.getMessage() + "; not a valid number");
			attr(Values.HAS_ERROR, true);
		}
	}
	
	// set objects used to build the HTML dropdown
	attr(Values.OPERATORS, getOperators(op));
}
```

Most of this code deals with error handling, handling the initial loading of the page and maintaining
the values in the text boxes. The actual logic is just one line:

```
attr(Values.RESULT, p.operator().apply(p.operandOne(), p.operandTwo()));
```

Let's also look at the last line: since StringTemplate does not support comparison maintaining the selection of the operator dropdown requires a wrapper object:

```
/** View that wraps a {@link Operator} and provides method names
 * for use in a HTML form as well as whether the operator is
 * selected.
 */
interface OperatorWebView {
	/** @return the value used in the HTML form */
	String getValue();
	/** @return the value displayed to the user */
	String getText();
	/** @return true if this operator is selected */
	boolean isSelected();
}

private List<OperatorWebView> getOperators(final Operator selected) {
	List<OperatorWebView> l = Empty.list();
	for (final Operator op : Operator.values())
		l.add(new OperatorWebView() {
			@Implement public String getValue() { return op.name(); }
			@Implement public String getText() { return op.toString(); }
			@Implement public boolean isSelected() { return op == selected; }
		});
	return l;
}
```

While this does require a fair bit of extra code compared to putting the Enum objects directly in
the template it does de-couple the method names which can be made more appropriate for an HTML
dropdown; it also provides a good place for adding documentation.

Finally, the HTML page title is given by:

```
@Implement public String getPageTitle() { return "Calculator"; }
```

Next, let's look at how this is tested. First, a common base class for all Calculator tests has to be created:

```
public abstract class CalculatorTestCase extends RenderTaskTestCase {
	@Override protected ServletMock getServletMock(Class<? extends RenderTask> renderableClass) throws Exception {
		return makeServletMock(CalculatorServlet.class);
	}
}
```

This class basically serves as a link to the servlet class. The actual test is:

```
public class CalculatorTaskTest extends CalculatorTestCase {
	
	/** test initial loading of the page without request parameters */
	public void testInitial() throws Exception {
		assertHasContent(action(), "PLUS", "MINUS");
	}

	/** test the plus operator */
	public void testPlus() throws Exception {
		Map<String, String> m = Empty.hashMap();
		m.put("operandOne", "42");
		m.put("operator", "plus");
		m.put("operandTwo", "10");

		assertHasContent(action(m), "52");
	}
	
	/** test the mod operator */
	public void testMod() throws Exception {
		Map<String, String> m = Empty.hashMap();
		m.put("operandOne", "523");
		m.put("operator", "mod");
		m.put("operandTwo", "127");
		assertHasContent(action(m), "15");		
	}
	
	/** test the error handling */
	public void testError() throws Exception {
		Map<String, String> m = Empty.hashMap();
		m.put("operandOne", "523");
		m.put("operator", "plus");
		m.put("operandTwo", "foo");
		assertHasContent(action(m), "not a valid number");		
	}
}
```

The action() method is used to initiate a request; it can optionally be passed a Map<String, String> with request parameters. The assertHasContent() method is used to check for the presence of certain strings in the rendered page. Note that action() does not return a String, but rather a StRenderResult object which has methods for checking redirects as well as binary data.

## CSS and images ##

`.css` files are by convention put into the package `web.css` and the file `style.css` is referenced by default. Any URI which ends in `.css` will be served from the `web.css` package.

Images are put into the package `web.images`. Any URI with extension `gif`, `jpg` or `png` are served from this package.

See StaticResourceCaching for more information.