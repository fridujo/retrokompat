# RetroKompat
[![Build Status](https://travis-ci.com/fridujo/retrokompat.svg?branch=master)](https://travis-ci.com/fridujo/retrokompat)
[![Coverage Status](https://codecov.io/gh/fridujo/retrokompat/branch/master/graph/badge.svg)](https://codecov.io/gh/fridujo/retrokompat/)
[![License](https://img.shields.io/github/license/fridujo/retrokompat.svg)](https://opensource.org/licenses/Apache-2.0)

Backward compatibility for Java binaries.

## Motivation
When publishing a library, a sdk or any binary that is used externally, one may want to check for backward compatibility.

This tool will help achieving that verification in the form of a Maven plugin.

## Usage

Add the plugin in the **pom.xml** file:
```xml
<plugins>
    ...
    <plugin>
        <groupId>com.github.fridujo</groupId>
        <artifactId>retrokompat-maven-plugin</artifactId>
        <version>${retrokompat-maven-plugin.version}</version>
        <executions>
            <execution>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
```

By default the goal `check` is bound to **pre-integration-test** phase (see [build lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)).
