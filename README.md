# JDK Mission Control Agent Plugin

a Mission Control plug-in to control JMC agents

## 1. Getting Started

The JMC Agent Plugin is implemented as an Eclipse platform plug-in, thus, you'll need to the latest Eclipse for Committers and a JDK of your choice.

### 1.1 Download a Pre-release

Alpha pre-leases are available for downloading from the [release page](https://github.com/rh-jmc-team/jmc-agent-plugin/releases/)

## 2. Building from Source

### 2.1 Build JMC Agent

Since Java Mission Control (JMC) agent is not currently published, the Maven build process expects to be able to find the agent artifacts in the local Maven repository. You'll need to build these artifacts yourself and install them to your local repository.

1. Clone the [openjdk/jmc](https://github.com/openjdk/jmc) repository.
2. Navigate to `agent` sub-directory.
3. Run `mvn install`.

Confirm `org.openjdk.jmc.agent` now exists in your local maven repository.

### 2.2 Build JMC with Agent Plugin

#### 2.2.1 Build with Eclipse 

The JMC Agent Plugin is implemented as an Eclipse platform plug-in, thus, you'll need to the latest [Eclipse for Committers](https://www.eclipse.org/downloads/packages/release/2020-03/r/eclipse-ide-eclipse-committers) and a JDK of your choice.

1. Import the JMC project into Eclipse. See [this blog post](http://hirt.se/blog/?p=989) for detailed instructions. 
2. Clone this repository. 
3. In Eclipse, use the *File -> Import -> Existing Projects into Workspace* wizard to import the `org.openjdk.jmc.feature.console.ext.agent` sub-folder.
4. In Eclipse, use the *File -> Import -> Existing Maven Project* wizard to import the `org.openjdk.jmc.console.ext.agent` sub-folder.
5. In Eclipse, go to *Run -> Run Configurations* and right click to duplicate *JMC RPC* entry.  In the newly created configuration, enable `org.openjdk.jmc.feature.console.ext.agent` feature in *Plug-ins* tab.
6. Launch JDK Mission Control with this new configuration.

#### 2.2.2 Build in Command Line

First, clone the JMC source tree:
```sh
$ git clone https://github.com/openjdk/jmc.git
```

Then, clone the JMC Agent Plugin source tree:
```sh
$ git clone https://github.com/rh-jmc-team/jmc-agent-plugin.git
```

Copy the agent plugin and feature folds from the JMC Agent Plugin source tree to JMC:
```sh
$ cp -r jmc-agent-plugin/org.openjdk.jmc* jmc/application
```

Apply the patch to JMC's root directory:
```sh
$ cd jmc
$ patch -p0 < ../jmc-agent-plugin/scripts/diff.patch
``` 

Get third party dependencies into a local p2 repo and make it available on localhost:
```
$ cd releng/third-party && mvn p2:site && mvn jetty:run
```

Finally, in another terminal, compile and package JMC:
```
$ cd jmc/core && mvn install && cd .. && mvn package
```

Find the built artifacts in `target/products` directory.
