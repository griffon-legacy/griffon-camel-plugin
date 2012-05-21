@artifact.package@import org.apache.camel.builder.RouteBuilder;
import org.codehaus.griffon.runtime.camel.AbstractGriffonRoute;

public class @artifact.name@ extends AbstractGriffonRoute {
    public void configure(RouteBuilder routeBuilder) {
        routeBuilder.from("seda:input").to("stream:out");
    }
}
