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

import griffon.util.CallableWithArgs
import org.apache.camel.language.groovy.CamelGroovyMethods
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.ProcessorDefinition
import org.codehaus.griffon.runtime.camel.ClosureProcessor
import org.slf4j.LoggerFactory
import org.slf4j.Logger

/**
 * @author Andres Almiray
 */
final class CamelEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(CamelEnhancer)

    private CamelEnhancer() {}

    static void enhanceCamelClasses() {
        ProcessorDefinition.metaClass.filter = { filter ->
            if (filter instanceof Closure) {
                filter = CamelGroovyMethods.toExpression(filter)
            }
            delegate.filter(filter);
        }

        ChoiceDefinition.metaClass.when = { filter ->
            if (filter instanceof Closure) {
                filter = CamelGroovyMethods.toExpression(filter)
            }
            delegate.when(filter);
        }

        ProcessorDefinition.metaClass.process = { filter ->
            if (filter instanceof Closure) {
                filter = new ClosureProcessor(filter)
            }
            delegate.process(filter);
        }
    }

    static void enhance(MetaClass mc, CamelProvider provider = CamelContextHolder.instance) {
        if (LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withCamelContext = { Closure closure ->
            provider.withCamelContext(closure)
        }
        mc.withCamelContext << {CallableWithArgs callable ->
            provider.withCamelContext(callable)
        }
        mc.withCamelProducer = { Closure closure ->
            provider.withCamelProducer(closure)
        }
        mc.withCamelProducer << {CallableWithArgs callable ->
            provider.withCamelProducer(callable)
        }
        mc.withCamelConsumer = { Closure closure ->
            provider.withCamelConsumer(closure)
        }
        mc.withCamelConsumer << {CallableWithArgs callable ->
            provider.withCamelConsumer(callable)
        }
    }
}