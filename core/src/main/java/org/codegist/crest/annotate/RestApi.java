package org.codegist.crest.annotate;

import org.codegist.crest.*;
import org.codegist.crest.config.Destination;
import org.codegist.crest.config.InterfaceConfig;
import org.codegist.crest.config.MethodConfig;
import org.codegist.crest.config.ParamConfig;
import org.codegist.crest.injector.DefaultRequestInjector;
import org.codegist.crest.injector.RequestInjector;
import org.codegist.crest.interceptor.EmptyRequestInterceptor;
import org.codegist.crest.interceptor.RequestInterceptor;
import org.codegist.crest.serializer.DefaultSerializer;
import org.codegist.crest.serializer.Serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for marking an interface as a REST client interface.
 * <p>Interface methods are meant to map remote REST services methods.
 * Each methods and method's arguments get a default configuration defined by {@link org.codegist.crest.annotate.RestApi} default method &amp; param properties.
 * These values can be overridden by annotating methods and arguments with respectively {@link org.codegist.crest.annotate.RestMethod} and {@link org.codegist.crest.annotate.RestParam}.
 * <p>Any interface annotated with {@link org.codegist.crest.annotate.RestApi} can be passed to a {@link org.codegist.crest.CRest} (as long as it has been preconfigured to handle annotation driven configuration, see {@link org.codegist.crest.config.AnnotationDrivenInterfaceConfigFactory}) to build an instance of it as follow:
 * <br/><br/>
 * <code>
 * <pre>
 * &#64;RestApi(end-point="http://my-server", path="/rest")
 * interface FooInterface {
 *    &#64;RestMethod(path="/model/{0}")
 *    MyModel getModel(long id);
 * }
 * CRest crest = new CRestBuilder().build();
 * FooInterface fooInstance = crest.build(FooInterface.class);
 * MyModel m = fooInstance.getModel(5); // a GET request is fired to http://my-server/rest/model/5, response is automatically marshalled and returned as an object.
 * </pre>
 * </code>
 * <br/>
 * <p>For more information on how these values are used, please consult {@link org.codegist.crest.config.InterfaceConfig} documentation.
 *
 * @see org.codegist.crest.annotate.RestMethod
 * @see org.codegist.crest.annotate.RestParam
 * @see org.codegist.crest.CRest
 * @see org.codegist.crest.CRestBuilder
 * @see org.codegist.crest.config.InterfaceConfig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RestApi {

    /**
     * <p>See documentation for {@link org.codegist.crest.config.InterfaceConfig#getServer()}.
     *
     * @return server path
     * @see org.codegist.crest.config.InterfaceConfig#getServer()
     */
    String endPoint();

    /**
     * <p>See documentation for {@link org.codegist.crest.config.InterfaceConfig#getPath()}.
     * <p>{@link RestApi#endPoint()} concatenated to this value gives the base service URL all methods will use.
     * <p>Defaults to {@value org.codegist.crest.config.InterfaceConfig#DEFAULT_PATH}.
     *
     * @return service's base path
     * @see org.codegist.crest.config.InterfaceConfig#getPath()
     * @see RestApi#endPoint()
     */
    String path() default InterfaceConfig.DEFAULT_PATH;

    /**
     * Encoding used to read/write to the remote service.
     * <p>Defaults to {@value org.codegist.crest.config.InterfaceConfig#DEFAULT_ENCODING}.
     *
     * @return remote service's encoding
     * @see org.codegist.crest.config.InterfaceConfig#getEncoding()
     */
    String encoding() default InterfaceConfig.DEFAULT_ENCODING;

    /**
     * Interface global request interceptor for all requests.
     * <p>Defaults to {@link org.codegist.crest.interceptor.EmptyRequestInterceptor}.
     *
     * @return default interface's request interceptor
     * @see org.codegist.crest.config.InterfaceConfig#getRequestInterceptor()
     * @see org.codegist.crest.interceptor.RequestInterceptor
     * @see org.codegist.crest.interceptor.EmptyRequestInterceptor
     */
    Class<? extends RequestInterceptor> requestInterceptor() default EmptyRequestInterceptor.class;

    /**
     * Socket read timeout fallback in milliseconds for all methods.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#socketTimeout()}.
     * <p>Defaults to {@value org.codegist.crest.config.MethodConfig#DEFAULT_SO_TIMEOUT}ms.
     *
     * @return default socket read timeout fallback
     * @see org.codegist.crest.annotate.RestMethod#socketTimeout()
     */
    long methodsSocketTimeout() default MethodConfig.DEFAULT_SO_TIMEOUT;

    /**
     * Connection timeout fallback in milliseconds for all methods.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#connectionTimeout()}.
     * <p>Defaults to {@value org.codegist.crest.config.MethodConfig#DEFAULT_CO_TIMEOUT}ms.
     *
     * @return connection timeout fallback
     * @see org.codegist.crest.annotate.RestMethod#connectionTimeout()
     */
    long methodsConnectionTimeout() default MethodConfig.DEFAULT_CO_TIMEOUT;


    /**
     * Method path fallback for all methods.
     * <p>Full service URL is the concatenation of {@link RestApi#endPoint()} + {@link RestApi#path()} + this value.
     * <p>See {@link org.codegist.crest.annotate.RestMethod#path()} documentation for more information on path format.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#path()}.
     * <p>Defaults to {@value org.codegist.crest.config.MethodConfig#DEFAULT_PATH}.
     *
     * @return default method's path fallback
     * @see org.codegist.crest.annotate.RestMethod#path()
     */
    String methodsPath() default MethodConfig.DEFAULT_PATH;

    /**
     * HTTP method fallback for all methods.
     * <p>Can be overridden by {@link RestMethod#method()}.
     * <p>Defaults to {@link HttpMethod#GET}.
     *
     * @return default method's path fallback
     * @see org.codegist.crest.HttpMethod
     * @see org.codegist.crest.annotate.RestMethod#method()
     */
    HttpMethod methodsHttpMethod() default HttpMethod.GET;

    /**
     * Method requestInterceptor fallback for all methods.
     * <p>Can be overridden by {@link RestMethod#requestInterceptor()}.
     * <p>Defaults to {@link org.codegist.crest.interceptor.EmptyRequestInterceptor}.
     *
     * @return default method's request requestInterceptor fallback
     * @see org.codegist.crest.interceptor.RequestInterceptor
     * @see org.codegist.crest.interceptor.EmptyRequestInterceptor
     * @see org.codegist.crest.annotate.RestMethod#requestInterceptor()
     */
    Class<? extends RequestInterceptor> methodsRequestInterceptor() default EmptyRequestInterceptor.class;

    /**
     * Response handler fallback for all methods.
     * <p>Can be overridden by {@link RestMethod#responseHandler()}.
     * <p>Defaults to {@link org.codegist.crest.DefaultResponseHandler}.
     *
     * @return default method's response handler fallback
     * @see org.codegist.crest.ResponseHandler
     * @see org.codegist.crest.DefaultResponseHandler
     * @see org.codegist.crest.annotate.RestMethod#responseHandler()
     */
    Class<? extends ResponseHandler> methodsResponseHandler() default DefaultResponseHandler.class;

    /**
     * Error handler fallback for all methods.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#errorHandler()}.
     * <p>Defaults to {@link org.codegist.crest.ErrorDelegatorHandler}.
     *
     * @return default method's response handler fallback
     * @see org.codegist.crest.ErrorHandler
     * @see org.codegist.crest.ErrorDelegatorHandler
     * @see org.codegist.crest.annotate.RestMethod#errorHandler()
     */
    Class<? extends ErrorHandler> methodsErrorHandler() default ErrorDelegatorHandler.class;


    /**
     * Param name fallback for all method arguments.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#paramsName()} or by {@link org.codegist.crest.annotate.RestParam#name()}.
     * <p>Defaults to {@value org.codegist.crest.config.ParamConfig#DEFAULT_NAME}.
     *
     * @return Default param name
     * @see org.codegist.crest.annotate.RestMethod#paramsName()
     * @see org.codegist.crest.annotate.RestParam#name()
     */
    String paramsName() default ParamConfig.DEFAULT_NAME;

    /**
     * Param destination fallback for all method arguments.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#paramsDestination()} or by {@link org.codegist.crest.annotate.RestParam#destination()}.
     * <p>Defaults to {@link org.codegist.crest.config.Destination#URL}.
     *
     * @return default param destination fallback
     * @see org.codegist.crest.config.Destination
     * @see org.codegist.crest.annotate.RestMethod#paramsDestination()
     * @see org.codegist.crest.annotate.RestParam#destination()
     */
    Destination paramsDestination() default Destination.URL;

    /**
     * Param serializer fallback for all params
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#paramsSerializer()} or by {@link org.codegist.crest.annotate.RestParam#serializer()}.
     * <p>Defaults to {@link org.codegist.crest.serializer.DefaultSerializer}.
     *
     * @return default param's serializer fallback
     * @see org.codegist.crest.serializer.Serializer
     * @see org.codegist.crest.serializer.DefaultSerializer
     * @see org.codegist.crest.annotate.RestMethod#paramsSerializer()
     * @see org.codegist.crest.annotate.RestParam#serializer()
     */
    Class<? extends Serializer> paramsSerializer() default DefaultSerializer.class;

    /**
     * Param injector fallback for all params, overrides the {@link org.codegist.crest.annotate.RestMethod#paramsInjector()} configuration.
     * <p>Can be overridden by {@link org.codegist.crest.annotate.RestMethod#paramsInjector()}.
     * <p>Defaults to {@link org.codegist.crest.injector.DefaultRequestInjector}.
     *
     * @return default param's injector fallback
     * @see org.codegist.crest.injector.RequestInjector
     * @see org.codegist.crest.annotate.RestMethod#paramsInjector()
     * @see org.codegist.crest.annotate.RestParam#injector()
     */
    Class<? extends RequestInjector> paramsInjector() default DefaultRequestInjector.class;
}
