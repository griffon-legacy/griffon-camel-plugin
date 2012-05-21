@artifact.package@import org.apache.camel.builder.RouteBuilder

class @artifact.name@ {
    void configure(RouteBuilder routeBuilder) {
        routeBuilder.from('seda:input').to('stream:out')
    }
}