# ARCore-NLP-persistent-augmentation

This is a sample project that allows users to match voice commands to 3D models and load them dynamically in the augmented world. The voice command is processed using StanfordNLP while the models are obtained and loaded dynamically from Google Poly API. The AR interaction is implemented through ARCore with Sceneform.

Also, with the use of Cloud Anchors, the augmentated experience is persistent thus allowing the user to add models to a 3D room and then retrieve them at any time. The 3D room contains details like assets ids (that correspond to Google Poly assets) and cloud anchor ids thus allowing to recover the state of several models throughout the room.
# Table of contents
### 1. Demo
### 2. Instalation tutorial
### 3. Architecture details
### 4. Limitations

# 1. Demo
[![Watch the video](https://i.imgur.com/bF0KSn6.jpg)](https://www.youtube.com/watch?v=ecx2hxhGqbg)

# 2. Instalation tutorial
- Download or clone the repository
- Add your own Google Poly API key in the Manifest.xml file:
  ```xml
          <meta-data
              android:name="com.google.android.ar.API_KEY"
              android:value="YOUR_KEY_HERE" />
  ```
  You can create your own Poly API key using [the official documentation](https://developers.google.com/poly/develop/api).
- Make sure you can run the app on a physical device that supports ARCore. You can check the devices available [here](https://developers.google.com/ar/discover/supported-devices).
- Install the app and provide Audio permissions like below:

![Provide Audio permissions](https://imgur.com/a/fEm92fU)

- Enjoy!

# 3. Architecture details

## Flows relation to modules
![Flows relation to modules](https://i.imgur.com/HDJ6i1u.jpg)

## Module A. Transform and process voice commands
![Module A](https://i.imgur.com/zO0qFXx.jpg)

## Module B. Visualize corresponding assets and perform selection
![Module B](https://i.imgur.com/EdDAeld.jpg)

## Module C. Persistent AR experience
![Module C](https://i.imgur.com/91mkiZs.jpg)

# 4. Limitations:
- 24h persistence limit for anchors imposed by Cloud Anchors API
- Local persistence of 3D room
