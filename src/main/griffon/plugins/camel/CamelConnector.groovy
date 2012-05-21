/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package griffon.plugins.camel

import griffon.core.GriffonApplication
import griffon.util.CallableWithArgs

import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.codehaus.griffon.runtime.camel.SimpleRouteBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class CamelConnector implements CamelProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CamelConnector)

    Object withCamelContext(Closure closure) {
        CamelContextHolder.instance.withCamelContext(closure)
    }

    public <T> T withCamelContext(CallableWithArgs<T> callable) {
        return CamelContextHolder.instance.withCamelContext(callable)
    }

    Object withCamelProducer(Closure closure) {
        CamelContextHolder.instance.withCamelProducer(closure)
    }

    public <T> T withCamelProducer(CallableWithArgs<T> callable) {
        return CamelContextHolder.instance.withCamelProducer(callable)
    }

    Object withCamelConsumer(Closure closure) {
        CamelContextHolder.instance.withCamelConsumer(closure)
    }

    public <T> T withCamelConsumer(CallableWithArgs<T> callable) {
        return CamelContextHolder.instance.withCamelConsumer(callable)
    }

    // ======================================================

    CamelContext connect(GriffonApplication app, ConfigObject config) {
        if (CamelContextHolder.instance.camelContext) {
            return CamelContextHolder.instance.camelContext
        }

        app.event('CamelConnectStart', [config])
        CamelContextHolder.instance.camelContext = startCamel(app, config)
        app.event('CamelConnectEnd', [config, CamelContextHolder.instance.camelContext])
        CamelContextHolder.instance.camelContext
    }

    void disconnect(GriffonApplication app, ConfigObject config) {
        if (CamelContextHolder.instance.camelContext) {
            app.event('CamelDisconnectStart', [config, CamelContextHolder.instance.camelContext])
            stopCamel(config, CamelContextHolder.instance.camelContext)
            app.event('CamelDisconnectEnd', [config])
            CamelContextHolder.instance.camelContext = null
        }
    }

    private CamelContext startCamel(GriffonApplication app, ConfigObject config) {
        CamelContext camelContext = new DefaultCamelContext()
        config.each { key, value ->
            camelContext[key] = value
        }
        app.event('ConfigureCamel', [camelContext])

        for (GriffonRouteClass griffonRouteClass : app.artifactManager.getClassesOfType(GriffonRouteClass.TYPE)) {
            GriffonRoute griffonRoute = griffonRouteClass.newInstance()
            SimpleRouteBuilder routeBuilder = new SimpleRouteBuilder()
            if (LOG.debugEnabled) LOG.debug("Adding routes from $griffonRouteClass")
            griffonRoute.configure(routeBuilder)
            camelContext.addRoutes(routeBuilder)
        }

        camelContext.start()
        camelContext
    }

    private void stopCamel(ConfigObject config, CamelContext camelContext) {
        camelContext.stop()
    }
}
