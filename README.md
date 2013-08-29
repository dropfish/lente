lente
=====

Lente (Greek for "Lens") is an Android app that enables the visually impaired to read small text using optical character recognition and text-to-speech. Lente was built entirely by David Fish (Emory University) and Eric Ostrowski (Grand Valley State University) during an REU at Auburn University in the summer of 2011.

***

This repository contains all the source code for the app, which has not been modified in any way since 2011. At that time, the app was fully functional and tested on a limited range of Android devices. The app was not brought to the Google Play store (then "Android Market") for two reasons. First, the open source optical character recognition (OCR) and image preprocessing libraries used were designed to process 2D, scanned images and therefore often failed to faithfully capture characters in real-world images. Second, a goal of the app was to house all image processing algorithms locally (i.e., on the device), but due to time constraints we instead created a dedicated server to handle all image processing. The source code for the server is not part of this repository.

Lente consists of three Activities, each of which plays a specific role in the capture, processing, and display of text. The first Activity features a custom camera interface that allows a user to take a picture of real-world text. The second Activity contains a custom cropping interface to select which part of the image contains the text. The third Activity shows the recognized characters and allows the user to copy it their clipboard, view it in different sizes, fonts, and color combinations, and provides text-to-speech access.
