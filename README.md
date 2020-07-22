# ARCore-NLP-persistent-augmentation

This is a sample project that allows users to match voice commands to 3D models and load them dinamically in the augmented world. The voice command is processed using StanfordNLP while the models are obtained and loaded dynamically from Google Poly API. The AR interaction is implemented through ARCore with Sceneform.
Also, with the use of Cloud Anchors, the augmentated experience is persistent thus allowing the user to add models to a 3D room and then retrieve them at any time. The 3D room contains details like assets ids (that correspond to Google Poly assets) and cloud anchor ids, thus allowing to recover the state of several models throughout the room.

# Demo
[![Watch the video](https://i.imgur.com/bF0KSn6.jpg)](https://streamable.com/sdjsec)

# Architecture details

## Flows relation to modules
![Flows relation to modules](https://i.imgur.com/HDJ6i1u.jpg)

## Module A. Transform and process voice commands
![Module A](https://i.imgur.com/zO0qFXx.jpg)

## Module B. Visualize corresponding assets and perform selection
![Module B](https://i.imgur.com/EdDAeld.jpg)

## Module C. Persistent AR experience
![Module C](https://i.imgur.com/91mkiZs.jpg)

