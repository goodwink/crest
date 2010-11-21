package org.codegist.crest.annotate;

import org.codegist.crest.injector.RequestInjector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to bind a custom {@link org.codegist.crest.injector.RequestInjector} to an user defined Object type when used as a method argument of a RestApi interface.
 * <p>Though this setting can be overriden by the RestApi itself by configuring what method parameter is bind to what RequestInjector.
 * <p>eg :
 * <br/><br/>
 * <code><pre>
 * // Interceptor for a single Class Type (loosing the generic argument will make it shareable accross different class type though.).
 * class MyCustomRequestInjector implements RequestInjector<MyValueObject> {
 *       public void inject(HttpRequest.Builder builder, ParamContext<MyValueObject> context) {
 *          MyValueObject myObject = context.getArgValue();
 *          builder.addQueryParam("MyCustomParam1", myObject.getValue1());
 *          builder.addQueryParam("MyCustomParam2", myObject.getValue2());
 *          // anything else...
 *       }
 * }
 * // Class type annotated to make it use our custom RequestInjector
 * &#64;RestInjector(MyCustomRequestInjector.class)
 * class MyValueObject {
 *       private String value1;
 *       private String value2;
 *       public String getValue1() {
 *           return value1;
 *       }
 *       public void setValue1(String value1) {
 *           this.value1 = value1;
 *       }
 *       public String getValue2() {
 *           return value2;
 *       }
 *       public void setValue2(String value2) {
 *           this.value2 = value2;
 *       }
 * }
 * // Rest interface (configured it as you want, either via annotation or properties or custom configuration strategy)
 * interface MyRestInterface {
 *     void storeMyValueObject(MyValueObject valueObject, int a, String b);
 *     void storeMyValueObjects(MyValueObject[] valueObjects);
 * }
 * CRest crest = ...;
 * MyRestInterface instance = crest.build(MyRestInterface.class);
 * MyValueObject valueObject = ...;
 * // MyCustomRequestInjector will be called only for MyValueObject parameter to inject it into the request. The other parameter will use the default injector.
 * instance.storeMyValueObject(valueObject, 1, "my string");
 * // ****** NOTE ******
 * // This will call the default injector.
 * instance.storeMyValueObjects(new MyValueObject[]{...}});
 * </pre></code>
 *
 * @see org.codegist.crest.injector.RequestInjector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RestInjector {
    /**
     * Request injector to be used to inject user defined object into the request.
     *
     * @return The user custom request injector.
     */
    Class<? extends RequestInjector> value();
}
