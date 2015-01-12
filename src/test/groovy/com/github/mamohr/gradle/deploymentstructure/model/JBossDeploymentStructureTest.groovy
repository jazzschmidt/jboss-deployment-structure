/*
 * Copyright [2015] Mario Mohr <mario_mohr@web.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mamohr.gradle.deploymentstructure.model

import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.internal.reflect.DirectInstantiator
import org.gradle.internal.reflect.Instantiator
import spock.lang.Specification

/**
 * Created by mario on 12.01.2015.
 */
class JBossDeploymentStructureTest extends Specification {

    def structure = new JBossDeploymentStructure(new DirectInstantiator())

    def setupSpec() {
        XMLUnit.setIgnoreWhitespace(true)
    }

    def 'empty deployment structure creates valid xml'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies/>
                <exclusions/>
              </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml,expectedXml)
    }

    def 'dependecy module with export is created'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies>
                    <module name="my-dependency" slot="1.1" export="true"/>
                </dependencies>
                <exclusions/>
              </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.dependency('my-dependency:1.1') { dep ->
             dep.export = true
        }
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml,expectedXml)
    }

    def 'subdeployment is added'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies/>
                <exclusions/>
              </deployment>
              <sub-deployment name="my-ejb.jar">
                <dependencies/>
                <exclusions/>
              </sub-deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.subdeployments.create("my-ejb.jar")
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml,expectedXml)
    }

    def 'ear subdeployment isolation tag is added if set to false'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
                <ear-subdeployments-isolated>false</ear-subdeployments-isolated>
                <deployment>
                    <dependencies/>
                    <exclusions/>
                </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.earSubdeploymentsIsolated = false
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml,expectedXml)
    }

    def boolean nodeIsSimilarToString(Node node, String expectedString) {
        StringWriter sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(node)
        String nodeString = sw.toString()
        def diff = new Diff(expectedString,nodeString)
        diff.similar()
    }

}
