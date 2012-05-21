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
import org.apache.camel.CamelContext
import org.apache.camel.ConsumerTemplate
import org.apache.camel.ProducerTemplate

/**
 * @author Andres Almiray
 */
@Singleton
class CamelContextHolder implements CamelProvider {
    CamelContext camelContext
    ProducerTemplate producerTemplate
    ConsumerTemplate consumerTemplate

    void setCamelContext(CamelContext camelContext) {
        if (this.camelContext != camelContext) {
            if (null != producerTemplate) {
                producerTemplate.stop()
                producerTemplate = null
            }
            if (null != consumerTemplate) {
                consumerTemplate.stop()
                consumerTemplate = null
            }
            this.camelContext = camelContext

            if (null != camelContext) {
                this.producerTemplate = camelContext.createProducerTemplate()
                this.consumerTemplate = camelContext.createConsumerTemplate()
            }
        }
    }

    Object withCamelContext(Closure closure) {
        closure.call(camelContext)
    }

    public <T> T withCamelContext(CallableWithArgs<T> callable) {
        callable.args = [camelContext] as Object[]
        callable.call()
    }

    Object withCamelProducer(Closure closure) {
        closure.call(camelContext, producerTemplate)
    }

    public <T> T withCamelProducer(CallableWithArgs<T> callable) {
        callable.args = [camelContext, producerTemplate] as Object[]
        callable.call()
    }

    Object withCamelConsumer(Closure closure) {
        closure.call(camelContext, consumerTemplate)
    }

    public <T> T withCamelConsumer(CallableWithArgs<T> callable) {
        callable.args = [camelContext, consumerTemplate] as Object[]
        callable.call()
    }
}
