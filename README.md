# SvgExe

SvgExe is a program that builds self-extracting jars. These self-extracting jars can handle external
        files and native resources.

## Building with Maven

Ensure [Maven](http://maven.apache.org) is installed on your system.

1. Go to the project directory.

    > \> cd /path/to/project

2. Build the project.

    > \> mvn clean install

3. Verify the project jar were created.

    > \> ls target/svgexe*

    > target/svgexe-{version}.jar

4. All done! Now you can run `svgexe-{version}.jar`
