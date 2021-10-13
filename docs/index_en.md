---
description: >-
  This Step Plugin for Goobi workflow automatically requests specific monument information from an internal Vocabulary data source to map these fields into the METS file. It was developed for the BDA in Austria.
  
---

goobi-plugin-step-herisimport
===========================================================================


Introduction
---------------------------------------------------------------------------
ZZZ


Overview
---------------------------------------------------------------------------

Details             |  
------------------- | -----------------------------------------------------
Identifier          | intranda_step_herisimport |
Source code         | [https://github.com/intranda/goobi-plugin-step-herisimport](https://github.com/intranda/goobi-plugin-step-herisimport)
License             | GPL 2.0 or newer 
Documentation date  | {DATE} 


Installation
---------------------------------------------------------------------------
The plugin consists in total of the following files to be installed:

```text
goobi_plugin_step_herisimport.jar
goobi_plugin_step_herisimport-GUI.jar
goobi_plugin_step_herisimport.xml
```

These files must be installed in the correct directories so that they are available under the following paths after installation:

```bash
/opt/digiverso/goobi/plugins/step/goobi_plugin_step_herisimport.jar
/opt/digiverso/goobi/plugins/GUI/goobi_plugin_step_herisimport-GUI.jar
/opt/digiverso/goobi/config/goobi_plugin_step_herisimport.xml
```


Configuration
---------------------------------------------------------------------------
The plugin is configured via the configuration file `goobi-plugin-step-herisimport.xml` and can be adapted during operation. The following is an example configuration file:

```xml
<config_plugin>
    <!--
        order of configuration is:
          1.) project name and step name matches
          2.) step name matches and project is *
          3.) project name matches and step name is *
          4.) project name and step name are *
	-->
    
    <config>
        <!-- which projects to use for (can be more then one, otherwise use *) -->
        <project>*</project>
        <step>*</step>
        
        <!-- sample parameter -->
        <value>test</value>
        <!-- display button to finish the task directly from within the entered plugin -->
        <allowTaskFinishButtons>true</allowTaskFinishButtons>
    </config>

</config_plugin>
```

The block `<config>` can occur repeatedly for different projects or workflow steps in order to be able to perform different actions within different workflows. The other parameters within this configuration file have the following meanings:

Parameter           |  Explanation
------------------- | ----------------------------------------------------- 
`project`           | This parameter determines for which project the current block `<config>` should apply. The name of the project is used here. This parameter can occur several times per `<config>` block.
`step`              | This parameter controls which workflow steps the `<config>` block should apply to. The name of the workflow step is used here. This parameter can occur several times per `<config>` block.
`parameter`         | ZZZ
`parameter`         | ZZZ
`parameter`         | ZZZ
`parameter`         | ZZZ
`parameter`         | ZZZ
`parameter`         | ZZZ
`parameter`         | ZZZ



Setting up required rights
---------------------------------------------------------------------------
This plugin has its own permission level for use. For this reason, users must have the necessary rights. Therefore, please assign the following right to the user group of the corresponding users:

```
ZZZ
```

![Correctly assigned rights for the users](placeholder.png)


Integration of the plugin into the workflow
---------------------------------------------------------------------------
To put the plugin into operation, it must be activated for one or more desired tasks in the workflow. This is done as shown in the following screenshot by selecting the plugin `intranda_step_herisimport` from the list of installed plugins.

![Assigning the plugin to a specific task](placeholder.png)

Since this plugin should usually be executed automatically, the workflow step should be configured as 'automatic'.


Operation of the plugin
---------------------------------------------------------------------------

ZZZ 


How the plugin works
---------------------------------------------------------------------------
Once the plugin has been fully installed and set up, it is usually run automatically within the workflow, so there is no manual interaction with the user. Instead, calling the plugin through the workflow in the background does the following: 

ZZZ