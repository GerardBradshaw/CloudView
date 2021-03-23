# CloudView
A View for Android that animates clouds moving past the screen.

## Screenshot
Below is an app with the CloudView filling the whole screen. Note: animations are much smoother than they appear in the image.

<img src="/art/example_app.gif?raw=true" width="400px">

## Dependency
To use the library, add the following to your Gradle build files:
```groovy
allprojects { 
  repositories {
    maven {url "https://jitpack.io" }
  }
}

dependencies {
  implementation "com.github.GerardBradshaw:CloudView:1.0.2"
}
```

## Building from Source
You can build this library directly from the source code. Enter the terminal command below to clone this repo:
```shell
git clone https://github.com/GerardBradshaw/CloudView.git  
```

## Compatibility
This library is compatible with SDK21+.

## License
This library is available under the Apache Licence 2.0. See the LICENSE file for more info.