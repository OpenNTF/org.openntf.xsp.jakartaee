# Compatibility

This project is largely compatible with known common XPages projects, such as the OpenNTF Domino API. However, there are a few cases where problems may arise.

### POI 4 XPages

Versions of this project 2.8.0 and earlier contain a version of Apache Commons IO that is packaged in a way mildly incompatible with the one included with [POI 4 XPages](https://openntf.org/main.nsf/project.xsp?r=project/POI%204%20XPages). When using both projects, it is safest to deploy them into separate Update Site NSFs or to the filesystem. This is fixed by [Issue #334](https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/334), slated for inclusion in 2.9.0.

### XPages Toolbox and XPiNC

Also in 2.8.0 and earlier, using the agent included in the [XPages Toolbox](https://xpages.info/main.nsf/project.xsp?r=project/XPages%20Toolbox) project or running in XPiNC will lead to a "Bean name is ambiguous" exception when loading the application. This is tracked as [Issue #342](https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/342).