#### Maven
[![](https://jitpack.io/v/CodingAir/CodingAPI.svg)](https://jitpack.io/#CodingAir/CodingAPI)
```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>  
  <groupId>com.github.CodingAir</groupId>
  <artifactId>CodingAPI</artifactId>  
  <version>1.0</version>  
</dependency>
```

To compile the API
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.6.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.3</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>

            <configuration>
                <artifactSet>
                    <includes>
                        <include>com.github.CodingAir:CodingAPI</include>
                    </includes>
                </artifactSet>
            </configuration>
        </plugin>
    </plugins>
</build>
```
