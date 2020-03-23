# JDK Mission Control Agent Plugin

a Mission Control plug-in to control JMC agents

## 1. Getting Started

The JMC Agent Plugin is implemented as an Eclipse platform plug-in, thus, you'll need to the latest Eclipse for Committers and a JDK of your choice.

### 1.1 Build JMC Agent

Since Java Mission Control (JMC) agent is not currently published, the Maven build process expects to be able to find the agent artifacts in the local Maven repository. You'll need to build these artifacts yourself and install them to your local repository.

1. Clone the [openjdk/jmc](https://github.com/openjdk/jmc) repository.
2. Navigate to `agent` sub-directory.
3. Run `mvn install`.

Confirm `org.openjdk.jmc.agent` now exists in your local maven repository.

### 1.2 Build JMC with Agent Plugin

The JMC Agent Plugin is implemented as an Eclipse platform plug-in, thus, you'll need to the latest [Eclipse for Committers](https://www.eclipse.org/downloads/packages/release/2020-03/r/eclipse-ide-eclipse-committers) and a JDK of your choice.



1. Import the JMC project into Eclipse. See [this blog post](http://hirt.se/blog/?p=989) for detailed instructions. 
2. Clone this repository. 
3. In Eclipse, use the *File -> Import -> Existing Projects into Workspace* wizard to import the `org.openjdk.jmc.feature.console.ext.agent` sub-folder.
4. In Eclipse, use the *File -> Import -> Existing Maven Project* wizard to import the `org.openjdk.jmc.console.ext.agent` sub-folder.
5. In Eclipse, go to *Run -> Run Configurations* and right click to duplicate *JMC RPC* entry.  In the newly created configuration, enable `org.openjdk.jmc.feature.console.ext.agent` feature in *Plug-ins* tab.
6. Launch JDK Mission Control with this new configuration.
