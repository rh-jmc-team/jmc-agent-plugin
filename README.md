# JDK Mission Control Agent Plugin

a Mission Control plug-in to control JMC agents

## Getting Started

The JMC Agent Plugin is implemented as an Eclipse platform plug-in, thus, you'll need to the lastest Eclipse for Committers and a JDK of your choice.

### Build JMC with Agent Plugin

1. Import the JMC project into Eclipse. See [this blog post](http://hirt.se/blog/?p=989) for detailed instructions. 
2. Clone this repository. In Eclipse, go to *File -> Import* and use *Existing Projects into Workspace* to import the two containing sub-folders.
3. In Eclipse, go to *Run -> Run Configurations* and right click to duplicate *JMC RPC* entry.  In the newly created configuration, enable `org.openjdk.jmc.feature.console.ext.agent` feature in *Plug-ins* tab.
4. Launch JDK Mission Control with this new configuration.
