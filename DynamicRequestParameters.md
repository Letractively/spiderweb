# How to access dynamic request parameters #

As mentioned in CalculatorSampleApplication request parameters are accessed by defining an interface and annotate it with `@Input`. There are instances where the number of request parameters is not static, however, typically with dynamically generated forms. In this case an instance of `DynamicInput` can be used; it is dependency injected to the action method as usual. It works exactly the same way as the interface:

```
Integer operandOne = di.getInput("operandOne", Integer.class);
```

This is the same as calling:

```
Integer operandOne = p.operandOne();
```

where `p` is the request parameter interface.

Note that it is not possible to get a list of the request parameters sent by the client. Since the client could inject parameters with arbitrary names and values there is a risk of introducing security issues; instead retrieve all possible request parameters and ignore those which return null.