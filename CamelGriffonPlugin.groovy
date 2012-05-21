/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing pecamelssions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class CamelGriffonPlugin {
    // the plugin version
    String version = '0.1'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '0.9.5 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-camel-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Enterprise Integration Patterns via Apache Camel'

    String description = '''
[Apache Camel][1] is a versatile open-source integration framework based on known [Enterprise Integration Patterns][2].
This plugin is influenced by [Grails' Routing plugin][3] and contains some code from that project.

Usage
-----
The plugin will inject the following dynamic methods:

* `withCamelContext(Closure callback)` - executes callback sending the current `CamelContext` as the sole argument
* `withCamelProducer(Closure callback)` - executes callback sending the current `CamelContext`and default `ProducerTemplate` as arguments.
* `withCamelConsumer(Closure callback)` - executes callback sending the current `CamelContext`and default `ConsumerTemplate` as arguments.

These methods are also accessible to any component through the singleton `griffon.plugins.camel.CamelEnhancer`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`CamelEnhancer.enhance(metaClassInstance)`.

Configuration
-------------
### Dynamic method injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.camel.injectInto = ['controller', 'service']

The following events will be triggered by this addon

 * CamelConnectStart[config] - triggered before instantiating a `CamelContext`
 * ConfigureCamel[camelContext] - further customize the freshly instantiated `CamelContext`. Triggered before the context is started.
 * CamelConnectEnd[config, camelContext] - triggered after instantiating the `CamelContext`
 * CamelDisconnectStart[config, camelContext] - triggered before stopping the `CamelContext`
 * CamelDisconnectEnd[config] - triggered after stopping the `CamelContext`


### Example

Follow these steps to get a basic route working

1. Create a new Griffon application. We'll pick `sample` as the application name

        griffon create-app sample
    
2. Install the camel plugin

        griffon install-plugin camel

3. Create a simple route

        griffon create-route simple
        
4. Edit the `SampleView.groovy` to include a button

        package sample
        application(title: 'Camel',
          preferredSize: [320, 240],
          pack: true,
          locationByPlatform:true,
          iconImage: imageIcon('/griffon-icon-48x48.png').image,
          iconImages: [imageIcon('/griffon-icon-48x48.png').image,
                       imageIcon('/griffon-icon-32x32.png').image,
                       imageIcon('/griffon-icon-16x16.png').image]) {
            button('Send a message', actionPerformed: controller.sendMessage)
        }

5. Edit `SampleController` by adding an action named `sendMessage`

        package sample
        class SampleController {
            def sendMessage = {
                withCamelProducer { camelContext, producer ->
                    def myMessage = [name: 'foo',data: 'bar']
                    producer.sendBody('seda:input', myMessage)
                }
            }
        }

6. Run the application and click the button. You should see the map as the output in the console

        griffon run-app
        {name=foo, data=bar}
        

Scripts
-------
 * **create-route** - Creates a new Route artifact

Testing
-------
Dynamic methods will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `CamelEnhancer.enhance(metaClassInstance, camelProviderInstance)` where 
`camelProviderInstance` is of type `griffon.plugins.camel.CamelProvider`. The contract for this interface looks like this

    public interface CamelProvider {
        Object withCamelContext(Closure closure);
        <T> T withCamelContext(CallableWithArgs<T> callable);
        Object withCamelProducer(Closure closure);
        <T> T withCamelProducer(CallableWithArgs<T> callable);
        Object withCamelConsumer(Closure closure);
        <T> T withCamelConsumer(CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyCamelProvider implements CamelProvider {
        Object withCamelContext(Closure closure) { null }
        public <T> T withCamelContext(CallableWithArgs<T> callable) { null }
        Object withCamelProducer(Closure closure) { null }
        public <T> T withCamelProducer(CallableWithArgs<T> callable) { null }
        Object withCamelConsumer(Closure closure) { null }
        public <T> T withCamelConsumer(CallableWithArgs<T> callable) { null }
    }
    
This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            CamelEnhancer.enhance(service.metaClass, new MyCamelProvider())
            // exercise service methods
        }
    }


[1]: http://camel.apache.org/
[2]: http://camel.apache.org/enterprise-integration-patterns.html
[3]: http://grails.org/plugin/routing
'''
}
